package com.punchcard.app.dao;


public class ProjectDBHelper {
	
	public static final String TABLE = "project";
	public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PROJECT_ID = "project_id";
	public static final String COLUMN_COMPANY_ID = "company_id";
	public static final String COLUMN_LOCATION = "location";
	public static final String COLUMN_NAME = "name";

	// Database creation sql statement
	public static final String DATABASE_CREATE = "create table "
			+ TABLE + "(" 
			+ COLUMN_ID + " integer primary key, "
            + COLUMN_PROJECT_ID + " integer not null, "
			+ COLUMN_COMPANY_ID + " integer not null, "
			+ COLUMN_NAME + " text not null, "
            + COLUMN_LOCATION + " text"
            + ");";

} 