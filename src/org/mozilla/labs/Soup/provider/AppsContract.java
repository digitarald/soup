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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.labs.Soup.app.AppActivity;
import org.mozilla.labs.Soup.http.HttpFactory;
import org.mozilla.labs.Soup.http.ImageFactory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Convenience definitions for AppsProvider
 */
public final class AppsContract {

    private static final String TAG = "AppsContract";

    /**
     * Apps table
     */
    public static final class App implements BaseColumns {

        public static final String AUTHORITY = "org.mozilla.labs.Soup.provider.AppsProvider";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/apps");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mozilla.apps";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
         * note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mozilla.apps";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "name ASC";

        public static final String ORIGIN = "origin";

        public static final String MANIFEST = "manifest";

        public static final String MANIFEST_URL = "manifest_url";

        public static final String NAME = "name";

        public static final String DESCRIPTION = "description";

        public static final String LAUNCH_PATH = "launch_path";

        public static final String ICON = "icon";

        public static final String INSTALL_DATA = "install_data";

        public static final String INSTALL_ORIGIN = "install_origin";

        public static final String INSTALL_TIME = "install_date";

        public static final String INSTALL_STATUS = "install_status";

        public static final String RECEIPT = "receipt";

        public static final String MANIFEST_DATE = "manifest_date";

        public static final String SYNC_STATUS = "sync_status";

        public static final String SYNC_DATE = "sync_date";

        public static final String INSTALL_LOCAL_DATE = "install_local_date";

        public static final String VERIFIED_DATE = "verified_date";

        public static final String LAUNCHED_DATE = "launched_date";

        /**
         * The timestamp for when the app was created
         * <P>
         * Type: INTEGER (long)
         * </P>
         */
        public static final String CREATED_DATE = "created_date";

        /**
         * The timestamp for when the note was last modified
         * <P>
         * Type: INTEGER (long)
         * </P>
         */
        public static final String MODIFIED_DATE = "modified_date";

        public static enum STATUS_ENUM {
            OK, DELETED, UNKNOWN
        }

        public Long id = null;

        public String origin = null;

        public JSONObject manifest = new JSONObject();

        public String manifestUrl = null;

        public String name = null;

        public String description = null;

        public String launchPath = null;

        public Bitmap icon = null;

        public JSONObject installData = new JSONObject();

        public String installOrigin = null;

        public Calendar installTime = null;

        public STATUS_ENUM installStatus = STATUS_ENUM.UNKNOWN;

        public JSONArray receipt = new JSONArray();

        public Calendar manifestDate = null;

        public STATUS_ENUM syncStatus = STATUS_ENUM.UNKNOWN;

        public Calendar syncDate = null;

        public Calendar localInstallDate = null;

        public Calendar verifiedDate = null;

        public Calendar launchedDate = null;

        public Calendar createdDate = null;

        public Calendar modifiedDate = null;

        public Calendar installLocalDate = null;

        App() {
        }

        public static App fromCursor(Context ctx, Cursor cur) {

            App app = new App();

            app.id = cur.getLong(cur.getColumnIndex(App._ID));

            app.origin = cur.getString(cur.getColumnIndex(App.ORIGIN));
            try {
                app.manifest = new JSONObject(cur.getString(cur.getColumnIndex(App.MANIFEST)));
            } catch (JSONException e) {
            }
            app.manifestUrl = cur.getString(cur.getColumnIndex(App.MANIFEST_URL));
            app.name = cur.getString(cur.getColumnIndex(App.NAME));
            app.description = cur.getString(cur.getColumnIndex(App.DESCRIPTION));
            app.launchPath = cur.getString(cur.getColumnIndex(App.LAUNCH_PATH));

            // TODO Store Bitmap or just Blob?
            final byte[] imageDate = cur.getBlob(cur.getColumnIndex(App.ICON));

            if (imageDate != null) {
                app.icon = BitmapFactory.decodeByteArray(imageDate, 0, imageDate.length);
            }

            try {
                app.installData = new JSONObject(
                        cur.getString(cur.getColumnIndex(App.INSTALL_DATA)));
            } catch (JSONException e) {
            }

            app.installOrigin = cur.getString(cur.getColumnIndex(App.INSTALL_ORIGIN));

            final int installTime = cur.getInt(cur.getColumnIndex(App.INSTALL_TIME));

            if (installTime > 0) {
                app.installTime = Calendar.getInstance();
                app.installTime.setTimeInMillis(installTime);
            }

            final int installStatus = cur.getInt(cur.getColumnIndex(App.INSTALL_STATUS));
            app.installStatus = App.STATUS_ENUM.values()[installStatus];

            try {
                app.receipt = new JSONArray(cur.getString(cur.getColumnIndex(App.RECEIPT)));
            } catch (JSONException e) {
            }

            final int manifestDate = cur.getInt(cur.getColumnIndex(App.MANIFEST_DATE));

            if (manifestDate > 0) {
                app.manifestDate = Calendar.getInstance();
                app.manifestDate.setTimeInMillis(manifestDate);
            }

            final int syncStatus = cur.getInt(cur.getColumnIndex(App.SYNC_STATUS));
            app.syncStatus = App.STATUS_ENUM.values()[syncStatus];

            final int syncDate = cur.getInt(cur.getColumnIndex(App.SYNC_DATE));

            if (syncDate > 0) {
                app.syncDate = Calendar.getInstance();
                app.syncDate.setTimeInMillis(syncDate);
            }

            final int installLocalDate = cur.getInt(cur.getColumnIndex(App.INSTALL_LOCAL_DATE));

            if (installLocalDate > 0) {
                app.installLocalDate = Calendar.getInstance();
                app.installLocalDate.setTimeInMillis(installLocalDate);
            }

            final int verifiedDate = cur.getInt(cur.getColumnIndex(App.VERIFIED_DATE));

            if (verifiedDate > 0) {
                app.verifiedDate = Calendar.getInstance();
                app.verifiedDate.setTimeInMillis(verifiedDate);
            }

            final int launchedDate = cur.getInt(cur.getColumnIndex(App.LAUNCHED_DATE));

            if (launchedDate > 0) {
                app.launchedDate = Calendar.getInstance();
                app.launchedDate.setTimeInMillis(launchedDate);
            }

            return app;
        }

