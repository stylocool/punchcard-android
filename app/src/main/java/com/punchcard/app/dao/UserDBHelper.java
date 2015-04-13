package com.punchcard.app.dao;


public class UserDBHelper {
	
	public static final String TABLE = "user";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_REMEMBER_ME = "remember_me";

    // Database creation sql statement
	public static final String DATABASE_CREATE = "create table "
			+ TABLE + "(" 
			+ COLUMN_ID + " integer primary key, " 
			+ COLUMN_EMAIL + " text, "
            + COLUMN_PASSWORD + " text, "
            + COLUMN_REMEMBER_ME + " integer"
            + ");";

} 