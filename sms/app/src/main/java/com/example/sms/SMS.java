package com.example.sms;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class SMS extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        String format = bundle.getString("format");

        for (Object pdu : pdus) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu, format);
            String sender = sms.getOriginatingAddress();
            String message = sms.getMessageBody().trim().toLowerCase();

            //  Use the international format (e.g., "+1" for US, "+63" for PH).
            if ("1234".equals(sender) && message.startsWith("asa na ka?")) {
                LocationManager locationManager = (LocationManager)
                        context.getSystemService(Context.LOCATION_SERVICE);

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "GPS permission missing", Toast.LENGTH_SHORT).show();
                    return;
                }

                LocationListener listener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        String reply = "Here's my current location:\nLAT: " + location.getLatitude() +
                                ", LNG: " + location.getLongitude();

                        SmsManager.getDefault().sendTextMessage(sender, null, reply, null, null);
                        Toast.makeText(context, "Current Location Sent Successfully", Toast.LENGTH_SHORT).show();

                        locationManager.removeUpdates(this);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}

                    @Override
                    public void onProviderEnabled(String provider) {}

                    @Override
                    public void onProviderDisabled(String provider) {}
                };

                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null);
            }
        }
    }
}