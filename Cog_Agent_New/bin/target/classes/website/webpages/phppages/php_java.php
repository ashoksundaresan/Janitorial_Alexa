<?php
session_start();
function insertPref($email) {

	$prefs = [
	"startwork" => $_POST["startwork"],
	"endwork" => $_POST["endwork"],
	"default_duration" => $_POST["default_duration"],
	"busycutoff" => $_POST["busycutoff"],
	"amazon_email" => $_POST["amazon_email"],
	];


	foreach($prefs as $pref => $value) {
		if(!is_null($value)) {
			$str = $pref . ' ' . $value . ' ' . $email;
			exec("java -jar php_java_sql.jar $str");

		}
	}
}

insertPref($_COOKIE['userid']);
session_unset();

$redirect = 'http://' . $_SERVER['HTTP_HOST'] . '/apache/index.html';
header('Location: ' . filter_var($redirect, FILTER_SANITIZE_URL));
?>