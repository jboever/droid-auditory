package com.droid.auditory.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.droid.auditory.R;
import com.droid.auditory.application.Dagger;
import com.droid.auditory.services.AuditoryService;
import com.droid.lib.constants.IntentConstants;
import com.droid.lib.utils.IntentUtils;

import javax.inject.Inject;

import timber.log.Timber;

public class StandAloneActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    @Inject
    IntentUtils intentUtils;

    boolean hasExternalStoragePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Dagger.getDagger().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stand_alone);
        Timber.i("com.droid.auditory.activities.StandAloneActivity - onCreate()");

        considerRunTimePermissions();
    }

    private void considerRunTimePermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else
        {
            hasExternalStoragePermission = true;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 2);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasExternalStoragePermission) {
            intentUtils.startService(this, intentUtils.createIntent(this, AuditoryService.class, IntentConstants.ACTION_AUDITORY_STARTUP, new Bundle()));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasExternalStoragePermission = true;
                    Timber.i("Permission for write external data granted successfully");
                } else {
                    Timber.i("Permission for read external data denied");
                }
                break;
            case 2:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.i("Permission granted successfully");

                } else {
                    Timber.i("Permission denied");
                }
                break;
            case 3:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.i("Permission granted successfully");
                } else {
                    Timber.i("Permission denied");
                }
                break;
        }
    }
}
