
package org.mozilla.labs.Soup.app;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.labs.Soup.R;
import org.mozilla.labs.Soup.provider.AppsContract.App;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ManageActivity extends Activity {

    private static final String TAG = "ManageActivity";

    private int iconSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.manage);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            doSearchQuery(intent);

            return;
        }

        Cursor cursor = managedQuery(App.CONTENT_URI, null, null, null, null);

        Log.d(TAG, "cursor.getCount()=" + cursor.getCount());

        GridView gridView = (GridView)findViewById(R.id.grid_view);

        iconSize = (int)(48 * getResources().getDisplayMetrics().density);

        int columnWidth = (int)(iconSize * 1.2);
        gridView.setColumnWidth(columnWidth);

        AppAdapter adapter = new AppAdapter(this, R.layout.grid_app, cursor);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                final AppItemCache cache = (AppItemCache)v.getTag();

                String launchUrl = (String)cache.nameView.getHint();

                final Intent shortcutIntent = new Intent(ManageActivity.this, AppActivity.class);
                shortcutIntent.setAction(AppActivity.ACTION_WEBAPP);

                shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                shortcutIntent.setData(Uri.parse(launchUrl));

                startActivity(shortcutIntent);
            };
        });

    }

    private void doSearchQuery(final Intent queryIntent) {

        Toast.makeText(this, "doSearchQuery", Toast.LENGTH_LONG).show();

        // String query = queryIntent.getDataString(); // from suggestions
        // if (query == null) {
        // query = intent.getStringExtra(SearchManager.QUERY); // from
        // // search-bar
        // }
        //
        // // display results here
        // bundle.putString("user_query", query);
        // intent.setData(Uri.fromParts("", "", query));
        //
        // intent.setAction(Intent.ACTION_SEARCH);
        // queryIntent.putExtras(bundle);
        // startActivity(intent);

    }

    public class AppAdapter extends ResourceCursorAdapter {

        public AppAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            final AppItemCache cache = (AppItemCache)view.getTag();

            cache.nameView.setText(cursor.getString(cursor.getColumnIndex(App.NAME)));

            JSONObject manifest = new JSONObject();
            try {
                manifest = new JSONObject(cursor.getString(cursor.getColumnIndex(App.MANIFEST)));
            } catch (JSONException e) {
            }

            final String origin = cursor.getString(cursor.getColumnIndex(App.ORIGIN));
            final String launchPath = origin + manifest.optString("launch_path", "/");

            cache.nameView.setHint(launchPath);

            final byte[] imageDate = cursor.getBlob(cursor.getColumnIndex(App.ICON));

            if (imageDate != null) {
                final Bitmap map = BitmapFactory.decodeByteArray(imageDate, 0, imageDate.length);
                cache.iconView.setImageBitmap(map);
            } else {
                cache.iconView.setImageResource(0);
            }

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            View view = super.newView(context, cursor, parent);

            AppItemCache cache = new AppItemCache();
            cache.nameView = (TextView)view.findViewById(R.id.text_view);
            cache.iconView = (ImageView)view.findViewById(R.id.icon_view);

            cache.iconView.setMaxHeight(iconSize);
            cache.iconView.setMaxWidth(iconSize);
            cache.iconView.setMinimumHeight(iconSize);
            cache.iconView.setMinimumWidth(iconSize);

            view.setTag(cache);

            return view;
        }

    }

    final static class AppItemCache {
        public TextView nameView;

        public ImageView iconView;
    }

}
