package com.psl.pallettracking.helper;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtils {
//    private String getPingStatus(String host) {
//        long startTime = System.currentTimeMillis();
//        try {
//            Process process = Runtime.getRuntime().exec("ping -c 1 " + host);
//            int returnVal = process.waitFor();
//            long endTime = System.currentTimeMillis();
//            long duration = (endTime - startTime) / 1000; // convert to seconds
//
//            if (returnVal == 0) {
//                if (duration < 5) {
//                    return "Strong";
//                } else if (duration >= 5 && duration < 20) {
//                    return "Medium";
//                } else if (duration >= 20 && duration < 60) {
//                    return "Weak";
//                } else {
//                    return "Very Weak";
//                }
//            } else {
//                return "Disconnected";
//            }
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//            return "Disconnected";
//        }
//
    public static void checkPingStrength(final String ipAddress, final PingStrengthCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    InetAddress address = InetAddress.getByName(ipAddress);
                    long startTime = System.currentTimeMillis();
                    boolean reachable = address.isReachable(1000); // 1 second timeout
                    long endTime = System.currentTimeMillis();
                    long pingTime = endTime - startTime;

                    if (!reachable) {
                        return "Disconnect";
                    } else if (pingTime < 50) {
                        return "Strong"+pingTime;
                    } else if (pingTime < 100) {
                        return "Medium"+pingTime;
                    } else {
                        return "Weak"+pingTime;
                    }
                } catch (IOException e) {
                    return "Unknown host";
                }
            }

            @Override
            protected void onPostExecute(String result) {
                callback.onPingStrengthResult(result);
            }
        }.execute();
    }

    public interface PingStrengthCallback {
        void onPingStrengthResult(String result);
    }
}
