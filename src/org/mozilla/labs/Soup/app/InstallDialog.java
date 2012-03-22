
package org.mozilla.labs.Soup.app;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.labs.Soup.R;
import org.mozilla.labs.Soup.provider.AppsContract.App;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class InstallDialog extends Activity {

    static final String TAG = InstallDialog.class.getSimpleName();

    private static final int DIALOG_PROMPT = 1;

    private static final int DIALOG_LOADING = 2;

    public static final String MANIFEST_URI_EXTRA = "org.mozilla.labs.Soup.app.MANIFEST_URI";

    public static final String INSTALL_ORIGIN_EXTRA = "org.mozilla.labs.Soup.app.INSTALL_ORIGIN";

    public static final String INSTALL_DATA_EXTRA = "org.mozilla.labs.Soup.app.INSTALL_DATA";

    private String manifestUri = null;

    private String installOrigin = null;

    private JSONObject installData = null;

    private Handler handler;

    private int NOTIFY_ID = 1001;

    // private InstallTask task = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Rotation aware task handling

        // task = (InstallTask)getLastNonConfigurationInstance();
        //
        // if (task != null) {
        // task.attach(this);
        // return;
        // }

        handler = new Handler();

        // Default result

        Intent intent = getIntent();
        setResult(RESULT_CANCELED, intent);

        // Retrieve intent extras

        manifestUri = getIntent().getExtras().getString(MANIFEST_URI_EXTRA);
        installOrigin = getIntent().getExtras().getString(INSTALL_ORIGIN_EXTRA);

        final String data = getIntent().getExtras().getString(INSTALL_ORIGIN_EXTRA);
        if (data != null) {
            try {
                installData = new JSONObject(data);
            } catch (JSONException e) {
            }
        }

        showDialog(DIALOG_LOADING);

        Runnable runnable = new Runnable() {
            public void run() {

                // Request manifest and all resources to create an App instance

                final App app = App.fromManifestUri(InstallDialog.this, manifestUri, installData,
                        installOrigin);

                handler.post(new Runnable() {
                    public void run() {
                        onInstallComplete(app);
                    }
                });
            }
        };

        new Thread(runnable).start();

    }

    public void onInstallComplete(final App app) {

        dismissDialog(DIALOG_LOADING);

        if (app == null) {
            Toast.makeText(this, "Installation failed. Application manifest was faulty!",
                    Toast.LENGTH_SHORT);

            finish();

            return;
        }

        // Install dialog setup

        AlertDialog.Builder installDlg = new AlertDialog.Builder(this);

        String title = "Install " + app.name + "?";

        if (app.id != null) {
            title = "Update " + app.name + "?";
        }

        installDlg.setTitle(title).setCancelable(true);

        if (app.icon != null) {
            installDlg.setIcon(new BitmapDrawable(app.icon));
        }

        installDlg.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = getIntent();
                        intent.putExtra(Intent.EXTRA_TEXT, "denied");
                        setResult(RESULT_CANCELED, intent);

                        finish();

                    }
                }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                // Set installation flags

                app.installTime = Calendar.getInstance();
                app.installStatus = App.STATUS_ENUM.OK;

                boolean saved = app.save(InstallDialog.this);

                if (!saved) {

                    Toast.makeText(InstallDialog.this, "Installation flopped. Please try again!",
                            Toast.LENGTH_SHORT);

                    Intent intent = getIntent();
                    intent.putExtra(Intent.EXTRA_TEXT, "denied");
                    setResult(RESULT_CANCELED, intent);

                    finish();

                    return;
                }

                app.createShortcut(InstallDialog.this);

                // Show notification

                final NotificationManager mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

                String title = "Installed " + app.name;
                String text = "Successfully installed";

                if (app.id != null) {
                    title = "Updated " + app.name;
                    text = "Successfully updated";
                }

                // Set the icon, scrolling text and timestamp
                Notification notification = new Notification(R.drawable.ic_launcher_rt, title,
                        System.currentTimeMillis());

                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                Intent intent = app.getIntent(InstallDialog.this);
                intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                // The PendingIntent to launch our activity if the user selects
                // this
                // notification
                PendingIntent contentIntent = PendingIntent.getActivity(InstallDialog.this,
                        app.id.intValue(), intent, PendingIntent.FLAG_ONE_SHOT);

                // Set the info for the views that show in the notification
                // panel.
                notification.setLatestEventInfo(InstallDialog.this, app.name, text, contentIntent);

                // Send the notification.
                // We use a string id because it is a unique number. We use it
                // later to cancel.
                mNM.notify(TAG, app.id.intValue(), notification);

                // Set result

                Intent result = getIntent();
                result.getExtras().putLong(Intent.EXTRA_UID, app.id);
                setResult(RESULT_OK, intent);

                finish();
            }
        });

        installDlg.create();
        installDlg.show();
    }

    // @Override
    // public Object onRetainNonConfigurationInstance() {
    // task.detach();
    //
    // return task;
    // }
    //
    // static class InstallTask extends AsyncTask<Void, Void, Void> {
    //
    // InstallDialog activity = null;
    //
    // InstallTask(InstallDialog activity) {
    // attach(activity);
    // }
    //
    // @Override
    // protected Void doInBackground(Void... unused) {
    //
    // return null;
    // }
    //
    // @Override
    // protected void onPostExecute(Void unused) {
    // if (activity == null) {
    // Log.w("InstallDialog", "onPostExecute() skipped -- no activity");
    // } else {
    // activity.onInstallComplete();
    // }
    // }
    //
    // void detach() {
    // activity = null;
    // }
    //
    // void attach(InstallDialog activity) {
    // this.activity = activity;
    // }
    //
    // }

    @Override
    protected Dialog onCreateDialog(int id) {

        switch (id) {

            case DIALOG_LOADING:

                ProgressDialog alert = new ProgressDialog(this);

                alert.setIndeterminate(true);
                alert.setCancelable(true);
                alert.setMessage("Loading");
                alert.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {

                    }

                });

                return alert;
        }
        return null;
    }

}
