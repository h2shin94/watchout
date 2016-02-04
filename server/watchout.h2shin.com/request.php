<?php
	require_once("includes/initialise.php");

	$latitude = "";
	$longitude = "";
	$tableName = 'hazards';

	if (!empty($_POST["latitude"]) && !empty($_POST["longitude"])) {
		$latitude = $_POST["latitude"];
		$longitude = $_POST["longitude"];
	}else{
		//incorrect post request
		exit("Correct parameters not provided");
	}

	//Great circle distance formula: 3963.0 * arccos[sin(lat1) *  sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 – lon1)]
	//Below converts degrees to radians.

	$query = "SELECT * FROM ?n WHERE (3963.0 * ACOS(SIN(?s/57.2958) * SIN(latitude/57.2958) + COS(?s/57.2958) * COS(latitude/57.2958) * COS(longitude/57.2958 - ?s/57.2958))) <= 10";

	$data = $db -> getAll($query, $tableName, $latitude, $latitude, $longitude);
	//Above gets all data as associative array already.

	$encoded = array('hazards' => $data);

	print json_encode($encoded);
?>