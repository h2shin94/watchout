<?php
	defined('SITE_ROOT') ? null : define("SITE_ROOT", "/home2/hyunho/public_html/watchout");
	defined("LIB_PATH") ? null : define("LIB_PATH", SITE_ROOT."/includes");
	require_once(LIB_PATH."/safemysql/safemysql.class.php");
	require_once(LIB_PATH."/functions.php");
	$config = parse_ini_file('config.ini'); //get database information from ini file.
	$db = new safemysql(array("host" => 'localhost', "user" => $config['username'], "pass" => $config['password'], "db" => $config['dbname']));
	//create instance of db for the script, initialised with database info.
	//Use mysql for safe and easy to use prepared statements for protection against attacks on database.
?>