# FireSpot AWS features

## Overview

FireSpot connects its TV interface to three optional cloud features: private save backups, document-grounded coaching, and spoken answers. Emulation and local saves operate independently of cloud connections. Deploy the supplied AWS stacks, configure the app, and follow the usage steps below.

## Cloud saves: S3 and Cognito

Settings → Cloud saves opens a TV-focused sign-in and backup screen. Expand Connection settings once to enter the deployed stack's endpoint, region, and public Cognito client ID. These values are saved locally. Passwords and identity tokens remain in memory for the session. Users can create an account, verify their email, resend a code, sign in, back up, restore, and sign out. After an expired session, sign out and sign in again. Password recovery can be performed through Cognito's APIs by the deployment operator; this initial screen does not provide a recovery flow.

Deploy from `services/cloud-saves` with AWS SAM:

```sh
sam build
sam deploy --guided
```

The stack creates a Cognito pool and client, a JWT-authorized API Gateway endpoint, a Lambda, and a private encrypted S3 bucket. It outputs Endpoint, CognitoClientId, and Region. Cognito passwords require at least 12 characters, uppercase and lowercase letters, a number, and a symbol. Email delivery depends on the account's Cognito email quota. Do not enter AWS access keys into FireSpot.

Each backup contains the `states/` and `battery/` directories. ROMs, BIOS, account data, covers, and database records are excluded. Each upload receives a new immutable object name under the verified user's identity; an upload does not replace an older snapshot. Presigned upload policies enforce a maximum 32 MiB compressed archive. Archives expand to at most 64 MiB and contain at most 2,000 entries. Backups expire after 30 days. The latest snapshot is selected for restore; the service returns up to 10 recent snapshots, though selecting older snapshots is not yet exposed in the UI.

Restore requires confirmation and fully validates paths and archive size before writing save files. Individual files are replaced using same-filesystem renames. Newer or equally recent local files are preserved. An I/O failure during the replacement phase can leave some files restored and others unchanged; it is not an all-or-nothing restore transaction. Save compatibility still requires matching game filenames and compatible console cores. Close running games before backup or restore. Uploads and downloads stream through temporary files and remove them on completion or failure. The Cognito JWT is verified by API Gateway, and the Lambda obtains the account prefix from verified claims rather than request data.

## Grounded Retro Coach: Bedrock Knowledge Bases

Deploy the coach stack as described in AWS_RETRO_COACH.md, supplying KnowledgeBaseId for an existing standard Bedrock Knowledge Base. The backend retrieves up to three reference chunks and sends them to the configured Bedrock model with the user's question. The interface displays reference excerpts and distinguishes general guidance from guidance with retrieved documents. These excerpts are retrieval references, not sentence-level model citations. If retrieval fails, the request reports a service error rather than silently claiming grounded guidance.

To prepare the knowledge base, use the Bedrock console to create a standard Knowledge Base with an S3 document source and a supported embedding model/vector store. Upload FireSpot's README.md and docs/DOCUMENTATION.md plus any verified control documentation; run data-source synchronization. Supply its ID when deploying the coach stack. Model access, vector-store configuration, embedding permissions, ingestion, and costs belong to that knowledge-base deployment. The coach SAM template connects to an existing knowledge base; it does not create the vector store or ingest documents. Do not upload ROMs or proprietary manuals.

## Spoken answers: Amazon Polly

After receiving an answer, select Listen to answer. FireSpot requests the backend's `/speech` endpoint, which calls Polly SynthesizeSpeech using the standard Joanna voice and returns MP3 data. Speech is optional and never starts automatically. Stop audio releases playback; leaving or backgrounding the screen also stops it. Playback failure keeps the text readable. Text requests are capped at 2,400 characters. The session coach token protects both endpoints, while AWS credentials remain in Lambda's execution role.

## Verification and submission evidence

```sh
JAVA_HOME=/tmp/firespot-jdk17 ./gradlew :app:testStandardDebugUnitTest :app:assembleStandardDebug
/tmp/firespot-coach-venv/bin/python -m unittest discover -s services/retro-coach/tests -v
/tmp/firespot-coach-venv/bin/python -m unittest discover -s services/cloud-saves/tests -v
cfn-lint services/retro-coach/template.yaml services/cloud-saves/template.yaml
```

Use your own Java 17 and Python environment if those local paths differ. Install each service's requirements before running its Python tests. Local tests cover archive traversal rejection, preserving newer local saves, backup scope, account isolation, upload limits, grounding, and Polly response handling.

