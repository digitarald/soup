
package org.mozilla.labs.Soup.provider;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class SearchProvider extends SearchRecentSuggestionsProvider {
    static final String TAG = SearchProvider.class.getSimpleName();

    public static final String AUTHORITY = SearchProvider.class.getName();

    public static final int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

    private static final String[] COLUMNS = {
            BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            SearchManager.SUGGEST_COLUMN_INTENT_ACTION, SearchManager.SUGGEST_COLUMN_SHORTCUT_ID
    };

    public SearchProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        String query = selectionArgs[0];
        if (query == null || query.length() == 0) {
            return null;
        }

        MatrixCursor cursor = new MatrixCursor(COLUMNS);

        cursor.addRow(new Object[] {
                1, // _id
                "Soundcloud", // text1
                "Broadcast Yourself", // text2
                "http://m.soundcloud.com/", "android.intent.action.SEARCH", // action
                SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT
        });

        // try {
        // List<myobj> list = callmyservice(query);
        // int n = 0;
        // for (MyObj obj : list) {
        // cursor.addRow(createRow(new Integer(n), query, obj.getText1(),
        // obj.getText2()));
        // n++;
        // }
        // } catch (Exception e) {
        // Log.e(TAG, "Failed to lookup " + query, e);
        // }

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

}
