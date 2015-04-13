package com.punchcard.app.dao;

import android.content.ContentValues;
import android.database.Cursor;
import com.punchcard.app.model.Project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectDataSource {
	//private static final String TAG = AccountDataSource.class.getName();

	// Database fields
	private DBHelper dbHelper;
	private String[] ALL_COLUMNS = {
				ProjectDBHelper.COLUMN_ID,
                ProjectDBHelper.COLUMN_PROJECT_ID,
                ProjectDBHelper.COLUMN_COMPANY_ID,
                ProjectDBHelper.COLUMN_NAME,
                ProjectDBHelper.COLUMN_LOCATION,
            };

	public ProjectDataSource(DBHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public Project add(Project project) {
        long insertId = dbHelper.getDatabase().insert(ProjectDBHelper.TABLE, null, getContentValues(project));
        Cursor cursor = dbHelper.getDatabase().query(ProjectDBHelper.TABLE, ALL_COLUMNS, ProjectDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(insertId) }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            project = cursorTo(cursor);
        }
        cursor.close();
    
		return project;
	}

	public Project get(Long id) {
		Project project = null;
		Cursor cursor = dbHelper.getDatabase().query(ProjectDBHelper.TABLE, ALL_COLUMNS, ProjectDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(id) }, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
            project = cursorTo(cursor);
		}
		cursor.close();
		return project;
	}

    public Project getByProjectId(Long projectId) {
        Project project = null;
        Cursor cursor = dbHelper.getDatabase().query(ProjectDBHelper.TABLE, ALL_COLUMNS, ProjectDBHelper.COLUMN_PROJECT_ID + " = ?", new String[] { String.valueOf(projectId) }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            project = cursorTo(cursor);
        }
        cursor.close();
        return project;
    }
	
	public Project getByCompanyId(Long companyId) {
		Project project = null;
		Cursor cursor = dbHelper.getDatabase().query(ProjectDBHelper.TABLE, ALL_COLUMNS, ProjectDBHelper.COLUMN_COMPANY_ID + " = ?", new String[] { String.valueOf(companyId) }, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
            project = cursorTo(cursor);
		}
		cursor.close();
		return project;
	}
	
	public Project getByName(String name) {
		Project project = null;
		Cursor cursor = dbHelper.getDatabase().query(ProjectDBHelper.TABLE, ALL_COLUMNS, ProjectDBHelper.COLUMN_NAME + " = ?", new String[] { name }, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
            project = cursorTo(cursor);
		}
		cursor.close();
		return project;
	}
    
    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<Project>();

        Cursor cursor = dbHelper.getDatabase().query(ProjectDBHelper.TABLE, ALL_COLUMNS, null, null, null, null, ProjectDBHelper.COLUMN_NAME + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Project project = cursorTo(cursor);
                projects.add(project);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return projects;
    }

	public int update(Project project) {
		return dbHelper.getDatabase().update(ProjectDBHelper.TABLE, getContentValues(project), ProjectDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(project.getId()) });
	}
	
	
	public int delete(Project project) {
		return dbHelper.getDatabase().delete(ProjectDBHelper.TABLE, ProjectDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(project.getId()) });
	}

    public int deleteAll() {
        return dbHelper.getDatabase().delete(ProjectDBHelper.TABLE, null, null);
    }

    public int deleteByCompanyId(Long companyId) {
        return dbHelper.getDatabase().delete(ProjectDBHelper.TABLE, ProjectDBHelper.COLUMN_COMPANY_ID + " = ?", new String[] { String.valueOf(companyId) });
    }

    private Project cursorTo(Cursor cursor) {
		Project project = new Project();
        project.setProjectId(cursor.getLong(cursor.getColumnIndex(ProjectDBHelper.COLUMN_PROJECT_ID)));
        project.setCompanyId(cursor.getLong(cursor.getColumnIndex(ProjectDBHelper.COLUMN_COMPANY_ID)));
        project.setId(cursor.getLong(cursor.getColumnIndex(ProjectDBHelper.COLUMN_ID)));
        project.setName(cursor.getString(cursor.getColumnIndex(ProjectDBHelper.COLUMN_NAME)));
        project.setLocation(cursor.getString(cursor.getColumnIndex(ProjectDBHelper.COLUMN_LOCATION)));
		return project;
	}
	
	private ContentValues getContentValues(Project project) {
		ContentValues values = new ContentValues();
        if (project.getId() != null && project.getId() > 0) {
            values.put(ProjectDBHelper.COLUMN_ID, project.getId());
        }
        values.put(ProjectDBHelper.COLUMN_PROJECT_ID, project.getProjectId());
        values.put(ProjectDBHelper.COLUMN_COMPANY_ID, project.getCompanyId());
		values.put(ProjectDBHelper.COLUMN_NAME, project.getName());
		values.put(ProjectDBHelper.COLUMN_LOCATION, project.getLocation());
        return values;
	}
	
}