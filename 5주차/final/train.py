import numpy as np
import cv2

# Load the data from gesture_train.csv
file = np.genfromtxt('gesture_train.csv', delimiter=',')
angle = file[:, :-1].astype(np.float32)
label = file[:, -1].astype(np.float32)

# Create a KNN model and train it
knn = cv2.ml.KNearest_create()
knn.train(angle, cv2.ml.ROW_SAMPLE, label)

# Check if the model is trained properly
if knn.isTrained():
    print("KNN model is trained successfully!")

# Save the trained KNN model to a file
knn.save('gesture_knn_model.yml')
