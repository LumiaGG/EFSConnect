package com.jwg.efsconnect;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissions();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();

        // 判断Intent是否是“分享”功能(Share Via)
        if (Intent.ACTION_SEND_MULTIPLE.equals(action) && extras.containsKey(Intent.EXTRA_STREAM)) {

            List<Uri> uriList = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
            startFileShare(uriList);

        }else if(Intent.ACTION_SEND.equals(action) && extras.containsKey(Intent.EXTRA_STREAM)){
            // 获取资源路径Uri
            Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
            List<Uri> uriList = new ArrayList<>();
            uriList.add(uri);
            startFileShare(uriList);
        }else {
            List<Uri> uriList = new ArrayList<>();
            startFileShare(uriList);
        }

        finish();

    }

    private void startFileShare(List<Uri> uriList){
        try {
            ArrayList<String> pathList = new ArrayList<>();
            for(Uri aUri: uriList){
                Log.d(TAG, "startFileShare: "+ aUri);
                pathList.add(PathUtils.getPath(getApplicationContext(), aUri));
            }
            Intent intentFileShare = new Intent(getApplicationContext(), FileShareServer.class);
            intentFileShare.putStringArrayListExtra("pathList", pathList);
            startService(intentFileShare);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void requestPermissions(){

        // 文件读取权限 前台服务
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.FOREGROUND_SERVICE,
            }, 100);
        }
    }
}