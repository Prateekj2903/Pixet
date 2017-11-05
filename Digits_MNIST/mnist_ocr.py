import cv2
import numpy as np
import keras
import matplotlib.pyplot as plt
import pytesseract
# from PIL import Image

img = cv2.imread('numbers1.png')
gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
gray = cv2.bitwise_not(gray)

model = keras.models.load_model('MNIST_model.h5')

ret,thresh = cv2.threshold(gray ,127,255,cv2.THRESH_BINARY)

_, contours , hierarchy = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
# print(len(contours))

# pytesseract.pytesseract.tesseract_cmd = 'C:/Program Files (x86)/Tesseract-OCR/tesseract'


fig, axes = plt.subplots(2, 5, figsize=(10, 10))
for i, ax in enumerate(axes.flat):
    contour = contours[i]
    [x, y, w, h] = cv2.boundingRect(contour)

    roi = gray[y-10:y+h+20, x-10:x+w+20]
    roi = cv2.pyrDown(roi)
    roi = cv2.resize(roi, (28, 28))

    # cv2.imwrite('asd.png', roi)
    # img = Image.open('asd.png')

    # result = pytesseract.image_to_string(img)

    ax.imshow(roi)
    roi = roi.reshape((1,28,28,1))
    pred = model.predict(roi)
    ax.set_xlabel(np.argmax(pred))
    ax.set_xticks([])
    ax.set_yticks([])
    # print(resu)
plt.show()