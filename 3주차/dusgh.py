import os  # 파일 시스템과 상호작용하기 위한 모듈
import random  # 난수 생성 모듈
import cv2  # 컴퓨터 비전 작업을 위한 OpenCV 라이브러리
from cvzone.FaceMeshModule import FaceMeshDetector  # cvzone에서 FaceMesh 검출기 모듈을 가져옴
import cvzone  # 컴퓨터 비전 작업을 도와주는 라이브러리

cap = cv2.VideoCapture(0)  # 비디오 캡처 객체 생성 (0은 기본 카메라를 의미)
cap.set(3, 1280)  # 비디오의 가로 크기를 1280으로 설정
cap.set(4, 720)  # 비디오의 세로 크기를 720으로 설정

detector = FaceMeshDetector(maxFaces=1)  # 최대 1개의 얼굴을 감지하는 FaceMeshDetector 객체 생성
idList = [0, 17, 78, 292]  # 사용할 FaceMesh 지점의 ID 리스트 생성

# 먹을 수 있는 오브젝트의 이미지를 로드
folderEatable = 'C:/Users/1/Downloads/opencvGame/Objects/eatable'
listEatable = os.listdir(folderEatable)
eatables = []
for object in listEatable:
    eatables.append(cv2.imread(f'{folderEatable}/{object}', cv2.IMREAD_UNCHANGED))

# 먹을 수 없는 오브젝트의 이미지를 로드
folderNonEatable = 'C:/Users/1/Downloads/opencvGame/Objects/noneatable'
listNonEatable = os.listdir(folderNonEatable)
nonEatables = []
for object in listNonEatable:
    nonEatables.append(cv2.imread(f'{folderNonEatable}/{object}', cv2.IMREAD_UNCHANGED))


currentobject = eatables[0]  # 현재 오브젝트를 먹을 수 있는 첫 번째 오브젝트로 설정
pos = [300, 0]  # 오브젝트의 초기 위치 설정
speed = 5  # 오브젝트의 초기 속도 설정
count = 0  # 게임 점수를 0으로 초기화
global isEatable  # 먹을 수 있는지 여부를 나타내는 글로벌 변수
isEatable = True  # 오브젝트는 초기에 먹을 수 있음
gameOver = False  # 게임 오버 상태를 False로 초기화


def resetobject():  # 오브젝트를 리셋하는 함수
    global isEatable
    pos[0] = random.randint(100, 1180)  # 오브젝트의 x 좌표를 랜덤하게 설정
    pos[1] = 0  # 오브젝트의 y 좌표를 0으로 설정
    randNo = random.randint(0, 2)  # 0, 1, 2 중 랜덤한 숫자를 생성
    if randNo == 0:  # 만약 생성된 숫자가 0이라면,
        currentobject = nonEatables[random.randint(0, 3)]  # 먹을 수 없는 오브젝트 중 하나를 랜덤하게 선택
        isEatable = False  # 오브젝트는 먹을 수 없음
    else:  # 생성된 숫자가 0이 아니라면,
        currentobject = eatables[random.randint(0, 3)]  # 먹을 수 있는 오브젝트 중 하나를 랜덤하게 선택
        isEatable = True  # 오브젝트는 먹을 수 있음

    return currentobject  # 선택된 오브젝트를 반환


