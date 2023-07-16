let imgSrc = null;
const firebaseConfig = {
	apiKey: "AIzaSyB-jNrM59JtrKt4ZPg4D33h_DJmWXCQwjY",
	authDomain: "cvzone-b9222.firebaseapp.com",
	projectId: "cvzone-b9222",
	storageBucket: "cvzone-b9222.appspot.com",
	messagingSenderId: "296952603101",
	appId: "1:296952603101:web:1dd08fcb67f3c460051634"
  };

// Firebase 초기화
firebase.initializeApp(firebaseConfig);
const firestore = firebase.firestore();

const $ = _ => document.querySelector(_);

const $c = _ => document.createElement(_);

const open = e => {
  if (e.target.files.length) {
    const url = window.URL || window.webkitURL;
    imgSrc = url.createObjectURL(e.target.files[0]);
    draw();
  }
};

const draw = img => {
  $('#preview').style.backgroundImage = `url(${imgSrc})`;
  $('#preview').style.backgroundRepeat = 'no-repeat';
  $('#preview').style.backgroundSize = 'contain';
  OCR();
};



const saveToFirestore = () => {
	const nameInput = document.getElementById('name');
	const name = nameInput.value.trim();
  
	if (name === '') {
	  alert('Please enter a name.');
	  return;
	}
  
	const text = getOCRText(); // OCR 결과 텍스트 가져오는 함수 호출
  
	if (text.trim() === '') {
	  alert('No OCR results to save.');
	  return;
	}
  
	const sentences = text.split('\n'); // 문장 분할
  
	const phoneNumberRegex = /\d{3}-\d{4}-\d{4}/g; // 전화번호 형식 정규식 패턴
	const emailRegex = /\S+@\S+\.\S+/g; // 이메일 형식 정규식 패턴
  
	const phoneNumbers = [];
	const emails = [];
  
	sentences.forEach((sentence) => {
	  const phoneNumberMatches = sentence.match(phoneNumberRegex); // 문장에서 전화번호 추출
	  const emailMatches = sentence.match(emailRegex); // 문장에서 이메일 추출
  
	  if (phoneNumberMatches) {
		phoneNumbers.push(phoneNumberMatches[0]); // 추출된 전화번호를 배열에 추가
	  }
  
	  if (emailMatches) {
		emails.push(emailMatches[0]); // 추출된 이메일을 배열에 추가
	  }
	});
  
	if (phoneNumbers.length > 0 || emails.length > 0) {
	  const data = {};
  
	  if (phoneNumbers.length > 0) {
		data.phoneNumbers = phoneNumbers;
	  }
  
	  if (emails.length > 0) {
		data.emails = emails;
	  }
  
	  data.name = name; // 이름 추가
  
	  firestore
		.collection('ocr-results')
		.add(data)
		.then(() => {
		  console.log('OCR results saved to Firestore');
		  alert('OCR results saved successfully!');
		})
		.catch((error) => {
		  console.error('Error saving OCR results to Firestore:', error);
		  alert('Failed to save OCR results.');
		});
	} else {
	  console.log('No OCR results to save');
	  alert('No OCR results to save.');
	}
  };
  
  
  const newPageBtn = document.getElementById('newPageBtn');
newPageBtn.addEventListener('click', () => {
  window.location.href = 'newpage.html'; // 새 페이지의 URL을 설정합니다.
});



  
const getOCRText = () => {
	return $('#recognizedText').innerText.trim();
  };
  

const OCR = () => {
  const progress = $c('progress');
  progress.value = 0;
  progress.style.display = 'block';
  progress.style.margin = '25% auto';
  $('#recognizedText').innerHTML = '';
  $('#recognizedText').appendChild(progress);

  Tesseract.recognize(imgSrc, $('#lang').value, {
    logger: m => {
      progress.value = m.progress;
    },
  })
    .then(({ data: { text } }) => {
      $('#recognizedText').style.padding = '1em';
      $('#recognizedText').innerText = text;
    })
    .catch(e => {
      $('#recognizedText').innerText = e;
    });
};

$('#import').addEventListener('change', open);
$('#lang').addEventListener('change', _ => {
  if (imgSrc) OCR();
});

window.addEventListener('DOMContentLoaded', () => {
  const parsedUrl = new URL(window.location);
  const title = parsedUrl.searchParams.get('title'),
    text = parsedUrl.searchParams.get('text'),
    url = parsedUrl.searchParams.get('url');

  if (title) alert('Title shared: ' + title);
  if (text) alert('Text shared: ' + text);
  if (url) alert('URL shared: ' + url);
});

if ('serviceWorker' in navigator) {
  navigator.serviceWorker
    .register('/ocr/sw.js', { scope: './' })
    .then(response => response)
    .catch(reason => reason);
}

let deferredPrompt;
const addBtn = document.createElement('button');

window.addEventListener('beforeinstallprompt', e => {
  e.preventDefault();
  deferredPrompt = e;
  addBtn.style.display = 'block';
  addBtn.addEventListener('click', e => {
    addBtn.style.display = 'none';
    deferredPrompt.prompt();
    deferredPrompt.userChoice.then(choiceResult => {
      deferredPrompt = null;
    });
  });
});

// 버튼 클릭 이벤트 핸들러
$('#saveBtn').addEventListener('click', () => {
	const text = $('#recognizedText').innerText;
	if (text) {
	  saveToFirestore(text);
	}
  });