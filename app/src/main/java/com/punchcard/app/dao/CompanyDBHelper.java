package com.punchcard.app.dao;


public class CompanyDBHelper {
	
	public static final String TABLE = "company";
	public static final String COLUMN_ID = "_id";
    public static final String COLUMN_COMPANY_ID = "company_id";
    public static final String COLUMN_NAME = "name";

	// Database creation sql statement
	public static final String DATABASE_CREATE = "create table "
			+ TABLE + "(" 
			+ COLUMN_ID + " integer primary key, "
            + COLUMN_COMPANY_ID + " integer, "
            + COLUMN_NAME + " text"
            + ");";

} 