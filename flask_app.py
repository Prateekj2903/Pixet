#loading the required dependencies
from flask import Flask, request, jsonify, send_file
import base64
import requests
import json
import os
import numpy as np
import cv2
import io
from PIL import Image
from PIL import ImageFilter
import urllib
import time
from google.cloud import vision

os.environ["GOOGLE_APPLICATION_CREDENTIALS"]="/Users/windo/Desktop/apikey.json"

app = Flask(__name__)
APP_ROOT = os.path.dirname(os.path.abspath(__file__))

#defining the api end-point for image handling on server
@app.route('/')

@app.route('/image', methods=['GET', 'POST'])
def imageFunction():
    # GET request to check if the end-point is working
    if request.method == 'GET':
        return "getting data"
    #POST request to recieve image from android app to server
    elif request.method == 'POST':
        # Getting data in the form of JSON with specifies keys
        data=request.get_json(force=True)
        newFile=data['imageFile']

        print('Image Recieved')
        # Decoding the base64 string ie the image
        imgdata = base64.b64decode(newFile)
        
        # Saving the retrieved image in root directory
        filename = 'some_image.png'
        with open(filename, 'wb') as f:
            f.write(imgdata)

        
        vision_client = vision.Client()
        file_name = 'some_image.png'

        #Loading the image
        with io.open(file_name, 'rb') as image_file:
            content = image_file.read()
        image = vision_client.image(content=content, )

        #Detecting text in image
        text = image.detect_text()

        ans = " "
        if len(text) != 0:
        	ans = text[0].description
        print(len(text))
        print(ans)
        return jsonify(result=ans, len=len(text))

if __name__ == '__main__':
    app.debug = False
    app.run(host='0.0.0.0', port=8079)  

