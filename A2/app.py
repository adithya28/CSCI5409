import logging
import random
import string
import urllib
import boto3
import requests

from botocore.exceptions import ClientError
from flask import Flask, request, json

app = Flask(__name__)
s3_bucket_name = "ad368540-a2"
s3_region = "us-east-1"
s3_file_name = ''

@app.route('/begin', methods=['POST','GET'])
def start():
    ## get public IP of EC2 instance.
    data = json.loads(urllib.request.urlopen("http://ip.jsontest.com/").read())
    query_params = {'banner': 'B00886916', 'ip': data['ip']}
    response = requests.post(json=query_params,url="http://52.91.127.198:8080/start")
    return 200

@app.route('/storedata', methods=['POST'])
def store_data():
    file_data = json.loads(request.data)['data']
    ## random filename string
    random_string = lambda n: ''.join([random.choice(string.ascii_lowercase) for i in range(n)])
    global s3_file_name
    s3_file_name = random_string(6)
    f = open(s3_file_name, "w")
    f.write(file_data)
    f.close()
    s3_client = boto3.client('s3') 
    try:
        response = s3_client.upload_file(s3_file_name, s3_bucket_name, s3_file_name)
    except ClientError as e:
        logging.error(e)
    ## http://s3-REGION-.amazonaws.com/BUCKET-NAME/KEY s3 URL basic format, reconstructing

    s3_object_uri = 'https://' + s3_bucket_name + '.s3.amazonaws.com/' + s3_file_name
    return {"s3uri":s3_object_uri},200


@app.route('/appenddata', methods=['POST'])
def append_data():
    s3_client = boto3.client('s3')
    file_data = json.loads(request.data)['data']
    b=bytes(file_data, 'utf-8')
    with open(s3_file_name, 'wb+') as f:
        s3_client.download_fileobj(s3_bucket_name, s3_file_name, f)
        f.write(b)
    s3_client.upload_file(s3_file_name, s3_bucket_name, s3_file_name)
    return {'data': file_data}, 200


@app.route('/deletefile', methods=['POST'])
def delete_file():
    s3_client = boto3.client('s3')
    s3_uri = json.loads(request.data)['s3uri']
    object_name = s3_uri.split('/')[-1]
    s3_client.delete_object(Bucket=s3_bucket_name, Key=object_name)
    return json.dumps({'data': "file_data"}), 200, {'ContentType': 'application/json'}


# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80, debug=True)
    start()
# See PyCharm help at https://www.jetbrains.com/help/pycharm/
