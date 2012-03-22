
package org.mozilla.labs.Soup.service;

import org.mozilla.labs.Soup.app.LauncherActivity;

import android.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class SyncService extends IntentService {

    private static final String TAG = "SyncService";

    public static final String EXTRA_STATUS_RECEIVER = "org.mozilla.labs.soup.extra.STATUS_RECEIVER";

    public static final int STATUS_RUNNING = 0x1;

    public static final int STATUS_ERROR = 0x2;

    public static final int STATUS_FINISHED = 0x3;

    public static final String EXTRA_STATUS_INSTALLED = "org.mozilla.labs.soup.extra.STATUS_INSTALLED";

    public static final String EXTRA_STATUS_UPDATED = "org.mozilla.labs.soup.extra.STATUS_UPDATED";

    public static final String EXTRA_STATUS_UPLOADED = "org.mozilla.labs.soup.extra.STATUS_UPLOADED";

    private ContentResolver resolver;

    private NotificationManager mNM;

    private int NOTIFY_ID = 1001;

    public SyncService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        resolver = getContentResolver();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent " + intent);

        // showNotification();

        final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_STATUS_RECEIVER);

        if (receiver != null) {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
        }

        int uploaded = 0;
        int updated = 0;
        int installed = 0;

        // Disable sync until new spec is up
        // final Context ctx = this;
        // final SharedPreferences prefs =
        // PreferenceManager.getDefaultSharedPreferences(ctx);
        // final long localSince = prefs.getLong("sync_since", 0);
        //
        // Log.d(TAG, "Sync since " + localSince);
        //
        // try {
        //
        // if (!HttpFactory.authorize(this)) {
        // throw new Exception("Current user is not authorized.");
        // }
        //
        // // Local list
        //
        // Cursor cur = resolver.query(App.CONTENT_URI, App.APP_PROJECTION,
        // null, null,
        // App.DEFAULT_SORT_ORDER);
        //
        // cur.moveToFirst();
        //
        // HashMap<String, JSONObject> localList = new HashMap<String,
        // JSONObject>();
        //
        // while (cur.isAfterLast() == false) {
        // JSONObject app = App.toJSONObject(cur, true);
        //
        // if (app != null) {
        // localList.put(app.optString("origin"), app);
        // }
        //
        // cur.moveToNext();
        // }
        //
        // cur.close();
        //
        // // Server list
        //
        // JSONObject response = HttpFactory.getAllApps(this, localSince);
        //
        // Log.d(TAG, "List: " + response);
        //
        // if (response == null) {
        // throw new Exception("Empty server response");
        // }
        //
        // JSONArray responseList = response.optJSONArray("applications");
        // if (responseList == null) {
        // responseList = new JSONArray();
        // }
        //
        // // TODO: Handle incomplete
        //
        // long until = response.optLong("until");
        // if (until == 0) {
        // until = localSince;
        // }
        //
        // HashMap<String, JSONObject> serverList = new HashMap<String,
        // JSONObject>();
        //
        // for (int i = 0, l = responseList.length(); i < l; i++) {
        // JSONObject app = responseList.getJSONObject(i);
        //
        // serverList.put(app.optString("origin"), app);
        // }
        //
        // // Sync the 2 lists
        //
        // HashMap<String, JSONObject> toServerList = new HashMap<String,
        // JSONObject>();
        // HashMap<String, JSONObject> toLocalList = new HashMap<String,
        // JSONObject>();
        //
        // for (HashMap.Entry<String, JSONObject> entry : serverList.entrySet())
        // {
        // String origin = entry.getKey();
        // JSONObject serverValue = entry.getValue();
        //
        // if (localList.containsKey(origin)) {
        // JSONObject localValue = localList.get(origin);
        //
        // long localDate = localValue.optLong("last_modified");
        // long serverDate = serverValue.optLong("last_modified");
        //
        // if (localDate > serverDate) {
        // Log.d(TAG, "to server: " + origin + ", " + localDate + " > " +
        // serverDate);
        // toServerList.put(origin, localValue);
        // } else if (localDate < serverDate) {
        // Log.d(TAG, "to local: " + origin + ", " + localDate + " < " +
        // serverDate);
        // toLocalList.put(origin, serverValue);
        // }
        // } else {
        // Log.d(TAG, "to local: " + origin);
        // toLocalList.put(origin, serverValue);
        // }
        // }
        //
        // for (HashMap.Entry<String, JSONObject> entry : localList.entrySet())
        // {
        // String origin = entry.getKey();
        // JSONObject localValue = entry.getValue();
        //
        // long localDate = localValue.optLong("last_modified");
        //
        // if (!serverList.containsKey(origin) && localDate > localSince) {
        // Log.d(TAG, "to server: " + origin + ", " + localDate + " > " +
        // localSince);
        // toServerList.put(origin, localValue);
        // }
        // }
        //
        // // Iterate sync result
        //
        // // Update server values
        //
        // JSONArray serverUpdates = new JSONArray();
        //
        // for (HashMap.Entry<String, JSONObject> entry :
        // toServerList.entrySet()) {
        // JSONObject localValue = entry.getValue();
        //
        // serverUpdates.put(localValue);
        // uploaded++;
        // }
        //
        // long updatedUntil = until;
        //
        // if (serverUpdates.length() > 0) {
        // updatedUntil = HttpFactory.updateApps(ctx, serverUpdates, until);
        //
        // if (updatedUntil < 1) {
        // throw new Exception("Update failed for " + serverUpdates);
        // }
        // }
        //
        // for (HashMap.Entry<String, JSONObject> entry :
        // toServerList.entrySet()) {
        // String origin = entry.getKey();
        //
        // App app = App.findAppByOrigin(this, origin, false);
        //
        // ContentValues values = new ContentValues();
        // values.put(App.MODIFIED_DATE, updatedUntil);
        //
        // Uri appUri = ContentUris.withAppendedId(App.CONTENT_URI, app.id);
        // getContentResolver().update(appUri, values, null, null);
        // }
        //
        // // Update local values
        //
        // for (HashMap.Entry<String, JSONObject> entry :
        // toLocalList.entrySet()) {
        // String origin = entry.getKey();
        // JSONObject serverValue = entry.getValue();
        //
        // App app = App.findAppByOrigin(this, origin, false);
        //
        // ContentValues values = app.toContentValues(this);
        //
        // // TODO: Set better updatedUntil (get latest date from sync
        // // server)
        // values.put(App.MODIFIED_DATE, updatedUntil);
        //
        // if (app == null) {
        // installed++;
        //
        // getContentResolver().insert(App.CONTENT_URI, values);
        // } else {
        // updated++;
        // Uri appUri = ContentUris.withAppendedId(App.CONTENT_URI, app.id);
        //
        // getContentResolver().update(appUri, values, null, null);
        // }
        //
        // }
        //
        // prefs.edit().putLong("sync_since", updatedUntil).commit();
        //
        // Log.d(TAG, "Sync until " + updatedUntil);
        //
        // // Visual feedback
        //
        // // hideNotification();
        //
        // } catch (Exception e) {
        // Log.w(TAG, "Sync did not happen", e);
        //
        // // hideNotification();
        //
        // if (receiver != null) {
        // // Pass back error to surface listener
        // final Bundle bundle = new Bundle();
        // bundle.putString(Intent.EXTRA_TEXT, e.toString());
        // receiver.send(STATUS_ERROR, bundle);
        // }
        // }

        // Announce success to any surface listener
        if (receiver != null) {
            final Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_STATUS_UPLOADED, uploaded);
            bundle.putInt(EXTRA_STATUS_INSTALLED, installed);
            bundle.putInt(EXTRA_STATUS_UPDATED, updated);

            receiver.send(STATUS_FINISHED, bundle);
        }
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        if (mNM == null) {
            return;
        }

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.stat_notify_sync, "Syncing Apps",
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this
        // notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                LauncherActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this,
                getText(org.mozilla.labs.Soup.R.string.app_name_launcher), "Synchronizing apps",
                contentIntent);

        // Send the notification.
        // We use a string id because it is a unique number. We use it later to
        // cancel.
        mNM.notify(TAG, NOTIFY_ID, notification);
    }

    private void hideNotification() {
        if (mNM == null) {
            return;
        }

        mNM.cancel(TAG, NOTIFY_ID);
    }

}
