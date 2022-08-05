#!/usr/bin/env python
# coding: utf-8

# In[3]:


from deepface import DeepFace
import cv2
import matplotlib.pyplot as plt

def analyzeImg(img_path):
    demography = DeepFace.analyze(img_path)
    return demography

def WriteOnAfile(demography):
    fichier = open("C:/Users/emnaa/Desktop/Emotions.txt", "w")
    fichier.write(demography['dominant_emotion'])
    fichier.close
    
def main():
    # Load Image
    img_path = 'C:/Users/emnaa/eclipse-workspace/OpenCv Camera/Visage0.png'
    img = cv2.imread(img_path)
    plt.imshow(img[:, :,::-1])
    #analyzing the image
    demo=analyzeImg(img_path)
    #Writing the dominant Emotion on a File
    WriteOnAfile(demo)
    
main()


# In[ ]:




