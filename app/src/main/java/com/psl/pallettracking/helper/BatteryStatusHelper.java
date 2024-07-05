package com.psl.pallettracking.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
    public class BatteryStatusHelper {
        public int level;
        public int scale;
        public boolean charging;
        public boolean usbCharging;
        public boolean acCharging;
        public int voltage;
        public int temperature;
        public String technology;
    }
