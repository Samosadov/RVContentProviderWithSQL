package com.example.rvcontentproviderwithsql;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.rvcontentproviderwithsql.contentprovider.RequestProvider;
import com.example.rvcontentproviderwithsql.tablemoc.TableItems;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "";

    public final int offset = 30;
    private int page = 0;

    private RecyclerView mRecyclerView;
    private boolean loadingMore = false;
    private Toast shortToast;
    private Handler handlerToWait = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        CustomCursorReyclerViewAdapter mAdapter = new CustomCursorReyclerViewAdapter(this, null);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        int itemsCountLocal = getItemsCountLocal();
        if (itemsCountLocal == 0) {
            fillTestElements();
        }
        shortToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                int maxPositions = layoutManager.getItemCount();

                if (lastVisibleItemPosition == maxPositions - 1) {
                    if (loadingMore) return;
                    loadingMore = true;
                    page++;
                    getSupportLoaderManager().restartLoader(0, null, MainActivity.this);
                }
            }
        });

        getSupportLoaderManager().restartLoader(0, null, this);
    }

    private int getItemsCountLocal() {
        int itemsCount = 0;

        Cursor query = getContentResolver().query(RequestProvider.urlForItems(0), null, null, null, null);
        if (query != null) {
            itemsCount = query.getCount();
            query.close();
        }
        return itemsCount;
    }

    private void fillTestElements() {
        int size = 1000;
        ContentValues[] cvArray = new ContentValues[size];
        for (int i = 0; i < cvArray.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put(TableItems.TEXT, ("text " + i));
            cvArray[i] = cv;
        }

        getContentResolver().bulkInsert(RequestProvider.urlForItems(0), cvArray);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case 0:
                return new CursorLoader(this, RequestProvider.urlForItems(offset * page), null, null, null, null);
            default:
                throw new IllegalArgumentException("no id handler");
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case 0:
                Log.d(TAG, "onLoadFinished: loading MORE");
                shortToast.setText("loading MORE " + page);;
                shortToast.show();

                Cursor cursor = ((CustomCursorReyclerViewAdapter) mRecyclerView.getAdapter()).getCursor();
                MatrixCursor mx = new MatrixCursor(TableItems.Columns);
                fillMx(cursor, mx);

                fillMx(data, mx);

                ((CustomCursorReyclerViewAdapter) mRecyclerView.getAdapter()).swapCursor(mx);
                handlerToWait.postDelayed(new Runnable() {
                    @Override
                    public void run() { loadingMore = false; }
                }, 2000);
                break;
            default:
                throw new IllegalArgumentException("no loader id handled!");
        }
    }

    @SuppressLint("Range")
    private void fillMx(Cursor data, MatrixCursor mx) {
        if (data == null) return;
        data.moveToPosition(-1);
        while (data.moveToNext()) {
            mx.addRow(new Object[]{
                    data.getString(data.getColumnIndex(TableItems._ID)),
                    data.getString(data.getColumnIndex(TableItems.TEXT))
            });
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}