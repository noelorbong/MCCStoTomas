package com.example.mccstotomas;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.mccstotomas.Database.SqlLiteHelper;
import com.example.mccstotomas.Model.URLModel;
import com.example.mccstotomas.Model.UserModel;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

public class RegisterFragment extends Fragment {


    public RegisterFragment() {
        // Required empty public constructor
    }

    private EditText et_name, et_email,et_address,et_dob,et_password,et_repassword, et_mobile_number;
    private Button btn_register;
    private MainActivityFragment maf;
    private int year, month, day;
    private ProgressDialog progress;
    DatePickerDialog picker;
    private URLModel url = new URLModel();
    private SqlLiteHelper db;
    ImageView imgIcon;
    Bitmap FixBitmap;

    File file;
    Uri uri;
    Intent CamIntent, GalIntent, CropIntent ;
    public  static final int RequestPermissionCode  = 1 ;
    DisplayMetrics displayMetrics ;
    int width, height;
    UserModel userModel = new UserModel();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_register, container, false);
        maf = (MainActivityFragment)getActivity();
        et_name = (EditText)view.findViewById(R.id.et_name);
        et_email = (EditText)view.findViewById(R.id.et_email);
        et_mobile_number  = (EditText)view.findViewById(R.id.et_mobile_number);
        et_address = (EditText)view.findViewById(R.id.et_address);
        et_dob = (EditText)view.findViewById(R.id.et_dob);
        et_password = (EditText)view.findViewById(R.id.et_password);
        et_repassword = (EditText)view.findViewById(R.id.et_repassword);
        imgIcon = (ImageView)view.findViewById(R.id.imgIcon);
        btn_register =(Button)view.findViewById(R.id.btn_register);


        FixBitmap = BitmapFactory.decodeResource(maf.getResources(),
                R.drawable.firefighter_240);
        // Inflate the layout for this fragment

        EnableRuntimePermission();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isAllFieldsvalid()){
                    userModel = new UserModel();
                    userModel.setId(0);
                    userModel.setName(et_name.getText().toString());
                    userModel.setAddress(et_address.getText().toString());
                    userModel.setDob(et_dob.getText().toString());
                    userModel.setPhoto("default.png");
                    userModel.setEmail(et_email.getText().toString());
                    userModel.setMobileNumber(et_mobile_number.getText().toString());
                    userModel.setPassword(et_password.getText().toString());
                    letsDoSomeNetworking(url.Register);
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
        return view;
    }

    public void GetImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK,
               android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

//        intent.setType("image/*");
//
//        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), 2);


//        GalIntent = new Intent(Intent.ACTION_PICK,
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//        startActivityForResult(Intent.createChooser(GalIntent, "Select Image From Gallery"), 2);

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
                imgIcon.setTag("New");

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


    private boolean isAllFieldsvalid(){
        boolean isSuccess = false;

        if(isEditTextNotEmpty(et_name,"Name Required!") && isEmailValid(et_email) && isNumberValid(et_mobile_number)
                &&isEditTextNotEmpty(et_address,"Address Required!") && isEditTextNotEmpty(et_dob,"Date of Birth Required!")
                && isPasswordValid(et_password) && isPasswordValid(et_repassword) ){

            if(et_password.getText().toString().equals(et_repassword.getText().toString())){
//                Toast.makeText(maf, "Registration Success!", Toast.LENGTH_LONG).show();
                isSuccess = true;
            }else{
                et_password.setError( "Password Do not match!");
                et_repassword.setError( "Password Do not match!");
            }
        }
        String backgroundImageName = String.valueOf(imgIcon.getTag());


        if(backgroundImageName.equals("old")){
            Toast.makeText(maf, "Photo is required!", Toast.LENGTH_LONG).show();
            isSuccess = false;
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

    private void showPicker() {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        // date picker dialog
        picker = new DatePickerDialog(maf,
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

    // TODO: complete the letsDoSomeNetworking() method
    private void letsDoSomeNetworking(String url) {

        db = new SqlLiteHelper(maf);
        db.open();

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
            FileOutputStream fos = maf.openFileOutput(userModel.getEmail()+".png", Context.MODE_PRIVATE);

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
                progress = ProgressDialog.show(maf, "Connecting...", "Please wait!!!");  //show a progress dialog
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // called when response HTTP status is "200 OK"
                Log.d("Response", "JSON: " + response.toString());


                try{
                    if(response.optString("response").equals("0")){

                        Toast.makeText(maf, "Registration Successful!", Toast.LENGTH_LONG).show();
                        db.insertUser(userModel);
                        Intent i = new Intent(maf , MainActivity.class);
                        getActivity().startActivity(i);
                        getActivity().finish();
                    }else{
                        Toast.makeText(maf, "Registration Failed!", Toast.LENGTH_LONG).show();
                        et_email.setError( "Email already Exist!");
                    }

                } catch (Exception e){
                    Log.e("Response", e.toString());
                    Toast.makeText(maf, "Registration Failed: !", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("Response", "Request fail! Status code: " + statusCode);
                Log.d("Response", "Fail response: " + response);
                Log.e("ERROR", e.toString());
                Toast.makeText(maf, "Registration Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                // Completed the request (either success or failure)
                progress.dismiss();
            }
        });


    }

    public void EnableRuntimePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(maf,
                Manifest.permission.CAMERA))
        {

            Toast.makeText(maf,"CAMERA permission allows us to Access CAMERA app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(maf,new String[]{
                    Manifest.permission.CAMERA}, RequestPermissionCode);

        }
    }
}