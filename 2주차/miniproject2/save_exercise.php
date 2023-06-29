<?php
// MySQL 연결 설정
$hostname = 'localhost';  // 호스트명 (일반적으로 'localhost' 또는 '127.0.0.1')
$username = 'root';       // MySQL 사용자 이름
$password = '';           // MySQL 비밀번호
$database = 'healthdate';     // 데이터베이스 이름

// MySQL 연결 생성
$conn = mysqli_connect($hostname, $username, $password, $database);
if($conn->connect_error){
    die("데이터베이스 연결 실패: " . $conn->connect_error);
}

$exerciseDate = $_POST['exercise_date'];
$exerciseName = $_POST['exercise_name'];
$sql = "INSERT INTO exercise_records (exercise_date, exercise_name) VALUES ('$exerciseDate', '$exerciseName')";
if($conn->query($sql) === TRUE) {
    echo "운동 기록이 성공적으로 저장되었습니다";
    sleep(2);   // 2초 대기

    // exercise_input.html 페이지로 리다이렉션
    header("Location: output_exercise.php");
    exit();
} else {
    echo "운동 기록 저장 실패: ". $conn->error;
}

$conn->close();
?>
