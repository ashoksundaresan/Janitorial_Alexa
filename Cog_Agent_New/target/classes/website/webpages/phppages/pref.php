<?php
  session_start();
?>
<!DOCTYPE html>

<html >

<head>

  <meta charset="UTF-8">

  <title>Set Preferences</title>

  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/normalize/5.0.0/normalize.min.css">

  

      <link rel="stylesheet" href="csss/style.css">

</head>

<body>

    <head>


        <meta charset="utf-8">

        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <title>Preferences</title>

        <link rel="stylesheet" href="css/normalize.css">

        <link href='https://fonts.googleapis.com/css?family=Nunito:400,300' rel='stylesheet' type='text/css'>

        <link rel="stylesheet" href="css/main.css">

    </head>

    <body>


      <form id="myForm" action="php_java.php" method="post">
	
      

        <h1>Preferences</h1>

        

        <fieldset>

          <legend><span class="number">1</span>Preferred Meeting Times</legend>

          <label for="startwork">Acceptable time to begin scheduling meetings:</label>

          <select id="startwork" name="startwork">
                <option value="06:00:00">6AM</option>
                <option value="06:30:00">6:30AM</option>
                <option value="07:00:00">7AM</option>
                <option value="07:30:00">7:30AM</option>
                <option value="08:00:00">8AM</option>
                <option value="08:30:00">8:30AM</option>
                <option value="09:00:00">9AM</option>
                <option value="09:30:00">9:30AM</option>
                <option value="10:00:00">10AM</option>
                <option value="10:30:00">10:30AM</option>
                <option value="11:00:00">11AM</option>
                <option value="11:30:00">11:30AM</option>
                <option value="12:00:00">12PM</option>
                <option value=<?php echo $_SESSION["actual_start"]; ?> selected="selected"><?php echo $_SESSION["startwork"]; ?></option>
          </select>

          <label for="endwork">Preferred time to end scheduling meetings:</label>

          <select id="endwork" name="endwork">
                <option value="14:00:00">2PM</option>
                <option value="14:30:00">2:30PM</option>
                <option value="15:00:00">3PM</option>
                <option value="15:30:00">3:30PM</option>
                <option value="16:00:00">4PM</option>
                <option value="16:30:00">4:30PM</option>
                <option value="17:00:00">5PM</option>
                <option value="17:30:00">5:30PM</option>
                <option value="18:00:00">6PM</option>
                <option value="18:30:00">6:30PM</option>
                <option value="19:00:00">7PM</option>
                <option value="19:30:00">7:30PM</option>
                <option value="20:00:00">8PM</option>
                <option value="20:30:00">8:30PM</option>
                <option value="21:00:00">9PM</option>
                <option value=<?php echo $_SESSION["actual_end"]; ?> selected="selected"><?php echo $_SESSION["endwork"]; ?></option>
          </select>

          <label for="default_duration">Default meeting length:</label>

          <select id="default_duration" name="default_duration">
                <option value=15>15 minutes</option>
                <option value=30>30 minutes</option>
                <option value=45>45 minutes</option>
                <option value=60>1 hour</option>
                <option value=<?php echo $_SESSION["default_duration"];?> selected="selected"><?php if($_SESSION["default_duration"] == 60) { echo "1 hour";} else {echo $_SESSION["default_duration"] . " minutes";}?>
          </select>
                

          <legend><span class="number">2</span>Personal Info</legend>

          <label for="busycutoff">A workday is too busy for meetings when I have ...</label>

          <select id="busycutoff" name="busycutoff">
                <option value=0 >I'm never too busy</option>
                <option value=1 >Less than 1 free hour</option>
                <option value=2 >Less than 2 free hours</option>
                <option value=3 >Less than 3 free hours</option>
                <option value=4 >Less than 4 free hours</option>
                <option value=5 >Less than 5 free hours</option>
                <option value=6 >Less than 6 free hours</option>
                <option value=7 >Less than 7 free hours</option>
                <option value=<?php echo $_SESSION["actual_busy"]; ?> selected="selected"><?php if($_SESSION["actual_busy"] == 0) {echo "I'm never too busy";} else if ($_SESSION["actual_busy"] == 1) {echo "Less than 1 free hour";}else {echo "Less than " . $_SESSION["actual_busy"] . " free hours";}?></option>
          </select>

          <label for="amazon_email">(Optional) Enable Alexa Integration</label>

          <input type="text" id="amazon_email" name="amazon_email" placeholder=<?php if($_SESSION["actual_amazon"] == "denied") {echo "Enter your Amazon Account email here";}else {echo $_SESSION["actual_amazon"];}?>>

        </fieldset>

        <button type="submit">Set Preferences</button>

        <button id="end" type="submit" formaction="index.html">Cancel</button>

      </form>
	
      

    </body>
</html>