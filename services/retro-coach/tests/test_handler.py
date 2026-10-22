import importlib.util
import json
import os
import unittest
from pathlib import Path
from unittest.mock import Mock, patch

spec = importlib.util.spec_from_file_location("coach", Path(__file__).parents[1] / "handler.py")
coach = importlib.util.module_from_spec(spec)
spec.loader.exec_module(coach)


class CoachTests(unittest.TestCase):
    def setUp(self):
        self.env = patch.dict(os.environ, {"COACH_TOKEN": "test-token", "MODEL_ID": "test-model"})
        self.env.start()
        self.addCleanup(self.env.stop)
        coach._client = Mock()

    def ask(self, data, auth="Bearer test-token"):
        return coach.handler({"headers": {"Authorization": auth}, "body": json.dumps(data)}, None)

    def test_unauthorized_never_invokes_model(self):
        self.assertEqual(self.ask({"question": "Hi"}, "bad")["statusCode"], 401)
        coach._client.converse.assert_not_called()

    def test_invalid_requests_never_invoke_model(self):
        for data in [[], {}, {"question": " "}, {"question": 5}, {"question": "x" * 1001}, {"question": "hi", "game": []}]:
            self.assertEqual(self.ask(data)["statusCode"], 400)
        coach._client.converse.assert_not_called()

    def test_success_uses_converse_and_bounds_tokens(self):
        coach._client.converse.return_value = {"output": {"message": {"content": [{"text": "Try jumping."}]}}}
        result = self.ask({"question": "How do I cross?", "game": "Example"})
        self.assertEqual(json.loads(result["body"])["answer"], "Try jumping.")
        call = coach._client.converse.call_args.kwargs
        self.assertEqual(call["modelId"], "test-model")
        self.assertEqual(call["inferenceConfig"]["maxTokens"], 350)

    def test_service_failure_is_redacted(self):
        coach._client.converse.side_effect = coach.ClientError({"Error": {"Code": "AccessDeniedException", "Message": "private detail"}}, "Converse")
        result = self.ask({"question": "Help"})
        self.assertEqual(result["statusCode"], 503)
        self.assertNotIn("private detail", result["body"])

    def test_bad_base64_and_large_body(self):
        result = coach.handler({"headers": {"authorization": "Bearer test-token"}, "body": "%%%", "isBase64Encoded": True}, None)
        self.assertEqual(result["statusCode"], 400)
        result = coach.handler({"headers": {"authorization": "Bearer test-token"}, "body": "x" * 12001}, None)
        self.assertEqual(result["statusCode"], 413)

    @patch.dict(os.environ, {"KNOWLEDGE_BASE_ID": "kb-test"})
    @patch.object(coach.boto3, "client")
    def test_grounding_is_passed_to_model_and_returned(self, factory):
        factory.return_value.retrieve.return_value = {"retrievalResults": [{"content": {"text": "Escape pauses the game."}}]}
        coach._client.converse.return_value = {"output": {"message": {"content": [{"text": "Press Escape."}]}}}
        result = self.ask({"question": "How do I pause?"})
        payload = json.loads(result["body"])
        self.assertTrue(payload["grounded"])
        self.assertIn("Escape", payload["sources"][0]["excerpt"])
        self.assertIn("Escape pauses", coach._client.converse.call_args.kwargs["messages"][0]["content"][0]["text"])

    @patch.object(coach.boto3, "client")
    def test_speech_calls_polly_and_closes_stream(self, factory):
        import io
        stream = io.BytesIO(b"fake-mp3")
        factory.return_value.synthesize_speech.return_value = {"AudioStream": stream}
        result = coach.handler({"rawPath": "/speech", "headers": {"authorization": "Bearer test-token"}, "body": json.dumps({"text": "Hello"})}, None)
        self.assertEqual(result["statusCode"], 200)
        self.assertTrue(stream.closed)
        factory.return_value.synthesize_speech.assert_called_once()

    def test_speech_limit_prevents_model_call(self):
        result = coach.handler({"rawPath": "/speech", "headers": {"authorization": "Bearer test-token"}, "body": json.dumps({"text": "x" * 2401})}, None)
        self.assertEqual(result["statusCode"], 400)
