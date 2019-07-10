package com.shaoxinjin.pageviewer.websites.xixi;

import com.shaoxinjin.pageviewer.MainPage;
import com.shaoxinjin.pageviewer.Util;
import com.shaoxinjin.pageviewer.websites.BaseWebOperation;
import com.shaoxinjin.pageviewer.websites.WebOperationView;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class Xixi extends BaseWebOperation {
    public Xixi(MainPage mainPage, ThreadPoolExecutor threadPoolExecutor) {
        super(mainPage, threadPoolExecutor);
    }

    @Override
    public WebOperationView getViewWebOperation() {
        return mWebOperationView;
    }

    @Override
    public void initWebInfo() {
        URL_BASE = "http://m.xmkkzy.net";
        URL_END = ".html";
        mSectionInfo = new SectionInfo[]{
            new SectionInfo("/44rtnet/sy/list_1_", 1, 0),
            new SectionInfo("/44rtnet/xz/list_2_", 1, 0),
            new SectionInfo("/44rtnet/zg/list_3_", 1, 0),
            new SectionInfo("/44rtnet/rb/list_4_", 1, 0),
            new SectionInfo("/44rtnet/ddrtys/list_5_", 1, 0)};
        mWebOperationView = new XixiView();
    }

    @Override
    public int getTotalPageNum(String url) throws Exception {
        Document doc = Util.getDocument(url);
        String pageNum = doc.selectFirst("span.pageinfo strong").text();
        return pageNum == null ? 0 : Integer.valueOf(pageNum);
    }

    @Override
    public void setListFromHtmlTable(String url, String s) throws Exception {
        Document doc = Util.getDocument(url);
        Elements aTags = doc.select("div.libox a");
        for (Element e : aTags) {
            String href = e.attr("href");
            Element img = e.selectFirst("img");
            if (img != null) {
                String name = img.attr("alt");
                HashMap<String, String> map = new HashMap<>();
                map.put(MainPage.TEXT_KEY, Util.getCommonName(name));
                if (s.equals("") && mMainPage.getInSearchStatus()) {
                    return;
                }
                if (!s.equals("") && !map.get(MainPage.TEXT_KEY).contains(s)) {
                    continue;
                }
                String imgSrc = img.attr("src");
                map.put(MainPage.IMAGE_KEY, imgSrc);
                map.put(MainPage.URL_KEY, URL_BASE + href);
                map.put(MainPage.TYPE_KEY, Xixi.class.getSimpleName());
                mMainPage.updateGridView(map);
            }
        }
    }
}
