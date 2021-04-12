package com.jwg.efsconnect.FileShare;

import android.util.Log;


public interface ProgressListener {

	void update(long read, long contentLength);

	void setShowContent(String showContent);
}
