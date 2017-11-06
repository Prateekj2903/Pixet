#loading the required dependencies
from flask import Flask, request, jsonify
import base64
import requests
import json
import os
import numpy as np
import cv2
import io
from PIL import Image, ImageFilter, ImageDraw, ImageFont
import pytesseract
# from google.cloud import vision


org_img_filename = 'received_image.png'
reshaped_img_filename = 'reshaped_image.png'
preprocessed_img_filename = 'preprocessed_image.png'

# os.environ["GOOGLE_APPLICATION_CREDENTIALS"]="/Users/windo/Desktop/apikey.json"

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

        print('Image Received')
        
        # Decoding the base64 string ie the image
        imgdata = base64.b64decode(newFile)
        
        # Saving the retrieved image in root directory
        
        with open(org_img_filename, 'wb') as f:
            f.write(imgdata)

        #Preprocessing of the original image for better accuracy
        preprocess_image()

#         vision_client = vision.Client()

        #Loading the image
        with io.open(preprocessed_img_filename, 'rb') as image_file:
            content = image_file.read()
#         image = vision_client.image(content=content, )

        #Detecting text in image
#         text = image.detect_text()
        text = pytesseract.image_to_string(content)

        ans = ""
        if len(text) != 0:
            ans = text[0].description
            print("Extracted Text")
            print(ans)

        else:
        	print("NO TEXT DETECTED")

        return jsonify(result=ans, len=len(text))

    
def get_size_of_scaled_image(img):
    length, width = img.size
    factor = max(1, int(1800 / length))
    size = factor * length, factor * width
    img_resized = img.resize(size, Image.ANTIALIAS)
    return img_resized


def preprocess_image():
    image = Image.open(org_img_filename)
 
    #Resizing the image
    resized_image = get_size_of_scaled_image(image)
    
    #Saving the resized image
    resized_image.save(reshaped_img_filename,"JPEG", dpi=(300, 300))

    #Reading the image as numpy array to apply preprocessing
    image = cv2.imread(reshaped_img_filename, 0)

    #removing noise
    filtered = cv2.adaptiveThreshold(image.astype(np.uint8), 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY, 41, 3)
    kernel = np.ones((1, 1), np.uint8)
    #Opening is just another name of erosion followed by dilation
    opening = cv2.morphologyEx(filtered, cv2.MORPH_OPEN, kernel)
    #Closing is reverse of Opening, Dilation followed by Erosion
    closing = cv2.morphologyEx(opening, cv2.MORPH_CLOSE, kernel)

    #smoothening
    _, th1 = cv2.threshold(image,180, 255, cv2.THRESH_BINARY)
    _, th2 = cv2.threshold(th1, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    blur = cv2.GaussianBlur(th2, (1, 1), 0)
    _, th3 = cv2.threshold(blur, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

    preprocessed_img = cv2.bitwise_or(th3, closing)
   
    #Saving the preprocessed image
    cv2.imwrite(preprocessed_img_filename, preprocessed_img)


if __name__ == '__main__':
    app.debug = False
    app.run(host='0.0.0.0', port=8079)  

