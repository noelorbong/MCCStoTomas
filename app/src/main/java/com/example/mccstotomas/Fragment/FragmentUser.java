package com.example.mccstotomas.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.mccstotomas.Database.SqlLiteHelper;
import com.example.mccstotomas.MainActivity;
import com.example.mccstotomas.Model.URLModel;
import com.example.mccstotomas.Model.UserModel;
import com.example.mccstotomas.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

public class FragmentUser extends Fragment {

    public Activity mainActivity;
    private ProgressDialog progress;
    private URLModel url = new URLModel();
    private SqlLiteHelper db;
    ImageView imgIcon;
    Bitmap FixBitmap;
    DatePickerDialog picker;
    private Button btn_update;
    private EditText et_name,et_address,et_dob,et_password,et_mobile_number;
    File file;
    Uri uri;
    Intent CamIntent, GalIntent, CropIntent ;
    public  static final int RequestPermissionCode  = 1 ;
    DisplayMetrics displayMetrics ;
    int width, height;
    UserModel userModel = new UserModel();
    public final static String APP_PATH_SD_CARD = "/DesiredSubfolderName/";
    public final static String APP_THUMBNAIL_PATH_SD_CARD = "thumbnails";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View view =  inflater.inflate(R.layout.frame_user,
                container, false);
        mainActivity = (MainActivity) getActivity();
        et_name = (EditText)view.findViewById(R.id.et_name);
        et_mobile_number  = (EditText)view.findViewById(R.id.et_mobile_number);
        et_address = (EditText)view.findViewById(R.id.et_address);
        et_dob = (EditText)view.findViewById(R.id.et_dob);
        et_password = (EditText)view.findViewById(R.id.et_password);
        imgIcon = (ImageView)view.findViewById(R.id.imgIcon);
        btn_update =(Button)view.findViewById(R.id.btn_update);


        // Inflate the layout for this fragment

        EnableRuntimePermission();
        db = new SqlLiteHelper(mainActivity);
        db.open();
        userModel = db.getSpecificUser();
        et_name.setText(userModel.getName());
        et_mobile_number.setText(userModel.getMobileNumber());
        et_address.setText(userModel.getAddress());

