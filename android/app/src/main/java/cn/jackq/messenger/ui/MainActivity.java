package cn.jackq.messenger.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jackq.messenger.R;
import cn.jackq.messenger.network.protocol.User;

public class MainActivity extends AbstractMessengerActivity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.buddy_list)
    ListViewCompat mBuddyList;

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

        checkPermission();


    }

    class BuddyListAdapter extends ArrayAdapter<User> {
        public BuddyListAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_user, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            User item = getMainService().getBuddyList().get(position);

            holder.name.setText(item.getName());
            holder.ip.setText(item.getIp());
            holder.unread.setText(String.valueOf(getMainService().getMessageManager().getUnread(item.getName())));

            return convertView;
        }

        class ViewHolder {
            @BindView(R.id.user_name)
            AppCompatTextView name;
            @BindView(R.id.user_ip)
            AppCompatTextView ip;
            @BindView(R.id.user_unread)
            AppCompatTextView unread;

            ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }

    BuddyListAdapter buddyListAdapter;

    @Override
    protected void onServiceBound() {
        super.onServiceBound();

        buddyListAdapter = new BuddyListAdapter(this, R.layout.list_item_user, getMainService().getBuddyList());
        mBuddyList.setAdapter(buddyListAdapter);
        mBuddyList.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("user", getMainService().getBuddyList().get(position).getName());
            startActivity(intent);
        });
    }

    @Override
    public void onServerStateChange() {
        super.onServerStateChange();
        this.runOnUiThread(() ->
                this.buddyListAdapter.notifyDataSetChanged());
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
