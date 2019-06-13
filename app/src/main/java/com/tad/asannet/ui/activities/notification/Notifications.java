package com.tad.asannet.ui.activities.notification;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tad.asannet.R;
import com.tad.asannet.adapters.NotificationsAdapter;
import com.tad.asannet.models.Notification;
import com.tad.asannet.utils.database.NotificationsHelper;

import java.util.ArrayList;
import java.util.List;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

/**
 * Created by tad on 29/3/18.
 */

public class Notifications extends AppCompatActivity {

    private List<Notification> notificationsList;
    private NotificationsAdapter notificationsAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout refreshLayout;

    public void getNotifications() {
        notificationsList.clear();
        notificationsAdapter.notifyDataSetChanged();

        findViewById(R.id.default_item).setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);

        NotificationsHelper notificationsHelper=new NotificationsHelper(this);

        if(notificationsHelper.getCount()==0){
            findViewById(R.id.default_item).setVisibility(View.VISIBLE);
            refreshLayout.setRefreshing(false);
            return;
        }

        notificationsList.addAll(notificationsHelper.getAllNotifications());
        refreshLayout.setRefreshing(false);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRecyclerView = findViewById(R.id.recyclerView);
        refreshLayout=findViewById(R.id.refreshLayout);

        notificationsList = new ArrayList<>();
        notificationsAdapter = new NotificationsAdapter(notificationsList, this);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(notificationsAdapter);

        refreshLayout.setOnRefreshListener(() -> getNotifications());

        getNotifications();

    }
}
