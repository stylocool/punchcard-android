package com.punchcard.app;

import android.app.Activity;
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

import java.util.Date;
import java.util.List;

/**
 * @author Jason Pang
 */

public class CheckinCheckoutActivity extends Activity { //implements LocationListener {
    private static final String TAG = "CheckinCheckoutActivity";

    private PunchCardService punchcardService;

    //private double lat = 0, lng = 0;
    private String projectName;
    private Long companyId, projectId;
    //private TextView gps;
    private Button checkinButton;
    private Button checkoutButton;
    private Button uploadButton;

    //private boolean disableCheck = false;

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

        // read current GPS coordinates
        //LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        //gps = (TextView) findViewById(R.id.gps_location);

        checkinButton = (Button) findViewById(R.id.checkin_button);

        // disable until gps coordinates is fixed
        //if (!disableCheck && lat == 0 && lng == 0)
        //    checkinButton.setEnabled(false);

        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckinCheckoutActivity.this, PunchCardActivity.class);
                intent.putExtra("companyId", companyId);
                intent.putExtra("projectId", projectId);
                intent.putExtra("type", "checkin");
                //intent.putExtra("gps", lat+","+lng);
                startActivity(intent);
            }
        });


        checkoutButton = (Button) findViewById(R.id.checkout_button);

        // disable until gps coordinates is fixed
        //if (!disableCheck && lat == 0 && lng == 0)
        //    checkoutButton.setEnabled(false);

        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckinCheckoutActivity.this, PunchCardActivity.class);
                intent.putExtra("companyId", companyId);
                intent.putExtra("projectId", projectId);
                intent.putExtra("type", "checkout");
                //intent.putExtra("gps", lat+","+lng);
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

        Button closeButton = (Button) findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckinCheckoutActivity.this.finish();
            }
        });
    }

    //public void reloadLocation() {
    //    gps.setText("Latitude:" + lat + ", Longitude:" + lng);
    //}
/*
    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();

        // TODO: check location is within 500m of project location

        if (!checkinButton.isEnabled())
            checkinButton.setEnabled(true);
        if (!checkoutButton.isEnabled())
            checkoutButton.setEnabled(true);
        reloadLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "GPS is disabled");
        gps.setText("GPS is disabled!");
    }

    @Override
    public void onProviderEnabled(String provider) {
        gps.setText("GPS is enabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG,"Status: "+status);
    }
*/

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.d(TAG, "Resume activity so reloading");
        load();
    }

    public class UploadTask extends AsyncTask<Void, Void, Boolean> {

        List<Punchcard> punchcards;
        private String errMsg = null;

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
                    // set status to sync
                    punchcard.setStatus(Punchcard.STATUS[3]);
                    punchcardService.getDbHelper().getPunchcardDS().update(punchcard);

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    errMsg = e.getMessage();
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
            }
        }

        @Override
        protected void onCancelled() {

        }
    }
}
