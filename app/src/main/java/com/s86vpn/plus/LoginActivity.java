package com.s86vpn.plus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.s86vpn.plus.Util.ApiUtils;
import com.s86vpn.plus.Util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.blinkt.openvpn.core.ICSOpenVPNApplication;


public class LoginActivity extends AppCompatActivity {

    String m_strEmail;
    LottieAnimationView la_animation;
    EditText editEmailView;
    TextView tv_pp;
    CheckBox ckRem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView(){

        ckRem = (CheckBox) findViewById(R.id.chkrem);
        ckRem.setChecked(Utils.readRemember(getApplicationContext()));
        tv_pp = findViewById(R.id.tv_pp);
        tv_pp.setPaintFlags(tv_pp.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        tv_pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://vokkagames.com/policy.html"));
                startActivity(intent);
            }
        });


        la_animation = findViewById(R.id.animation_view);
        la_animation.setVisibility(View.INVISIBLE);
        editEmailView =  findViewById(R.id.editEmail);

        if(Utils.readRemember(getApplicationContext()))
        {
            if(!Utils.readusername(getApplicationContext()).equalsIgnoreCase(""))
            {
                editEmailView.setText(Utils.readusername(getApplicationContext()));
            }

        }

        findViewById(R.id.btnLogin).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {



                editEmailView.setError(null);

                m_strEmail = editEmailView.getText().toString();

                if (TextUtils.isEmpty(m_strEmail)) {
                    editEmailView.setError(getString(R.string.error_field_required));
                }

                else {

                    if(Utils.hasInternetConnection(getApplicationContext()))
                    {

                        try {
                            new LoginTask().execute("");
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"No connection",Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

    }


    @SuppressLint("StaticFieldLeak")
    class LoginTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            la_animation.setVisibility(View.VISIBLE);

        }

        @Override
        protected String doInBackground(String... aurl) {

            statusRequest();
            loginRequest();

            return null;

        }

        @Override
        protected void onPostExecute(String unused) {

            la_animation.setVisibility(View.INVISIBLE);
        }
    }


    public void loginRequest() {


        ApiUtils utils = new ApiUtils();
        String result = "";
        String hpassword = Utils.md5(Utils.LoginPASS);

        JSONObject loginJson = new JSONObject();
        try {
            loginJson.put(Utils.Action, Utils.LoginAction);
            loginJson.put(Utils.Username, m_strEmail);
            loginJson.put(Utils.Password,hpassword);
            loginJson.put(Utils.getDeviceIMEI,Utils.getAndroidId(getApplicationContext()));

        } catch (JSONException e) {
            // TODO Auto-generated catch block this way its working

            e.printStackTrace();
        }

        try {

            result = utils.doPostRequest(Utils.LoginUrl,loginJson.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }


        if(!result.equalsIgnoreCase(""))
        {
            try {
                final JSONObject jsonResponse = new JSONObject(result);

                final String status = jsonResponse.getString("status");

                if(status.equalsIgnoreCase("success")) {

                    if(ckRem.isChecked())
                        Utils.saveRemember(getApplicationContext(),true);
                    else
                        Utils.saveRemember(getApplicationContext(),false);


                    Utils.saveislogin(getApplicationContext(),true);
                    Utils.saveusername(getApplicationContext(),m_strEmail);
                    Utils.savepassword(getApplicationContext(),Utils.LoginPASS);
                    Utils.savetoken(getApplicationContext(),jsonResponse.getString("sessionhash"));
                    parseIT(jsonResponse);

                }
                else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(getApplicationContext(),status,Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private  void parseIT(JSONObject responce)
    {

        Utils.deletejson();
        Utils.writejson(LoginActivity.this,responce.toString());

        String ID = "NULL", File = "NULL", Country = "NULL", Image = "NULL";

        try {

            JSONArray jsonArray = responce.getJSONArray("ovpn_file");


            for(int i=0; i<jsonArray.length();i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if(jsonObject.getString("Country").equalsIgnoreCase("All Global"))
                {
                    ID = jsonObject.getString("id");
                    File = jsonObject.getString("file");
                    Country = jsonObject.getString("Country");
                    Image = jsonObject.getString("Image");

                    EncryptData En = new EncryptData();
                    SharedPreferences SharedAppDetails = getSharedPreferences("connection_data", 0);
                    SharedPreferences.Editor Editor = SharedAppDetails.edit();
                    Editor.putString("id", ID);
                    Editor.putString("file_id", ID);
                    Editor.putString("file", En.encrypt(File));
                    Editor.putString("city", Country);
                    Editor.putString("country", Country);
                    Editor.putString("image", Image);
                    Editor.apply();
                    ICSOpenVPNApplication.hasFile = true;
                    ICSOpenVPNApplication.abortConnection = true;
                }


            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    la_animation.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                }
            });

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    public void statusRequest() {


        ApiUtils utils = new ApiUtils();
        String result = "";
        String hpassword = Utils.md5(Utils.LoginPASS);

        JSONObject loginJson = new JSONObject();
        try {
            loginJson.put(Utils.Action, Utils.StatusAction);
            loginJson.put(Utils.Username, m_strEmail);
            loginJson.put(Utils.Password,hpassword);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {

            result = utils.doPostRequest(Utils.StatusUrl,loginJson.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }


        if(!result.equalsIgnoreCase(""))
        {

            try {
                final JSONObject jsonResponse = new JSONObject(result);

                final String status = jsonResponse.getString("status");

                if(status.equalsIgnoreCase("success")) {

                    JSONArray jsonArray = jsonResponse.getJSONArray("PinStatus");
                    for(int i=0; i<jsonArray.length();i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Utils.saveactivated(getApplicationContext(),jsonObject.getString("activated"));
                        Utils.saveexpire(getApplicationContext(),jsonObject.getString("expire"));

                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}

