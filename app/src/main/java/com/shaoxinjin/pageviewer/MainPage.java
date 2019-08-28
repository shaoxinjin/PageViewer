package com.shaoxinjin.pageviewer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.shaoxinjin.pageviewer.db.DbManager;
import com.shaoxinjin.pageviewer.websites.Star;
import com.shaoxinjin.pageviewer.websites.WebOperation;
import com.shaoxinjin.pageviewer.websites.WebOperationView;
import com.shaoxinjin.pageviewer.websites.Wuzhi.Wuzhi;
import com.shaoxinjin.pageviewer.websites.mhxxoo.Mhxxoo;
import com.shaoxinjin.pageviewer.websites.semanhua.Semanhua;
import com.shaoxinjin.pageviewer.websites.xixi.Xixi;
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

    private RecyclerViewAdapter mRecyclerViewAdapter;
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
                updateSearchPercentage(0);
                mRecyclerViewAdapter.notifyDataSetChanged();
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if (mCurrentID == item.getItemId()) {
            return true;
        }
        inSearch = false;
        mList.clear();
        mRecyclerViewAdapter.notifyDataSetChanged();
        updateSearchPercentage(0);

        mCurrentID = item.getItemId();

        Log.d(TAG, "current id is " + mCurrentID);
        mWebOperation = webOperationMap.get(getStringById(mCurrentID));
        if (mWebOperation != null) {
            mWebOperation.updatePage();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isNotStarPage() {
        return mCurrentID != R.id.nav_star;
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
            case R.id.nav_xixi:
                return Xixi.class.getSimpleName();
            case R.id.nav_wuzhi:
                return Wuzhi.class.getSimpleName();
        }
        return "";
    }

    private void initWebOperation() {
        webOperationMap = new HashMap<>();
        webOperationMap.put(Star.class.getSimpleName(), new Star(MainPage.this, mThreadPoolExecutor));
        webOperationMap.put(Mhxxoo.class.getSimpleName(), new Mhxxoo(MainPage.this, mThreadPoolExecutor));
        webOperationMap.put(Semanhua.class.getSimpleName(), new Semanhua(MainPage.this, mThreadPoolExecutor));
        webOperationMap.put(Zhuotu.class.getSimpleName(), new Zhuotu(MainPage.this, mThreadPoolExecutor));
        webOperationMap.put(Xixi.class.getSimpleName(), new Xixi(MainPage.this, mThreadPoolExecutor));
        webOperationMap.put(Wuzhi.class.getSimpleName(), new Wuzhi(MainPage.this, mThreadPoolExecutor));
    }

    private void initSearchView(MenuItem menuItem) {
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (mWebOperation != null && isNotStarPage()) {
                    inSearch = true;
                    mList.clear();
                    mRecyclerViewAdapter.notifyDataSetChanged();
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
        RecyclerView recyclerView = findViewById(R.id.content_grid_view);
        mRecyclerViewAdapter = new RecyclerViewAdapter(MainPage.this);
        recyclerView.setAdapter(mRecyclerViewAdapter);
        int column = getResources().getInteger(R.integer.grid_columns);
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(column, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (!inSearch && isNotStarPage() && mWebOperation != null) {
                    Log.d(TAG, "onScrollStateChanged");
                    if (!recyclerView.canScrollVertically(-1)) {
                        Log.d(TAG, "onScrollStateChanged updatePage");
                        mWebOperation.updatePage();
                    }
                }
            }
        });
        registerForContextMenu(recyclerView);
        if (mThreadPoolExecutor == null) {
            mThreadPoolExecutor = new ThreadPoolExecutor(50, 200, 5,
                    TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1024));
        }
    }

    private void deleteStarSet(int index) {
        DbManager dbManager = DbManager.getInstance(this);
        dbManager.deleteRecord(mList.get(index).get(URL_KEY));

        mList.remove(mList.get(index));
        mRecyclerViewAdapter.notifyDataSetChanged();

        Toast toast = Toast.makeText(this, getResources().getString(R.string.delete_star_success), Toast.LENGTH_SHORT);
        toast.show();
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {
        private Context mAdapterContext;

        private RecyclerViewAdapter(Context context) {
            mAdapterContext = context;
            setHasStableIds(true);
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mAdapterContext).inflate(R.layout.grid_item, parent, false);
            return new RecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerViewHolder viewHolder, int position) {
            HashMap<String, String> map = mList.get(position);
            if (map == null) {
                Log.d(TAG, "map is null");
                return;
            }
            String imageData = map.get(IMAGE_KEY);
            String textData = map.get(TEXT_KEY);

            if (imageData != null && textData != null) {
                Util.setPicFromUrl(MainPage.this, imageData, viewHolder.imageView);
                viewHolder.textView.setText(textData);
            }

            setOnClickListener(viewHolder, position);
            setMenuListener(viewHolder, position);
        }

        private void setOnClickListener(RecyclerViewHolder viewHolder, final int pos) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, String> map = mList.get(pos);
                    if (map == null) {
                        return;
                    }
                    WebOperation webOperation = webOperationMap.get(mList.get(pos).get(TYPE_KEY));
                    if (webOperation == null) {
                        return;
                    }
                    WebOperationView webOperationView= webOperation.getViewWebOperation();
                    Intent intent = new Intent(MainPage.this, ViewPage.class);
                    intent.putExtra(TYPE_KEY, getStringById(mCurrentID));
                    intent.putExtra(TEXT_KEY, mList.get(pos).get(TEXT_KEY));
                    intent.putExtra(URL_KEY, mList.get(pos).get(URL_KEY));
                    intent.putExtra(CLASS_KEY, webOperationView);
                    startActivity(intent);
                }
            });
        }

        private void setMenuListener(RecyclerViewHolder viewHolder, final int pos) {
            viewHolder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    if (isNotStarPage()) {
                        return;
                    }
                    getMenuInflater().inflate(R.menu.mainpage_menu, menu);
                    for (int index = 0; index < menu.size(); index++) {
                        MenuItem item = menu.getItem(index);
                        if (item.getItemId() == R.id.mainpage_delete_set) {
                            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    deleteStarSet(pos);
                                    return false;
                                }
                            });
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class RecyclerViewHolder extends RecyclerView.ViewHolder {
            private View itemView;
            private ImageView imageView;
            private TextView textView;

            RecyclerViewHolder(View view) {
                super(view);
                itemView = view;
                imageView = view.findViewById(R.id.image_item);
                textView = view.findViewById(R.id.text_item);
            }
        }
    }

    public void updateGridView(final HashMap<String, String> map) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mList.add(map);
                mRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    public void updateSearchPercentage(int percentage) {
        ProgressBar progressBar = findViewById(R.id.search_progress);
        progressBar.setProgress(percentage);
    }

    public boolean getInSearchStatus() {
        return inSearch;
    }
}
