package com.example.rvcontentproviderwithsql.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.rvcontentproviderwithsql.tablemoc.CustomSQLiteOpenHelper;
import com.example.rvcontentproviderwithsql.tablemoc.TableItems;

public class RequestProvider extends ContentProvider {

    private static final String TAG = "RequestProvider";
    private SQLiteOpenHelper mSqliteOpenHelper;
    private static final UriMatcher sUriMatcher;

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".db";
    public static final int TABLE_ITEMS = 0;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, TableItems.NAME + "/offset/" + "#", TABLE_ITEMS);
    }

    public static Uri urlForItems(int limit){
        return Uri.parse("content://" + AUTHORITY + "/" + TableItems.NAME + "/offset/" + limit);
    }

    @Override
    public boolean onCreate() {
        mSqliteOpenHelper = new CustomSQLiteOpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mSqliteOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder sdb = new SQLiteQueryBuilder();
        Cursor c = null;
        String offset = null;

        switch (sUriMatcher.match(uri)) {
            case TABLE_ITEMS:
                sdb.setTables(TableItems.NAME);
                offset = uri.getLastPathSegment();
                break;
            default:
                break;

        }
        int intOffset = Integer.parseInt(offset);
        String limitArg = intOffset + ", " + 30;
        Log.d(TAG, "query: " + limitArg);
        c = sdb.query(db, projection, selection, selectionArgs, null, null, sortOrder, limitArg);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return BuildConfig.APPLICATION_ID + ".item";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        String table = "";
        switch (sUriMatcher.match(uri)) {
            case TABLE_ITEMS:
                table = TableItems.NAME;
                break;
        }
        long result = mSqliteOpenHelper.getWritableDatabase().insertWithOnConflict(table, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        if (result == -1) {
            throw new SQLException("insert with conflict");
        }

        Uri returnUri = ContentUris.withAppendedId(uri, result);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return -1;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return -1;
    }
}
