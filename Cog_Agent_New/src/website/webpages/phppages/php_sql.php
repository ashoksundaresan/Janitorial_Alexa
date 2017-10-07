<?php
session_start();
session_unset();

function insertsql($email,$fname,$lname,$timezone,$pw1,$pw2) {
	if($pw1 == $pw2) {

		$servername = "127.0.0.1";
		$username = "root";
		$password = "CognitiveAgentDB";
		$dbname = "Tyco_Agent_Schema";

		$conn = new mysqli($servername, $username, $password, $dbname);
		if ($conn->connect_error) {
			die("Connection failed: " . $conn->connect_error);
		} 


		$sql = "INSERT INTO Users (id, last, first, timezone)
		VALUES ('$email', '$lname', '$fname', '$timezone')";
		$conn->query($sql);
		$conn->close();

		exec("java -jar password-insert.jar $email $pw2");
	} else {
		$redirect = 'http://' . $_SERVER['HTTP_HOST'] . '/apache/signup.html';
		header('Location: ' . filter_var($redirect, FILTER_SANITIZE_URL));
	}
}


insertsql($_POST["user_email"],$_POST["user_firstname"],$_POST["user_lastname"],$_POST["user_timezone"],$_POST["user_password1"],$_POST["user_password2"]);
$_SESSION['userid'] = $_POST["user_email"];
setcookie("userid",$_POST["user_email"]);

$redirect = 'http://' . $_SERVER['HTTP_HOST'] . '/apache/initialauth.php';
header('Location: ' . filter_var($redirect, FILTER_SANITIZE_URL));

?>