while True:  # 무한 루프
    success, img = cap.read()  # 카메라로부터 이미지를 캡처

    if gameOver is False:  # 게임 오버 상태가 아니라면,
        img, faces = detector.findFaceMesh(img, draw=False)  # FaceMesh를 찾아서 그리지 않음

        img = cvzone.overlayPNG(img, currentobject, pos)  # 현재 오브젝트를 이미지에 오버레이
        pos[1] += speed  # 오브젝트의 y 좌표를 속도만큼 증가시킴

        if pos[1] > 520:  # 오브젝트가 화면 하단에 도달하면,
            currentobject = resetobject()  # 오브젝트를 리셋

        if faces:  # 얼굴이 감지되면,
            face = faces[0]  # 첫 번째 얼굴을 선택

            up = face[idList[0]]  # 얼굴의 위쪽 지점 선택
            down = face[idList[1]]  # 얼굴의 아래쪽 지점 선택

            for id in idList:  # idList의 모든 id에 대해,
                cv2.circle(img, face[id], 5, (255, 0, 255), 5)  # 얼굴 지점에 원을 그림
            cv2.line(img, up, down, (0, 255, 0), 3)  # 얼굴의 위쪽과 아래쪽 지점 사이에 선을 그림
            cv2.line(img, face[idList[2]], face[idList[3]], (0, 255, 0), 3)  # 얼굴의 두 가운데 지점 사이에 선을 그림

            upDown, _ = detector.findDistance(face[idList[0]], face[idList[1]])  # 위쪽과 아래쪽 지점 사이의 거리 계산
            leftRight, _ = detector.findDistance(face[idList[2]], face[idList[3]])  # 두 가운데 지점 사이의 거리 계산

            ## Distance of the object
            cx, cy = (up[0] + down[0]) // 2, (up[1] + down[1]) // 2  # 위쪽과 아래쪽 지점의 중간 좌표 계산
            cv2.line(img, (cx, cy), (pos[0] + 50, pos[1] + 50), (0, 255, 0), 3)  # 중간 좌표와 오브젝트 사이에 선을 그림
            distMouthobject, _ = detector.findDistance((cx, cy), (pos[0] + 50, pos[1] + 50))  # 중간 좌표와 오브젝트 사이의 거리 계산
            print(distMouthobject)  # 계산된 거리를 출력

            # Lip opened or closed
            ratio = int((upDown / leftRight) * 100)  # 입이 열려있는지 여부를 결정하는 비율 계산
            # print(ratio)  # 계산된 비율을 출력
            if ratio > 60:  # 비율이 60보다 크면,
                mouthStatus = "Open"  # 입이 열려있음
            else:  # 비율이 60보다 작거나 같으면,
                mouthStatus = "Closed"  # 입이 닫혀있음
            cv2.putText(img, mouthStatus, (50, 50), cv2.FONT_HERSHEY_COMPLEX, 2, (255, 0, 255), 2)  # 입의 상태를 이미지에 텍스트로 표시

            if distMouthobject < 100 and ratio > 60:  # 거리가 100 미만이고, 입이 열려있으면,
                if isEatable:  # 오브젝트가 먹을 수 있는 경우,
                    currentobject = resetobject()  # 오브젝트를 리셋하고,
                    count += 1  # 점수를 1점 늘림
                else:  # 오브젝트가 먹을 수 없는 경우,
                    gameOver = True  # 게임 오버
        cv2.putText(img, str(count), (1100, 50), cv2.FONT_HERSHEY_COMPLEX, 2, (255, 0, 255), 5)  # 현재 점수를 이미지에 텍스트로 표시
    else:  # 게임 오버 상태라면,
        cv2.putText(img, "Game Over", (300, 400), cv2.FONT_HERSHEY_PLAIN, 7, (255, 0, 255), 10)  # "Game Over" 텍스트를 이미지에 표시

    cv2.imshow("Image", img)  # 이미지를 화면에 표시
    key = cv2.waitKey(1)  # 사용자의 키 입력을 기다림

    if key == ord('r'):  # 'r' 키를 누르면,
        resetobject()  # 오브젝트를 리셋하고,
        gameOver = False  # 게임 오버 상태를 해제하며,
        count = 0  # 점수를 0으로 리셋하고,
        currentobject = eatables[0]  # 첫 번째 먹을 수 있는 오브젝트를 현재 오브젝트로 설정
        pos = [300, 0]  # 오브젝트의 위치를 초기 위치로 설정

    if key == 27:  # ESC 키를 누르면,
        break  # 무한 루프를 빠져나옴

cap.release()  # 비디오 캡처 객체를 해제
cv2.destroyAllWindows()  # 모든 창을 닫음