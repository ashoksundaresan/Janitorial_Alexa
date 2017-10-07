<?php
session_start();
?>


<html>

<head>

  <meta charset="UTF-8">

  <title>Account</title>

  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/normalize/5.0.0/normalize.min.css">

  

  <link rel="stylesheet" href="csss/style.css">

</head>

<body>

  <head>


    <meta charset="utf-8">

    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Account Info</title>

    <link rel="stylesheet" href="css/normalize.css">

    <link href='https://fonts.googleapis.com/css?family=Nunito:400,300' rel='stylesheet' type='text/css'>

    <link rel="stylesheet" href="css/main.css">

  </head>

  <body>


    <form action="pref.php">



      <h1>Account Info</h1>
      <button id="logout" type="submit" formaction="logout.php">Log Out</button>



      <fieldset>
        <legend><span class="number">1</span>General Info</legend>

        <h3>Email: <?php echo $_SESSION["email"]; ?></h3>

        <h5>Name: <?php echo $_SESSION["f_name"] . ' ' . $_SESSION["l_name"]; ?></h5>

        <br>
        <legend><span class="number">2</span>Meeting Setup Information</legend>

        <h5>Workday: <?php echo $_SESSION["startwork"] . " to " . $_SESSION["endwork"]; ?></h5>

        <h5>Default meeting length is <?php echo $_SESSION["default_duration"]; ?> minutes</h5>



        <br>
        <legend><span class="number">3</span>Personal Info</legend>

        <h5>Minimum free hours required to schedule a meeting: <?php echo $_SESSION["busyamt"]?></h5>

        
        <h5>Alexa integration: <?php echo $_SESSION["amazon"]; ?></h5>

      </fieldset>

      <button type="submit">Edit Preferences</button>

      <button id="end" type="submit" formaction="index.html">Go to the Home Page</button>

    </form>



  </body>
  </html>
