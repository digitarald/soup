
package org.mozilla.labs.Soup.plugins;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.labs.Soup.app.InstallDialog;
import org.mozilla.labs.Soup.provider.AppsContract.App;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;

public class MozAppsPlugin extends Plugin {

    private static final String TAG = "MozAppsPlugin";

    static final int INSTALL_REQUEST = 0;

    private String callbackId;

    /*
     * (non-Javadoc)
     * @see com.phonegap.api.Plugin#execute(java.lang.String,
     * org.json.JSONArray, java.lang.String)
     */
    @Override
    public PluginResult execute(String action, JSONArray data, String callback) {
        Log.d(TAG, "Called " + action + ": " + data);

        String installOrigin = webView.getUrl();

        Uri originUri = Uri.parse(data.optString(0, installOrigin));
        String origin = originUri.getScheme() + "://" + originUri.getAuthority();

        try {

            if (action.equals("install")) {

                return install(callback, originUri.toString(), data.optJSONObject(1), installOrigin);

            } else if (action.equals("getSelf")) {

                App app = App.findAppByOrigin(ctx, origin);

                if (app != null) {

                    JSONObject json = app.toJSON();

                    if (json != null) {
                        return new PluginResult(Status.OK, json);
                    }

                    return new PluginResult(Status.ERROR);
                }

                return new PluginResult(Status.OK);

            } else if (action.equals("getInstalled")) {

                List<App> apps = App.findAppsByInstallOrigin(ctx, origin);
                JSONArray list = new JSONArray();

                for (App app : apps) {

                    JSONObject json = app.toJSON();

                    if (json != null) {
                        list.put(json);
                    }

                }

                return new PluginResult(Status.OK, list);
            }

        } catch (Exception e) {
            Log.w(TAG, action + " failed", e);
            return new PluginResult(Status.JSON_EXCEPTION);
        }

        return new PluginResult(Status.INVALID_ACTION);
    }

    /**
     * Identifies if action to be executed returns a value and should be run
     * synchronously.
     * 
     * @param action The action to execute
     * @return T=returns value
     */
    public boolean isSynch(String action) {
        if (action.equals("install")) {
            return true;
        }

        return false;
    }

    public synchronized PluginResult install(final String callbackId, final String manifestUri,
            final JSONObject install_data, final String installOrigin) throws Exception {

        Intent intent = new Intent(ctx, InstallDialog.class);

        intent.putExtra(InstallDialog.MANIFEST_URI_EXTRA, manifestUri);
        intent.putExtra(InstallDialog.INSTALL_ORIGIN_EXTRA, installOrigin);

        if (install_data != null) {
            intent.putExtra(InstallDialog.INSTALL_ORIGIN_EXTRA,
                    install_data.toString());
        }

        this.callbackId = callbackId;

        // Introduced in API 8
        // intent.putExtra(android.provider.MediaStore.EXTRA_DURATION_LIMIT,
        // duration);

        ctx.startActivityForResult((Plugin)this, intent, INSTALL_REQUEST);

        PluginResult result = new PluginResult(Status.NO_RESULT);
        result.setKeepCallback(true);
        return result;
    }

    /**
     * Called when the video view exits.
     * 
     * @param requestCode The request code originally supplied to
     *            startActivityForResult(), allowing you to identify who this
     *            result came from.
     * @param resultCode The integer result code returned by the child activity
     *            through its setResult().
     * @param intent An Intent, which can return result data to the caller
     *            (various data can be attached to Intent "extras").
     * @throws JSONException
     */
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {

        // Result received okay
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == INSTALL_REQUEST) {

                JSONObject result = new JSONObject();

                Long id = intent.getExtras().getLong(Intent.EXTRA_UID);

                Log.d(TAG, "INSTALLED " + id);

                success(new PluginResult(PluginResult.Status.OK, result), this.callbackId);

            }

        } else if (resultCode == Activity.RESULT_CANCELED) {

            JSONObject result = new JSONObject();

            String message = intent.getExtras().getString(Intent.EXTRA_TEXT);
            if (message != null) {
                try {
                    result.put("message", message).put("code", message);
                } catch (JSONException e) {
                }
            }

            error(new PluginResult(PluginResult.Status.ERROR, result), this.callbackId);

        }
    }

}
