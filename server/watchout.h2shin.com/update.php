<?php
	require_once("includes/initialise.php");
	$data = array();

	//Perhaps need to make this more defensive against wrong input.

	if (!($data = json_decode(file_get_contents("php://input"), true))) {
		exit("Provided string is not a correctly formatted JSON String");
	}

	var_dump($data);

	$newHazards = $data["new"];
	$updateHazards = $data["update"];

	$updateQuery1 = "UPDATE `hazards` SET `expires`=?s,";
	$updateQuery2 = "WHERE id = ?s";
	$updateQuery = "";

	$insertQuery = "INSERT INTO `hazards` (`latitude`, `longitude`, `title`, `reported`, `expires`, `description`, `acks`, `diss`) VALUES (?s,?s,?s,?s,?s,?s,?s,?s)";

	$getExpiredQuery = "SELECT * FROM `hazards` WHERE expires < NOW()";
	$deleteExpiredQuiry = "DELETE FROM `hazards` WHERE expires < NOW()";


	if ($updateHazards) {
		foreach ($updateHazards as $row) {
		if ($row["response"] == "ack") {
			$updateQuery = $updateQuery1." `acks`=`acks`+1 ".$updateQuery2;
			$db -> query($updateQuery, $row["expires"], $row["id"]);
		}else{
			$updateQuery = $updateQuery1." `diss`=`diss`+1 ".$updateQuery2;
			$db -> query($updateQuery, $row["expires"], $row["id"]);
		}
	}
	}

	if ($newHazards) {
		foreach ($newHazards as $row) {
		$db -> query($insertQuery, $row["latitude"], $row["longitude"], $row["title"], $row["reported"], $row["expires"], $row["description"], $row["acks"], $row["diss"]);
		}
	}

	$expired = $db -> getAll($getExpiredQuery);
	$db -> query($deleteExpiredQuiry);

	$archive = "";

	foreach ($expired as $row) {
		$archive .= $row["id"] . ", " . $row["latitude"] . ", " .  $row["longitude"] . ", " .  $row["title"] . ", " .  $row["reported"] . ", " .  $row["expires"] . ", " .  $row["description"] . ", " .  $row["acks"] . ", " .  $row["diss"] . ";\n";
	}

	file_put_contents(SITE_ROOT."/archive.txt", $archive, FILE_APPEND);
?>