package com.punchcard.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.punchcard.app.model.Company;
import com.punchcard.app.model.Project;
import com.punchcard.app.service.PunchCardService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * @author Jason Pang
 */

public class SettingsActivity extends Activity {
    private static final String TAG = "SettingsActivity";

    private PunchCardService punchcardService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // init PunchCard
        punchcardService = PunchCardService.getInstance(getApplicationContext());

        final TextView hostTextView = (TextView) findViewById(R.id.setting_host);
        final TextView portTextView = (TextView) findViewById(R.id.setting_port);

        hostTextView.setText(punchcardService.getHost());
        portTextView.setText(punchcardService.getPort());

        Button saveButton = (Button) findViewById(R.id.setting_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String host = hostTextView.getText().toString();
                String port = portTextView.getText().toString();

                punchcardService.savePreferences(host, port);
                SettingsActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Activity destroyed");
    }

}
