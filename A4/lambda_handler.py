import json
import boto3

# Create SQS client
sqs = boto3.client('sqs')

connect_queue_url = 'https://sqs.us-east-1.amazonaws.com/588785002555/ConnectQueue'
subscribe_queue_url = 'https://sqs.us-east-1.amazonaws.com/588785002555/SubscribeQueue'
publish_queue_url = 'https://sqs.us-east-1.amazonaws.com/588785002555/PublishQueue'


def poll_connect_queue():
    response = sqs.receive_message(
        QueueUrl=connect_queue_url,
        AttributeNames=[
            'SentTimestamp'
        ],
        MaxNumberOfMessages=1,
        MessageAttributeNames=[
            'All'
        ],
        WaitTimeSeconds=0
    )
    message = response['Messages'][0]
    message_body = json.loads(message['Body'])
    receipt_handle = message['ReceiptHandle']
    response = sqs.delete_message(
        QueueUrl=connect_queue_url,
        ReceiptHandle=receipt_handle
    )
    return {"type": "CONNACK", "returnCode": 0, "username": message_body["username"],
            "password": message_body["password"]}


def poll_subscribe_queue():
    response = sqs.receive_message(
        QueueUrl=subscribe_queue_url,
        AttributeNames=[
            'SentTimestamp'
        ],
        MaxNumberOfMessages=1,
        MessageAttributeNames=[
            'All'
        ],
        WaitTimeSeconds=0
    )
    message = response['Messages'][0]
    message_body = json.loads(message['Body'])
    receipt_handle = message['ReceiptHandle']
    response = sqs.delete_message(
        QueueUrl=subscribe_queue_url,
        ReceiptHandle=receipt_handle
    )
    return {"type": "SUBACK", "returnCode": 0}


def poll_publish_queue():
    response = sqs.receive_message(
        QueueUrl=publish_queue_url,
        AttributeNames=[
            'SentTimestamp'
        ],
        MaxNumberOfMessages=1,
        MessageAttributeNames=[
            'All'
        ],
        WaitTimeSeconds=0
    )
    message = response['Messages'][0]
    message_body = json.loads(message['Body'])
    message_body_json = json.loads(json.dumps(message_body))
    receipt_handle = message['ReceiptHandle']
    response = sqs.delete_message(
        QueueUrl=publish_queue_url,
        ReceiptHandle=receipt_handle
    )
    return {"type": "PUBACK", "returnCode": 0, "payload": message_body_json["payload"]}


def lambda_handler(event, context):
    eventBody = json.loads(event['body'])
    if (eventBody["type"] == "PUBLISH"):
        return poll_publish_queue()
    elif (eventBody["type"] == "SUBSCRIBE"):
        return poll_subscribe_queue()
    return poll_connect_queue()


