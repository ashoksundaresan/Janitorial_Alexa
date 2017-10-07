<?php

function verifyLogin($email,$password) {

	
		exec("java -jar login-check.jar $email $password",$output);
	//echo var_dump($output);
		if($output[0] == '1') {
			
			setSession($email);
			setcookie("userid",$email);
			setcookie("passwordJCI",1);
			$redirect = 'http://' . $_SERVER['HTTP_HOST'] . '/apache/account.php';
			header('Location: ' . filter_var($redirect, FILTER_SANITIZE_URL));
		} else {
			$redirect = 'http://' . $_SERVER['HTTP_HOST'] . '/apache/login.html';
			header('Location: ' . filter_var($redirect, FILTER_SANITIZE_URL));
		}
	

}

function setSession($email) {
	session_start();
	$servername = "127.0.0.1";
	$username = "root";
	$password = "CognitiveAgentDB";
	$dbname = "Tyco_Agent_Schema";

	$conn = new mysqli($servername, $username, $password, $dbname);
	if ($conn->connect_error) {
		die("Connection failed: " . $conn->connect_error);
	} 

	$sql = "Select * from Users where id = '" . $email . "'";
	$result = $conn->query($sql);
	if($result->num_rows >0) {
		$row = $result->fetch_assoc();
		exec("java -jar simple-time.jar " . $row["startwork"],$start);
		exec("java -jar simple-time.jar " . $row["endwork"],$end);
		exec("java -jar simple-zone.jar " . $row["timezone"],$zone);

		$_SESSION["email"] = $email;
		$_SESSION["f_name"] = $row["first"];
		$_SESSION["l_name"] = $row["last"];
		$_SESSION["timezone"] = $zone[0];
		$_SESSION["startwork"] = $start[0];
		$_SESSION["endwork"] = $end[0];
		$_SESSION["busyamt"] = $row["busycutoff"] . " hours";
		$_SESSION["actual_amazon"] = $row["amazon_email"];
		$_SESSION["actual_start"] = $row["startwork"];
		$_SESSION["actual_end"] = $row["endwork"];
		$_SESSION["actual_busy"] = $row["busycutoff"];
		if($row["busycutoff"] == 0) {
			$_SESSION["busyamt"] = "none";
		}
		if($row["amazon_email"] == "denied") {
			$_SESSION["amazon"] = "Not registered";
		} else {
			$_SESSION["amazon"] = "Yes, with Amazon account " . $row["amazon_email"];
		}
		$_SESSION["default_duration"] = $row["default_duration"];


	}
	// echo var_dump($_SESSION);
	$conn->close();
}



verifyLogin($_POST["user_email"],$_POST["user_password"]);

?>