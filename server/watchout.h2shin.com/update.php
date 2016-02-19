<?php
	require_once("includes/initialise.php");
	$data = array();

	$expireDelays = array("Road Works" => '14 days', "Pothole" => '28 days', "Road Closure" => '20 days', "Flooding" => '4 days', "Traffic Accident" => '3 days', "Broken Glass" => '5 days', "default" => '3 days');

	//Perhaps need to make this more defensive against wrong input.

	if (!($data = json_decode(file_get_contents("php://input"), true))) {
		exit("Provided string is not a correctly formatted JSON String");
	}


	$newHazards = $data["new"];
	$updateHazards = $data["update"];

	$updateQuery1 = "UPDATE `hazards` SET `expires`=?s,";
	$updateQuery2 = "WHERE id = ?s";
	$updateQuery = "";

	$insertQuery = "INSERT INTO `hazards` (`latitude`, `longitude`, `title`, `reported`, `expires`, `description`, `acks`, `diss`) VALUES (?s,?s,?s,?s,?s,?s,?s,?s)";

	$getExpiredQuery = "SELECT * FROM `hazards` WHERE expires < NOW()";
	$deleteExpiredQuiry = "DELETE FROM `hazards` WHERE expires < NOW()";



	//Increment ack or diss according to teh value of "response"
	if ($updateHazards) {
		foreach ($updateHazards as $row) {
			if ($row["response"] == "ack") {
				$updateHazard = $db -> getAll("SELECT * FROM `hazards` WHERE id = " . $row["id"]);
				$hazardName = $updateHazard[0]["title"];
				$ackDelay = $expireDelays["default"];

				if (array_key_exists($hazardName, $expireDelays)) {
					$ackDelay = $expireDelays[$hazardName];
				}

				$ackTime = new DateTime(); //For an acknowledgement add a default expire value set from time of receipt. Could be replaced with an addition of a differnet time for a different hazard type.
				$ackExpires = date_add($ackTime, date_interval_create_from_date_string($ackDelay));
				$expireString = date_format($ackExpires, 'Y-m-d H:i:s');
				$updateQuery = $updateQuery1." `acks`=`acks`+1 ".$updateQuery2;
				$db -> query($updateQuery, $expireString, $row["id"]);
			}else{
				$updateQuery = $updateQuery1." `diss`=`diss`+1 ".$updateQuery2;
				$db -> query($updateQuery, $row["expires"], $row["id"]);
			}
		}
	}

	//add all new hazards
	if ($newHazards) {
		foreach ($newHazards as $row) {
		$db -> query($insertQuery, $row["latitude"], $row["longitude"], $row["title"], $row["reported"], $row["expires"], $row["description"], $row["acks"], $row["diss"]);
		}
	}

	$expired = $db -> getAll($getExpiredQuery);
	$db -> query($deleteExpiredQuiry);

	$archive = "";
	//archive
	foreach ($expired as $row) {
		$archive .= $row["id"] . ", " . $row["latitude"] . ", " .  $row["longitude"] . ", " .  $row["title"] . ", " .  $row["reported"] . ", " .  $row["expires"] . ", " .  $row["description"] . ", " .  $row["acks"] . ", " .  $row["diss"] . ";\n";
	}

	file_put_contents(SITE_ROOT."/archive.txt", $archive, FILE_APPEND);
?>