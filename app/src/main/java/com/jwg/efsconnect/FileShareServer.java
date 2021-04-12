package com.jwg.efsconnect;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.jwg.efsconnect.FileShare.NetworkUtils;
import com.jwg.efsconnect.FileShare.ProgressListener;
import com.jwg.efsconnect.FileShare.WebServer;

import java.util.ArrayList;


public class FileShareServer extends Service {

    private static final String TAG = "FileShareServer";
    private String channelID = "FileShareID";
    private String channelNAME = "FileShare";
    private int notifyID = 1;
    private WebServer mWebserver = null;
    private String notificationShowContent;

    private NotificationCompat.Builder builder;
    private NotificationManagerCompat notificationManager;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mWebserver = new WebServer(getApplicationContext());
    }

    private Notification initNotification(){

        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent settiongsPending = PendingIntent.getActivity(this, 0, settingsIntent, 0);

        Intent stopFileShare = new Intent(this, StopFileShareActivity.class);
        PendingIntent stopFileSharePending = PendingIntent.getActivity(this, 0, stopFileShare, 0);

        String channelId = createNotificationChannel(channelID, channelNAME, NotificationManager.IMPORTANCE_HIGH);
        builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("本机IP: "+NetworkUtils.getLocalIPAddress())
                .setContentText(notificationShowContent)
                .addAction(R.mipmap.ic_launcher, "管理", settiongsPending)
                .addAction(R.mipmap.ic_launcher, "关闭服务", stopFileSharePending)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true);

        Notification notification = builder.build();
        notificationManager =  NotificationManagerCompat.from(this);
        return notification;
    }

    private String createNotificationChannel(String channelID, String channelNAME, int level) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelID, channelNAME, level);
            // 关闭通知声音
            channel.setSound(null, null);
            manager.createNotificationChannel(channel);
            return channelID;
        } else {
            return null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.

        ArrayList<String> pathList = (ArrayList<String>) intent.getStringArrayListExtra("pathList");
        if (pathList == null){
            pathList = new ArrayList<>();
        }

        for(String path: pathList){
            Log.d(TAG, "path: "+ path);

        }

        if(pathList.size() == 0){
            notificationShowContent = "服务已开启 正在等待文件传输";
        }else {
            notificationShowContent = "已分享 "+FileUtils.getFileNameFromPath(pathList.get(0))+"等"+pathList.size()+"个文件";
        }

        mWebserver.setPathList(pathList);
        mWebserver.setProgressListener(new ProgressListener() {

            private String showContent = "";

            public void complete(){
                builder.setContentText("传输完成")
                        .setProgress(100, 100,false)
                        .setOnlyAlertOnce(false);
                notificationManager.notify(notifyID, builder.build());
            }

            @Override
            public void update(long read, long contentLength) {
                if(read == contentLength){
                    complete();
                    return;
                }
                int percentage = (int)((read / (double)contentLength) * 100);
                builder.setContentText(showContent)
                        .setProgress(100, percentage,false);
                notificationManager.notify(notifyID, builder.build());
                builder.setOnlyAlertOnce(true);
            }

            @Override
            public void setShowContent(String showContent) {
                this.showContent = showContent;
            }
        });

        startForeground(notifyID, initNotification());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mWebserver != null){
            mWebserver.closeAllConnections();
            mWebserver.stop();
            mWebserver = null;
        }
        Toast.makeText(this, "服务已经停止", Toast.LENGTH_LONG).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

