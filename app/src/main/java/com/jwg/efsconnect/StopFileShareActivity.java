package com.jwg.efsconnect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class StopFileShareActivity extends Activity {
    private static final String TAG = "StopFileShareActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        Intent intent = new Intent(getApplicationContext(), FileShareServer.class);
        intent.putExtra("shut", true);
        stopService(intent);
        finish();
    }

}
