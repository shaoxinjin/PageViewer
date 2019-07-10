package com.shaoxinjin.pageviewer.websites;

import com.shaoxinjin.pageviewer.MainPage;
import com.shaoxinjin.pageviewer.db.DbManager;
import com.shaoxinjin.pageviewer.db.DbStarRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class Star implements WebOperation {
    private MainPage mMainPage;

    public Star(MainPage mainPage, ThreadPoolExecutor threadPoolExecutor) {
        mMainPage = mainPage;
    }

    @Override
    public void updatePage() {
        DbManager dbManager = DbManager.getInstance(mMainPage);
        ArrayList<DbStarRecord> list = dbManager.queryRecords();

        for (DbStarRecord record : list) {
            HashMap<String, String> map = new HashMap<>();
            map.put(MainPage.TYPE_KEY, record.type);
            map.put(MainPage.IMAGE_KEY, record.picUrl);
            map.put(MainPage.TEXT_KEY, record.name);
            map.put(MainPage.URL_KEY, record.url);
            mMainPage.updateGridView(map);
        }
    }

    @Override
    public void searchPage(String s) {

    }

    @Override
    public WebOperationView getViewWebOperation() {
        return null;
    }
}