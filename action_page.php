<?php
  require_once 'login.php';
  $conn = new mysqli($hn, $un, $pw, $db);
  $username = $POST[username];
  $password = $POST[password];
  if ($conn->connect_error) die($conn ->connect_error);


  if (isset($username) && isset($password) && !empty($username) && !empty($password)){
    $query = "SELECT * FROM userList where username = '$username' && password = '$password'";
    $result = $conn->query($query);
    if (!$result) die ($conn->error);

    }
  else{
    echo('Invalid login userinformation')
  }
