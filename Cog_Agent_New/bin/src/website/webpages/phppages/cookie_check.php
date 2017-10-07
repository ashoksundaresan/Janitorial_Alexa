<?php 


function checkCookie() {
  if(isset($_COOKIE['userid']) && isset($_COOKIE['passwordJCI'])){
    if($_COOKIE['passwordJCI'] == 1) {
      return 1;
    } else {
      return 0;
    }
  } else {
    return 0;
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

function redirect() {
  if(checkCookie() == '1') {
    setSession($_COOKIE['userid']);
   $redirect = 'http://' . $_SERVER['HTTP_HOST'] . '/apache/account.php';
   header('Location: ' . filter_var($redirect, FILTER_SANITIZE_URL));
  } else{
    echo "no cookies";

   $redirect = 'http://' . $_SERVER['HTTP_HOST'] . '/apache/login.html';
   header('Location: ' . filter_var($redirect, FILTER_SANITIZE_URL));
  }
}
//echo var_dump($_COOKIE);
redirect();
?>