        public static App fromManifestUri(Context ctx, String manifestUri, JSONObject installData,
                String installUrl) {

            final Uri originUri = Uri.parse(manifestUri);

            if (originUri == null || originUri.getHost() == null) {
                Log.e(TAG, "Faulty originUri: " + originUri);
                return null;
            }

            final Uri installUri = Uri.parse(installUrl);

            if (installUri == null || installUri.getHost() == null) {
                Log.e(TAG, "Faulty installUri: " + installUri);
                return null;
            }

            final String manifestOrigin = originUri.getScheme() + "://" + originUri.getAuthority();

            // Check against existing apps

            App app = App.findAppByOrigin(ctx, manifestOrigin);

            if (app == null) {
                app = new App();
            }

            // TODO: More error codes (JSON vs IO)
            final JSONObject manifest = HttpFactory.getManifest(ctx, manifestUri);

            app.name = manifest.optString("name", originUri.getHost());
            app.description = manifest.optString("description", "");

            app.launchPath = manifestOrigin + manifest.optString("launch_path", "/");

            app.origin = manifestOrigin;
            app.manifestUrl = manifestUri;
            app.manifest = manifest;

            // TODO: Fails for iframes
            final String installOrigin = installUri.getScheme() + "://" + installUri.getAuthority();

            app.installOrigin = installOrigin;

            if (installData != null) {

                app.installData = installData;

                if (installData.has("receipt")) {
                    app.receipt = installData.optJSONArray("receipt");
                }
            }

            app.manifestDate = Calendar.getInstance();

            app.fetchIcon(ctx);

            Log.d(TAG, "Icon set: " + (app.icon != null));

            return app;
        }

        public Intent getIntent(Context ctx) {

            Intent intent = new Intent(ctx, AppActivity.class);
            intent.setAction(AppActivity.ACTION_WEBAPP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.putExtra("origin", origin);
            intent.putExtra("uri", launchPath);

            return intent;

        }

        public void createShortcut(Context ctx) {

            Intent intent = new Intent();
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, getIntent(ctx));
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);

            if (icon != null) {
                intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
            }

            // Disallow the creation of duplicate
            // shortcuts (i.e. same
            // url, same title, but different screen
            // position).
            intent.putExtra("duplicate", false);

            ctx.sendBroadcast(intent);

        }

        public JSONObject toJSON() {
            return toJSON(false);
        }

        public JSONObject toJSON(boolean includeDeleted) {
            boolean deleted = (syncStatus == STATUS_ENUM.DELETED);

            // Skip deleted entries for non-sync use
            if (!includeDeleted && deleted) {
                return null;
            }

            JSONObject app = new JSONObject();

            try {

                app.put("origin", origin);
                app.put("manifest", manifest);
                app.put("manifest_url", manifestUrl);
                app.put("install_data", installData);
                app.put("install_origin", installOrigin);

                if (installTime != null) {
                    app.put("install_time", Long.valueOf(installTime.getTimeInMillis() / 1000));
                } else {

                }

                if (includeDeleted) {
                    app.put("last_modified", Long.valueOf(modifiedDate.getTimeInMillis() / 1000));
                    app.put("deleted", deleted);
                }

            } catch (JSONException e) {
                return null;
            }

            return app;
        }

