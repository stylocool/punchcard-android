package com.punchcard.app.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private static final String TAG = DBHelper.class.getName();

	private static final String DATABASE_NAME = "punchcard.db";
	private static final int DATABASE_VERSION = 1;

    private CompanyDataSource companyDS = null;
	private ProjectDataSource projectDS = null;
    private PunchcardDataSource punchcardDS = null;
    private UserDataSource userDS = null;
    private WorkerDataSource workerDS = null;
    private SQLiteDatabase database = null;
	
	//private static DBHelper instance = null;
	private Context context;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
		this.context = context;		
		database = this.getWritableDatabase();
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.i(TAG, "Creating company table");
        database.execSQL(CompanyDBHelper.DATABASE_CREATE);

        Log.i(TAG, "Creating project table");
        database.execSQL(ProjectDBHelper.DATABASE_CREATE);

        Log.i(TAG, "Creating punchcard table");
        database.execSQL(PunchcardDBHelper.DATABASE_CREATE);

        Log.i(TAG, "Creating user table");
        database.execSQL(UserDBHelper.DATABASE_CREATE);

        Log.i(TAG, "Creating worker table");
        database.execSQL(WorkerDBHelper.DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + CompanyDBHelper.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ProjectDBHelper.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PunchcardDBHelper.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + UserDBHelper.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + WorkerDBHelper.TABLE);
		onCreate(db);
	}

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public SQLiteDatabase getDatabase() {
        return database;
	}

    public synchronized CompanyDataSource getCompanyDS() {
        if (companyDS == null) {
            companyDS = new CompanyDataSource(this);
        }
        return companyDS;
    }

	public synchronized ProjectDataSource getProjectDS() {
		if (projectDS == null) {
            projectDS = new ProjectDataSource(this);
		}
		return projectDS;
	}

    public synchronized PunchcardDataSource getPunchcardDS() {
        if (punchcardDS == null) {
            punchcardDS = new PunchcardDataSource(this);
        }
        return punchcardDS;
    }

    public synchronized UserDataSource getUserDS() {
        if (userDS == null) {
            userDS = new UserDataSource(this);
        }
        return userDS;
    }

    public synchronized WorkerDataSource getWorkerDS() {
        if (workerDS == null) {
            workerDS = new WorkerDataSource(this);
        }
        return workerDS;
    }

} 