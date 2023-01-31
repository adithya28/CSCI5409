from flask import Flask
from flask import request
from os import path
import requests
import json
app = Flask(__name__)

@app.route('/checksum',methods=['GET','POST'])
def clean_input():
    global response_dict
    try:
        response_dict = dict()
        request_data=json.loads(request.data)
        if(request_data['file']==None or request_data['file']=='null'):
            response_dict['file']=None
            response_dict['error']="Invalid JSON input."
        elif(not path.exists('/app/'+ request_data['file'])):
            response_dict['file']=request_data['file']
            response_dict['error']="File not found."
        else:
            checksum=get_checksum(request_data)
            response_dict['file']=request_data['file']
            response_dict['checksum']=checksum.content.decode()
        return response_dict
    except:
        response_dict['file'] = None
        response_dict['error'] = "Invalid JSON input."
        return response_dict

def get_checksum(request_data):
    query = {'file':request_data['file']}
    response = requests.get('http://compute:3001/checksum', params=query)
    return response

if __name__ == "__main__":
    app.run(host='0.0.0.0',port=5000,debug=True)

