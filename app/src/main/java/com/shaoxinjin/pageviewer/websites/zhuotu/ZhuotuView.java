package com.shaoxinjin.pageviewer.websites.zhuotu;

import android.os.Parcel;
import android.os.Parcelable;

import com.shaoxinjin.pageviewer.Util;
import com.shaoxinjin.pageviewer.websites.WebOperationView;

import org.jsoup.nodes.Document;

public class ZhuotuView implements WebOperationView {
    private int totalPicNum = Integer.MAX_VALUE;

    private int getTotalPicNum(Document doc) {
        String picNum = doc.title().split("]")[0].split("/")[1];
        return picNum == null ? 0 : Integer.valueOf(picNum);
    }

    @Override
    public String[] getPicUrl(String firstPicUrl, int pageNum) throws Exception {
        String currentPage = Util.getCommonPageUrl(firstPicUrl, pageNum);
        Document doc = Util.getDocument(currentPage);
        if (pageNum == 2) {
            totalPicNum = getTotalPicNum(doc);
        }
        if (totalPicNum < pageNum) {
            return null;
        }
        return new String[]{doc.selectFirst("div.xq_cont img").attr("src").trim()};
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    public static final Parcelable.Creator<ZhuotuView> CREATOR = new Creator<ZhuotuView>() {
        @Override
        public ZhuotuView createFromParcel(Parcel source) {
            return new ZhuotuView();
        }

        @Override
        public ZhuotuView[] newArray(int size) {
            return new ZhuotuView[size];
        }
    };
}