For a functional check, create two Cognito accounts and confirm each sees only its own backups. Back up a game, restore on another installation using the same filename and compatible core, ask a control question covered by the knowledge documents, and use Listen / Stop. Check expired sessions, interrupted connections, and app backgrounding as part of deployment verification.

## Preconfigure release builds

To keep deployment fields out of the normal user flow, provide public configuration at build time using Gradle properties: `-PcoachEndpoint=... -PcloudEndpoint=... -PcognitoRegion=... -PcognitoClientId=...`. These values prefill the corresponding screens, while Connection settings stays available for operators. Never use these properties for AWS keys, passwords, or bearer tokens. The coach session token remains a separate operator-provided access control; a production multi-user coach should use individual authentication. Ambient music ducks during spoken playback and returns to its configured volume when playback stops.

## Environment configuration

From the repository root:

```sh
cp .env.example .env
# Edit .env locally and paste the values from your deployed stack outputs.
./scripts/build_v1.sh
```

The build helper sources your own local shell-compatible `.env` file, so use a trusted file and quote values containing spaces. `.env` and `.env.*` are ignored by Git; `.env.example` is the tracked template. The helper passes only the four public connection fields into Android BuildConfig. It does not embed your AWS profile, model settings, signing passwords, or credentials. A profile name selects credentials already configured through the AWS CLI; it does not create credentials.

| Local variable | Source and purpose |
|---|---|
| `FIRESPOT_COACH_ENDPOINT` | Coach stack's CoachEndpoint output, ending in `/coach` |
| `FIRESPOT_CLOUD_ENDPOINT` | Cloud stack's Endpoint output, ending in `/backups` |
| `FIRESPOT_COGNITO_REGION` | Cloud stack's Region output |
| `FIRESPOT_COGNITO_CLIENT_ID` | Cloud stack's CognitoClientId output; public client, no secret |
| `AWS_PROFILE` | Your authenticated local AWS CLI profile |
| `AWS_DEFAULT_REGION` | Region in which you deploy the stacks |
| `FIRESPOT_MODEL_ID` | Regional Converse-compatible foundation model ID; supply during guided deployment |
| `FIRESPOT_MODEL_ARN` | Exact matching model ARN for the Lambda invocation policy |
| `FIRESPOT_KNOWLEDGE_BASE_ID` | Existing synchronized standard Knowledge Base ID; supply during guided deployment |

The model and Knowledge Base variables are operator reference values, not automatic SAM parameter overrides. Enter them in `sam deploy --guided`. The generated coach bearer token is a separate deployment parameter and is entered in the app per session. Do not paste secrets into chat or commit them. Website routes, blog URLs, core download URLs, and existing release links do not change when these settings are added.

If signing inputs are provided privately through `FIRESPOT_KEYSTORE`, `FIRESPOT_STORE_PASSWORD`, and `FIRESPOT_KEY_PASSWORD`, Gradle uses the existing `firespot` distribution alias. Otherwise release outputs are unsigned and must be signed with the established distribution key before installation. Do not use a new key for an update to an existing installation. The build helper retains versionName 1.0.0 and uses Android versionCode 5.

## API contracts

### Coach requests

`POST /coach` takes `Authorization: Bearer YOUR_COACH_TOKEN` and JSON:

```json
{"game":"Game Boy Advance", "question":"How do I remap controls in FireSpot?"}
```

The response contains `answer` (text), `grounded` (boolean), and `sources` (a list of objects with `label` and `excerpt`). The title limit is 160 characters. The question must contain 1–1,000 characters after trimming. The entire incoming encoded body is capped at 12,000 characters. Empty model output produces HTTP 502; malformed input produces 400; invalid authentication produces 401; service access errors produce 503. Model and retrieval exceptions are not returned with private internal details.

When KnowledgeBaseId is configured, Lambda calls Bedrock Agent Runtime Retrieve with three results. Each retrieved chunk is capped at 4,000 characters; the UI receives an excerpt capped at 600 characters. Those reference documents are provided to Bedrock Runtime Converse as user data, alongside the title and question. The model's system instruction requests concise guidance, distinguishes unknown facts, and prevents claims of seeing gameplay. Responses use a maximum of 350 tokens and temperature 0.3. Retrieved text is context, not executable app commands.

