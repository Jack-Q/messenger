package cn.jackq.messenger.ui;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ViewAnimator;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jackq.messenger.R;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.view_animator) ViewAnimator mViewAnimator;
    @BindView(R.id.navigation) BottomNavigationView navigation;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.main_home_page) View mHomePageView;
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
    }

}
