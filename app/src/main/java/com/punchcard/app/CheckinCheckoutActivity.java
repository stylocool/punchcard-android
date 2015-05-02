package com.punchcard.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.punchcard.app.model.Punchcard;
import com.punchcard.app.service.PunchCardService;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * @author Jason Pang
 */

public class CheckinCheckoutActivity extends Activity {
    private static final String TAG = "CheckinCheckoutActivity";

    private PunchCardService punchcardService;

    private String projectName;
    private Long companyId, projectId;
    private Button checkinButton;
    private Button checkoutButton;
    private Button uploadButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin_checkout);

        // init PunchCard
        punchcardService = PunchCardService.getInstance(getApplicationContext());

        companyId = (Long) getIntent().getSerializableExtra("companyId");
        projectId = (Long) getIntent().getSerializableExtra("projectId");
        projectName = (String) getIntent().getSerializableExtra("projectName");

        load();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Activity destroyed");
    }

    private void load() {

        TextView projectTextView = (TextView) findViewById(R.id.project_text);
        projectTextView.setText(projectName);

        checkinButton = (Button) findViewById(R.id.checkin_button);
        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckinCheckoutActivity.this, PunchCardActivity.class);
                intent.putExtra("companyId", companyId);
                intent.putExtra("projectId", projectId);
                intent.putExtra("type", "checkin");
                startActivity(intent);
            }
        });

        checkoutButton = (Button) findViewById(R.id.checkout_button);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckinCheckoutActivity.this, PunchCardActivity.class);
                intent.putExtra("companyId", companyId);
                intent.putExtra("projectId", projectId);
                intent.putExtra("type", "checkout");
                startActivity(intent);
            }
        });

        uploadButton = (Button) findViewById(R.id.upload_button);

        // get total punchcard not uploaded
        final List<Punchcard> punchcards = punchcardService.getDbHelper().getPunchcardDS().getPunchcardsWithNotSyncStatusByProjectId(projectId);
        if (punchcards != null && punchcards.size() > 0) {
            uploadButton.setEnabled(true);
            uploadButton.setText(getString(R.string.upload) + " " + punchcards.size() + " records");
        } else {
            uploadButton.setEnabled(false);
        }

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UploadTask(punchcards).execute((Void) null);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure you want to delete all uploaded records?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new HousekeepingTask().execute((Void) null);
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog alert = builder.create();

        Button housekeepingButton = (Button) findViewById(R.id.housekeeping_button);
        housekeepingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.show();
            }
        });

        Button closeButton = (Button) findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckinCheckoutActivity.this.finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.d(TAG, "Resume activity so reloading");
        load();
    }

    public class UploadTask extends AsyncTask<Void, Void, Boolean> {

        List<Punchcard> punchcards;
        private String errMsg = null;
        private int uploaded = 0;
        UploadTask(List<Punchcard> punchcards) {
            this.punchcards = punchcards;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            for (Punchcard punchcard : punchcards) {
                try {
                    Log.d(TAG, "Uploading punchcard: " + punchcard.toString());
                    String response = punchcardService.doAddPunchcard(companyId, punchcard);
                    Log.d(TAG, "Upload punchcard: " + response);

                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("punchcard")) {
                        JSONObject punchcardObject = jsonObject.getJSONObject("punchcard");
                        if (punchcardObject.has("id")) {
                            // set status to sync
                            punchcard.setStatus(Punchcard.STATUS[3]);
                            punchcardService.getDbHelper().getPunchcardDS().update(punchcard);
                            uploaded++;
                        }
                    } else {
                        if (jsonObject.has("success") && !jsonObject.getBoolean("success")) {
                            punchcard.setStatus(Punchcard.STATUS[3]);
                            punchcardService.getDbHelper().getPunchcardDS().update(punchcard);
                            errMsg = jsonObject.getString("message");
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    errMsg = e.getMessage();
                    return false;
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            uploadButton.setText(getString(R.string.upload));
            uploadButton.setEnabled(false);

            if (errMsg != null) {
                Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), uploaded + " record(s) uploaded", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {

        }
    }

    public class HousekeepingTask extends AsyncTask<Void, Void, Boolean> {
        private int deleted = 0;
        HousekeepingTask() {}

        @Override
        protected Boolean doInBackground(Void... params) {
            deleted = punchcardService.getDbHelper().getPunchcardDS().deleteAllSynced(projectId);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Toast.makeText(getApplicationContext(), deleted + " records deleted", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onCancelled() {

        }
    }

}
