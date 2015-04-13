package com.punchcard.app.dao;

import android.content.ContentValues;
import android.database.Cursor;
import com.punchcard.app.model.Company;

import java.util.ArrayList;
import java.util.List;

public class CompanyDataSource {
	//private static final String TAG = AccountDataSource.class.getName();

	// Database fields
	private DBHelper dbHelper;
	private String[] ALL_COLUMNS = {
				CompanyDBHelper.COLUMN_ID,
                CompanyDBHelper.COLUMN_COMPANY_ID,
                CompanyDBHelper.COLUMN_NAME
            };

	public CompanyDataSource(DBHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public Company add(Company company) {
        long insertId = dbHelper.getDatabase().insert(CompanyDBHelper.TABLE, null, getContentValues(company));
        Cursor cursor = dbHelper.getDatabase().query(CompanyDBHelper.TABLE, ALL_COLUMNS, CompanyDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(insertId) }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            company = cursorTo(cursor);
        }
        cursor.close();
    
		return company;
	}

	public Company get(Long id) {
		Company company = null;
		Cursor cursor = dbHelper.getDatabase().query(CompanyDBHelper.TABLE, ALL_COLUMNS, CompanyDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(id) }, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
            company = cursorTo(cursor);
		}
		cursor.close();
		return company;
	}

	public Company getByName(String name) {
		Company company = null;
		Cursor cursor = dbHelper.getDatabase().query(CompanyDBHelper.TABLE, ALL_COLUMNS, CompanyDBHelper.COLUMN_NAME + " = ?", new String[] { name }, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
            company = cursorTo(cursor);
		}
		cursor.close();
		return company;
	}
    
    public List<Company> getAllCompanies() {
        List<Company> companys = new ArrayList<Company>();

        Cursor cursor = dbHelper.getDatabase().query(CompanyDBHelper.TABLE, ALL_COLUMNS, null, null, null, null, CompanyDBHelper.COLUMN_NAME + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Company company = cursorTo(cursor);
                companys.add(company);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return companys;
    }

	public int update(Company company) {
		return dbHelper.getDatabase().update(CompanyDBHelper.TABLE, getContentValues(company), CompanyDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(company.getId()) });
	}
	
	
	public int delete(Company company) {
		return dbHelper.getDatabase().delete(CompanyDBHelper.TABLE, CompanyDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(company.getId()) });
	}

    public int deleteAll() {
        return dbHelper.getDatabase().delete(CompanyDBHelper.TABLE, null, null);
    }

    private Company cursorTo(Cursor cursor) {
		Company company = new Company();
        company.setId(cursor.getLong(cursor.getColumnIndex(CompanyDBHelper.COLUMN_ID)));
        company.setCompanyId(cursor.getLong(cursor.getColumnIndex(CompanyDBHelper.COLUMN_COMPANY_ID)));
        company.setName(cursor.getString(cursor.getColumnIndex(CompanyDBHelper.COLUMN_NAME)));
        return company;
	}
	
	private ContentValues getContentValues(Company company) {
		ContentValues values = new ContentValues();
		if (company.getId() != null && company.getId() > 0) {
			values.put(CompanyDBHelper.COLUMN_ID, company.getId());
		}
        values.put(CompanyDBHelper.COLUMN_COMPANY_ID, company.getCompanyId());
		values.put(CompanyDBHelper.COLUMN_NAME, company.getName());
		return values;
	}
	
}