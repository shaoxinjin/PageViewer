package com.shaoxinjin.pageviewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.shaoxinjin.pageviewer.db.DbManager;
import com.shaoxinjin.pageviewer.websites.WebOperationView;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ViewPage extends AppCompatActivity {
    private static final String TAG = Util.PREFIX + ViewPage.class.getSimpleName();
    private ListViewAdapter mListViewAdapter;
    private static ThreadPoolExecutor mThreadPoolExecutor;
    private ArrayList<String> mList = new ArrayList<>();
    private int mCurrentPage;
    private String mCurrentName;
    private String mCurrentType;
    private String mCurrentUrl;
    private WebOperationView mWebOperationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_page);
        mCurrentPage = 0;
        if (mThreadPoolExecutor == null) {
            mThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 5,
                    TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1024));
        }
        ListView mListView = findViewById(R.id.content_list_view);
        mListViewAdapter = new ListViewAdapter(ViewPage.this);
        mListView.setAdapter(mListViewAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (view.getLastVisiblePosition() == view.getCount() - 1) {
                    updateImage();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        registerForContextMenu(mListView);
        Intent intent = getIntent();
        mCurrentType = intent.getStringExtra(MainPage.TYPE_KEY);
        mCurrentName = intent.getStringExtra(MainPage.TEXT_KEY);
        mCurrentUrl = intent.getStringExtra(MainPage.URL_KEY);
        mWebOperationView = intent.getParcelableExtra(MainPage.CLASS_KEY);
        updateImage();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.content_list_view) {
            getMenuInflater().inflate(R.menu.viewpage_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        Log.d(TAG, "menuItem is " + menuItemIndex);
        switch (menuItemIndex) {
            case R.id.viewpage_download_pic:
                downloadPic(info.position);
                break;
            case R.id.viewpage_star:
                starPicSet(info.position);
                break;
        }
        return true;
    }

    private void downloadPic(int pageNum) {
        String url = (String) mListViewAdapter.getItem(pageNum);
        Util.downloadPic(this, url);
    }

    private void starPicSet(int pageNum) {
        //this.deleteDatabase("PageViewer.db");
        String url = (String) mListViewAdapter.getItem(pageNum);
        DbManager dbManager = DbManager.getInstance(this);
        dbManager.insertRecord(mCurrentType, mCurrentName, mCurrentUrl, url);
        Toast toast = Toast.makeText(this, getResources().getString(R.string.star_success), Toast.LENGTH_SHORT);
        toast.show();
    }

    private void updateImage() {
        updateSomeImages();
    }

    private void updateSomeImages() {
        mThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 1; i <= 3; i++) {
                        mCurrentPage++;
                        ArrayList<String> list = new ArrayList<>();
                        String picUrl = mWebOperationView.getPicUrl(mCurrentUrl, mCurrentPage);
                        if (picUrl == null || picUrl.equals("")) {
                            return;
                        }
                        list.add(picUrl);
                        updateListView(list);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "exception in updateSomeImages is " + e.getClass());
                    e.printStackTrace();
                }
            }
        });
    }

    class ListViewAdapter extends BaseAdapter {
        private Context mAdapterContext;

        private ListViewAdapter(Context context) {
            mAdapterContext = context;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ListViewHolder();
                convertView = LayoutInflater.from(mAdapterContext).inflate(R.layout.list_item, null);
                viewHolder.imageView = convertView.findViewById(R.id.image_item);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ListViewHolder) convertView.getTag();
            }

            String imageData = mList.get(position);
            Util.setPicFromUrl(ViewPage.this, imageData, viewHolder.imageView);

            return convertView;
        }

        class ListViewHolder {
            private ImageView imageView;
        }
    }

    public void updateListView(final ArrayList<String> list) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mList.addAll(list);
                mListViewAdapter.notifyDataSetChanged();
            }
        });
    }
}
