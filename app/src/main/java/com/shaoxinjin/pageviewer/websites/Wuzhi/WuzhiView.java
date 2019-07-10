package com.shaoxinjin.pageviewer.websites.Wuzhi;

import android.os.Parcel;
import android.os.Parcelable;
import com.shaoxinjin.pageviewer.Util;
import com.shaoxinjin.pageviewer.websites.WebOperationView;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WuzhiView implements WebOperationView {
    @Override
    public String[] getPicUrl(String firstPicUrl, int pageNum) throws Exception {
        if (pageNum > 1) {
            return null;
        }
        Document doc = Util.getDocument(firstPicUrl);
        Elements elements = doc.select("div.content img");
        String[] srcs = new String[elements.size()];
        for (int i = 0; i < srcs.length; i++) {
            srcs[i] = elements.get(i).attr("src").trim();
        }
        return srcs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    public static final Parcelable.Creator<WuzhiView> CREATOR = new Creator<WuzhiView>() {
        @Override
        public WuzhiView createFromParcel(Parcel source) {
            return new WuzhiView();
        }

        @Override
        public WuzhiView[] newArray(int size) {
            return new WuzhiView[size];
        }
    };
}
