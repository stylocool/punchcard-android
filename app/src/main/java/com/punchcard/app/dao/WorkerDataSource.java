package com.punchcard.app.dao;

import android.content.ContentValues;
import android.database.Cursor;
import com.punchcard.app.model.Worker;

import java.util.ArrayList;
import java.util.List;

public class WorkerDataSource {
	//private static final String TAG = AccountDataSource.class.getName();

	// Database fields
	private DBHelper dbHelper;
	private String[] ALL_COLUMNS = {
				WorkerDBHelper.COLUMN_ID,
                WorkerDBHelper.COLUMN_WORKER_ID,
                WorkerDBHelper.COLUMN_NAME,
                WorkerDBHelper.COLUMN_WORK_PERMIT
            };

	public WorkerDataSource(DBHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public Worker add(Worker worker) {
        long insertId = dbHelper.getDatabase().insert(WorkerDBHelper.TABLE, null, getContentValues(worker));
        Cursor cursor = dbHelper.getDatabase().query(WorkerDBHelper.TABLE, ALL_COLUMNS, WorkerDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(insertId) }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            worker = cursorTo(cursor);
        }
        cursor.close();
    
		return worker;
	}

	public Worker get(Long id) {
		Worker worker = null;
		Cursor cursor = dbHelper.getDatabase().query(WorkerDBHelper.TABLE, ALL_COLUMNS, WorkerDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(id) }, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
            worker = cursorTo(cursor);
		}
		cursor.close();
		return worker;
	}

    public Worker getByWorkerId(Long workerId) {
        Worker worker = null;
        Cursor cursor = dbHelper.getDatabase().query(WorkerDBHelper.TABLE, ALL_COLUMNS, WorkerDBHelper.COLUMN_WORKER_ID + " = ?", new String[] { String.valueOf(workerId) }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            worker = cursorTo(cursor);
        }
        cursor.close();
        return worker;
    }

	public Worker getByName(String name) {
		Worker worker = null;
		Cursor cursor = dbHelper.getDatabase().query(WorkerDBHelper.TABLE, ALL_COLUMNS, WorkerDBHelper.COLUMN_NAME + " = ?", new String[] { name }, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
            worker = cursorTo(cursor);
		}
		cursor.close();
		return worker;
	}

    public Worker getByWorkPermit(String workPermit) {
        Worker worker = null;
        Cursor cursor = dbHelper.getDatabase().query(WorkerDBHelper.TABLE, ALL_COLUMNS, WorkerDBHelper.COLUMN_WORK_PERMIT + " = ?", new String[] { workPermit }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            worker = cursorTo(cursor);
        }
        cursor.close();
        return worker;
    }
    
    public List<Worker> getAllWorkers() {
        List<Worker> workers = new ArrayList<Worker>();

        Cursor cursor = dbHelper.getDatabase().query(WorkerDBHelper.TABLE, ALL_COLUMNS, null, null, null, null, WorkerDBHelper.COLUMN_NAME + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Worker worker = cursorTo(cursor);
                workers.add(worker);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return workers;
    }

	public int update(Worker worker) {
		return dbHelper.getDatabase().update(WorkerDBHelper.TABLE, getContentValues(worker), WorkerDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(worker.getId()) });
	}
	
	
	public int delete(Worker worker) {
		return dbHelper.getDatabase().delete(WorkerDBHelper.TABLE, WorkerDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(worker.getId()) });
	}

    public int deleteAll() {
        return dbHelper.getDatabase().delete(WorkerDBHelper.TABLE, null, null);
    }

    private Worker cursorTo(Cursor cursor) {
		Worker worker = new Worker();
        worker.setId(cursor.getLong(cursor.getColumnIndex(WorkerDBHelper.COLUMN_ID)));
        worker.setWorkerId(cursor.getLong(cursor.getColumnIndex(WorkerDBHelper.COLUMN_WORKER_ID)));
        worker.setName(cursor.getString(cursor.getColumnIndex(WorkerDBHelper.COLUMN_NAME)));
        worker.setWorkPermit(cursor.getString(cursor.getColumnIndex(WorkerDBHelper.COLUMN_WORK_PERMIT)));
        return worker;
	}
	
	private ContentValues getContentValues(Worker worker) {
		ContentValues values = new ContentValues();
		if (worker.getId() != null && worker.getId() > 0) {
			values.put(WorkerDBHelper.COLUMN_ID, worker.getId());
		}
		values.put(WorkerDBHelper.COLUMN_WORKER_ID, worker.getWorkerId());
        values.put(WorkerDBHelper.COLUMN_NAME, worker.getName());
        values.put(WorkerDBHelper.COLUMN_WORK_PERMIT, worker.getWorkPermit());
		return values;
	}
	
}