<?php
	class Database{
		protected static $connection; //the MySQLi connection, only need cone connection.

		function __construct(){
			$this -> connect();
		}

		function __destruct(){
			if (isset(self::$connection)) { //close connection when destructed at end of script
				mysqli_close(self::$connection);
			}
		}

		/**
		* Connect to the database
		*
		* @return bool false on failure / mysqli MySQLi object on success
		*/

		public function connect(){
			if (!isset(self::$connection)) {
				$config = parse_ini_file('config.ini');
				self::$connection = new mysqli('localhost', $config['username'], $config['password'], $config['dbname']);
			} //create new mysqli object if not already set

			//Connection was not successful
			if (self::$connection === false) {
				echo "connection to database failed!";
			}

			return self::$connection;
		}

		/**
		* Query the database
		*
		* @param $query the query string
		* @return The resut of the mysqli::query() function
		*/

		public function query($query){
			$connection = $this -> connect();
			//in case of dropped connection between constructor and here

			$result = $connection -> query($query);

			return $result;
		}

		/**
		*Fetch rows from the database
		*
		*@param $query The query string
		*@return bool false on failure / array database rows on success
		*/

		public function select($query){
			$rows = array(); // array
			$result = $this->query($query);
			if ($result === false) {
				return false;
			}
			while ($row = $result -> fetch_assoc()) {
				$rows[] = $row;
			}

			return $rows; //return rows of associative results
		}

		/**
		* return last reported error from database
		* @return string database error message
		*/

		public function error(){
			$connection = $this -> connect();
			return $connection -> error;
		}

		/**
		*Quote and escape value for use in a database query
		*
		*@param string $value The value to be quoted and escaped
		*@return string The quoted and escaped string
		*/

		public function quote($value){
			$connection = $this -> connnect();
			return "'" . $connection -> real_escape_string($value) . "'";
		}
	}
?>