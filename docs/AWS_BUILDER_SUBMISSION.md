# AWS Builder submission field

FireSpot incorporates Amazon Bedrock, Bedrock Knowledge Bases, Amazon Polly, Amazon Cognito, Amazon S3, AWS Lambda, and Amazon API Gateway. Retro Coach sends gameplay and app-control questions through Lambda to Bedrock. Its Knowledge Base integration retrieves FireSpot documentation and displays reference excerpts alongside the answer. Polly provides optional spoken answers, with playback controls and ambient-music ducking. Cognito provides account creation, email verification, and sign-in for cloud saves. JWT-authorized API Gateway routes let Lambda issue short-lived S3 requests for private backup and restore. Backups contain save states and cartridge progress, preserve newer local files during restore, and exclude game ROMs. The repository includes AWS SAM deployment templates, configurable Android connection settings, request-validation and archive-safety tests, and detailed deployment and usage documentation.

Integration guide: docs/AWS_CLOUD_FEATURES.md
Coach deployment: docs/AWS_RETRO_COACH.md
Application guide: docs/DOCUMENTATION.md
