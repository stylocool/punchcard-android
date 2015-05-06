package com.punchcard.app;

import android.app.Activity;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.client.android.Intents;
import com.punchcard.app.exception.ExistingCheckinException;
import com.punchcard.app.exception.NoCheckinException;
import com.punchcard.app.model.Punchcard;
import com.punchcard.app.model.Worker;
import com.punchcard.app.service.PunchCardService;
import com.punchcard.app.zxing.client.android.CaptureActivity;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * @author Jason Pang
 */

public class PunchCardActivity extends Activity implements OnClickListener, LocationListener {
    private static final String TAG = "PunchCardActivity";

    private Button scanBtn;
    private TextView contentTxt, gpsStatusTxt;
    private PunchCardService punchcardService;
    private Long projectId;
    private String type;
    private double lat = 0, lng = 0;
    private LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_punchcard);
        scanBtn = (Button)findViewById(R.id.scan_button);
        contentTxt = (TextView)findViewById(R.id.scan_text);
        TextView scanMode = (TextView)findViewById(R.id.scan_mode);
        gpsStatusTxt = (TextView)findViewById(R.id.gps_status);

        // init PunchCard
        punchcardService = PunchCardService.getInstance(getApplicationContext());

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        projectId = (Long) getIntent().getSerializableExtra("projectId");
        type = (String) getIntent().getSerializableExtra("type");

        scanMode.setText(type.toUpperCase());
        scanBtn.setOnClickListener(this);

        if (lat == 0 && lng == 0)
            scanBtn.setEnabled(false);

        Button closeButton = (Button) findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PunchCardActivity.this.finish();
            }
        });

        /*
        Button testButton = (Button) findViewById(R.id.test_button);
        testButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new PunchIt("S12345678", "1.0,1.0").execute((Void) null);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    showMessage("Error: " + e.getMessage(), Toast.LENGTH_SHORT);
                }
            }
        });
        */
    }

    public void onClick(View v) {
        if (v.getId()==R.id.scan_button) {

            if (isGpsEnabled() && lat > 0 && lng > 0) {
                Intent i = new Intent(this, CaptureActivity.class);
                i.setAction(com.google.zxing.client.android.Intents.Scan.ACTION);
                i.putExtra(Intents.Scan.MODE, Intents.Scan.ONE_D_MODE);
                i.putExtra(Intents.Scan.SAVE_HISTORY, false);
                i.putExtra("GPS", lat+","+lng);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(i, 1);
            } else {
                showMessage("GPS is disabled!", Toast.LENGTH_SHORT);
            }
            // disable button
            scanBtn.setEnabled(false);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        String gps = lat+","+lng;
        gpsStatusTxt.setText("GPS ("+isGpsEnabled()+"): "+gps);

        if (!scanBtn.isEnabled())
            scanBtn.setEnabled(true);
    }

    @Override
    public void onProviderDisabled(String provider) {
        //gpsStatusTxt.setText("GPS provider "+provider+" disabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
        //gpsStatusTxt.setText("GPS provider "+provider+" enabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        gpsStatusTxt.setText("GPS ("+isGpsEnabled()+"): "+status);
    }

    @Override
    public void onResume() {
        super.onResume();
        lat = 0;
        lng = 0;
        gpsStatusTxt.setText("Waiting for GPS...");
    }

    @Override
    public void onPause() {
        super.onPause();
        lat = 0;
        lng = 0;
        gpsStatusTxt.setText("Waiting for GPS...");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Activity destroyed");
    }

    private boolean isGpsEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public class PunchIt extends AsyncTask<Void, Void, Boolean> {

        private final String workPermit;
        private String msg = "";
        private String gps = "";

        PunchIt(String workPermit, String gps) {
            this.workPermit = workPermit;
            this.gps = gps;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                Punchcard punchcard = punch(workPermit, gps);
                if (punchcard == null) {
                    msg = "Error adding/updating punchcard!";
                } else {
                    msg = workPermit + "'s punchcard successfully added/updated!";
                }
                return true;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                msg = e.getMessage();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            scanBtn.setEnabled(true);
            if (success) {
                Log.d(TAG, msg);
                showMessage(msg, Toast.LENGTH_SHORT);
            } else {
                Log.d(TAG, msg);
                showMessage(msg, Toast.LENGTH_SHORT);
            }
        }
    }

    private Punchcard punch(String workPermit, String gps) throws Exception {
        // get worker from DB
        Worker worker = punchcardService.getDbHelper().getWorkerDS().getByWorkPermit(workPermit);
        if (worker == null) {
            // get worker from server
            Log.d(TAG, "Get worker from server");
            String response = punchcardService.getWorkerByWorkPermit(workPermit);
            Log.d(TAG, "Response: "+response);
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.has("worker")) {
                JSONObject workerJson = jsonResponse.getJSONObject("worker");
                Long id = workerJson.getLong("id");
                String name = workerJson.getString("name");

                // save worker
                worker = new Worker();
                worker.setWorkerId(id);
                worker.setName(name);
                worker.setWorkPermit(workPermit);
                worker = punchcardService.getDbHelper().getWorkerDS().add(worker);
            }
        }

        if (!isGpsEnabled()) {
            showMessage("GPS is disabled! Worker not captured!", Toast.LENGTH_SHORT);
            return null;
        }

        //if (worker != null) {

            Date scannedTime = punchcardService.getServerTime();

            //Punchcard punchcard = punchcardService.getDbHelper().getPunchcardDS().getPunchcardsWithCheckinCheckoutStatusByProjectAndWorkerId(projectId, worker.getWorkerId());
            List<Punchcard> punchcards = punchcardService.getDbHelper().getPunchcardDS().getPunchcardsWithCheckinStatusByProjectAndWorkerId(projectId, worker.getWorkerId());
            //Log.d(TAG, "******** No. of checkin records: "+punchcards.size());
            Punchcard punchcard = punchcards.size() > 0 ? punchcards.get(0) : null;

            if (type.equals("checkin")) {
                Log.d(TAG, "Doing checkin");
                if (punchcard == null) {
                    // create new
                    Log.d(TAG, "Adding new punchcard");
                    punchcard = new Punchcard();
                    punchcard.setProjectId(projectId);
                    punchcard.setWorkerId(worker.getWorkerId());
                    punchcard.setCheckin(scannedTime);
                    punchcard.setCheckinLocation(gps);
                    punchcard.setCheckout(null);
                    punchcard.setCheckoutLocation(null);
                    // set status to checkin
                    punchcard.setStatus(Punchcard.STATUS[1]);
                    punchcard = punchcardService.getDbHelper().getPunchcardDS().add(punchcard);
                } else {
                    Log.d(TAG, "Existing checkin punchcard found!");
                //    Log.d(TAG, "Updating existing punchcard's checkin date");

                //    if (punchcard.getStatus().equals(Punchcard.STATUS[1])) {
                        // existing checkin so no change in status
                //    } else if (punchcard.getStatus().equals(Punchcard.STATUS[2])) {
                        // existing checkout so set to new
                //        punchcard.setStatus(Punchcard.STATUS[0]);
                //    }

                    // update with new checkin date and gps
                //    punchcard.setCheckin(scannedTime);
                //    punchcard.setCheckinLocation(gps);

                //    punchcardService.getDbHelper().getPunchcardDS().update(punchcard);
                    throw new ExistingCheckinException();
                }
            } else {
                Log.d(TAG, "Doing checkout");
                if (punchcard == null) {
                    Log.d(TAG, "No existing checkin punchcard found!");
                    //punchcard = new Punchcard();
                    //punchcard.setProjectId(projectId);
                    //punchcard.setWorkerId(worker.getWorkerId());
                    //punchcard.setCheckin(null);
                    //punchcard.setCheckinLocation(null);
                    //punchcard.setCheckout(scannedTime);
                    //punchcard.setCheckoutLocation(gps);
                    // set status to checkout
                    //punchcard.setStatus(Punchcard.STATUS[2]);
                    //punchcard = punchcardService.getDbHelper().getPunchcardDS().add(punchcard);
                    throw new NoCheckinException();
                } else {
                    Log.d(TAG, "Found existing");
                    // update with new checkin date
                    punchcard.setCheckout(scannedTime);
                    punchcard.setCheckoutLocation(gps);

                    if (punchcard.getStatus().equals(Punchcard.STATUS[1])) {
                        // existing checkin so set to new
                        punchcard.setStatus(Punchcard.STATUS[0]);
                    } else if (punchcard.getStatus().equals(Punchcard.STATUS[2])) {
                        // existing checkout so no change in status
                    }

                    punchcardService.getDbHelper().getPunchcardDS().update(punchcard);
                }
            }

            punchcards = punchcardService.getDbHelper().getPunchcardDS().getPunchcardsWithCheckinStatusByProjectAndWorkerId(projectId, worker.getWorkerId());
            Log.d(TAG, "******** No. of checkin records: "+punchcards.size());
            return punchcard;
        //} else {
            // will not happen
            // worker not found
        //    showMessage("Work permit not found in system! Please inform system administrator.", Toast.LENGTH_SHORT);
        //}
        //return null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == 1) {
            if(resultCode == RESULT_OK){

                String contents = intent.getStringExtra("SCAN_RESULT");
                String gps = intent.getStringExtra("GPS");
                //Log.d(TAG, "************** gps: "+gps);
                contentTxt.setText(contents);

                try {
                    new PunchIt(contents, gps).execute((Void) null);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    showMessage("Error: " + e.getMessage(), Toast.LENGTH_SHORT);
                }
            }
            else if(resultCode == RESULT_CANCELED) { // Handle cancel
                Log.i(TAG, "Cancelled");
                showMessage("No scan data received", Toast.LENGTH_SHORT);
            }
        }
    }

    private void showMessage(String message, int length) {
        Toast.makeText(getApplicationContext(), message, length).show();
    }
}