        public ContentValues toContentValues(Context ctx) {

            ContentValues values = new ContentValues();

            values.put(App.ORIGIN, origin);

            values.put(App.MANIFEST, manifest.toString());
            values.put(App.MANIFEST_URL, manifestUrl);

            values.put(App.NAME, name);
            values.put(App.DESCRIPTION, description);

            values.put(App.LAUNCH_PATH, launchPath);

            if (icon != null) {
                values.put(App.ICON, ImageFactory.bitmapToBytes(icon));
            }

            if (installData != null) {
                values.put(App.INSTALL_DATA, installData.toString());
            } else {
                values.put(App.INSTALL_DATA, new JSONObject().toString());
            }

            values.put(App.INSTALL_ORIGIN, installOrigin);

            if (installTime != null) {
                values.put(App.INSTALL_TIME, installTime.getTimeInMillis());
            }

            values.put(App.INSTALL_STATUS, installStatus.ordinal());

            if (receipt != null) {
                values.put(App.RECEIPT, receipt.toString());
            } else {
                values.put(App.RECEIPT, new JSONArray().toString());
            }

            if (manifestDate != null) {
                values.put(App.MANIFEST_DATE, manifestDate.getTimeInMillis());
            }

            values.put(App.SYNC_STATUS, syncStatus.ordinal());

            if (syncDate != null) {
                values.put(App.SYNC_DATE, syncDate.getTimeInMillis());
            }

            if (localInstallDate != null) {
                values.put(App.INSTALL_LOCAL_DATE, localInstallDate.getTimeInMillis());
            }

            if (verifiedDate != null) {
                values.put(App.VERIFIED_DATE, verifiedDate.getTimeInMillis());
            }

            if (launchedDate != null) {
                values.put(App.LAUNCHED_DATE, launchedDate.getTimeInMillis());
            }

            return values;
        }

        public static App findAppByOrigin(Context ctx, String origin) {

            Cursor cur = ctx.getContentResolver().query(App.CONTENT_URI, null, App.ORIGIN + " = ?",
                    new String[] {
                        origin
                    }, App.DEFAULT_SORT_ORDER);

            if (cur.moveToFirst() == false) {
                cur.close();
                return null;
            }

            App app = App.fromCursor(ctx, cur);

            cur.close();

            return app;
        }

        public static List<App> findAppsByInstallOrigin(Context ctx, String origin) {

            Cursor cur = ctx.getContentResolver().query(App.CONTENT_URI, null,
                    App.INSTALL_ORIGIN + " = ?", new String[] {
                        origin
                    }, App.DEFAULT_SORT_ORDER);

            List<App> list = new ArrayList<App>();

            if (cur.moveToFirst() != false) {

                while (cur.isAfterLast() == false) {
                    App app = App.fromCursor(ctx, cur);

                    if (app != null) {
                        list.add(app);
                    }

                    cur.moveToNext();
                }

            }

            cur.close();

            return list;
        }

        public void fetchIcon(Context ctx) {

            JSONObject icons = manifest.optJSONObject("icons");

            if (icons == null || icons.length() == 0) {
                Log.d(TAG, "fetchIcon: No icons found");
                return;
            }

            JSONArray sizes = icons.names();

            List<Integer> sizesSort = new ArrayList<Integer>();
            for (int i = 0, l = sizes.length(); i < l; i++) {
                sizesSort.add(sizes.optInt(i));
            }
            String max = Collections.max(sizesSort).toString();

            String data = icons.optString(max);

            String iconUrl = icons.optString(max);
            String scheme = Uri.parse(data).getScheme();

            if (scheme == null || !scheme.equals("data")) { // base64 string
                iconUrl = origin + iconUrl;
            }

            Log.d(TAG, "fetchIcon: Fetching icon " + max + ": " + iconUrl);

            int iconSize = (int)(48 * ctx.getResources().getDisplayMetrics().density);

            icon = ImageFactory.getResizedImage(iconUrl, iconSize, iconSize);
        }

        public boolean save(Context ctx) {

            Uri uri = null;

            try {
                if (id != null) {
                    uri = ContentUris.withAppendedId(App.CONTENT_URI, id);
                    ctx.getContentResolver().update(uri, toContentValues(ctx), null, null);
                } else {
                    uri = ctx.getContentResolver().insert(App.CONTENT_URI, toContentValues(ctx));

                    id = ContentUris.parseId(uri);
                }

            } catch (Exception e) {

                Log.e(TAG, "Installation failed for " + origin, e);
                return false;
            }

            return true;
        }

    }

}
