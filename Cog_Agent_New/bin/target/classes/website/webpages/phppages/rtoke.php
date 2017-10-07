<?php

function insertToken($email, $token) {
	$servername = "127.0.0.1";
	$username = "root";
	$password = "CognitiveAgentDB";
	$dbname = "Tyco_Agent_Schema";

	$conn = new mysqli($servername, $username, $password, $dbname);
	if ($conn->connect_error) {
		die("Connection failed: " . $conn->connect_error);
		header("localhost");
	} 

	$sql = "UPDATE Users SET refresh_token='$token' WHERE id='$email'";

	$conn->query($sql);
	$conn->close();

}
session_start();
if (isset($_SESSION['refresh_token']) && $_SESSION['refresh_token']) {
	insertToken($_SESSION['userid'],$_SESSION['refresh_token']);
	echo("java -jar /home/ubuntu/TycoAgent/MassEmail/initial-email.jar " . $_SESSION['userid']);
	exec("java -jar /home/ubuntu/TycoAgent/MassEmail/initial-email.jar " . $_SESSION['userid']);
	//echo var_dump($output);
	$redirect_uri = 'http://' . $_SERVER['HTTP_HOST'] . '/apache/congrats.html';
	header('Location: ' . filter_var($redirect_uri, FILTER_SANITIZE_URL));
} else {

	echo "failure on refresh_token";
	session_unset();
	$redirect_uri = 'http://' . $_SERVER['HTTP_HOST'] . '/apache/alert.html';
	header('Location: ' . filter_var($redirect_uri, FILTER_SANITIZE_URL));

}
?>