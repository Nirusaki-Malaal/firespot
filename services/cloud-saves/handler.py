"""JWT identity is verified by API Gateway, never taken from request JSON."""
import datetime
import json
import os
import uuid
import boto3
from botocore.exceptions import ClientError, BotoCoreError


def reply(status, data):
    return {"statusCode": status, "headers": {"Content-Type": "application/json", "Cache-Control": "no-store"}, "body": json.dumps(data)}


def handler(event, context):
    sub = event.get("requestContext", {}).get("authorizer", {}).get("jwt", {}).get("claims", {}).get("sub", "")
    if not sub or any(c not in "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-" for c in sub):
        return reply(401, {"error": "Please sign in again."})
    s3 = boto3.client("s3")
    bucket = os.environ["SAVE_BUCKET"]
    prefix = "users/" + sub + "/"
    method = event.get("requestContext", {}).get("http", {}).get("method", "")
    try:
        if method == "POST":
            # Inverse time sorts newest backups first; each upload has its own immutable key.
            stamp = 9999999999999 - int(datetime.datetime.now(datetime.timezone.utc).timestamp() * 1000)
            key = prefix + str(stamp).zfill(13) + "-" + uuid.uuid4().hex + ".zip"
            post = s3.generate_presigned_post(bucket, key,
                Fields={"Content-Type": "application/zip"},
                Conditions=[{"Content-Type": "application/zip"}, ["content-length-range", 1, 33554432]], ExpiresIn=120)
            return reply(200, {"upload": post})
        if method == "GET":
            objects = s3.list_objects_v2(Bucket=bucket, Prefix=prefix, MaxKeys=10).get("Contents", [])
            backups = [{"created": x["LastModified"].isoformat(), "size": x["Size"],
                        "url": s3.generate_presigned_url("get_object", Params={"Bucket": bucket, "Key": x["Key"]}, ExpiresIn=120)} for x in objects]
            return reply(200, {"backups": backups})
        return reply(405, {"error": "Method not supported."})
    except (ClientError, BotoCoreError):
        return reply(503, {"error": "Cloud saves are unavailable. Local saves remain on this device."})
