<?php
//echo var_dump($_COOKIE);
setcookie('userid');
setcookie('passwordJCI');
//unset($_COOKIE);
//echo var_dump($_COOKIE);
 $redirect = 'http://' . $_SERVER['HTTP_HOST'] . '/apache/index.html';
 header('Location: ' . filter_var($redirect, FILTER_SANITIZE_URL));

?>