        et_dob.setText(userModel.getDob());
        et_password.setText(userModel.getPassword());
        FixBitmap = getThumbnail(userModel.getEmail()+".png");
        imgIcon.setImageBitmap(FixBitmap);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isAllFieldsvalid()){
                   // userModel = new UserModel();
                    userModel.setId(0);
                    userModel.setName(et_name.getText().toString());
                    userModel.setMobileNumber(et_mobile_number.getText().toString());
                    userModel.setAddress(et_address.getText().toString());
                    userModel.setDob(et_dob.getText().toString());
                    userModel.setPhoto("default.png");
                    userModel.setPassword(et_password.getText().toString());
                   letsDoSomeNetworking(url.Update);
                }
            }
        });

        et_dob.setInputType(InputType.TYPE_NULL);
        et_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPicker();
            }
        });

        imgIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetImageFromGallery();
            }
        });

        return  view;
        //return inflater.inflate(R.layout.activity_insert_data, container, false);
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
                File filePath = mainActivity.getFileStreamPath(filename);
                FileInputStream fi = new FileInputStream(filePath);
                thumbnail = BitmapFactory.decodeStream(fi);
            } catch (Exception ex) {
                Log.e("getThumbnail()on is", ex.getMessage());
                thumbnail = BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.firefighter_240);
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



    private boolean isAllFieldsvalid(){
        boolean isSuccess = false;

        if(isEditTextNotEmpty(et_name,"Name Required!") && isNumberValid(et_mobile_number)
                &&isEditTextNotEmpty(et_address,"Address Required!") && isEditTextNotEmpty(et_dob,"Date of Birth Required!")
                && isPasswordValid(et_password)){
            isSuccess = true;
        }

        return  isSuccess;
    }
    private boolean isEditTextNotEmpty(EditText editText,String response){
        boolean isNotNull;
        View focusView = null;
        if( TextUtils.isEmpty(editText.getText().toString().trim())){
            /**
             *   You can Toast a message here that the Username is Empty
             **/
            editText.setError( response);
            isNotNull = false;
            focusView = editText;
            focusView.requestFocus();
        }else{
            isNotNull = true;
        }
        return isNotNull;
    }

    private boolean isNumberValid(EditText editText){
        boolean isNumberValid;
        View focusView;
        if( TextUtils.isEmpty(editText.getText().toString().trim())){
            /**
             *   You can Toast a message here that the Username is Empty
             **/
            editText.setError( "Number Required!");
            isNumberValid = false;
            focusView = editText;
            focusView.requestFocus();
        }else{
            if(editText.getText().toString().length() == 11 && editText.getText().toString().startsWith("09")){
                isNumberValid = true;
            }else{
                editText.setError( "Invalid Number! Make sure it is 11 digit and start with 09");
                isNumberValid = false;
                focusView = editText;
                focusView.requestFocus();
            }

        }

        return  isNumberValid;
    }

    private boolean isEmailValid(EditText editText){
        boolean isEmailValid;
        View focusView;
        if( TextUtils.isEmpty(editText.getText().toString().trim())){
            /**
             *   You can Toast a message here that the Username is Empty
             **/
            editText.setError( "Email Required!");
            isEmailValid = false;
            focusView = editText;
            focusView.requestFocus();
        }else{
            if(editText.getText().toString().contains("@")){
                isEmailValid = true;
            }else{
                editText.setError( "Invalid Email!");
                isEmailValid = false;
                focusView = editText;
                focusView.requestFocus();
            }

        }

        return  isEmailValid;
    }

    private boolean isPasswordValid(EditText editText) {
        boolean isPasswordValid;
        View focusView;
        if( TextUtils.isEmpty(editText.getText().toString().trim())){
            /**
             *   You can Toast a message here that the Username is Empty
             **/
            editText.setError( "Password Required!");
            isPasswordValid = false;
            focusView = editText;
            focusView.requestFocus();
        }else{
            if(editText.getText().toString().trim().length() > 4){
                isPasswordValid = true;
            }else{
                editText.setError( "Password too short!");
                isPasswordValid = false;
                focusView = editText;
                focusView.requestFocus();
            }

        }

        return  isPasswordValid;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {

            ImageCropFunction();

        }
        else if (requestCode == 2) {

            if (data != null) {

                uri = data.getData();

                ImageCropFunction();

            }
        }
        else if (requestCode == 1) {

            if (data != null) {

                Bundle bundle = data.getExtras();

                FixBitmap = bundle.getParcelable("data");

                imgIcon.setImageBitmap(FixBitmap);

            }
        }
    }
    public void ImageCropFunction() {

        // Image Crop Code
        try {
            CropIntent = new Intent("com.android.camera.action.CROP");

            CropIntent.setDataAndType(uri, "image/*");

            CropIntent.putExtra("crop", "true");
            CropIntent.putExtra("outputX", 260);
            CropIntent.putExtra("outputY", 260);
            CropIntent.putExtra("aspectX", 4);
            CropIntent.putExtra("aspectY", 4);
            CropIntent.putExtra("scaleUpIfNeeded", true);
            CropIntent.putExtra("return-data", true);

            startActivityForResult(CropIntent, 1);

        } catch (ActivityNotFoundException e) {

        }
    }


    public void GetImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), 2);

    }

    private void showPicker() {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        // date picker dialog
        picker = new DatePickerDialog(mainActivity,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        showDate(year,monthOfYear+1,dayOfMonth);
                    }
                }, year, month, day);
        picker.show();
    }

    private void showDate(int year, int month, int day) {
        et_dob.setText(new StringBuilder().append(year).append("-")
                .append(month).append("-").append(day));
    }
    public void EnableRuntimePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,
                Manifest.permission.CAMERA))
        {

            Toast.makeText(mainActivity,"CAMERA permission allows us to Access CAMERA app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(mainActivity,new String[]{
                    Manifest.permission.CAMERA}, RequestPermissionCode);

        }
    }

    // TODO: complete the letsDoSomeNetworking() method
    private void letsDoSomeNetworking(String url) {



        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FixBitmap.compress(Bitmap.CompressFormat.PNG, 85, out);
        byte[] myByteArray = out.toByteArray();
//        RequestParams params = new RequestParams();
        params.put("userprofile", new ByteArrayInputStream(myByteArray), "image.png");
        params.put("name", userModel.getName());
        params.put("address", userModel.getAddress());
        params.put("dob", userModel.getDob());
        params.put("photo",userModel.getPhoto());
        params.put("email", userModel.getEmail());
        params.put("number", userModel.getMobileNumber());
        params.put("password", userModel.getPassword());

        try {
// Use the compress method on the Bitmap object to write image to
// the OutputStream
            FileOutputStream fos = mainActivity.openFileOutput(userModel.getEmail()+".png", Context.MODE_PRIVATE);

// Writing the bitmap to the output stream
//            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            FixBitmap.compress(Bitmap.CompressFormat.PNG, 85, fos);
            fos.close();

        } catch (Exception e) {
            Log.e("saveToInternalStorage()", e.getMessage());
        }
        client.setTimeout(60 * 1000);
        client.post(url,params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                //Start progress indicator here
                progress = ProgressDialog.show(mainActivity, "Connecting...", "Please wait!!!");  //show a progress dialog
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // called when response HTTP status is "200 OK"
                Log.d("Response", "JSON: " + response.toString());


                try{
                    if(response.optString("response").equals("0")){

                        Toast.makeText(mainActivity, "Update Successful!", Toast.LENGTH_LONG).show();
                        db.deleteUser();
                        db.insertUser(userModel);
                        Intent i = new Intent(mainActivity , MainActivity.class);
                        getActivity().startActivity(i);
                        getActivity().finish();
                    }else{
                        Toast.makeText(mainActivity, "Update Failed!", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e){
                    Log.e("Response", e.toString());
                    Toast.makeText(mainActivity, "Update Failed: !", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("Response", "Request fail! Status code: " + statusCode);
                Log.d("Response", "Fail response: " + response);
                Log.e("ERROR", e.toString());
                Toast.makeText(mainActivity, "Update Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                // Completed the request (either success or failure)
                progress.dismiss();
            }
        });


    }


}