`POST /speech` uses the same coach token and accepts `{"text":"The answer to read aloud."}`. It returns `{"audio":"BASE64_ENCODED_MP3"}`. Input must contain 1–2,400 characters. Lambda requests plain-text, standard-engine Joanna speech. The app decodes the response to a temporary MP3, prepares MediaPlayer asynchronously, and removes the temporary file when playback ends or is stopped. It does not save spoken answers in the game library.

### Cloud save requests

Cloud endpoints require a Cognito **ID token** in `Authorization: Bearer ID_TOKEN`. The supplied Cognito app client has no client secret. API Gateway validates its JWT against the pool issuer and client audience before Lambda receives the request. The Lambda reads `requestContext.authorizer.jwt.claims.sub`; a caller-supplied account identifier in JSON is ignored.

`POST /backups` returns an `upload` object with `url` and `fields` from S3's presigned POST API. The Android client streams a multipart upload, including those exact fields followed by the ZIP file, directly to S3. The signed policy expires in 120 seconds and limits file length to 1–33,554,432 bytes. Generating a policy does not create a successful backup; the UI reports success only after S3 returns a successful HTTP response.

Object keys use `users/SUB/INVERSE_TIMESTAMP-UUID.zip`. The inverse timestamp puts the newest keys first in S3's lexicographic listing. The UUID prevents concurrent uploads from overwriting each other. `GET /backups` returns `{"backups":[{"created":"TIMESTAMP","size":1234,"url":"SIGNED_DOWNLOAD_URL"}]}` for up to ten objects in that verified account's prefix. Download URLs expire after 120 seconds. The UI selects the first result when Restore latest is confirmed. Account isolation is enforced in the server's prefix selection; presigned URLs are bearer capabilities and should not be shared.

### Cognito account calls

The Android client sends AWS JSON protocol requests over HTTPS to the regional Cognito endpoint. It uses SignUp, ConfirmSignUp, ResendConfirmationCode, and InitiateAuth with USER_PASSWORD_AUTH. Passwords are sent to Cognito over TLS and remain only in the current screen's memory. The returned ID token remains in memory and is discarded on sign-out or screen destruction. MFA and other additional sign-in challenges receive an explanatory message; this screen implements the password and email-verification flow configured by the supplied stack. Session refresh and password-recovery UI are separate future extensions.

## Permissions and data boundaries

The coach role can invoke the configured model ARN, retrieve the configured Knowledge Base ARN, and synthesize speech with Polly. Polly's synthesis operation uses a wildcard resource because it does not target a stored application resource. The API limits coach traffic to one request per second with a burst of two, and the function reserves at most two concurrent executions. Limits reduce concurrency but are not a cost ceiling.

The cloud role can list its backup bucket and get/put objects under `users/*`; its application code restricts each request to the verified identity prefix. The S3 bucket blocks public access, uses AES256 server-side encryption, and denies insecure transport. CloudFormation retains the backup bucket on stack replacement/deletion. Backups expire through the 30-day lifecycle rule; users' local saves are not subject to that cloud rule. The app does not upload ROMs, BIOS files, covers, stored passwords, or its library database.

## Troubleshooting and operations

| Symptom | Action |
|---|---|
| Coach reports invalid token | Re-enter the session coach token matching the stack parameter |
| Coach reports service unavailable | Check model ID/ARN, regional access, IAM permissions, and Knowledge Base configuration |
| Answer has general-guidance status | Configure KnowledgeBaseId and confirm that ingestion returns relevant chunks |
| Email code is missing | Check the address/spam folder, resend, and inspect the Cognito email quota/configuration |
| Sign-in session expired | Sign out and sign in again |
| No backup is available | Back up an installation with actual save files before restoring |
| Restore keeps a local file | Its timestamp is at least as recent as the archived file, or the archive has no usable timestamp |
| Backup is too large | Reduce local save files or use a separate transfer; the app enforces its documented limits |
| Speech fails | Keep reading the answer; check Polly access, endpoint configuration, network, and system media volume |
| Android rejects an update | Confirm distribution certificate and package ID match the installed version |

Use CloudFormation stack outputs as the source of app configuration. Keep existing stacks and API IDs when updating them if you need stable deployed API URLs. Changing app configuration does not rename project website routes. Inspect Lambda logs through CloudWatch when diagnosing infrastructure errors, and avoid adding request bodies, tokens, signed URLs, or passwords to logs. Back up the distribution signing key independently. To retire a deployment, delete its stack intentionally and review retained backup storage separately.
