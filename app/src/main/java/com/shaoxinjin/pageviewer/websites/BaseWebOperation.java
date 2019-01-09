package com.shaoxinjin.pageviewer.websites;

import android.util.Log;

import com.shaoxinjin.pageviewer.MainPage;
import com.shaoxinjin.pageviewer.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class BaseWebOperation implements WebOperation {
    private static final String TAG = Util.PREFIX + BaseWebOperation.class.getSimpleName();
    protected String URL_BASE;
    protected String URL_END;
    protected SectionInfo mSectionInfo[];
    protected WebOperationView mWebOperationView;
    private MainPage mMainPage;
    private ThreadPoolExecutor mThreadPoolExecutor;

    public BaseWebOperation(MainPage mainPage, ThreadPoolExecutor threadPoolExecutor) {
        mMainPage = mainPage;
        mThreadPoolExecutor = threadPoolExecutor;
        initWebInfo();
    }

    @Override
    public void updatePage() {
        for (SectionInfo sectionInfo : mSectionInfo) {
            updateSection(sectionInfo);
        }
    }

    @Override
    public void searchPage(String s) {
        for (SectionInfo sectionInfo : mSectionInfo) {
            searchSection(s, sectionInfo);
        }
    }

    @Override
    public WebOperationView getViewWebOperation() {
        return null;
    }

    protected class SectionInfo {
        String mUrlMid;
        int mCurrentPageNum;
        int mTotalPageNum;

        public SectionInfo(String urlMid, int currentPageNum, int totalPageNum) {
            mUrlMid = urlMid;
            mCurrentPageNum = currentPageNum;
            mTotalPageNum = totalPageNum;
        }
    }

    private void updateSection(SectionInfo sectionInfo) {
        String url = getUrlForPage(sectionInfo);
        Log.d(TAG, "updateSection " + sectionInfo.mCurrentPageNum + " " + url);
        if (sectionInfo.mCurrentPageNum == 1) {
            updateSectionPageInfo(url, sectionInfo);
        }
        updateSectionPage(url, sectionInfo);
        sectionInfo.mCurrentPageNum++;
    }

    private void updateSectionPageInfo(final String url, final SectionInfo sectionInfo) {
        mThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    sectionInfo.mTotalPageNum = getTotalPageNum(url);
                    Log.d(TAG, "total num is " + sectionInfo.mTotalPageNum + " type is " + sectionInfo.mUrlMid);
                } catch (Exception e) {
                    Log.d(TAG, "exception in updateSectionPageInfo is " + e.getClass());
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateSectionPage(final String url, final SectionInfo sectionInfo) {
        mThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (sectionInfo.mCurrentPageNum <= 3 || sectionInfo.mCurrentPageNum <= sectionInfo.mTotalPageNum) {
                        ArrayList<HashMap<String, String>> list = new ArrayList<>();
                        setListFromHtmlTable(url, list, "");
                        if (list.size() > 0) {
                            mMainPage.updateGridView(list);
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "exception in updateSectionPage is " + e.getClass());
                    e.printStackTrace();
                }
            }
        });
    }

    private void searchSection(String s, SectionInfo sectionInfo) {
        searchSectionPage(s, sectionInfo);
    }

    private void searchSectionPage(final String s, final SectionInfo sectionInfo) {
        for (int pageNum = 1; pageNum <= sectionInfo.mTotalPageNum; pageNum++) {
            final int tempPageNum = pageNum;
            mThreadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (mMainPage.getInSearchStatus()) {
                        try {
                            String url = URL_BASE + sectionInfo.mUrlMid + String.valueOf(tempPageNum) + URL_END;
                            ArrayList<HashMap<String, String>> list = new ArrayList<>();
                            setListFromHtmlTable(url, list, s);
                            if (list.size() > 0) {
                                mMainPage.updateGridView(list);
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "exception in searchSectionPage is " + e.getClass());
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void initWebInfo() {
    }

    public int getTotalPageNum(String url) throws Exception {
        return 0;
    }

    public String getUrlForPage(SectionInfo sectionInfo) {
        return URL_BASE + sectionInfo.mUrlMid + String.valueOf(sectionInfo.mCurrentPageNum) + URL_END;
    }

    public void setListFromHtmlTable(String url, ArrayList<HashMap<String, String>> list, String s) throws Exception {
    }
}
