# Amazon Bedrock integration: Retro Coach

FireSpot includes a Retro Coach screen under Settings → Your setup. It accepts a game or console title and a question, sends them over HTTPS to an AWS backend, and displays the model's answer. Requests run off the UI thread. The feature does not inspect gameplay, upload ROMs, or require AWS credentials in the APK. Answers are generated guidance and may be inaccurate.

## Architecture

FireSpot Kotlin/Compose screen → API Gateway HTTP API `/coach` → Python Lambda → Amazon Bedrock Runtime `Converse` → answer in FireSpot.

The AWS SAM template in `services/retro-coach/template.yaml` defines the endpoint, Lambda, throttling, concurrency limits, and permission to invoke one specified model ARN. Python uses boto3 and the Lambda execution role. A separate bearer token restricts access for this initial integration. It is entered per app session and is not persisted. This token is not an AWS access key; do not distribute it publicly. A public multi-user deployment should replace shared-token access with individual authentication.

## Deployment

Prerequisites: AWS account and billing, an authenticated AWS CLI profile, AWS SAM CLI, and access to a Converse-compatible **regional foundation model**. Choose a model supported in your deployment region and supply its exact ID and ARN. The template deliberately grants only that ARN; cross-region inference profiles require additional permissions and are not configured here.

From `services/retro-coach`:

```sh
sam build
sam deploy --guided
```

During guided deployment, provide `ModelId`, its matching `ModelArn`, and a randomly generated `CoachToken` of at least 32 characters. Use a password manager to generate/store the token. Do not commit it or put AWS access keys in the app. Accept creation of the stack's IAM role. AWS usage incurs charges; API throttling and concurrency limits reduce throughput but are not a spending cap.

Copy `CoachEndpoint` from the stack outputs. Open FireSpot Settings → Retro Coach and enter this HTTPS URL and the access token. Enter a game title and ask a specific question. The title is optional. Questions are limited to 1,000 characters and responses to 350 output tokens. The UI reports authentication, network, and service failures without replacing them with canned answers.

## Verification

```sh
python3 -m venv /tmp/firespot-coach-venv
/tmp/firespot-coach-venv/bin/pip install -r services/retro-coach/requirements.txt
/tmp/firespot-coach-venv/bin/python -m unittest discover -s services/retro-coach/tests -v
```

The local tests mock the Bedrock client and cover request limits, authentication before inference, Converse request construction, and redacted service errors. They do not prove live AWS deployment or model access. After deployment, verify a real answer in the app, an invalid-token failure, and connectivity failure handling. For hackathon evidence, include a recording of a live request and describe the Bedrock, Lambda, and API Gateway integration in the submission's AWS feedback fields.

Delete the CloudFormation stack when the endpoint is no longer needed. Configure credentials locally and deploy the endpoint before connecting the app.

## Knowledge grounding and spoken playback

The coach now supports the optional `KnowledgeBaseId` deployment parameter and a `/speech` endpoint for Amazon Polly. The screen shows retrieved reference excerpts, general-guidance status, and explicit Listen / Stop controls. Full cloud-save, knowledge-base setup, playback, and validation details are documented in AWS_CLOUD_FEATURES.md.
