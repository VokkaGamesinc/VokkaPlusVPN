package com.vokkavpn.plus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/*import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;*/
import com.vokkavpn.plus.Util.Utils;
import com.vokkavpn.plus.model.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.blinkt.openvpn.core.ICSOpenVPNApplication;
import de.blinkt.openvpn.core.ProfileManager;

public class ServerActivity extends Activity {


    ProfileManager pm;
    private CategoryArray adapter;
    ListView listView_light;

   // private AdView mAdView;

    public static ArrayList<Server> servermodel=new ArrayList<>();
    // 100
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servers);

        pm = ProfileManager.getInstance(ServerActivity.this);
        listView_light = findViewById(R.id.ls_servers_list_light);


       /* MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/


        loaddata();


        Typeface RobotoMedium = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Medium.ttf");
        TextView tv_servers_title = findViewById(R.id.tv_servers_title);
        tv_servers_title.setTypeface(RobotoMedium);

        LinearLayout ll_server_back = findViewById (R.id.ll_server_back);
        ll_server_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
            }
        });

        LinearLayout linearLayoutServers = findViewById(R.id.constraintLayoutServers);
        ImageView iv_servers_go_back = findViewById(R.id.iv_servers_go_back);
        SharedPreferences SettingsDetails = getSharedPreferences("settings_data", 0);
        linearLayoutServers.setBackgroundColor(getResources().getColor(R.color.colorLightBackground));
        tv_servers_title.setTextColor(getResources().getColor(R.color.colorLightText));
        iv_servers_go_back.setImageResource(R.drawable.ic_go_back);
    }


    public class CategoryArray extends ArrayAdapter<Server> {

        private List<Server> dataSet;
        TextView tv_country;


        private  CategoryArray(List<Server> dataSet, Context mContext) {
            super(mContext, R.layout.server_list_item, dataSet);
            this.dataSet = dataSet;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.server_list_item, null);
            }

            final Server server = dataSet.get(position);

            if (server != null) {
                tv_country =  v.findViewById(R.id.tv_country);
                ImageView iv_flag =  v.findViewById(R.id.iv_flag);

                iv_flag.setImageResource(
                        getResources().getIdentifier(server.GetImage(),
                                "drawable",
                                getPackageName()));
                final LinearLayout ll_item = v.findViewById(R.id.ll_item);

                Typeface RobotoRegular = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Regular.ttf");

                tv_country.setText(server.GetCountry());
                tv_country.setTypeface(RobotoRegular);
                tv_country.setTextColor(getResources().getColor(R.color.colorLightText));


                ll_item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ll_item.setBackgroundColor(getResources().getColor(R.color.colorSelectItem));
                        tv_country.setTextColor(getResources().getColor(R.color.colorDarkText));
                        EncryptData En = new EncryptData();
                        try {
                            SharedPreferences SharedAppDetails = getSharedPreferences("connection_data", 0);
                            SharedPreferences.Editor Editor = SharedAppDetails.edit();
                            Editor.putString("id", server.GetID());
                            Editor.putString("file_id", server.GetFileID());
                            Editor.putString("file", En.encrypt(server.GetFileID()));
                            Editor.putString("city", server.GetCountry());
                            Editor.putString("country", server.GetCountry());
                            Editor.putString("image", server.GetImage());
                            Editor.apply();
                            ICSOpenVPNApplication.hasFile = true;
                            ICSOpenVPNApplication.abortConnection = true;
                        } catch (Exception e){
                            Bundle params = new Bundle();
                            params.putString("device_id", ICSOpenVPNApplication.device_id);
                            params.putString("exception", "SA6" + e.toString());

                        }
                        finish();
                        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);


                    }
                });

            }


            SharedPreferences ConnectionDetails = getSharedPreferences("connection_data", 0);
            String country = ConnectionDetails.getString("country", "NA");

            if (server.GetCountry().equalsIgnoreCase(country)) {
                v.setBackgroundColor(getResources().getColor(R.color.colorSelectItem));
            } else {
                v.setBackgroundColor(getResources().getColor(R.color.colorLightBackground));
            }

            return v;
        }
    }






    private void loaddata()
    {
        String ID = "NULL", File = "NULL", Country = "NULL", Image = "NULL";

        try {
            JSONObject responce = Utils.readJson();
            servermodel.clear();
            JSONArray jsonArray = responce.getJSONArray("ovpn_file");

            for(int i=0; i<jsonArray.length();i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ID = jsonObject.getString("id");
                File = jsonObject.getString("file");
                Country = jsonObject.getString("Country");
                Image = jsonObject.getString("Image");

                Server server = new Server();
                server.SetID(ID);
                server.SetFileID(File);
                server.SetCountry(Country);
                server.SetImage(Image);

                servermodel.add(server);
            }

            Collections.sort(servermodel, new Comparator<Server>()
            {
                @Override
                public int compare(Server obj1, Server obj2)
                {
                    return obj1.GetCountry().compareToIgnoreCase(obj2.GetCountry());
                }
            });

            adapter = new CategoryArray(servermodel, ServerActivity.this);
            listView_light.setAdapter(adapter);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
