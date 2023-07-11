from flask import Flask, render_template, Response, request, redirect, url_for
import cv2
from cvzone.FaceMeshModule import FaceMeshDetector
import cvzone
import os
import random
from datetime import datetime
from google.cloud import firestore

import firebase_admin
from firebase_admin import credentials, firestore

cred = credentials.Certificate('C:/Users/1/Downloads/opencvGame/serviceAccountKey.json')
firebase_admin.initialize_app(cred)
db = firestore.client()

app = Flask(__name__)

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/save_result', methods=['POST'])
def save_result_to_db(result, count):
    current_time = datetime.now()
    doc_ref = db.collection('results').document()
    doc_ref.set({
        'result': result,
        'count': count,
        'timestamp': current_time
    })

@app.route('/video_feed')
def video_feed():
    return Response(generate_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')


@app.route('/leaderboard')
def leaderboard():
    # Retrieve all count values from Firebase and create a ranking list with the corresponding timestamps
    results = db.collection('results').get()
    count_timestamp_list = []

    for doc in results:
        data = doc.to_dict()
        if 'count' in data and 'timestamp' in data:
            count = data['count']
            timestamp = data['timestamp']
            count_timestamp_list.append((count, timestamp))

    # Sort the count_timestamp_list based on count values in descending order
    count_timestamp_list = sorted(count_timestamp_list, key=lambda x: x[0], reverse=True)

    # Assign ranks to count_timestamp_list
    ranked_list = []
    rank = 1
    for i, (count, timestamp) in enumerate(count_timestamp_list):
        if i > 0 and count != count_timestamp_list[i-1][0]:
            rank += 1
        ranked_list.append((rank, count, timestamp))

    return render_template('leaderboard.html', count_timestamp_list=ranked_list)






def save_result_to_db(count):
    # 결과와 count 값을 Firestore에 저장
    current_time = firestore.SERVER_TIMESTAMP
    doc_ref = db.collection('results').document()
    doc_ref.set({
        'count': count,
        'timestamp': current_time
    })
def generate_frames():
    cap = cv2.VideoCapture(0)
    cap.set(3, 1280)
    cap.set(4, 720)

    detector = FaceMeshDetector(maxFaces=2)
    idList = [0, 17, 78, 292]

    folderEatable = 'C:/Users/1/Downloads/opencvGame/Objects/eatable'
    listEatable = os.listdir(folderEatable)
    eatables = []
    for obj in listEatable:
        eatables.append(cv2.imread(f'{folderEatable}/{obj}', cv2.IMREAD_UNCHANGED))

    folderNonEatable = 'C:/Users/1/Downloads/opencvGame/Objects/noneatable'
    listNonEatable = os.listdir(folderNonEatable)
    nonEatables = []
    for obj in listNonEatable:
        nonEatables.append(cv2.imread(f'{folderNonEatable}/{obj}', cv2.IMREAD_UNCHANGED))

    currentObject = eatables[0]
    pos = [300, 0]
    speed = 5
    count = 0
    global isEatable
    isEatable = True
    gameOver = False

    def resetObject():
        global isEatable
        pos[0] = random.randint(100, 1180)
        pos[1] = 0
        randNo = random.randint(0, 2)
        if randNo == 0:
            currentObject = nonEatables[random.randint(0, 3)]
            isEatable = False
        else:
            currentObject = eatables[random.randint(0, 3)]
            isEatable = True

        return currentObject

    while True:
        success, img = cap.read()

        if gameOver is False:
            img, faces = detector.findFaceMesh(img, draw=False)

            img = cvzone.overlayPNG(img, currentObject, pos)
            pos[1] += speed

            if pos[1] > 520:
                currentObject = resetObject()

            if faces:
                face = faces[0]

                up = face[idList[0]]
                down = face[idList[1]]

                for idx in idList:
                    cv2.circle(img, face[idx], 5, (255, 0, 255), 5)
                cv2.line(img, face[idList[2]], face[idList[3]], (0, 255, 0), 3)

                upDown, _ = detector.findDistance(face[idList[0]], face[idList[1]])
                leftRight, _ = detector.findDistance(face[idList[2]], face[idList[3]])

                cx, cy = (up[0] + down[0]) // 2, (up[1] + down[1]) // 2
                cv2.line(img, (cx, cy), (pos[0] + 50, pos[1] + 50), (0, 255, 0), 3)
                distMouthObject, _ = detector.findDistance((cx, cy), (pos[0] + 50, pos[1] + 50))  # Calculate distance between the middle point and the object
                print(distMouthObject)

                ratio = int((upDown / leftRight) * 100)
                print(ratio)
                if ratio > 60:
                    mouthStatus = "Open"
                else:
                    mouthStatus = "Closed"

                cv2.putText(img, mouthStatus, (50, 50), cv2.FONT_HERSHEY_COMPLEX, 2, (255, 0, 255), 2)

                if distMouthObject < 100 and ratio > 60:
                    if isEatable:
                        currentObject = resetObject()
                        count += 1
                    else:
                        gameOver = True
            cv2.putText(img, str(count), (1100, 50), cv2.FONT_HERSHEY_COMPLEX, 2, (255, 0, 255), 2)
        else:
            cv2.putText(img, "Game Over", (300, 400), cv2.FONT_HERSHEY_PLAIN, 2, (255, 0, 255), 10)
            if count > 0:  # 게임이 종료되었을 때 count 값이 0보다 큰 경우에만 결과 전송
                result = 'Game Over'
                # 결과와 count 값을 전송
                save_result_to_db(count)
                count = 0  # count 값을 초기화

        ret, buffer = cv2.imencode('.jpg', img)
        frame = buffer.tobytes()
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')

        key = cv2.waitKey(1)
        if key == ord('r'):
            resetObject()
            gameOver = False
            count = 0
            currentObject = eatables[0]
            pos = [300, 0]
        if key == 27:
            break

    cap.release()
    cv2.destroyAllWindows()

if __name__ == '__main__':
    app.run(debug=True)
