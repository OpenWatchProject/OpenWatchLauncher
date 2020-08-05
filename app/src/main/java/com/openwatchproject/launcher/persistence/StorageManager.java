package com.openwatchproject.launcher.persistence;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.provider.DocumentsContract;

import androidx.appcompat.app.AppCompatActivity;

public class StorageManager {
    private static final String TAG = "StorageManager";

    private static final String STORAGE_PREFERENCES = "storage_preferences";
    private static final String WATCHFACE_FOLDER_PREFERENCE = "watchface_folder";

    private Context context;
    private SharedPreferences sharedPreferences;

    public StorageManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(STORAGE_PREFERENCES, Context.MODE_PRIVATE);
    }

    public Uri getWatchfaceFolder() {
        String uri = sharedPreferences.getString(WATCHFACE_FOLDER_PREFERENCE, null);
        if (uri != null) {
            for (UriPermission up : context.getContentResolver().getPersistedUriPermissions()) {
                if (up.getUri().toString().equals(uri)) {
                    return up.getUri();
                }
            }
        }

        return null;
    }

    public void setWatchfaceFolder(Intent data) {
        Uri uri = data.getData();
        context.getContentResolver().takePersistableUriPermission(uri,
                data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));

        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString(WATCHFACE_FOLDER_PREFERENCE, uri.toString());
        sharedPreferencesEditor.apply();
    }

    public static void openDirectory(AppCompatActivity activity, int requestId, Uri uriToLoad) {
        // Choose a directory using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Provide read access to files and sub-directories in the user-selected
        // directory.
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when it loads.
        if (uriToLoad != null) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);
        }

        activity.startActivityForResult(intent, requestId);
    }
}
