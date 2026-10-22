import importlib.util
import json
import os
import unittest
from pathlib import Path
from unittest.mock import Mock, patch

spec = importlib.util.spec_from_file_location("cloud", Path(__file__).parents[1] / "handler.py")
cloud = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cloud)

class CloudTests(unittest.TestCase):
    def event(self, method, sub="user-123"):
        return {"requestContext": {"http": {"method": method}, "authorizer": {"jwt": {"claims": {"sub": sub}}}}, "body": '{"sub":"other-user"}'}
    def test_unverified_identity_is_rejected(self):
        for event in [{}, self.event("GET", "../../bad")]:
            self.assertEqual(cloud.handler(event, None)["statusCode"], 401)
    @patch.dict(os.environ, {"SAVE_BUCKET": "test"})
    @patch.object(cloud.boto3, "client")
    def test_upload_is_scoped_and_size_limited(self, factory):
        factory.return_value.generate_presigned_post.return_value = {"url": "https://example.com", "fields": {}}
        result = cloud.handler(self.event("POST"), None)
        self.assertEqual(result["statusCode"], 200)
        args = factory.return_value.generate_presigned_post.call_args
        self.assertTrue(args.args[1].startswith("users/user-123/"))
        self.assertIn(["content-length-range", 1, 33554432], args.kwargs["Conditions"])
    @patch.dict(os.environ, {"SAVE_BUCKET": "test"})
    @patch.object(cloud.boto3, "client")
    def test_list_cannot_read_another_user_prefix(self, factory):
        factory.return_value.list_objects_v2.return_value = {}
        result = cloud.handler(self.event("GET"), None)
        self.assertEqual(json.loads(result["body"])["backups"], [])
        self.assertEqual(factory.return_value.list_objects_v2.call_args.kwargs["Prefix"], "users/user-123/")
