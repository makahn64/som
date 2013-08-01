package com.appdelegates.solgame;

/*
 * DATABASE FORMAT NOTES
 * 
 * 1. Two tables: score_table and event_table
 * 
 * 		
 */

public class DBSchema {
	
	public static final String SOL_DATABASE_FILENAME = "sol.sqlite";
	
	public static final String SQL_CREATE_SCORE_TABLE = "CREATE TABLE IF NOT EXISTS score_table (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"gamertag TEXT, email TEXT, game_time INTEGER, game_type TEXT, score INTEGER, event_id INTEGER, complex_id INTEGER )";
	
	public static final String SQL_CREATE_EVENT_TABLE = "CREATE TABLE IF NOT EXISTS event_table (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"event_name TEXT, event_start INTEGER, event_end INTEGER, event_note TEXT, show_on_leaderboard INTEGER )";
	
	public static final String SQL_CREATE_LOG_TABLE = "CREATE TABLE IF NOT EXISTS log_table (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"log_time INTEGER, log_event TEXT, log_complex_id INTEGER )";


	public static final String  SQL_COUNT_GAMES = "SELECT COUNT() FROM score_table";
		
	public static final String SCORE_TABLE_NAME = "score_table";
	
	public static final String EVENT_TABLE_NAME = "event_table";
	
	public static final String LOG_TABLE_NAME = "log_table";
	
	

}
