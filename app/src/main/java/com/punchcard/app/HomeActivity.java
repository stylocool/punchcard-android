package com.punchcard.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.punchcard.app.model.Company;
import com.punchcard.app.model.Project;
import com.punchcard.app.service.PunchCardService;

import org.apache.commons.lang3.time.DateParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Jason Pang
 */

public class HomeActivity extends Activity {
    private static final String TAG = "HomeActivity";

    private SwipeRefreshLayout swipeContainer;
    private ProjectAdapter adapter;
    private ListView listView;

    private HashMap<Long, Project> projects = new HashMap<Long, Project>();
    private HashMap<Long, Project> searchResults = new HashMap<Long, Project>();
    private PunchCardService punchcardService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // init PunchCard
        punchcardService = PunchCardService.getInstance(getApplicationContext());

        load();

        new LoadProjectsTask(this, false).execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Activity destroyed");
    }

    private void load() {

        listView = (ListView) findViewById(R.id.projects_list);
        adapter = new ProjectAdapter() {};

        listView.setAdapter(adapter);
        listView.setFastScrollEnabled(true);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new LoadProjectsTask(HomeActivity.this, true).execute();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        EditText search = (EditText) findViewById(R.id.search_project);
        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int start, int before, int count) {
                searchProjects(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.d(TAG, "Resume activity so reloading");
        try {
            if (punchcardService.isOutdated()) {
                new LoadProjectsTask(this, true).execute();
            } else {
                //new LoadProjectsTask(this, false).execute();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void reload() {
        Log.i(TAG, "Reloading");
        if (projects != null && !projects.isEmpty()) {
            adapter.loadData();
            swipeContainer.setRefreshing(false);
        }
    }


    private void searchProjects(CharSequence cs) {
        searchResults.clear();
        for(Project project : projects.values()) {
            if (project.getName().toLowerCase().contains(cs.toString().toLowerCase())) {
                searchResults.put(project.getProjectId(), project);
            }
        }
        if (!searchResults.isEmpty()) {
            adapter.searchData();
            swipeContainer.setRefreshing(false);
        }
    }

    // tasks
    private class LoadProjectsTask extends AsyncTask<String, Void, String> {
        private ProgressDialog dialog;
        private boolean refresh;

        public LoadProjectsTask(Context context, boolean refresh) {
            dialog = new ProgressDialog(context);
            this.refresh = refresh;
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Loading projects...");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            try {
                // clear projects
                projects = new HashMap<Long, Project>();

                // force to get server time
                if (punchcardService.getServerTime() == null) {
                    refresh = true;
                }

                if (refresh) {
                    result = refreshProjects();
                }
                else {
                    List<Project> dbProjects = punchcardService.getDbHelper().getProjectDS().getAllProjects();
                    Log.d(TAG, "DB projects: "+dbProjects.size());
                    if (dbProjects == null || dbProjects.size() == 0) {
                        refreshProjects();
                    } else {
                        for (Project proj : dbProjects)
                            projects.put(proj.getProjectId(), proj);
                    }
                }
                Log.d(TAG, "Loaded "+projects.size());

            } catch (Exception e) {
                Log.e(TAG, "exception", e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(final String result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (result.equals("")) {
                        reload();
                    }
                    else if (result.startsWith("Authentication token")) {
                        Toast toast = Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG);
                        toast.show();
                        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                        HomeActivity.this.finish();
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            });
        }
    }

    public abstract class ProjectAdapter extends android.widget.BaseAdapter {
        private ArrayList<Project> items = new ArrayList<Project>();

        public class ViewHolder {
            public String id;
            public TextView name;
        }

        /**
         * Loads the data.
         */
        public void loadData() {
            try {
                items = sort(projects.values());
                notifyDataSetChanged();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        public void searchData() {
            try {
                items = sort(searchResults.values());
                notifyDataSetChanged();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Project getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            final Project record = getItem(position);

            LayoutInflater inflater = HomeActivity.this.getLayoutInflater();

            ViewHolder viewHolder = new ViewHolder();

            if (convertView == null){
                rowView = inflater.inflate(R.layout.adapter_project, null);
                viewHolder.name = (TextView) rowView.findViewById(R.id.project_name);
                rowView.setTag(viewHolder);
            }

            final ViewHolder holder = (ViewHolder) rowView.getTag();

            holder.name.setText(record.getName());

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, CheckinCheckoutActivity.class);
                    intent.putExtra("projectId", record.getProjectId());
                    intent.putExtra("projectName", record.getName());
                    intent.putExtra("companyId", record.getCompanyId());
                    startActivity(intent);
                }
            });


            return rowView;
        }


    }

    private ArrayList<Project> sort(Collection<Project> projects) {
        ArrayList<Project> items = new ArrayList<Project>();
        items.addAll(projects);
        Collections.sort(items, new ProjectComparator());
        return items;
    }

    private String refreshProjects() throws Exception {

        Log.d(TAG, "Loading from server...");
        // clear db
        projects.clear();
        punchcardService.getDbHelper().getCompanyDS().deleteAll();
        punchcardService.getDbHelper().getProjectDS().deleteAll();

        String response = punchcardService.getCompanies();
        Log.d(TAG, response);

        // {"company":{"id":2,"name":"J-Solutions 2 Pte Ltd","projects":[{"id":2,"name":"Punggol Emerald 1","location":"1.405573,103.897984"}]}}
        JSONObject jsonResponse = new JSONObject(response);
        if (jsonResponse.has("company")) {
            JSONObject companyJson = jsonResponse.getJSONObject("company");

            // company
            Company company = new Company();
            company.setCompanyId(companyJson.getLong("id"));
            company.setName(companyJson.getString("name"));
            company = punchcardService.getDbHelper().getCompanyDS().add(company);

            SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            try {
                Date date = format.parse(companyJson.getString("time"));
                Log.d(TAG, date.toString());
                punchcardService.setServerTime(date);
            } catch (ParseException e) {
                Log.e(TAG, e.getMessage(), e);
            }

            Log.d(TAG, "Company ID: " + company.getCompanyId());

            // projects
            JSONArray projectsJson = companyJson.getJSONArray("projects");
            for (int i = 0; i < projectsJson.length(); i++) {
                JSONObject projectJson = projectsJson.getJSONObject(i);
                Project project = new Project();
                project.setProjectId(projectJson.getLong("id"));
                project.setCompanyId(company.getCompanyId());
                project.setName(projectJson.getString("name"));
                project.setLocation(projectJson.getString("location"));

                // save to db
                project = punchcardService.getDbHelper().getProjectDS().add(project);

                projects.put(project.getProjectId(), project);
            }



            return "";
        } else {
            // {"success":false,"message":"Authentication token not available! Please login again."}
            if (jsonResponse.has("message")) {
                return jsonResponse.getString("message");
            }

            return "Unknown error has occurred! Please try again later.";
        }
    }

    private class ProjectComparator implements Comparator<Project> {
        @Override
        public int compare(Project a, Project b) {
            return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
        }
    }
}
