<?php

require_once __DIR__.'/vendor/autoload.php';

session_start();

$client = new Google_Client();
$client->setAuthConfig('calendar_secret_web.json');
$client->addScope(Google_Service_Calendar::CALENDAR);
$client->setAccessType('offline');

if (isset($_SESSION['access_token']) && $_SESSION['access_token']) {
  $client->setAccessToken($_SESSION['access_token']);
       if ($client->getAccessToken()) {
           $token = $client->getAccessToken();
           $refreshToken = $token["refresh_token"];
	
	$_SESSION['refresh_token'] = $refreshToken;
	//echo "refresh: $refreshToken";
	//var_dump($token);
 $redirect = 'http://' . $_SERVER['HTTP_HOST'] . '/apache/rtoke.php';
header('Location: ' . filter_var($redirect, FILTER_SANITIZE_URL));
        } else {
		echo "bad";
	}





} else {
  $redirect_uri = 'http://' . $_SERVER['HTTP_HOST'] . '/apache/secondaryauth.php';
  header('Location: ' . filter_var($redirect_uri, FILTER_SANITIZE_URL));
}


?>