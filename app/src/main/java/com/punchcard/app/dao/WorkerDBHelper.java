package com.punchcard.app.dao;


public class WorkerDBHelper {
	
	public static final String TABLE = "worker";
	public static final String COLUMN_ID = "_id";
    public static final String COLUMN_WORKER_ID = "worker_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_WORK_PERMIT = "work_permit";

	// Database creation sql statement
	public static final String DATABASE_CREATE = "create table "
			+ TABLE + "(" 
			+ COLUMN_ID + " integer primary key, "
            + COLUMN_WORKER_ID + " integer, "
            + COLUMN_NAME + " text, "
            + COLUMN_WORK_PERMIT + " text"
            + ");";

} 