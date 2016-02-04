<?php
	require_once("includes/initialise.php");

	//Script to help testing, such as populate database etc.
	$tableName = 'hazards';

	$query = "SELECT * FROM ?n";

	//test
	$data = $db -> query($query, $tableName);
	$row = $db -> fetch($data);
	var_dump($row);

?>