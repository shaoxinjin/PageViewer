package com.shaoxinjin.pageviewer.websites.mhxxoo;

import android.os.Parcel;
import android.os.Parcelable;

import com.shaoxinjin.pageviewer.Util;
import com.shaoxinjin.pageviewer.websites.WebOperationView;

import org.jsoup.nodes.Document;

public class MhxxooView implements WebOperationView {
    private int totalPicNum = Integer.MAX_VALUE;

    private int getTotalPicNum(Document doc) {
        String picNum = doc.selectFirst("ul.page11list a").text();
        /* format is "共xx页" */
        return picNum == null ? 0 : Integer.valueOf(picNum.split("页")[0].split("共")[1]);
    }

    @Override
    public String getPicUrl(String firstPicUrl, int pageNum) throws Exception {
        String currentPage = Util.getCommonPageUrl(firstPicUrl, pageNum);
        Document doc = Util.getDocument(currentPage);
        if (pageNum == 1) {
            totalPicNum = getTotalPicNum(doc);
        }
        if (totalPicNum < pageNum) {
            return "";
        }
        return doc.selectFirst("div.pic img").attr("src");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    public static final Parcelable.Creator<MhxxooView> CREATOR = new Creator<MhxxooView>() {
        @Override
        public MhxxooView createFromParcel(Parcel source) {
            return new MhxxooView();
        }

        @Override
        public MhxxooView[] newArray(int size) {
            return new MhxxooView[size];
        }
    };
}
