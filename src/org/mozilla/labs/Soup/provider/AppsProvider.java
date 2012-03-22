/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mozilla.labs.Soup.provider;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mozilla.labs.Soup.provider.AppsContract.App;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;

/**
 * Provides access to a database of apps.
 */
public class AppsProvider extends ContentProvider {

    private static final String TAG = "AppsProvider";

    private static final String DATABASE_NAME = "apps.db";

    private static final int DATABASE_VERSION = 7;

    private static final String APPS_TABLE_NAME = "apps";

    private static HashMap<String, String> sAppsProjectionMap;

    private static HashMap<String, String> sLiveFolderProjectionMap;

    private static final int APPS = 1;

    private static final int APP_ID = 2;

    private static final int LIVE_FOLDER_APPS = 3;

    private static final String SQL_CREATE = "CREATE TABLE " + APPS_TABLE_NAME + " (" + App._ID
            + " INTEGER PRIMARY KEY," + App.ORIGIN + " TEXT," + App.MANIFEST + " BLOB,"
            + App.MANIFEST_URL + " TEXT," + App.NAME + " TEXT," + App.DESCRIPTION + " TEXT,"
            + App.LAUNCH_PATH + " TEXT," + App.ICON + " BLOB," + App.INSTALL_DATA + " BLOB,"
            + App.INSTALL_ORIGIN + " TEXT," + App.INSTALL_TIME + " INTEGER," + App.INSTALL_STATUS
            + " INTEGER," + App.RECEIPT + " BLOB," + App.MANIFEST_DATE + " INTEGER,"
            + App.SYNC_STATUS + " INTEGER," + App.SYNC_DATE + " INTEGER," + App.INSTALL_LOCAL_DATE
            + " INTEGER," + App.VERIFIED_DATE + " INTEGER," + App.LAUNCHED_DATE + " INTEGER,"
            + App.CREATED_DATE + " INTEGER," + App.MODIFIED_DATE + " INTEGER" + ");";

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG + ".DatabaseHelper", "onCreate: " + SQL_CREATE);

            db.execSQL(SQL_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG + ".DatabaseHelper", "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");

            db.execSQL("DROP TABLE IF EXISTS apps");
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");

        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(APPS_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
            case APPS:
                qb.setProjectionMap(sAppsProjectionMap);
                break;

            case APP_ID:
                qb.setProjectionMap(sAppsProjectionMap);
                qb.appendWhere(App._ID + "=" + uri.getPathSegments().get(1));
                break;

            case LIVE_FOLDER_APPS:
                qb.setProjectionMap(sLiveFolderProjectionMap);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = AppsContract.App.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data
        // changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case APPS:
            case LIVE_FOLDER_APPS:
                return App.CONTENT_TYPE;

            case APP_ID:
                return App.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != APPS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set

        if (!values.containsKey(AppsContract.App.MANIFEST)) {
            values.put(AppsContract.App.MANIFEST, new JSONObject().toString());
        }
        if (!values.containsKey(AppsContract.App.MANIFEST_URL)) {
            values.put(AppsContract.App.MANIFEST_URL, "");
        }


        if (!values.containsKey(AppsContract.App.NAME)) {
            Resources r = Resources.getSystem();
            values.put(AppsContract.App.NAME, r.getString(android.R.string.untitled));
        }
        if (!values.containsKey(AppsContract.App.DESCRIPTION)) {
            values.put(AppsContract.App.DESCRIPTION, "");
        }

        if (!values.containsKey(AppsContract.App.INSTALL_TIME)) {
            values.put(AppsContract.App.INSTALL_TIME, 0);
        }

        if (!values.containsKey(AppsContract.App.INSTALL_DATA)) {
            values.put(AppsContract.App.INSTALL_DATA, new JSONObject().toString());
        }
        if (!values.containsKey(AppsContract.App.RECEIPT)) {
            values.put(AppsContract.App.RECEIPT, new JSONArray().toString());
        }

        if (!values.containsKey(AppsContract.App.CREATED_DATE)) {
            values.put(AppsContract.App.CREATED_DATE, now);
        }
        if (!values.containsKey(AppsContract.App.MODIFIED_DATE)) {
            values.put(AppsContract.App.MODIFIED_DATE, now);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(APPS_TABLE_NAME, null, values);

        if (rowId > 0) {
            Uri appUri = ContentUris.withAppendedId(AppsContract.App.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(appUri, null);
            return appUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case APPS:
                count = db.delete(APPS_TABLE_NAME, where, whereArgs);
                break;

            case APP_ID:
                String appId = uri.getPathSegments().get(1);
                count = db.delete(APPS_TABLE_NAME,
                        App._ID + "=" + appId
                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
                        whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;

        Log.d(TAG, "Update " + values.toString());

        switch (sUriMatcher.match(uri)) {
            case APPS:
                count = db.update(APPS_TABLE_NAME, values, where, whereArgs);
                break;

            case APP_ID:
                String appId = uri.getPathSegments().get(1);

                // values.put(AppsContract.Apps.MODIFIED_DATE,
                // Long.valueOf(System.currentTimeMillis()));

                count = db.update(APPS_TABLE_NAME, values,
                        App._ID + "=" + appId
                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
                        whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(App.AUTHORITY, "apps", APPS);
        sUriMatcher.addURI(App.AUTHORITY, "apps/#", APP_ID);
        sUriMatcher.addURI(App.AUTHORITY, "live_folders/apps", LIVE_FOLDER_APPS);

        sAppsProjectionMap = new HashMap<String, String>();
        sAppsProjectionMap.put(App._ID, App._ID);
        sAppsProjectionMap.put(App.ORIGIN, App.ORIGIN);
        sAppsProjectionMap.put(App.MANIFEST, App.MANIFEST);
        sAppsProjectionMap.put(App.MANIFEST_URL, App.MANIFEST_URL);
        sAppsProjectionMap.put(App.NAME, App.NAME);
        sAppsProjectionMap.put(App.DESCRIPTION, App.DESCRIPTION);
        sAppsProjectionMap.put(App.LAUNCH_PATH, App.LAUNCH_PATH);
        sAppsProjectionMap.put(App.ICON, App.ICON);
        sAppsProjectionMap.put(App.INSTALL_DATA, App.INSTALL_DATA);
        sAppsProjectionMap.put(App.INSTALL_ORIGIN, App.INSTALL_ORIGIN);
        sAppsProjectionMap.put(App.INSTALL_TIME, App.INSTALL_TIME);
        sAppsProjectionMap.put(App.INSTALL_STATUS, App.INSTALL_STATUS);
        sAppsProjectionMap.put(App.RECEIPT, App.RECEIPT);
        sAppsProjectionMap.put(App.MANIFEST_DATE, App.MANIFEST_DATE);
        sAppsProjectionMap.put(App.SYNC_STATUS, App.SYNC_STATUS);
        sAppsProjectionMap.put(App.SYNC_DATE, App.SYNC_DATE);
        sAppsProjectionMap.put(App.INSTALL_LOCAL_DATE, App.INSTALL_LOCAL_DATE);
        sAppsProjectionMap.put(App.VERIFIED_DATE, App.VERIFIED_DATE);
        sAppsProjectionMap.put(App.LAUNCHED_DATE, App.LAUNCHED_DATE);
        sAppsProjectionMap.put(App.CREATED_DATE, App.CREATED_DATE);
        sAppsProjectionMap.put(App.MODIFIED_DATE, App.MODIFIED_DATE);

        // Support for Live Folders.
        sLiveFolderProjectionMap = new HashMap<String, String>();
        sLiveFolderProjectionMap.put(LiveFolders._ID, App._ID + " AS " + LiveFolders._ID);
        sLiveFolderProjectionMap.put(LiveFolders.NAME, App.NAME + " AS " + LiveFolders.NAME);
        sLiveFolderProjectionMap.put(LiveFolders.DESCRIPTION, App.DESCRIPTION + " AS "
                + LiveFolders.DESCRIPTION);
        sLiveFolderProjectionMap.put(LiveFolders.ICON_BITMAP, App.ICON + " AS "
                + LiveFolders.ICON_BITMAP);

        // Add more columns here for more robust Live Folders.
    }
}
