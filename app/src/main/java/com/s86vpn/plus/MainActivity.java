package com.s86vpn.plus;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.s86vpn.plus.Util.ApiUtils;
import com.s86vpn.plus.Util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.ICSOpenVPNApplication;
import de.blinkt.openvpn.core.IOpenVPNServiceInternal;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;


public class MainActivity extends AppCompatActivity implements VpnStatus.ByteCountListener, VpnStatus.StateListener {


    IOpenVPNServiceInternal mService;

    InputStream inputStream;
    BufferedReader bufferedReader;
    ConfigParser cp;
    VpnProfile vp;
    ProfileManager pm;
    Thread thread;

    // NEW
    ImageView iv_data,ic_home,ic_logout;
    LinearLayout  ll_main_data, ll_main_today;
    TextView tv_message_top_text, tv_message_bottom_text, tv_data_text, tv_data_name;
    TextView tv_data_today, tv_data_today_text, tv_data_today_name, tv_user, tv_expire, tv_activated;

    Button btn_connection;
    boolean EnableConnectButton = false;
    int  progress = 0;
    String TODAY;
    CountDownTimer ConnectionTimer;


    // new
    boolean hasFile = false;
    String FileID = "NULL", File = "NULL", City = "NULL", Image = "NULL";

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    boolean showAd = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IOpenVPNServiceInternal.Stub.asInterface(service);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };


    @Override
    protected void onStop() {
        VpnStatus.removeStateListener(this);
        VpnStatus.removeByteCountListener(this);
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private  void initAdmob()
    {

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(Utils.readislogin(getApplicationContext()))
        {
            EncryptData En = new EncryptData();
            SharedPreferences ConnectionDetails = getSharedPreferences("connection_data", 0);
            FileID = ConnectionDetails.getString("file_id", "NA");
            File = En.decrypt(ConnectionDetails.getString("file", "NA"));
            City = ConnectionDetails.getString("city", "NA");
            Image = ConnectionDetails.getString("image", "NA");

            if(!FileID.isEmpty()){
                hasFile = true;
            } else {
                hasFile = false;
            }

            initAdmob();

            try {
                VpnStatus.addStateListener(this);
                VpnStatus.addByteCountListener(this);
                Intent intent = new Intent(this, OpenVPNService.class);
                intent.setAction(OpenVPNService.START_SERVICE);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            } catch (Exception e){
                e.printStackTrace();
            }


            if(Utils.hasInternetConnection(getApplicationContext()))
            {
                new CheckTask().execute("");
            }
        }
        else
        {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }



    class CheckTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected String doInBackground(String... aurl) {

            logincheck();

            return null;

        }

        @Override
        protected void onPostExecute(String unused) {

        }
    }


    public void logincheck() {


        ApiUtils utils = new ApiUtils();
        String result = "";
        String hpassword = Utils.md5(Utils.readpassword(getApplicationContext()));

        JSONObject loginJson = new JSONObject();
        try {
            loginJson.put(Utils.Action, Utils.Actioncheak);
            loginJson.put(Utils.Sessionhash, Utils.readtoken(getApplicationContext()));
            loginJson.put(Utils.Username, Utils.readusername(getApplicationContext()));
            loginJson.put(Utils.Password,hpassword);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
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

                if(status.equalsIgnoreCase("failure")) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        logout();

                        }
                    });

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void logout()
    {

        if(!Utils.readRemember(getApplicationContext()))
        {
            Utils.saveusername(getApplicationContext(),"");
            Utils.savepassword(getApplicationContext(),"");
            Utils.savetoken(getApplicationContext(),"");
            Utils.saveactivated(getApplicationContext(),"");
            Utils.saveexpire(getApplicationContext(),"");
        }

        stop_vpn();
        Utils.saveislogin(getApplicationContext(),false);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdView != null) {
            mAdView.pause();
        }
        unbindService(mConnection);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.adMobAppFullID));


        Date Today = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        TODAY = df.format(Today);

        Typeface RobotoMedium = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Medium.ttf");
        Typeface RobotoRegular = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Regular.ttf");
        Typeface RobotoBold = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Bold.ttf");

        iv_data = findViewById(R.id.iv_data);
        ic_home = findViewById(R.id.iv_home);
        ic_logout= findViewById(R.id.iv_logout);
        ll_main_data = findViewById(R.id.ll_main_data);
        ll_main_today = findViewById(R.id.ll_main_today);
        tv_message_top_text = findViewById(R.id.tv_message_top_text);
        tv_message_bottom_text = findViewById(R.id.tv_message_bottom_text);
        tv_data_text = findViewById(R.id.tv_data_text);
        tv_data_name = findViewById(R.id.tv_data_name);
        btn_connection = findViewById(R.id.btn_connection);
        tv_data_today = findViewById(R.id.tv_data_today);
        tv_data_today_text = findViewById(R.id.tv_data_today_text);
        tv_data_today_name = findViewById(R.id.tv_data_today_name);

        tv_user = findViewById(R.id.tv_user);
        tv_expire = findViewById(R.id.tv_expire);
        tv_activated = findViewById(R.id.tv_activated);

        tv_user.setTypeface(RobotoBold);
        tv_expire.setTypeface(RobotoMedium);
        tv_activated.setTypeface(RobotoMedium);

        tv_user.setText("Username : "+Utils.readusername(getApplicationContext()));
        tv_activated.setText("Activated : "+Utils.readactivated(getApplicationContext()));
        tv_expire.setText("Expiry : "+Utils.readexpire(getApplicationContext()));
        tv_message_top_text.setTypeface(RobotoMedium);
        tv_message_bottom_text.setTypeface(RobotoMedium);

        btn_connection.setTypeface(RobotoBold);

        tv_data_text.setTypeface(RobotoRegular);
        tv_data_name.setTypeface(RobotoMedium);

        tv_data_today.setTypeface(RobotoMedium);
        tv_data_today_text.setTypeface(RobotoRegular);
        tv_data_today_name.setTypeface(RobotoMedium);

        ic_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent Servers = new Intent(MainActivity.this, ServerActivity.class);
                startActivity(Servers);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
            }
        });

        ic_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                logout();

                            }

                        })
                        .setNegativeButton("No", null)
                        .show();

            }
        });


        btn_connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if (!ICSOpenVPNApplication.isStart) {
                            if (!hasFile) {
                                Log.i("TAG","no file selected");
                            } else {
                                if(Utils.hasInternetConnection(getApplicationContext())) {
                                    try {
                                        start_vpn(File);
                                        final Handler handlerToday = new Handler();
                                        handlerToday.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startAnimation(MainActivity.this, R.id.ll_main_today, R.anim.slide_down_800, false);
                                            }
                                        }, 500);

                                        final Handler handlerData = new Handler();
                                        handlerData.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startAnimation(MainActivity.this, R.id.ll_main_data, R.anim.slide_up_800, true);
                                            }
                                        }, 1000);
                                        progress = 10;
                                        ICSOpenVPNApplication.CountDown = 30;
                                        try {
                                            ConnectionTimer = new CountDownTimer(32_000, 1000) {
                                                public void onTick(long millisUntilFinished) {
                                                    ICSOpenVPNApplication.CountDown = ICSOpenVPNApplication.CountDown - 1;
                                                    progress = progress +  (int) getResources().getDimension(R.dimen.lo_10dpGrid);

                                                    if(ICSOpenVPNApplication.connection_status == 2){
                                                        ConnectionTimer.cancel();
                                                        SharedPreferences SharedAppDetails = getSharedPreferences("settings_data", 0);
                                                        SharedPreferences.Editor Editor = SharedAppDetails.edit();
                                                        Editor.putString("connection_time", String.valueOf(ICSOpenVPNApplication.CountDown));
                                                        Editor.apply();
                                                    }

                                                    if (ICSOpenVPNApplication.CountDown <= 20){
                                                        EnableConnectButton = true;
                                                    }

                                                    if(ICSOpenVPNApplication.CountDown <= 1){
                                                        ConnectionTimer.cancel();

                                                        try {
                                                            stop_vpn();

                                                            final Handler handlerToday = new Handler();
                                                            handlerToday.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    startAnimation(MainActivity.this, R.id.ll_main_data, R.anim.slide_down_800, false);
                                                                }
                                                            }, 500);

                                                            final Handler handlerData = new Handler();
                                                            handlerData.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    startAnimation(MainActivity.this, R.id.ll_main_today, R.anim.slide_up_800, true);
                                                                }
                                                            }, 1000);

                                                            ICSOpenVPNApplication.ShowDailyUsage = true;
                                                        } catch (Exception e) {
                                                            Bundle params = new Bundle();
                                                            params.putString("device_id", ICSOpenVPNApplication.device_id);
                                                            params.putString("exception", "MA3" + e.toString());
                                                        }
                                                        ICSOpenVPNApplication.isStart = false;
                                                    }

                                                }

                                                public void onFinish() {

                                                }

                                            };
                                        } catch (Exception e) {
                                            e.printStackTrace();

                                        }
                                        ConnectionTimer.start();

                                        EnableConnectButton = false;
                                        ICSOpenVPNApplication.isStart = true;

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        } else {
                            if(EnableConnectButton) {
                                try {
                                    stop_vpn();
                                    try {
                                        ConnectionTimer.cancel();
                                    } catch (Exception ignored) {
                                        //new SyncFunctions(MainActivity.this, "MA6 " +  e.toString()).set_error_log();
                                    }

                                    final Handler handlerToday = new Handler();
                                    handlerToday.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startAnimation(MainActivity.this, R.id.ll_main_data, R.anim.slide_down_800, false);
                                            ll_main_data.setVisibility(View.INVISIBLE);
                                        }
                                    }, 500);


                                    final Handler handlerData = new Handler();
                                    handlerData.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startAnimation(MainActivity.this, R.id.ll_main_today, R.anim.slide_up_800, true);
                                        }
                                    }, 1000);

                                    SharedPreferences settings = getSharedPreferences("settings_data", 0);
                                    String ConnectionTime = settings.getString("connection_time", "0");


                                    ICSOpenVPNApplication.ShowDailyUsage = true;
                                } catch (Exception e) {
                                    e.printStackTrace();

                                }
                                ICSOpenVPNApplication.isStart = false;
                            }
                        }
                    }
                };
                r.run();
            }

        });



        // ui refresh
        thread = new Thread() {
            boolean ShowData = true;

            @Override
            public void run() {
                try {
                    while (!thread.isInterrupted()) {
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // set country flag
                                if (ICSOpenVPNApplication.abortConnection){
                                    ICSOpenVPNApplication.abortConnection = false;

                                    if(ICSOpenVPNApplication.connection_status != 2) {
                                        ICSOpenVPNApplication.CountDown = 1;
                                    }

                                    if(ICSOpenVPNApplication.connection_status == 2){
                                        try {
                                            stop_vpn();
                                            try {
                                                ConnectionTimer.cancel();
                                            } catch (Exception e) {
                                               e.printStackTrace();
                                            }

                                            final Handler handlerToday = new Handler();
                                            handlerToday.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    startAnimation(MainActivity.this, R.id.ll_main_data, R.anim.slide_down_800, false);
                                                    ll_main_data.setVisibility(View.INVISIBLE);
                                                }
                                            }, 500);


                                            final Handler handlerData = new Handler();
                                            handlerData.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    startAnimation(MainActivity.this, R.id.ll_main_today, R.anim.slide_up_800, true);
                                                }
                                            }, 1000);


                                            ICSOpenVPNApplication.ShowDailyUsage = true;
                                        } catch (Exception e) {
                                            e.printStackTrace();

                                        }
                                        ICSOpenVPNApplication.isStart = false;
                                    }

                                }

                                // set connection button
                                if (hasFile) {
                                    if(ICSOpenVPNApplication.connection_status == 0){
                                        // disconnected
                                        btn_connection.setText("Connect");
                                        btn_connection.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_connect));

                                    } else if(ICSOpenVPNApplication.connection_status == 1){
                                        // connecting
                                        if(EnableConnectButton){
                                            btn_connection.setText("Cancel");
                                            btn_connection.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_retry));
                                        } else {
                                            btn_connection.setText("Connecting");
                                            btn_connection.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_retry));
                                        }
                                    } else if(ICSOpenVPNApplication.connection_status == 2){
                                        // connected
                                        btn_connection.setText("Disconnect");
                                        btn_connection.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_disconnect));
                                    }  else if(ICSOpenVPNApplication.connection_status == 3){
                                        // connected
                                        btn_connection.setText("Remove VPN Apps");
                                        btn_connection.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_retry));

                                    }

                                }

                                // set message text
                                if (hasFile) {
                                    if(Utils.hasInternetConnection(getApplicationContext())){
                                        if(ICSOpenVPNApplication.connection_status == 0){
                                            // disconnected
                                            tv_message_top_text.setText("The connection is ready");
                                            tv_message_bottom_text.setText("Tap CONNECT to start :)");

                                        } else if(ICSOpenVPNApplication.connection_status == 1){
                                            // connecting
                                            tv_message_top_text.setText("Connecting " + City);
                                            tv_message_bottom_text.setText(VpnStatus.getLastCleanLogMessage(MainActivity.this));

                                        } else if(ICSOpenVPNApplication.connection_status == 2){
                                            // connected
                                            tv_message_top_text.setText("Connected " + City);
                                            tv_message_bottom_text.setText(Data.StringCountDown);


                                        } else if(ICSOpenVPNApplication.connection_status == 3){
                                            // connected
                                            tv_message_top_text.setText("Dangerous VPN apps found");
                                            tv_message_bottom_text.setText("Your device at a risk, remove other VPN apps! potential dangerous VPN apps keep blocking internet connection");
                                        }
                                    } else {
                                        tv_message_top_text.setText("Connection is not available");
                                        tv_message_bottom_text.setText("Check your internet connection to continue");
                                    }

                                }

                                // show data limit
                                if(ShowData){
                                    ShowData = false;
                                    if(ICSOpenVPNApplication.connection_status == 0) {
                                        final Handler handlerData = new Handler();
                                        handlerData.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startAnimation(MainActivity.this, R.id.ll_main_today, R.anim.slide_up_800, true);
                                            }
                                        }, 1000);
                                    }  else if(ICSOpenVPNApplication.connection_status == 1) {
                                        final Handler handlerData = new Handler();
                                        handlerData.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startAnimation(MainActivity.this, R.id.ll_main_today, R.anim.slide_up_800, true);
                                            }
                                        }, 1000);
                                    }  else if(ICSOpenVPNApplication.connection_status == 2) {
                                        final Handler handlerData = new Handler();
                                        handlerData.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startAnimation(MainActivity.this, R.id.ll_main_data, R.anim.slide_up_800, true);
                                            }
                                        }, 1000);
                                    } else if(ICSOpenVPNApplication.connection_status == 3){
                                        // connected
                                        final Handler handlerData = new Handler();
                                        handlerData.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startAnimation(MainActivity.this, R.id.ll_main_today, R.anim.slide_up_800, true);
                                            }
                                        }, 1000);
                                    }
                                }

                                // get daily usage
                                if (hasFile) {
                                    if(ICSOpenVPNApplication.connection_status == 0){
                                        // disconnected
                                        if(ICSOpenVPNApplication.ShowDailyUsage) {
                                            ICSOpenVPNApplication.ShowDailyUsage = false;
                                            String PREF_USAGE = "daily_usage";
                                            SharedPreferences settings = getSharedPreferences(PREF_USAGE, 0);
                                            long long_usage_today = settings.getLong(TODAY, 0);

                                            if(long_usage_today < 1000){
                                                tv_data_today_text.setText("1KB");
                                            }  else if ((long_usage_today >= 1000) && (long_usage_today <= 1000_000)) {
                                                tv_data_today_text.setText((long_usage_today/1000) + "KB");
                                            } else  {
                                                tv_data_today_text.setText((long_usage_today/1000_000) + "MB");
                                            }
                                        }
                                    }
                                }

                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }
        };
        thread.start();

    }



    private void start_vpn(String VPNFile){
        showAd = true;
        SharedPreferences sp_settings;
        sp_settings = getSharedPreferences("daily_usage", 0);
        long connection_today = sp_settings.getLong(TODAY + "_connections", 0);
        long connection_total = sp_settings.getLong("total_connections", 0);
        SharedPreferences.Editor editor = sp_settings.edit();
        editor.putLong(TODAY + "_connections", connection_today + 1);
        editor.putLong("total_connections", connection_total + 1);
        editor.apply();

        ICSOpenVPNApplication.connection_status = 1;
        try {
            inputStream = null;
            bufferedReader = null;
            try {
                assert VPNFile != null;
                inputStream = new ByteArrayInputStream(VPNFile.getBytes(Charset.forName("UTF-8")));
            } catch (Exception e) {
                e.printStackTrace();

            }

            try { // M8
                assert inputStream != null;
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream/*, Charset.forName("UTF-8")*/));
            } catch (Exception e) {
                e.printStackTrace();

            }

            cp = new ConfigParser();
            try {
                cp.parseConfig(bufferedReader);
            } catch (Exception e) {
                e.printStackTrace();

            }
            vp = cp.convertProfile();
            vp.mAllowedAppsVpnAreDisallowed = true;

            try {
                vp.mName = Build.MODEL;
            } catch (Exception e) {
                e.printStackTrace();

            }

            vp.mUsername = Data.FileUsername;
            vp.mPassword = Data.FilePassword;

            try {
                pm = ProfileManager.getInstance(MainActivity.this);
                pm.addProfile(vp);
                pm.saveProfileList(MainActivity.this);
                pm.saveProfile(MainActivity.this, vp);
                vp = pm.getProfileByName(Build.MODEL);
                Intent intent = new Intent(getApplicationContext(), LaunchVPN.class);
                intent.putExtra(LaunchVPN.EXTRA_KEY, vp.getUUID().toString());
                intent.setAction(Intent.ACTION_MAIN);
                startActivity(intent);
                ICSOpenVPNApplication.isStart = false;
            } catch (Exception e) {
                e.printStackTrace();

            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void stop_vpn(){
        //showAd = false;
        ICSOpenVPNApplication.connection_status = 0;
        OpenVPNService.abortConnectionVPN = true;
        ProfileManager.setConntectedVpnProfileDisconnected(this);

        if (mService != null) {

            try {
                mService.stopVPN(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                pm = ProfileManager.getInstance(this);
                vp = pm.getProfileByName(Build.MODEL);
                pm.removeProfile(this, vp);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }



    // does not need
    @Override
    public void finish() {
        super.finish();
    }


    @Override
    public void updateState(final  String state, String logmessage, int localizedResId, ConnectionStatus level, Intent Intent) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                if (state.equals("CONNECTED")) {
                    ICSOpenVPNApplication.isStart = true;
                    ICSOpenVPNApplication.connection_status = 2;
                    EnableConnectButton = true;
                }
                else if (state.equals("NOPROCESS"))
                {
                    ICSOpenVPNApplication.isStart = false;
                    ICSOpenVPNApplication.connection_status = 0;
                    OpenVPNService.abortConnectionVPN = true;

                }
            }
        });
    }

    public void updateState(final String state, String logmessage, int localizedResId, ConnectionStatus level) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state.equals("CONNECTED")) {
                    ICSOpenVPNApplication.isStart = true;
                    ICSOpenVPNApplication.connection_status = 2;
                    EnableConnectButton = true;

                    if (showAd && mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                        showAd = false;
                    }

                }
                else if (state.equals("NOPROCESS"))
                {
                    ICSOpenVPNApplication.isStart = false;
                    showAd = true;
                    ICSOpenVPNApplication.connection_status = 0;
                    OpenVPNService.abortConnectionVPN = true;

                }
            }
        });
    }

    @Override
    public void setConnectedVPN(String uuid) {


    }

    @Override
    public void onCreate() {

    }

    @Override
    public void updateByteCount(long ins, long outs, long diffIns, long diffOuts) {
        final long Total = ins + outs;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // size
                if(Total < 1000){
                    tv_data_text.setText("1KB");
                    tv_data_name.setText("USED");
                }  else if ((Total >= 1000) && (Total <= 1000_000)) {
                    tv_data_text.setText((Total/1000) + "KB");
                    tv_data_name.setText("USED");
                } else  {
                    tv_data_text.setText((Total/1000_000) + "MB");
                    tv_data_name.setText("USED");
                }
            }
        });
    }

    public void startAnimation(Context ctx, int view, int animation, boolean show) {
        final View Element = findViewById(view);
        if(show) {
            Element.setVisibility(View.VISIBLE);
        } else {
            Element.setVisibility(View.INVISIBLE);
        }
        Animation anim = AnimationUtils.loadAnimation(ctx, animation);
        Element.startAnimation(anim);
    }

}


