<?php
	require_once("includes/initialise.php");
	$data = array();

	$expireDelays = array("Road Works" => '14 days', "Pothole" => '28 days', "Road Closure" => '20 days', "Flooding" => '4 days', "Traffic Accident" => '3 days', "Broken Glass" => '5 days', "default" => '3 days');

	//Perhaps need to make this more defensive against wrong input.

	//Decode json input and create an associative array into data
	if (!($data = json_decode(file_get_contents("php://input"), true))) {
		exit("Provided string is not a correctly formatted JSON String");
	}

	//Extract new and update hazards
	$newHazards = $data["new"];
	$updateHazards = $data["update"];

	//Template for update query
	$updateQuery1 = "UPDATE `hazards` SET `expires`=?s,";
	$updateQuery2 = "WHERE id = ?s";
	$updateQuery = "";

	//Template for insert query
	$insertQuery = "INSERT INTO `hazards` (`latitude`, `longitude`, `title`, `reported`, `expires`, `description`, `acks`, `diss`) VALUES (?s,?s,?s,?s,?s,?s,?s,?s)";

	//Queries to return and delete expired queries
	$getExpiredQuery = "SELECT * FROM `hazards` WHERE expires < NOW()";
	$deleteExpiredQuiry = "DELETE FROM `hazards` WHERE expires < NOW()";



	//Increment ack or diss according to teh value of "response" and for acknowledgements
	//set expiry to be the specified time from current date.
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
			}else{ //stick to original expiry for dismissal
				$updateHazard = $db -> getAll("SELECT * FROM `hazards` WHERE id = " . $row["id"]);
				$originalExpire = $updateHazard[0]["expires"];
				$updateQuery = $updateQuery1." `diss`=`diss`+1 ".$updateQuery2;
				$db -> query($updateQuery, $originalExpire, $row["id"]);
			}
		}
	}

	//add all new hazards, setting expiry with same concept as update.
	if ($newHazards) {
		foreach ($newHazards as $row) {
			$hazardName = $row["title"];
			$ackDelay = $expireDelays["default"];

			if (array_key_exists($hazardName, $expireDelays)) {
				$ackDelay = $expireDelays[$hazardName];
			}
			$ackTime = new DateTime(); //For an acknowledgement add a default expire value set from time of receipt. Could be replaced with an addition of a differnet time for a different hazard type.
			$ackExpires = date_add($ackTime, date_interval_create_from_date_string($ackDelay));
			$expireString = date_format($ackExpires, 'Y-m-d H:i:s');


			$db -> query($insertQuery, $row["latitude"], $row["longitude"], $row["title"], $row["reported"], $expireString, $row["description"], $row["acks"], $row["diss"]);
		}
	}

	//Return all expired hazards and delete
	$expired = $db -> getAll($getExpiredQuery);
	$db -> query($deleteExpiredQuiry);

	//Archive as CSV into a file called archive.txt
	$archive = "";
	//archive
	foreach ($expired as $row) {
		$archive .= $row["id"] . ", " . $row["latitude"] . ", " .  $row["longitude"] . ", " .  $row["title"] . ", " .  $row["reported"] . ", " .  $row["expires"] . ", " .  $row["description"] . ", " .  $row["acks"] . ", " .  $row["diss"] . ";\n";
	}

	file_put_contents(SITE_ROOT."/archive.txt", $archive, FILE_APPEND);
?>