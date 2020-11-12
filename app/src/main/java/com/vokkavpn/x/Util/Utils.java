package com.vokkavpn.x.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    public static String BASE = "/data/data/com.vokkavpn.x";

    public static String LoginUrl = "https://vpn.globalsend.us/api3/api.php";
    public static String StatusUrl = "https://vpn.globalsend.us/app2/apid.php";
    public static String Action = "action";
    public static String Actioncheak = "cheak";
    public static String Sessionhash = "sessionhash";
    public static String LoginAction = "auth";
    public static String StatusAction = "status";
    public static String LoginPASS = "0549945556";

    public static String Username = "appusername";
    public static String Password = "apppin";

    public static String PrefName = "vokkavpnx";
    public static String getDeviceIMEI = "imei";


    @SuppressLint("HardwareIds")
    public static String getDeviceIMEI(Context context){
        String ts = Context.TELEPHONY_SERVICE;
        TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(ts);
        return mTelephonyMgr.getDeviceId();
    }

    public static boolean hasInternetConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        haveConnectedWifi = true;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        haveConnectedMobile = true;
            }
        } catch (Exception e){
            e.printStackTrace();

        }

        return haveConnectedWifi || haveConnectedMobile;
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void writejson(Context context, String sBody) {
        try {

            File gpxfile = new File(BASE, "server.json");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject readJson() throws JSONException {
        File file = new File(BASE,"server.json");
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

        JSONObject  jsonResponse = new JSONObject(text.toString());

        return jsonResponse;
    }

    public static void deletejson()
    {
        try {
            File gpxfile = new File(BASE, "server.json");
            if(gpxfile.exists())
            {
                gpxfile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveusername(Context context, String str) {
        SharedPreferences saveHistory = context.getSharedPreferences(PrefName,
                0);
        SharedPreferences.Editor editor = saveHistory.edit();
        editor.putString("username", str);
        editor.commit();
    }

    public static String readusername(Context context) {
        SharedPreferences readHistory = context.getSharedPreferences(PrefName,
                0);
        return readHistory.getString("username", "");
    }

    public static void savepassword(Context context, String str) {
        SharedPreferences saveHistory = context.getSharedPreferences(PrefName,
                0);
        SharedPreferences.Editor editor = saveHistory.edit();
        editor.putString("password", str);
        editor.commit();
    }

    public static String readpassword(Context context) {
        SharedPreferences readHistory = context.getSharedPreferences(PrefName,
                0);
        return readHistory.getString("password", "");
    }


    public static void savetoken(Context context, String str) {
        SharedPreferences saveHistory = context.getSharedPreferences(PrefName,
                0);
        SharedPreferences.Editor editor = saveHistory.edit();
        editor.putString("token", str);
        editor.commit();
    }

    public static String readtoken(Context context) {
        SharedPreferences readHistory = context.getSharedPreferences(PrefName,
                0);
        return readHistory.getString("token", "");
    }

    public static void saveislogin(Context context, boolean str) {
        SharedPreferences saveHistory = context.getSharedPreferences(PrefName,
                0);
        SharedPreferences.Editor editor = saveHistory.edit();
        editor.putBoolean("islogin", str);
        editor.commit();
    }

    public static boolean readislogin(Context context) {
        SharedPreferences readHistory = context.getSharedPreferences(PrefName,
                0);
        return readHistory.getBoolean("islogin", false);
    }

    public static void saveRemember(Context context, boolean str) {
        SharedPreferences saveHistory = context.getSharedPreferences(PrefName,
                0);
        SharedPreferences.Editor editor = saveHistory.edit();
        editor.putBoolean("remember", str);
        editor.commit();
    }

    public static boolean readRemember(Context context) {
        SharedPreferences readHistory = context.getSharedPreferences(PrefName,
                0);
        return readHistory.getBoolean("remember", true);
    }




    public static String readactivated(Context context) {
        SharedPreferences readHistory = context.getSharedPreferences(PrefName,
                0);
        return readHistory.getString("activated", "");
    }

    public static void saveactivated(Context context, String str) {
        SharedPreferences saveHistory = context.getSharedPreferences(PrefName,
                0);
        SharedPreferences.Editor editor = saveHistory.edit();
        editor.putString("activated", str);
        editor.commit();
    }

    public static String readexpire(Context context) {
        SharedPreferences readHistory = context.getSharedPreferences(PrefName,
                0);
        return readHistory.getString("expire", "");
    }

    public static void saveexpire(Context context, String str) {
        SharedPreferences saveHistory = context.getSharedPreferences(PrefName,
                0);
        SharedPreferences.Editor editor = saveHistory.edit();
        editor.putString("expire", str);
        editor.commit();
    }

}