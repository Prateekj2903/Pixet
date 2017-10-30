#!/usr/bin/python
# -*- coding: utf-8 -*-
#loading the required dependencies
from flask import Flask, request, jsonify, send_file
import base64
import requests
import pytesseract
import json
import os
import numpy as np
from pyfcm import FCMNotification
import cv2
import io
from PIL import Image
import urllib
import time
from collections import OrderedDict
from operator import itemgetter


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

        # Decoding the base64 string ie the image
        imgdata = base64.b64decode(newFile)
        
        # Saving the retrieved image in root directory
        filename = 'some_image.png'
        with open(filename, 'wb') as f:
            f.write(imgdata)
        # Retrieving image from root directory
        img = cv2.imread('some_image.png', 0)
        print(img.shape)
        # if i==0 we call the detection of digit function

        val = detect_images(img)

        testNotif(0, val)
        return "{'status':'Success'}"

# This function is used to detect the digit from an image
def detect_images(img):
    #Applying bitwise not operation on image
    # img = cv2.bitwise_not(img)
        
    #Scale down the image to the required size  
    # for j in range(5):
    #     img = cv2.pyrDown(img)

    #Setting up the parameters for tesseract
    # kernel = np.ones((1, 1), np.uint8)
    # img = cv2.dilate(img, kernel, iterations=1)
    # img = cv2.erode(img, kernel, iterations=1)

    # tessdata_dir_config = '--tessdata-dir "C:/Program Files (x86)/Tesseract-OCR/tesseract" '
    pytesseract.pytesseract.tesseract_cmd = 'C:/Program Files (x86)/Tesseract-OCR/tesseract'
    
    #Applying tesseract on the input image to detect characters 
    arr = Image.fromarray(img)
    result = pytesseract.image_to_string(arr)
    
    print(result)
    return result

def testNotif(i, val):
    #Sends notification to the app with calculated result
    push_service = FCMNotification(api_key="AAAAupVl040:APA91bHF4i3_t-8vnZgES2pNDHHtx7EDGGQ_t6lRWMc5QA3ehSVkJgGJIHXVgtr1ZbkBGpmXAaWRsfwCkT-pWX0PzXDppYrMiXCAQ1dRVNq6Cv0Uiw_j8HfkDYhGQIEmxOElfJ0DdtDm")
    registration_id = "c4zjdHKlkjo:APA91bFWi-kFkoxF1l-AYZHmX4XVqEvcc2B5UHYOhLX6ThM4p0PHNsGYiyDvIky4kAHgHFNZLGwPDDxuKg6gOLVsNA74GCYhw5xHRgzTDDl659MUIwp0Gzv515gJbavS66hgYeKnXVVj"

    data_message = {
        "ans":val, 
        "type":i
        }
    result = push_service.notify_single_device(registration_id=registration_id, data_message=data_message)
    print(result)
    return "success"

if __name__ == '__main__':
    app.debug = False
    app.run(host='0.0.0.0', port=8079)  

