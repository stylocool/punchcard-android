package com.punchcard.app.dao;


public class PunchcardDBHelper {
	
	public static final String TABLE = "punchcard";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PROJECT_ID = "project_id";
    public static final String COLUMN_WORKER_ID = "worker_id";
	public static final String COLUMN_CHECKIN = "checkin";
    public static final String COLUMN_CHECKIN_LOCATION = "checkin_location";
    public static final String COLUMN_CHECKOUT = "checkout";
    public static final String COLUMN_CHECKOUT_LOCATION = "checkout_location";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_SYNC_DATE = "sync_date";

	// Database creation sql statement
	public static final String DATABASE_CREATE = "create table "
			+ TABLE + "(" 
			+ COLUMN_ID + " integer primary key, " 
			+ COLUMN_PROJECT_ID + " integer not null, "
            + COLUMN_WORKER_ID + " integer not null, "
            + COLUMN_CHECKIN_LOCATION + " text, "
            + COLUMN_CHECKIN + " integer, "
            + COLUMN_CHECKOUT_LOCATION + " text, "
            + COLUMN_CHECKOUT + " integer, "
            + COLUMN_STATUS + " text, "
            + COLUMN_SYNC_DATE + " integer"
            + ");";

} 