package com.punchcard.app.dao;

import android.content.ContentValues;
import android.database.Cursor;
import com.punchcard.app.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDataSource {
	//private static final String TAG = AccountDataSource.class.getName();

	// Database fields
	private DBHelper dbHelper;
	private String[] ALL_COLUMNS = {
				UserDBHelper.COLUMN_ID,
                UserDBHelper.COLUMN_EMAIL,
                UserDBHelper.COLUMN_PASSWORD,
                UserDBHelper.COLUMN_REMEMBER_ME
            };

	public UserDataSource(DBHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public User add(User user) {
        long insertId = dbHelper.getDatabase().insert(UserDBHelper.TABLE, null, getContentValues(user));
        Cursor cursor = dbHelper.getDatabase().query(UserDBHelper.TABLE, ALL_COLUMNS, UserDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(insertId) }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorTo(cursor);
        }
        cursor.close();
    
		return user;
	}

	public User get(Long id) {
		User user = null;
		Cursor cursor = dbHelper.getDatabase().query(UserDBHelper.TABLE, ALL_COLUMNS, UserDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(id) }, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
            user = cursorTo(cursor);
		}
		cursor.close();
		return user;
	}

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<User>();

        Cursor cursor = dbHelper.getDatabase().query(UserDBHelper.TABLE, ALL_COLUMNS, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                User user = cursorTo(cursor);
                users.add(user);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return users;
    }

    public int update(User user) {
		return dbHelper.getDatabase().update(UserDBHelper.TABLE, getContentValues(user), UserDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(user.getId()) });
	}
	
	
	public int delete(User user) {
		return dbHelper.getDatabase().delete(UserDBHelper.TABLE, UserDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(user.getId()) });
	}
    
    private User cursorTo(Cursor cursor) {
		User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndex(UserDBHelper.COLUMN_ID)));
        user.setEmail(cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_PASSWORD)));
        user.setRememberMe(cursor.getInt(cursor.getColumnIndex(UserDBHelper.COLUMN_REMEMBER_ME)) == 1 ? true : false);
        return user;
	}
	
	private ContentValues getContentValues(User user) {
		ContentValues values = new ContentValues();
		if (user.getId() != null && user.getId() > 0) {
			values.put(UserDBHelper.COLUMN_ID, user.getId());
		}
		values.put(UserDBHelper.COLUMN_EMAIL, user.getEmail());
        values.put(UserDBHelper.COLUMN_PASSWORD, user.getPassword());
        values.put(UserDBHelper.COLUMN_REMEMBER_ME, user.isRememberMe());
		return values;
	}
	
}