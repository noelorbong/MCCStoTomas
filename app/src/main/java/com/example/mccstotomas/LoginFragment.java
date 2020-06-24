package com.example.mccstotomas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.mccstotomas.Database.SqlLiteHelper;
import com.example.mccstotomas.Model.URLModel;
import com.example.mccstotomas.Model.UserModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class LoginFragment extends Fragment {
    SqlLiteHelper db;
    private ProgressDialog progress;
    private Button btn_login;
    private EditText et_password, et_email;
    public FragmentActivity mainActivity;

    private URLModel url = new URLModel();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_login,
                container, false);

        mainActivity = (MainActivityFragment) getActivity();
        btn_login = (Button)view.findViewById(R.id.btn_login);
        et_password = (EditText) view.findViewById(R.id.et_password);
        et_email = (EditText) view.findViewById(R.id.et_email);

        db = new SqlLiteHelper(mainActivity);
        db.open();
        try {
            checkLogin();
        }catch (Exception e) {}

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isEmailValid(et_email) && isPasswordValid(et_password)){
                    letsDoSomeNetworking(url.Login);
                }

            }
        });

        // Inflate the layout for this fragment
        return view;
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



    private void checkLogin(){
        List<UserModel> userList = db.getAllUser();

         if (userList.size() > 0) {
//             Toast.makeText(mainActivity, "Login Success.. "+userList, Toast.LENGTH_LONG).show();
                Intent i = new Intent(getActivity() , MainActivity.class);
                getActivity().startActivity(i);
                getActivity().finish();
        }
    }


    // TODO: complete the letsDoSomeNetworking() method
    private void letsDoSomeNetworking(String url) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("email", et_email.getText());
        params.put("password", et_password.getText());

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
//                JSONObject  jsonRootObject = new JSONObject(strJson);
                JSONArray jsonArray = response.optJSONArray("records");
                UserModel userModel = new UserModel();
                try{
                    if(jsonArray.length()>0){
//                        Toast.makeText(mainActivity, "Login Success..", Toast.LENGTH_SHORT).show();
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        int id = Integer.parseInt(jsonObject.optString("id").toString());
                        String name = jsonObject.optString("name").toString();
                        String address = jsonObject.optString("address").toString();
                        String photo = jsonObject.optString("photo").toString();
                        String dob = jsonObject.optString("dob").toString();
                        String email = jsonObject.optString("email").toString();
                        String password = jsonObject.optString("password").toString();
                        String number = jsonObject.optString("number").toString();
                        userModel.setId(id);
                        userModel.setName(name);
                        userModel.setAddress(address);
                        userModel.setPhoto(photo);
                        userModel.setDob(dob);
                        userModel.setEmail(email);
                        userModel.setPassword(password);
                        userModel.setMobileNumber(number);
                        db.insertUser(userModel);

                        Toast.makeText(mainActivity, "Login Successful! ", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getActivity() , MainActivity.class);
                        getActivity().startActivity(i);
                        getActivity().finish();
                    }else{
                        Toast.makeText(mainActivity, "Login Failed, Make sure the Email and Password are correct..", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e){
                   Log.e("Response", e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("Response", "Request fail! Status code: " + statusCode);
                Log.d("Response", "Fail response: " + response);
                Log.e("ERROR", e.toString());
                Toast.makeText(mainActivity, "Login Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                // Completed the request (either success or failure)
                progress.dismiss();
            }
        });


    }


}
