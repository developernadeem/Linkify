package com.sumbal.linkify;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.sumbal.linkify.Adapter.CallsAdapter;

import pk.edu.uaf.linkify.R;

public class MainActivity2 extends AppCompatActivity {

    Toolbar toolbar;
    com.sumbal.linkify.ViewPagerAdapter adapter;
    ViewPager viewPager;
    TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager =  findViewById(R.id.viewPager_id);

        adapter = new com.sumbal.linkify.ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new com.sumbal.linkify.ConnectFragment(), "Connect");
        adapter.addFragment(new com.sumbal.linkify.CallsFragment(), "Calls");
        adapter.addFragment(new com.sumbal.linkify.ChatsFragment(), "Chats");


        viewPager.setAdapter(adapter);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

    }


    }

