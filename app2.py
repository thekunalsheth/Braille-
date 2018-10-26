from __future__ import print_function
from importlib import import_module
import os
from flask import Flask, render_template, Response, request
from flask_uploads import UploadSet, configure_uploads
import sys
import json
from darkflow.net.build import TFNet
import cv2
from io import BytesIO
import time
from PIL import Image
import numpy as np
from flask_restful import Resource, Api
from werkzeug import secure_filename
from google.cloud import vision
import urllib2
from poster.encode import multipart_encode
import requests
import subprocess
import ast
from PIL import Image

options = {
    'model': 'cfg/tiny-yolo.cfg',
    'load': 'bin/tiny-yolo.weights',
    'threshold': 0.5,
}

tfnet = TFNet(options)
client = vision.ImageAnnotatorClient()

app = Flask(__name__)
api=Api(app)



class JSON(Resource):
    def post(self):
        file = request.files['img']
        # file_name = "frames/"+file.filename
        # file.save(file_name)
        # imgcv = cv2.imread(file_name)
        # results = tfnet.return_predict(imgcv)
        # #return str(results)

        encoded_string = file.read()
        response = client.annotate_image({'image': {'content': encoded_string}, })
        print (response)
        final = {"object":[], 'emotion':[]}
        for obj in response.localized_object_annotations:
            newList = {}
            newList['label'] = obj.name
            newList['score'] = obj.score
            newList['topx'] = obj.bounding_poly.normalized_vertices[0].x
            newList['topy'] = obj.bounding_poly.normalized_vertices[0].y
            newList['bottomx'] = obj.bounding_poly.normalized_vertices[2].x
            newList['bottomy'] = obj.bounding_poly.normalized_vertices[2].y
            final['object'].append(newList)
        for emo in response.face_annotations:
            newList = {}
            newList['joy'] = emo.joy_likelihood
            newList['sorrow']= emo.sorrow_likelihood
            newList['anger'] = emo.anger_likelihood
            newList['surprise'] = emo.surprise_likelihood
            final['emotion'].append(newList)
        print (final)
        return final

        # datagen, headers = multipart_encode({"file":file})
        # req = urllib2.Request("http://localhost:5000/model/predict", datagen, headers)
        # req.add_header('accept','application/json')
        # req.add_header('Content-Type','multipart/form-data')
        # response = urllib2.urlopen(req)
        # return response
        # headers = {'accept':'application/json', 'Content-Type':'multipart/form-data'}
        # files = {'image': ('input.jpg', open('/home/kunal/Documents/input.jpg'), 'image/jpeg')}
        # url = "http://localhost:5000/model/predict"
        # output = requests.post(url, files = files, headers = headers)
        # return output.content
        

class JSON1(Resource):
    def post(self):
        file = request.files['img']
        file.save('abcd.jpg')
        # img = Image.open(file)
        # newFile = img.resize((256, 256))
        # newFile.save('abcd.png','png') 
        output = subprocess.check_output(['curl','-X', 'POST','http://localhost:5000/model/predict','-F','image=@abcd.jpg;type=image/jpg'])
        tempj = ast.literal_eval(str(output))
        print (tempj['predictions'][0]['caption'])
        return {'caption': tempj['predictions'][0]['caption']}



api.add_resource(JSON, '/json')
api.add_resource(JSON1, '/json1')

if __name__ == "__main__":
    app.run(host='127.0.0.1', debug=True, port=24000)