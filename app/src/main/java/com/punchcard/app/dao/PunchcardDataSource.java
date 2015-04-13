package com.punchcard.app.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import com.punchcard.app.model.Punchcard;
import com.punchcard.app.model.Worker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PunchcardDataSource {
	private static final String TAG = PunchcardDataSource.class.getName();

	// Database fields
	private DBHelper dbHelper;
	private String[] ALL_COLUMNS = {
				PunchcardDBHelper.COLUMN_ID,
                PunchcardDBHelper.COLUMN_PROJECT_ID,
                PunchcardDBHelper.COLUMN_WORKER_ID,
                PunchcardDBHelper.COLUMN_CHECKIN,
                PunchcardDBHelper.COLUMN_CHECKIN_LOCATION,
                PunchcardDBHelper.COLUMN_CHECKOUT,
                PunchcardDBHelper.COLUMN_CHECKOUT_LOCATION,
                PunchcardDBHelper.COLUMN_STATUS,
                PunchcardDBHelper.COLUMN_SYNC_DATE
            };

	public PunchcardDataSource(DBHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public Punchcard add(Punchcard punchcard) {
        long insertId = dbHelper.getDatabase().insert(PunchcardDBHelper.TABLE, null, getContentValues(punchcard));
        Cursor cursor = dbHelper.getDatabase().query(PunchcardDBHelper.TABLE, ALL_COLUMNS, PunchcardDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(insertId) }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            punchcard = cursorTo(cursor);
        }
        cursor.close();
    
		return punchcard;
	}

	public Punchcard get(Long id) {
		Punchcard punchcard = null;
		Cursor cursor = dbHelper.getDatabase().query(PunchcardDBHelper.TABLE, ALL_COLUMNS, PunchcardDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(id) }, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
            punchcard = cursorTo(cursor);
		}
		cursor.close();
		return punchcard;
	}

	public List<Punchcard> getByProjectId(Long projectId) {
        List<Punchcard> punchcards = new ArrayList<Punchcard>();

        Cursor cursor = dbHelper.getDatabase().query(PunchcardDBHelper.TABLE, ALL_COLUMNS, PunchcardDBHelper.COLUMN_PROJECT_ID + " = ?", new String[] { String.valueOf(projectId) }, null, null, PunchcardDBHelper.COLUMN_ID + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Punchcard punchcard = cursorTo(cursor);
                punchcards.add(punchcard);
                cursor.moveToNext();
            }
        }
		cursor.close();
		return punchcards;
	}

    public List<Punchcard> getPunchcardsWithNotSyncStatusByProjectId(Long projectId) {
        List<Punchcard> punchcards = new ArrayList<Punchcard>();

        Cursor cursor = dbHelper.getDatabase().query(PunchcardDBHelper.TABLE, ALL_COLUMNS, PunchcardDBHelper.COLUMN_PROJECT_ID + " = ? and " +
                PunchcardDBHelper.COLUMN_STATUS + " <> ?",// and " +
                //PunchcardDBHelper.COLUMN_CHECKIN + " is not NULL and " +
                //PunchcardDBHelper.COLUMN_CHECKOUT + " is not NULL",
                new String[] { String.valueOf(projectId), Punchcard.STATUS[3] }, null, null, PunchcardDBHelper.COLUMN_ID + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Punchcard punchcard = cursorTo(cursor);
                punchcards.add(punchcard);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return punchcards;
    }

    public List<Punchcard> getByWorkerId(Long workerId) {
        List<Punchcard> punchcards = new ArrayList<Punchcard>();

        Cursor cursor = dbHelper.getDatabase().query(PunchcardDBHelper.TABLE, ALL_COLUMNS, PunchcardDBHelper.COLUMN_WORKER_ID + " = ?", new String[] { String.valueOf(workerId) }, null, null, PunchcardDBHelper.COLUMN_ID + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Punchcard punchcard = cursorTo(cursor);
                punchcards.add(punchcard);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return punchcards;
    }

    public List<Punchcard> getAllPunchcards() {
        List<Punchcard> punchcards = new ArrayList<Punchcard>();

        Cursor cursor = dbHelper.getDatabase().query(PunchcardDBHelper.TABLE, ALL_COLUMNS, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Punchcard punchcard = cursorTo(cursor);
                punchcards.add(punchcard);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return punchcards;
    }

    public Punchcard getPunchcardsWithCheckinCheckoutStatusByProjectAndWorkerId(Long projectId, Long workerId) {
        List<Punchcard> punchcards = new ArrayList<Punchcard>();
        Cursor cursor = dbHelper.getDatabase().query(PunchcardDBHelper.TABLE, ALL_COLUMNS, PunchcardDBHelper.COLUMN_PROJECT_ID + " = ? and " +
                PunchcardDBHelper.COLUMN_WORKER_ID + " = ? and (" +
                PunchcardDBHelper.COLUMN_STATUS + " = ? or " +
                PunchcardDBHelper.COLUMN_STATUS + " = ?)", new String[] { String.valueOf(projectId), String.valueOf(workerId), Punchcard.STATUS[1], Punchcard.STATUS[2] }, null, null, PunchcardDBHelper.COLUMN_ID + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            Log.d(TAG, "Cursor: " + cursor.getCount());
            while (!cursor.isAfterLast()) {
                Punchcard punchcard = cursorTo(cursor);
                punchcards.add(punchcard);
                cursor.moveToNext();
            }
        }
        cursor.close();

        // TOOD: may have more than 1
        if (punchcards.size() > 0)
            return punchcards.get(0);
        else
            return null;
    }

    public List<Punchcard> getUnsynced() {
        List<Punchcard> punchcards = new ArrayList<Punchcard>();

        Cursor cursor = dbHelper.getDatabase().query(PunchcardDBHelper.TABLE, ALL_COLUMNS, PunchcardDBHelper.COLUMN_STATUS + " = ?", new String[] { Punchcard.STATUS[0] }, null, null, PunchcardDBHelper.COLUMN_ID + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Punchcard punchcard = cursorTo(cursor);
                punchcards.add(punchcard);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return punchcards;
    }

	public int update(Punchcard punchcard) {
		return dbHelper.getDatabase().update(PunchcardDBHelper.TABLE, getContentValues(punchcard), PunchcardDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(punchcard.getId()) });
	}
	
	
	public int delete(Punchcard punchcard) {
		return dbHelper.getDatabase().delete(PunchcardDBHelper.TABLE, PunchcardDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(punchcard.getId()) });
	}

    public int deleteAll() {
        return dbHelper.getDatabase().delete(PunchcardDBHelper.TABLE, null, null);
    }

    private Punchcard cursorTo(Cursor cursor) {
		Punchcard punchcard = new Punchcard();
        punchcard.setId(cursor.getLong(cursor.getColumnIndex(PunchcardDBHelper.COLUMN_ID)));
        punchcard.setProjectId(cursor.getLong(cursor.getColumnIndex(PunchcardDBHelper.COLUMN_PROJECT_ID)));
        punchcard.setWorkerId(cursor.getLong(cursor.getColumnIndex(PunchcardDBHelper.COLUMN_WORKER_ID)));
        punchcard.setCheckin(new Date(cursor.getLong(cursor.getColumnIndex(PunchcardDBHelper.COLUMN_CHECKIN))));
        punchcard.setCheckout(new Date(cursor.getLong(cursor.getColumnIndex(PunchcardDBHelper.COLUMN_CHECKOUT))));
        punchcard.setCheckinLocation(cursor.getString(cursor.getColumnIndex(PunchcardDBHelper.COLUMN_CHECKIN_LOCATION)));
        punchcard.setCheckoutLocation(cursor.getString(cursor.getColumnIndex(PunchcardDBHelper.COLUMN_CHECKOUT_LOCATION)));
        punchcard.setStatus(cursor.getString(cursor.getColumnIndex(PunchcardDBHelper.COLUMN_STATUS)));
        punchcard.setSyncDate(new Date(cursor.getLong(cursor.getColumnIndex(PunchcardDBHelper.COLUMN_SYNC_DATE))));
        return punchcard;
	}
	
	private ContentValues getContentValues(Punchcard punchcard) {
		ContentValues values = new ContentValues();
		if (punchcard.getId() != null && punchcard.getId() > 0) {
			values.put(PunchcardDBHelper.COLUMN_ID, punchcard.getId());
		}
		values.put(PunchcardDBHelper.COLUMN_PROJECT_ID, punchcard.getProjectId());
        values.put(PunchcardDBHelper.COLUMN_WORKER_ID, punchcard.getWorkerId());
        values.put(PunchcardDBHelper.COLUMN_CHECKIN, punchcard.getCheckin() != null ? punchcard.getCheckin().getTime() : null);
        values.put(PunchcardDBHelper.COLUMN_CHECKOUT, punchcard.getCheckout() != null ? punchcard.getCheckout().getTime() : null);
        values.put(PunchcardDBHelper.COLUMN_CHECKIN_LOCATION, punchcard.getCheckinLocation());
        values.put(PunchcardDBHelper.COLUMN_CHECKOUT_LOCATION, punchcard.getCheckoutLocation());
        values.put(PunchcardDBHelper.COLUMN_STATUS, punchcard.getStatus());
        values.put(PunchcardDBHelper.COLUMN_SYNC_DATE, punchcard.getSyncDate() != null ? punchcard.getSyncDate().getTime() : null);
		return values;
	}
	
}