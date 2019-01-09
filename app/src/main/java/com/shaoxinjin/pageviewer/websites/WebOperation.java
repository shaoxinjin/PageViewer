package com.shaoxinjin.pageviewer.websites;

public interface WebOperation {
    void updatePage();
    void searchPage(String s);

    WebOperationView getViewWebOperation();
}
