<?php
require_once __DIR__.'/vendor/autoload.php';
session_start();

$client = new Google_Client();
$client->setAuthConfigFile('calendar_secret_web.json');
$client->setRedirectUri('http://' . $_SERVER['HTTP_HOST'] . '/apache/secondaryauth.php');
$client->addScope(Google_Service_Calendar::CALENDAR);
$client->setAccessType('offline');

if(! isset($_GET['code'])) {
$auth_url = $client->createAuthUrl();
header('Location: ' . filter_var($auth_url,FILTER_SANITIZE_URL));

} else {
$client->authenticate($_GET['code']);
$_SESSION['access_token'] = $client->getAccessToken();
  $redirect_uri = 'http://' . $_SERVER['HTTP_HOST'] . '/apache/initialauth.php';
  header('Location: ' . filter_var($redirect_uri, FILTER_SANITIZE_URL));
}

?>

