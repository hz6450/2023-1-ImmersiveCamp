<?php
$hostname = 'localhost';  // 호스트명 (일반적으로 'localhost' 또는 '127.0.0.1')
$username = 'root';       // MySQL 사용자 이름
$password = '';           // MySQL 비밀번호
$database = 'healthdate'; // 데이터베이스 이름

// MySQL 데이터베이스 연결
$connection = mysqli_connect($hostname, $username, $password, $database);

// 연결 오류 확인
if (mysqli_connect_errno()) {
    die('MySQL 연결 오류: ' . mysqli_connect_error());
}

// 오늘 날짜 정보 가져오기
$year = date('Y');
$month = date('n');

// 월의 첫 번째 날의 요일 가져오기
$firstDayOfMonth = mktime(0, 0, 0, $month, 1, $year);
$firstDayOfWeek = date('w', $firstDayOfMonth);

// 월의 마지막 날짜 가져오기
$lastDayOfMonth = mktime(0, 0, 0, $month + 1, 0, $year);
$lastDay = date('j', $lastDayOfMonth);

// 클릭된 날짜의 exercise_name 값 가져오기
$selectedDate = '';
if (isset($_GET['date'])) {
    $selectedDate = $_GET['date'];
}

// 캘린더 출력
echo "<h2>{$year}년 {$month}월</h2>";
echo "<table class='calendar'>";
echo "<tr><th>일</th><th>월</th><th>화</th><th>수</th><th>목</th><th>금</th><th>토</th></tr>";

// 첫 번째 날짜 이전의 빈 칸 출력
echo "<tr>";
for ($i = 0; $i < $firstDayOfWeek; $i++) {
    echo "<td></td>";
}

// 날짜 출력
$dayCount = 1;
for ($i = $firstDayOfWeek; $i < 7; $i++) {
    $formattedDate = sprintf('%04d-%02d-%02d', $year, $month, $dayCount);
    $highlight = '';
    $exerciseNames = array();

    // exercise_records 테이블에서 해당 날짜의 레코드 가져오기
    $query = "SELECT exercise_name FROM exercise_records WHERE DATE(exercise_date) = '$formattedDate'";
    $result = mysqli_query($connection, $query);

    if ($result && mysqli_num_rows($result) > 0) {
        while ($row = mysqli_fetch_assoc($result)) {
            $exerciseNames[] = $row['exercise_name'];
        }
        $highlight = 'highlight'; // CSS 클래스 이름
    }

    // 클릭 가능한 링크로 날짜 출력
    echo "<td class='calendar-day {$highlight}'>";
    echo "<a href='?date={$formattedDate}'>$dayCount</a>";
    foreach ($exerciseNames as $exerciseName) {
        echo "<br>{$exerciseName}";
    }
    echo "</td>";
    $dayCount++;
}

echo "</tr>";

// 남은 날짜 출력
while ($dayCount <= $lastDay) {
    echo "<tr>";
    for ($i = 0; $i < 7; $i++) {
        if ($dayCount > $lastDay) {
            echo "<td></td>";
        } else {
            $formattedDate = sprintf('%04d-%02d-%02d', $year, $month, $dayCount);
            $highlight = '';

            // exercise_records 테이블에서 해당 날짜의 레코드가 있는지 확인
            $query = "SELECT COUNT(*) as count FROM exercise_records WHERE DATE(exercise_date) = '$formattedDate'";
            $result = mysqli_query($connection, $query);
            $row = mysqli_fetch_assoc($result);
            $recordCount = $row['count'];

            if ($recordCount > 0) {
                $highlight = 'highlight'; // CSS 클래스 이름
            }

            // 클릭 가능한 링크로 날짜 출력
            echo "<td class='calendar-day {$highlight}'>";
            echo "<a href='?date={$formattedDate}'>$dayCount</a>";
            echo "</td>";
            $dayCount++;
        }
    }
    echo "</tr>";
}

echo "</table>";

// 선택된 날짜의 exercise_name 값 가져오기
if (!empty($selectedDate)) {
    $query = "SELECT exercise_name FROM exercise_records WHERE DATE(exercise_date) = '$selectedDate'";
    $result = mysqli_query($connection, $query);

    if ($result && mysqli_num_rows($result) > 0) {
        echo "<h3>{$selectedDate}의 운동:</h3>";
        echo "<ul>";
        while ($row = mysqli_fetch_assoc($result)) {
            $exerciseName = $row['exercise_name'];
            echo "<li>{$exerciseName}</li>";
        }
        echo "</ul>";
    } else {
        echo "<h3>{$selectedDate}에 운동 기록이 없습니다.</h3>";
    }
}

// MySQL 연결 닫기
mysqli_close($connection);
?>

<style>
.calendar {
    width: 100%;
    border-collapse: collapse;
}

.calendar th,
.calendar td {
    border: 1px solid #ccc;
    padding: 10px;
}

.calendar-day {
    text-align: center;
}

.highlight {
    background-color: yellow;
}
</style>
