import discord
from botocore.exceptions import ClientError
from discord.ext import commands
from discord.ext.commands import bot
import botocore
import botocore.session
import json
import boto3

target_user_name=None
target_discriminator = None

client = discord.Client(intents=discord.Intents.all())

@client.event
async def on_ready():
    guild = client.get_guild(1084164938232238160)
    user = discord.utils.get(guild.members, name=target_user_name, discriminator=target_discriminator)
    await user.send("Hello there!")

def lambda_handler(event, context):
    secret_name = "discordBotAPIKey"
    region_name = "us-east-1"

    # Create a Secrets Manager client
    session = boto3.session.Session()
    secrets_client = session.client(
        service_name='secretsmanager',
        region_name=region_name
    )

    try:
        get_secret_value_response = secrets_client.get_secret_value(
            SecretId=secret_name
        )
    except ClientError as e:
        raise e

    # Decrypts secret using the associated KMS key.
    secret = json.loads(get_secret_value_response['SecretString'])['discordBotAPIKey']
    user_details = event["discordName"].split('#')
    global target_user_name
    global target_discriminator
    target_user_name=user_details[0]
    target_discriminator=user_details[1]
    client.run(secret)
    return 200