package com.shaoxinjin.pageviewer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shaoxinjin.pageviewer.db.DbManager;
import com.shaoxinjin.pageviewer.websites.Star;
import com.shaoxinjin.pageviewer.websites.WebOperation;
import com.shaoxinjin.pageviewer.websites.WebOperationView;
import com.shaoxinjin.pageviewer.websites.mhxxoo.Mhxxoo;
import com.shaoxinjin.pageviewer.websites.semanhua.Semanhua;
import com.shaoxinjin.pageviewer.websites.zhuotu.Zhuotu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainPage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = Util.PREFIX + MainPage.class.getSimpleName();
    public static final String IMAGE_KEY = "image_key";
    public static final String TEXT_KEY = "text_key";
    public static final String URL_KEY = "url_key";

    public static final String TYPE_KEY = "type_key";
    public static final String CLASS_KEY = "class_key";

    private GridViewAdapter mGridViewAdapter;
    private ArrayList<HashMap<String, String>> mList = new ArrayList<>();
    private int mCurrentID;
    private WebOperation mWebOperation;
    private HashMap<String, WebOperation> webOperationMap;
    private static ThreadPoolExecutor mThreadPoolExecutor;
    private boolean inSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        int REQUEST_CODE_CONTACT = 101;
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (inSearch) {
            if (mWebOperation != null) {
                inSearch = false;
                mList.clear();
                mGridViewAdapter.notifyDataSetChanged();
                mWebOperation.updatePage();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_page, menu);
        initSearchView(menu.findItem(R.id.action_search));
        initGridView();
        initWebOperation();
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        if (mCurrentID == item.getItemId()) {
            return true;
        }
        inSearch = false;
        mList.clear();
        mGridViewAdapter.notifyDataSetChanged();

        mCurrentID = item.getItemId();

        Log.d(TAG, "current id is " + mCurrentID);
        mWebOperation = webOperationMap.get(getStringById(mCurrentID));
        mWebOperation.updatePage();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isStarPage() {
        return mCurrentID == R.id.nav_star;
    }

    private String getStringById(int id) {
        switch (id) {
            case R.id.nav_star:
                return Star.class.getSimpleName();
            case R.id.nav_mhxxoo:
                return Mhxxoo.class.getSimpleName();
            case R.id.nav_semanhua:
                return Semanhua.class.getSimpleName();
            case R.id.nav_zhuotu:
                return Zhuotu.class.getSimpleName();
        }
        return "";
    }

    private void initWebOperation() {
        webOperationMap = new HashMap<>();
        webOperationMap.put(Star.class.getSimpleName(), new Star(MainPage.this, mThreadPoolExecutor));
        webOperationMap.put(Mhxxoo.class.getSimpleName(), new Mhxxoo(MainPage.this, mThreadPoolExecutor));
        webOperationMap.put(Semanhua.class.getSimpleName(), new Semanhua(MainPage.this, mThreadPoolExecutor));
        webOperationMap.put(Zhuotu.class.getSimpleName(), new Zhuotu(MainPage.this, mThreadPoolExecutor));
    }

    private void initSearchView(MenuItem menuItem) {
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (mWebOperation != null && !isStarPage()) {
                    inSearch = true;
                    mList.clear();
                    mGridViewAdapter.notifyDataSetChanged();
                    mWebOperation.searchPage(s);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void initGridView() {
        AutoGridView mGridView = findViewById(R.id.content_grid_view);
        mGridViewAdapter = new GridViewAdapter(MainPage.this);
        mGridView.setAdapter(mGridViewAdapter);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (!inSearch && !isStarPage()) {
                    if (view.getLastVisiblePosition() == view.getCount() - 1) {
                        mWebOperation.updatePage();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = mList.get(position);
                if (map != null) {
                    Intent intent = new Intent(MainPage.this, ViewPage.class);
                    intent.putExtra(TYPE_KEY, getStringById(mCurrentID));
                    intent.putExtra(TEXT_KEY, mList.get(position).get(TEXT_KEY));
                    intent.putExtra(URL_KEY, mList.get(position).get(URL_KEY));
                    WebOperationView webOperationView;
                    webOperationView = webOperationMap.get(mList.get(position).get(TYPE_KEY)).getViewWebOperation();
                    intent.putExtra(CLASS_KEY, webOperationView);
                    startActivity(intent);
                }
            }
        });
        registerForContextMenu(mGridView);
        mGridView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                if (isStarPage()) {
                    getMenuInflater().inflate(R.menu.mainpage_menu, menu);
                }
            }
        });
        if (mThreadPoolExecutor == null) {
            mThreadPoolExecutor = new ThreadPoolExecutor(50, 200, 5,
                    TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1024));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        Log.d(TAG, "menuItem is " + menuItemIndex);
        switch (menuItemIndex) {
            case R.id.mainpage_delete_set:
                deleteStarSet(info.position);
                break;
        }
        return true;
    }

    private void deleteStarSet(int index) {
        DbManager dbManager = DbManager.getInstance(this);
        dbManager.deleteRecord(mList.get(index).get(URL_KEY));

        mList.remove(mList.get(index));
        mGridViewAdapter.notifyDataSetChanged();

        Toast toast = Toast.makeText(this, getResources().getString(R.string.delete_star_success), Toast.LENGTH_SHORT);
        toast.show();
    }

    class GridViewAdapter extends BaseAdapter {
        private Context mAdapterContext;

        private GridViewAdapter(Context context) {
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
            GridViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new GridViewHolder();
                convertView = LayoutInflater.from(mAdapterContext).inflate(R.layout.grid_item, null);
                viewHolder.imageView = convertView.findViewById(R.id.image_item);
                viewHolder.textView = convertView.findViewById(R.id.text_item);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (GridViewHolder) convertView.getTag();
            }

            HashMap<String, String> map = mList.get(position);
            if (map != null) {
                String imageData = map.get(IMAGE_KEY);
                String textData = map.get(TEXT_KEY);

                Util.setPicFromUrl(MainPage.this, imageData, viewHolder.imageView);
                viewHolder.textView.setText(textData);
            }

            return convertView;
        }

        class GridViewHolder {
            private ImageView imageView;
            private TextView textView;
        }
    }

    public void updateGridView(final ArrayList<HashMap<String, String>> list) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mList.addAll(list);
                mGridViewAdapter.notifyDataSetChanged();
            }
        });
    }

    public boolean getInSearchStatus() {
        return inSearch;
    }
}
