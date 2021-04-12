package com.jwg.efsconnect.FileShare;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.jwg.efsconnect.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class WebServer<HTTPProgressSession> extends NanoHTTPD{

    private final static int PORT = 2333;
    private Context _mainContext;
    private ArrayList<String> pathList;
    private ProgressListener mProgressListener;

    /*
    主构造函数，也用来启动http服务
    */
    public WebServer(Context context){
        super(PORT);
        try{
            _mainContext = context;
            start();
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public void setPathList(ArrayList<String> pathList){
        this.pathList = pathList;
    }
    public void setProgressListener(ProgressListener mProgressListener){
        this.mProgressListener = mProgressListener;
    }

    /*
    解析的主入口函数，所有请求从这里进，也从这里出
    */
    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        Log.d(TAG, "uri: "+uri);
        switch (uri){
            case "/":
                String msg = "<html><body><h1>Hello server</h1>\n";
                msg += "<p>We serve " + session.getUri() + " !</p>";
                return newFixedLengthResponse( msg + "</body></html>\n" );

            case "/download":
                return downloadFile(session);

            case "/upload":
                return uploadFile(session);

        }
        return null;
    }

    private Response downloadFile(IHTTPSession session) {
        Log.d(TAG, "download");

        try {
            session.parseBody(new HashMap());
            Map parms = new HashMap();
            parms = session.getParameters();
            List<String> paths = (List<String>)parms.get("path");
            String path = paths.get(0);
            if(pathList == null || !pathList.contains(path)){
                return newFixedLengthResponse("文件未分享");
            }
            Log.d(TAG, "downloadFile: "+path);

            String fileName = FileUtils.getFileNameFromPath(path);

            FileInputStream fis = new FileInputStream(path);
            ProgressInputStream pis = new ProgressInputStream(fis, (long) new File(path).length());
            mProgressListener.setShowContent("正在分享文件 "+fileName);
            pis.setListener(mProgressListener);

            Response res = newFixedLengthResponse(Response.Status.OK,"application/octet-stream;charset=UTF-8",pis,fis.available());
            res.addHeader("Content-Disposition", "attachment; filename=\""+fileName+"\"");
            return res;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ResponseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Response uploadFile(IHTTPSession session){
        Map<String, String> files = new HashMap<String, String>();

        try {
            mProgressListener.setShowContent("正在接收文件");
            session.setProgressListener(mProgressListener);
            session.parseBody(files);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        }


        if (!files.isEmpty()) {
            Map<String, List<String>> params = session.getParameters();
            String fileName = null;
            String tmpFilePath = null;
            for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                final String paramsKey = entry.getKey();
                final List<String> fileNames = entry.getValue();
                tmpFilePath = files.get(paramsKey);
                if (TextUtils.isEmpty(tmpFilePath)) {
                    return newFixedLengthResponse("404");
                }
                if (fileNames != null && fileNames.size() > 0) {
                    fileName = fileNames.get(fileNames.size() - 1);
                }
            }
            final File tmpFile = new File(tmpFilePath);
            Log.d(TAG, "save: " + Environment.getExternalStorageDirectory() + File.separator + "/RevFile");
            File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "/RevFile");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            final File targetFile = new File(dir, fileName);
            try {
                FileUtils.copyFile(tmpFile, targetFile);
                return newFixedLengthResponse("传输完成");
            } catch (IOException e) {
                e.printStackTrace();
                return newFixedLengthResponse("文件写入错误");
            }
        }
        return newFixedLengthResponse("404");
    }

}