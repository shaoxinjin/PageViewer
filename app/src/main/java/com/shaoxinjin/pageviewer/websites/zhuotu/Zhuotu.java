package com.shaoxinjin.pageviewer.websites.zhuotu;

import com.shaoxinjin.pageviewer.MainPage;
import com.shaoxinjin.pageviewer.Util;
import com.shaoxinjin.pageviewer.websites.BaseWebOperation;
import com.shaoxinjin.pageviewer.websites.WebOperationView;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class Zhuotu extends BaseWebOperation {
    public Zhuotu(MainPage mainPage, ThreadPoolExecutor threadPoolExecutor) {
        super(mainPage, threadPoolExecutor);
    }

    @Override
    public WebOperationView getViewWebOperation() {
        return mWebOperationView;
    }

    @Override
    public void initWebInfo() {
        URL_BASE = "http://m.win4000.com";
        URL_END = ".html";
        mSectionInfo = new SectionInfo[]{
                new SectionInfo("/meitu_2_", 1, 0),
                new SectionInfo("/meitu_3_", 1, 0),
                new SectionInfo("/meitu_4_", 1, 0),
                new SectionInfo("/meitu_29_", 1, 0),
                new SectionInfo("/meitu_31_", 1, 0),
                new SectionInfo("/meitu_32_", 1, 0),
                new SectionInfo("/meitu_47_", 1, 0),
                new SectionInfo("/meitu_59_", 1, 0)};
        mWebOperationView = new ZhuotuView();
    }

    @Override
    public int getTotalPageNum(String url) throws Exception {
        Document doc = Util.getDocument(url);
        Element element = doc.select("div.ym a").last();
        if (element != null) {
            String href = element.attr("href");
            return Integer.valueOf(href.split(".html")[0].split("_")[2]);
        }
        return 0;
    }

    @Override
    public void setListFromHtmlTable(String url, ArrayList<HashMap<String, String>> list, String s) throws Exception {
        Document doc = Util.getDocument(url);
        Elements aTags = doc.select("div.img_cont a");
        for (Element e : aTags) {
            String href = e.attr("href");
            Element img = e.selectFirst("img");
            if (img != null) {
                String imgSrc = img.attr("data-original");
                String name = img.attr("alt");
                if (imgSrc == null || imgSrc.equals("")) {
                    continue;
                }
                HashMap<String, String> map = new HashMap<>();
                map.put(MainPage.IMAGE_KEY, imgSrc);
                map.put(MainPage.TEXT_KEY, Util.getCommonName(name));
                map.put(MainPage.URL_KEY, href);
                map.put(MainPage.TYPE_KEY, Zhuotu.class.getSimpleName());
                if (s.equals("") || map.get(MainPage.TEXT_KEY).contains(s)) {
                    list.add(map);
                }
            }
        }
    }
}
