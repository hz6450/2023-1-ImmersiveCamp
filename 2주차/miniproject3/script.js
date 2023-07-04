const API = window.SpeechRecognition || window.webkitSpeechRecognition;

if (API) {
  const recognition = new API();

  recognition.continuous = true;
  recognition.lang = 'ko-KR';

  const button = document.querySelector('.speech-recognition');
  const speechResult = document.querySelector('.result');
  const imageContainer = document.querySelector('.image-container');
  let remainingImages = [];
  let correctAnswer = '';

  const images = [
    { name: '다리미', file: '다리미.jfif' },
    { name: '골프', file: '골프.jfif' },
    { name: '오징어', file: '오징어.jfif' },
    { name: '감자', file: '감자.jfif' },
    { name: '고구마', file: '고구마.jfif' }
  ];

  // 랜덤 이미지 선택 함수
  function getRandomImages() {
    const randomImages = [];
    while (randomImages.length < 2) {
      const randomIndex = Math.floor(Math.random() * remainingImages.length);
      const randomImage = remainingImages[randomIndex];
      if (!randomImages.includes(randomImage)) {
        randomImages.push(randomImage);
      }
    }
    return randomImages;
  }

  // 이미지 표시 함수
  function displayImages() {
    const randomImages = getRandomImages();

    imageContainer.innerHTML = '';
    randomImages.forEach((image) => {
      const imgElement = document.createElement('img');
      imgElement.src = image.file;
      imgElement.alt = image.name;
      const imgContainer = document.createElement('div');
      imgContainer.classList.add('image-item');
      imgContainer.appendChild(imgElement);
      imageContainer.appendChild(imgContainer);
    });

    return randomImages;
  }

  button.addEventListener('click', () => {
    recognition.start();
    button.textContent = '인식 중...';
  });

  recognition.onresult = (event) => {
    const transcript = event.results[event.results.length - 1][0].transcript;
    speechResult.textContent = transcript;

    const randomImages = displayImages();
    let foundMatch = false;

    // 음성 인식 결과와 이미지 이름 일치 여부 확인
    for (let i = 0; i < randomImages.length; i++) {
      const image = randomImages[i];
      if (transcript.includes(image.name)) {
        foundMatch = true;
        if (image.name === correctAnswer) {
          const answerText = document.createElement('div');
          answerText.classList.add('answer-text');
          answerText.textContent = '오답';
          document.body.appendChild(answerText);
          setTimeout(() => {
            answerText.remove(); // 정답 텍스트 제거
          }, 2000);
        } else {
          const answerText = document.createElement('div');
          answerText.classList.add('answer-text');
          answerText.textContent = '정답';
          document.body.appendChild(answerText);
          setTimeout(() => {
            answerText.remove(); // 정답 텍스트 제거
          }, 2000);
        }
        break; // 정답을 찾았으므로 더 이상 비교하지 않음
      }
    }

    // 맞춘 이미지가 없으면 다음 단계로 진행하지 않음
    if (!foundMatch) {
      recognition.stop();
      button.textContent = 'Start Listening!';
    } else {
      nextEvent();
    }
  };

  // 다음 이벤트로 진행 함수
  function nextEvent() {
    recognition.stop();
    button.textContent = 'Start Listening!';
    const randomImages = displayImages();
    correctAnswer = randomImages[0].name;
  }

  // 페이지 로드 시 초기 이미지 설정
  remainingImages = [...images];
  displayImages();
}
