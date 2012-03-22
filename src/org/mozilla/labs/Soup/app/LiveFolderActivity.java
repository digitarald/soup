
package org.mozilla.labs.Soup.app;

import org.mozilla.labs.Soup.R;
import org.mozilla.labs.Soup.provider.AppsContract.App;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.LiveFolders;
import android.util.Log;

public class LiveFolderActivity extends Activity {

    private static final String TAG = "LiveFolderActivity";

    /**
     * The URI for the Notes Live Folder content provider.
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + App.AUTHORITY
            + "/live_folders/apps");

    public static final Uri APPS_URI = Uri.parse("content://" + App.AUTHORITY + "/apps/#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        Log.d(TAG, "onCreate called with " + action);

        if (LiveFolders.ACTION_CREATE_LIVE_FOLDER.equals(action)) {

            // Build the live folder intent.
            final Intent liveFolderIntent = createLiveFolder(this);

            // The result of this activity should be a live folder intent.
            setResult(RESULT_OK, liveFolderIntent);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    public static Intent createLiveFolder(Context ctx) {
        // Build the live folder intent.
        final Intent liveFolderIntent = new Intent();

        liveFolderIntent.setData(CONTENT_URI);
        liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME,
                ctx.getString(R.string.app_name_live));
        liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON,
                Intent.ShortcutIconResource.fromContext(ctx, R.drawable.ic_launcher_rt));
        liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
                LiveFolders.DISPLAY_MODE_LIST);
        liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT, new Intent(
                Intent.ACTION_VIEW, APPS_URI));

        return liveFolderIntent;
    }
}
