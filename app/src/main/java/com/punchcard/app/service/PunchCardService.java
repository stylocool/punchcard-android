package com.punchcard.app.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import com.punchcard.app.R;
import com.punchcard.app.dao.DBHelper;
import com.punchcard.app.model.Punchcard;
import com.punchcard.app.model.User;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Jason Pang
 */
public class PunchCardService {
    private static final String TAG = "PunchCard";

    private Context context;

    private DefaultHttpClient client = null;
    private String email;
    private String password;
    private boolean rememberMe;

    private Long userId;
    private String authenticationToken;

    private DBHelper dbHelper;

    private static PunchCardService instance = null;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private String API;

    private Date serverTime;
    private long elapsedTime;

    public static PunchCardService getInstance(Context context) {
        if (instance == null) {
            instance = new PunchCardService(context);
        }
        return instance;
    }

    public PunchCardService(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);

        User user = getUser();
        if (user != null) {
            setEmail(user.getEmail());
            setPassword(user.getPassword());
            setRememberMe(user.isRememberMe());
        }

        sharedPref = context.getSharedPreferences("punchcard.PREFERENCE", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        API = "http://" + getHost() + ":" + getPort() + "/api";

    }

    public DefaultHttpClient getClient() {
        if (client == null) {
            client = new DefaultHttpClient();
        }
        return client;
    }

    public DBHelper getDbHelper() {
        return dbHelper;
    }

    public String doLogin(String email, String password) throws Exception {
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("password", password));

        return doPost(context.getString(R.string.login_url), params);
    }

    public String doAddPunchcard(Long companyId, Punchcard punchcard) throws Exception {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("company_id", String.valueOf(companyId)));
        params.add(new BasicNameValuePair("project_id", String.valueOf(punchcard.getProjectId())));
        params.add(new BasicNameValuePair("worker_id", String.valueOf(punchcard.getWorkerId())));

        if (!punchcard.getCheckin().toString().endsWith("1970")) {
            params.add(new BasicNameValuePair("checkin", punchcard.getCheckin().toString()));
            params.add(new BasicNameValuePair("checkin_location", punchcard.getCheckinLocation()));
        }
        if (!punchcard.getCheckout().toString().endsWith("1970")) {
            params.add(new BasicNameValuePair("checkout", punchcard.getCheckout().toString()));
            params.add(new BasicNameValuePair("checkout_location", punchcard.getCheckoutLocation()));
        }

        return doPost(context.getString(R.string.add_punchcard_url), params);
    }

    private String doPost(String url, List<NameValuePair> nameValuePair) throws Exception {
        //try {
            HttpPost httpPost = new HttpPost(API + "/" + url);
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            httpPost.addHeader("X-API-EMAIL", email);
            httpPost.addHeader("X-API-TOKEN", authenticationToken);
            HttpResponse response = getClient().execute(httpPost);
            return EntityUtils.toString(response.getEntity());
        //} catch (Exception e) {
        //    Log.d(TAG, e.getMessage(), e);
        //    return e.getMessage();
        //}
    }

    public String getCompanies()throws Exception {
        return doGet(context.getString(R.string.companies_url));
    }

    public String getWorkerByWorkPermit(String workPermit) throws Exception {
        return doGet(context.getString(R.string.worker_work_permit_url) + "/" + workPermit);
    }

    public String doGet(String url) throws Exception {
        //try {
            HttpGet httpGet = new HttpGet(API + "/" + url);
            httpGet.addHeader("X-API-EMAIL", email);
            httpGet.addHeader("X-API-TOKEN", authenticationToken);
            HttpResponse response = getClient().execute(httpGet);
            return EntityUtils.toString(response.getEntity());
        //} catch (Exception e) {
        //    Log.d(TAG, e.getMessage(), e);
        //    return e.getMessage();
        //}
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public User getUser() {
        List<User> users = dbHelper.getUserDS().getAllUsers();
        if (users != null && users.size() > 0) {
            return users.get(0);
        } else return null;
    }

    public String getHost() {
        return sharedPref.getString("host", this.context.getString(R.string.host));
    }

    public String getPort() {
        return sharedPref.getString("port", this.context.getString(R.string.port));
    }

    public void savePreferences(String host, String port) {
        editor.putString("host", host);
        editor.putString("port", port);
        editor.commit();
        API = "http://" + getHost() + ":" + getPort() + "/api";
    }

    public Date getServerTime() {
        serverTime = new Date(serverTime.getTime()+getElapsedTimeDifference());
        elapsedTime = SystemClock.elapsedRealtime();
        return serverTime;
    }

    public void setServerTime(Date time) {
        serverTime = time;
        elapsedTime = SystemClock.elapsedRealtime();
    }

    public long getElapsedTimeDifference() {
        return SystemClock.elapsedRealtime() - elapsedTime;
    }

    public boolean isOutdated() {
        if (getElapsedTimeDifference() > 30 * 60 * 1000) return true;
        else return false;
    }
}
