package com.example.mccstotomas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mccstotomas.Database.SqlLiteHelper;
import com.example.mccstotomas.Fragment.FragmentDashboard;
import com.example.mccstotomas.Fragment.FragmentUser;
import com.example.mccstotomas.Model.URLModel;
import com.example.mccstotomas.Model.UserModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SqlLiteHelper db;
    TextView text_email,text_userName;
    public final static String APP_PATH_SD_CARD = "/DesiredSubfolderName/";
    public final static String APP_THUMBNAIL_PATH_SD_CARD = "thumbnails";
    ImageView profile_icon;
    URLModel url = new URLModel();
    UserModel userModel = new UserModel();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        text_email = (TextView) headerView.findViewById(R.id.text_email);
        text_userName = (TextView) headerView.findViewById(R.id.text_name);
        profile_icon = (ImageView) headerView.findViewById(R.id.profile_icon);
        db = new SqlLiteHelper(this);
        db.open();


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);



        userModel = db.getSpecificUser();
        text_userName.setText(userModel.getName().toString());
        text_email.setText(userModel.getEmail().toString());

        profile_icon.setImageBitmap(getThumbnail(userModel.getEmail()+".png"));
        changeFragment(new FragmentDashboard());
    }

    public Bitmap getThumbnail(String filename ) {

        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + APP_PATH_SD_CARD + APP_THUMBNAIL_PATH_SD_CARD;
        Bitmap thumbnail = null;

// Look for the file on the external storage
        try {
            if (isSdReadable() == true) {
                thumbnail = BitmapFactory.decodeFile(fullPath + "/" + filename);
            }
        } catch (Exception e) {
            Log.e("getThumbnail() on es", e.getMessage());
        }

// If no file on external storage, look in internal storage
        if (thumbnail == null) {
            try {
                File filePath = this.getFileStreamPath(filename);
                FileInputStream fi = new FileInputStream(filePath);
                thumbnail = BitmapFactory.decodeStream(fi);
            } catch (Exception ex) {
                Log.e("getThumbnail()on is", ex.getMessage());
                thumbnail = BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.firefighter_240);
                letsDoSomeNetworking(url.Profile+userModel.getEmail()+".png");
            }
        }
        return thumbnail;
    }

    public boolean isSdReadable() {

        boolean mExternalStorageAvailable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
// We can read and write the media
            mExternalStorageAvailable = true;
            Log.i("isSdReadable", "External storage card is readable.");
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
// We can only read the media
            Log.i("isSdReadable", "External storage card is readable.");
            mExternalStorageAvailable = true;
        } else {
// Something else is wrong. It may be one of many other
// states, but all we need to know is we can neither read nor write
            mExternalStorageAvailable = false;
        }

        return mExternalStorageAvailable;
    }




    // TODO: complete the letsDoSomeNetworking() method
    private void letsDoSomeNetworking(String url) {

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new FileAsyncHttpResponseHandler(/* Context */ this) {

            @Override
            public void onStart() {
                //Start progress indicator here
//                progress = ProgressDialog.show(mainActivity, "Connecting...", "Please wait!!!");  //show a progress dialog
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File response) {
                // Do something with the file `response`
                Bitmap thumbnail = null;
                try {
                    FileInputStream fi = new FileInputStream(response);
                    thumbnail = BitmapFactory.decodeStream(fi);
                    profile_icon.setImageBitmap(thumbnail);

                    try {
// Use the compress method on the Bitmap object to write image to
// the OutputStream
                        FileOutputStream fos = openFileOutput(userModel.getEmail()+".png", Context.MODE_PRIVATE);

// Writing the bitmap to the output stream
//            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        thumbnail.compress(Bitmap.CompressFormat.PNG, 85, fos);
                        fos.close();

                    } catch (Exception e) {
                        Log.e("saveToInternalStorage()", e.getMessage());
                    }
                } catch (Exception ex) {
                    Log.e("getThumbnail()on is", ex.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, File response) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("Response", "Request fail! Status code: " + statusCode);
                Log.d("Response", "Fail response: " + response);
                Log.e("ERROR", e.toString());
               // Toast.makeText(mainActivity, "Login Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                // Completed the request (either success or failure)
             //   progress.dismiss();
            }
        });


    }


    public void changeFragment(Fragment fragment){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameContact, fragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            changeFragment(new FragmentDashboard());
        }  else if (id == R.id.nav_share) {
            changeFragment(new FragmentUser());
        } else if (id == R.id.nav_send) {
            db.deleteUser();
            Intent i = new Intent(this , MainActivityFragment.class);
            this.startActivity(i);
            this.finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




}
