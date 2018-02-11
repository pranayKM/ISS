package com.example.chinnu.iss;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS =1 ;
    GpsHelper helper;
    RecyclerView recyclerView;
    ArrayList<DataModel> list = new ArrayList<DataModel>();
    RecyclerAdapter adapter;
double latitude,longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //location permission
        checkAndRequestPermissions();
        recyclerView=(RecyclerView)findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);        int perm = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(perm==0)
        {
         helper= new GpsHelper(MainActivity.this);
         if(helper.canGetLocation)
         {
             latitude= helper.getLatitude();
             longitude=helper.getLongitude();
             if(ConnectionDetector.getInstance(this).isOnline())
             {
                 getDetails(latitude,longitude);
             }
             else
             {
Toast.makeText(getApplicationContext(),"Please enable internet for getting the values",Toast.LENGTH_SHORT).show();
             }
         }
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Please provide Location permission to this application",Toast.LENGTH_SHORT).show();
        }



    }

    private void getDetails(double latitude, double longitude) {
        try
        {
            final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Please wait. Loading Values");
            dialog.setMax(5000);
            dialog.show();
            String URL = "http://api.open-notify.org/iss-pass.json?lat="+String.valueOf(latitude)+"&lon="+String.valueOf(longitude);
         Log.d("URl",URL);
            StringRequest stringRequest= new StringRequest(Request.Method.GET, URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            dialog.dismiss();
                         try
                         {
                             JSONObject jsonObject = new JSONObject(response);
                             if(jsonObject.getString("message").equals("success"))
                             {
                                 JSONArray jsonArray= jsonObject.getJSONArray("response");
                                 for(int i=0;i<jsonArray.length();i++)
                                 {
                                     JSONObject js= jsonArray.getJSONObject(i);
                                    String date= "Date    : "+ getDateCurrentTimeZone(Long.parseLong(js.getString("risetime")));
                                    String duration= "Duration : "+js.getString("duration");
                                     DataModel dataModel= new DataModel();
                                     dataModel.setDuration(duration);
                                     dataModel.setRisestamp(date);
                                     list.add(dataModel);
                                 }
                                 adapter= new RecyclerAdapter(list,MainActivity.this);
                                 recyclerView.setAdapter(adapter);
                             }
                         }
                         catch (Exception e)
                         {
                             e.printStackTrace();
                         }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dialog.dismiss();
                   Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
            RequestQueue queue= Volley.newRequestQueue(MainActivity.this);
            queue.add(stringRequest);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private  boolean checkAndRequestPermissions() {
        int locationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if(locationPermission!= PackageManager.PERMISSION_GRANTED)
        {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d("TAG", "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(android.Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.READ_PHONE_STATE,PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.SEND_SMS,PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        Log.d("TAG", "sms & location services permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showDialogOK("Please enable the permissions required",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.

                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {

                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }
    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setCancelable(false)
                .create()
                .show();
    }
    public  String getDateCurrentTimeZone(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        }catch (Exception e) {
        }
        return "";
    }
}
