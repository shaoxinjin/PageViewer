package com.shaoxinjin.pageviewer.websites;

import android.os.Parcelable;

public interface WebOperationView extends Parcelable {
    String getPicUrl(String firstPicUrl, int pageNum) throws Exception;
}
