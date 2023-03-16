import base64
import urllib
from Crypto.Cipher import PKCS1_OAEP
from Crypto.PublicKey import RSA
import requests
from flask import Flask, request, json

app = Flask(__name__)

@app.route('/begin', methods=['POST','GET'])
def start():
    ## get public IP of EC2 instance.
    data = json.loads(urllib.request.urlopen("http://ip.jsontest.com/").read())
    query_params = {'banner': 'B00886916', 'ip': data['ip']}
    response = requests.post(json=query_params,url="http://44.202.179.158:8080/start")
    return {"response":"started"},200

@app.route('/decrypt', methods=['POST'])
def decrypt_data():
    private_key = RSA.importKey(open('privateKey.pem').read())
    message_data = json.loads(request.data)['message']
    OAEP_cipher = PKCS1_OAEP.new(private_key)
    decrypted_msg = OAEP_cipher.decrypt(base64.b64decode(message_data))
    return {"response":decrypted_msg.decode('utf-8')},200


@app.route('/encrypt', methods=['POST'])
def encrypt_data():
    message_data = json.loads(request.data)['message']
    raw_data=message_data.encode("ascii")
    RSA_public_key = RSA.importKey(open('publicKey.pem').read())
    OAEP_cipher = PKCS1_OAEP.new(RSA_public_key)
    encrypted_msg =base64.b64encode(OAEP_cipher.encrypt(raw_data))
    json_message = encrypted_msg.decode('utf-8')
    return {'response': json_message}, 200
