package cn.jackq.messenger.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ViewAnimator;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jackq.messenger.R;

public class MainActivity extends AbstractMessengerActivity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.view_animator)
    ViewAnimator mViewAnimator;
    @BindView(R.id.navigation)
    BottomNavigationView navigation;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.main_home_page)
    View mHomePageView;
    private MainActivityHomePage mHomePage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_dashboard_black_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        navigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mViewAnimator.setDisplayedChild(0);
                    return true;
                case R.id.navigation_dashboard:
                    mViewAnimator.setDisplayedChild(1);
                    return true;
                case R.id.navigation_notifications:
                    mViewAnimator.setDisplayedChild(2);
                    return true;
            }
            return false;
        });

        mHomePage = new MainActivityHomePage(this, mHomePageView);

        checkPermission();


    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Log.d(TAG, "checkPermission: Require explanation");

            }

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    0);

            Log.d(TAG, "checkPermission: Requesting permission");

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            Log.d(TAG, "checkPermission: Permission already acquired");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onRequestPermissionsResult: Permission acquired " + Arrays.toString(permissions));
        } else {
            checkPermission();
        }
    }
}
