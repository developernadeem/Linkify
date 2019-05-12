package pk.edu.uaf.linkify;

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import pk.edu.uaf.linkify.Adapter.MyServicesRecyclerAdapter;
import pk.edu.uaf.linkify.Adapter.ViewPagerAdapter;
import pk.edu.uaf.linkify.Fragments.CallsFragment;
import pk.edu.uaf.linkify.Fragments.ChatsFragment;
import pk.edu.uaf.linkify.Fragments.ConnectFragment;
import pk.edu.uaf.linkify.Fragments.PeersDialog;
import pk.edu.uaf.linkify.Utils.PrefUtils;

import static pk.edu.uaf.linkify.Utils.AppConstant.SIGNED_UP_STATUS;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener,  MyServicesRecyclerAdapter.ClickListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.viewPager_id)
    ViewPager viewPager;
    @BindView(R.id.fab)
    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);
        if (!PrefUtils.getBooleanPref(this,SIGNED_UP_STATUS)){
            Intent signUpIntent = new Intent(this, SignUpActivity.class);
            startActivityForResult(signUpIntent,0);
        }
        setSupportActionBar(toolbar);
        fab.setOnClickListener(this);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ConnectFragment(), "Connect");
        adapter.addFragment(new ChatsFragment(), "Chats");
        adapter.addFragment(new CallsFragment(), "Calls");

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1,true);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab:
                FragmentManager fragmentManager = getSupportFragmentManager();
                PeersDialog newFragment = new PeersDialog();
                // The device is smaller, so show the fragment fullscreen
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                // For a little polish, specify a transition animation
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                // To make it fullscreen, use the 'content' root view as the container
                // for the fragment, which is always the root view for the activity
                transaction.add(android.R.id.content, newFragment)
                        .addToBackStack(null).commit();
        }
    }



    @Override
    public void ItemClickListener(NsdServiceInfo info, int which) {
        if (which == 0) {
            Intent call = new Intent(this, CallActivity.class);
            call.putExtra("info", info);
            startActivity(call);
        }else if (which == 1){
            Intent call = new Intent(this, DataChannelActivity.class);
            call.putExtra("info", info);
            startActivity(call);
        }
    }
}

