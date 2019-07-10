package com.shaoxinjin.pageviewer.websites.semanhua;

import android.os.Parcel;
import android.os.Parcelable;

import com.shaoxinjin.pageviewer.Util;
import com.shaoxinjin.pageviewer.websites.WebOperationView;

import org.jsoup.nodes.Document;

public class SemanhuaView implements WebOperationView {
    private int totalPicNum = Integer.MAX_VALUE;

    private int getTotalPicNum(Document doc) {
        String picNum = doc.selectFirst("li.mh a").text();
        /* format is "1/xx页" */
        return picNum == null ? 0 : Integer.valueOf(picNum.split("页")[0].split("/")[1]);
    }

    @Override
    public String[] getPicUrl(String firstPicUrl, int pageNum) throws Exception {
        String currentPage = Util.getCommonPageUrl(firstPicUrl, pageNum);
        Document doc = Util.getDocument(currentPage);
        if (pageNum == 1) {
            totalPicNum = getTotalPicNum(doc);
        }
        if (totalPicNum < pageNum) {
            return null;
        }
        return new String[]{doc.selectFirst("div.box img").attr("src").trim()};
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    public static final Parcelable.Creator<SemanhuaView> CREATOR = new Creator<SemanhuaView>() {
        @Override
        public SemanhuaView createFromParcel(Parcel source) {
            return new SemanhuaView();
        }

        @Override
        public SemanhuaView[] newArray(int size) {
            return new SemanhuaView[size];
        }
    };
}
