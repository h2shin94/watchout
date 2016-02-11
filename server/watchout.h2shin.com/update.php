<?php
	require_once("includes/initialise.php");
	$data = array();

	//Perhaps need to make this more defensive against wrong input.

	if (!($data = json_decode(file_get_contents("php://input"), true))) {
		exit("Provided string is not a correctly formatted JSON String");
	}

	print_r($data);

	$newHazards = $data["new"];
	$updateHazards = $data["update"];

	$updateQuery = "UPDATE `hazards` SET `expires`=?s ,`acks`=?s ,`diss`=?s WHERE id = ?s";

	$insertQuery = "INSERT INTO `hazards` (`latitude`, `longitude`, `title`, `reported`, `expires`, `description`, `acks`, `diss`) VALUES (?s,?s,?s,?s,?s,?s,?s,?s)";

	$getExpiredQuery = "SELECT * FROM `hazards` WHERE expires < NOW()";
	$deleteExpiredQuiry = "DELETE FROM `hazards` WHERE expires < NOW()";


	foreach ($updateHazards as $row) {
		$db -> query($updateQuery, $row["expires"], $row["acks"], $row["diss"], $row["id"]);
	}

	foreach ($newHazards as $row) {
		$db -> query($insertQuery, $row["latitude"], $row["longitude"], $row["title"], $row["reported"], $row["expires"], $row["description"], $row["acks"], $row["diss"]);
	}

	$expired = $db -> getAll($getExpiredQuery);
	$db -> query($deleteExpiredQuiry);

	$archive = "";

	foreach ($expired as $row) {
		$archive .= $row["id"] . ", " . $row["latitude"] . ", " .  $row["longitude"] . ", " .  $row["title"] . ", " .  $row["reported"] . ", " .  $row["expires"] . ", " .  $row["description"] . ", " .  $row["acks"] . ", " .  $row["diss"] . ";\n";
	}

	file_put_contents(SITE_ROOT."/archive.txt", $archive, FILE_APPEND);
?>