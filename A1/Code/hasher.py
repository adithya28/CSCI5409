from flask import Flask
from flask import request
import hashlib
app = Flask(__name__)

@app.route('/checksum',methods=['GET','POST'])
def get_checksum():
    filename ='/data/'+request.args['file']
    md5_hash = hashlib.md5()
    file = open(filename, "rb")
    contents = file.read()
    md5_hash.update(contents)
    digest = md5_hash.hexdigest()
    return digest

if __name__ == "__main__":
    app.run(host='0.0.0.0',debug=True,port =3001)
