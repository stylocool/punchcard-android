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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.client.android.Intents;
import com.punchcard.app.model.Punchcard;
import com.punchcard.app.model.Worker;
import com.punchcard.app.service.PunchCardService;
import com.punchcard.app.zxing.client.android.CaptureActivity;

import org.json.JSONObject;

import java.util.Date;

/**
 * @author Jason Pang
 */

public class PunchCardActivity extends Activity implements OnClickListener, LocationListener {
    private static final String TAG = "PunchCardActivity";

    private Button scanBtn;
    private TextView contentTxt, scannedTxt;
    private PunchCardService punchcardService;
    private Long projectId;
    private String gps;
    private String type;
    private int scanned;
    //private Date scannedTime;
    private double lat = 0, lng = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scanned = 0;
        //scannedTime = new Date();
        setContentView(R.layout.activity_punchcard);
        scanBtn = (Button)findViewById(R.id.scan_button);
        contentTxt = (TextView)findViewById(R.id.scan_text);
        TextView scanMode = (TextView)findViewById(R.id.scan_mode);
        scannedTxt = (TextView)findViewById(R.id.scanned);

        // init PunchCard
        punchcardService = PunchCardService.getInstance(getApplicationContext());

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        projectId = (Long) getIntent().getSerializableExtra("projectId");
        //gps = (String) getIntent().getSerializableExtra("gps");
        type = (String) getIntent().getSerializableExtra("type");
        //Log.d(TAG, projectId + " " + gps + " " + type);

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

    }

    public void onClick(View v) {
        if (v.getId()==R.id.scan_button) {
            
            Intent i = new Intent(this, CaptureActivity.class);
            i.setAction(com.google.zxing.client.android.Intents.Scan.ACTION);
            i.putExtra(Intents.Scan.MODE, Intents.Scan.ONE_D_MODE);
            i.putExtra(Intents.Scan.SAVE_HISTORY, false);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivityForResult(i,1);

            // disable button
            scanBtn.setEnabled(false);

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        gps = lat+","+lng;
        scannedTxt.setText("GPS: "+gps);
        if (!scanBtn.isEnabled())
            scanBtn.setEnabled(true);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "GPS is disabled");
        scannedTxt.setText("GPS is disabled");
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG,"Status: "+status);
        scannedTxt.setText("GPS status: "+status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scannedTxt.setText("Total scanned: "+scanned);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Activity destroyed");
    }

    public class PunchIt extends AsyncTask<Void, Void, Boolean> {

        private final String workPermit;
        private String msg = "";

        PunchIt(String workPermit) {
            this.workPermit = workPermit;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                Punchcard punchcard = punch(workPermit);
                if (punchcard == null) {
                    msg = "Error adding/updating punchcard!";
                } else {
                    msg = workPermit + "'s punchcard successfully added/updated!";
                }
                return true;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            scanBtn.setEnabled(true);
            if (success) {
                Log.d(TAG, msg);
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Worker captured!", Toast.LENGTH_SHORT);
                toast.show();
                scanned++;
                scannedTxt.setText("Total scanned: "+scanned);
            } else {
                Log.d(TAG, msg);
                Toast toast = Toast.makeText(getApplicationContext(),
                        "An error has occurred! Please try again.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private Punchcard punch(String workPermit) throws Exception {
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

        if (worker != null) {

            Date scannedTime = punchcardService.getServerTime();

            Punchcard punchcard = punchcardService.getDbHelper().getPunchcardDS().getPunchcardsWithCheckinCheckoutStatusByProjectAndWorkerId(projectId, worker.getWorkerId());
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
                    Log.d(TAG, "Updating existing punchcard's checkin date");

                    if (punchcard.getStatus().equals(Punchcard.STATUS[1])) {
                        // existing checkin so no change in status
                    } else if (punchcard.getStatus().equals(Punchcard.STATUS[2])) {
                        // existing checkout so set to new
                        punchcard.setStatus(Punchcard.STATUS[0]);
                    }

                    // update with new checkin date and gps
                    punchcard.setCheckin(scannedTime);
                    punchcard.setCheckinLocation(gps);

                    punchcardService.getDbHelper().getPunchcardDS().update(punchcard);
                }
            } else {
                Log.d(TAG, "Doing checkout");
                if (punchcard == null) {
                    Log.d(TAG, "No existing punchcard found! Adding new punchcard with checkout");
                    punchcard = new Punchcard();
                    punchcard.setProjectId(projectId);
                    punchcard.setWorkerId(worker.getWorkerId());
                    punchcard.setCheckin(null);
                    punchcard.setCheckinLocation(null);
                    punchcard.setCheckout(scannedTime);
                    punchcard.setCheckoutLocation(gps);
                    // set status to checkout
                    punchcard.setStatus(Punchcard.STATUS[2]);
                    punchcard = punchcardService.getDbHelper().getPunchcardDS().add(punchcard);

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
            return punchcard;
        } else {
            // worker not found
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Work permit not found in system! Please inform system administrator.", Toast.LENGTH_SHORT);
            toast.show();

        }
        return null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == 1) {
            if(resultCode == RESULT_OK){

                String contents = intent.getStringExtra("SCAN_RESULT");
                contentTxt.setText(contents);

                try {
                    new PunchIt(contents).execute((Void) null);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
            else if(resultCode == RESULT_CANCELED) { // Handle cancel
                Log.i(TAG, "Cancelled");
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No scan data received!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
