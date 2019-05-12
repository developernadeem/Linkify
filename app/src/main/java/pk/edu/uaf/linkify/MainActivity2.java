package pk.edu.uaf.linkify;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import pk.edu.uaf.linkify.Adapter.ViewPagerAdapter;
import pk.edu.uaf.linkify.Fragments.CallsFragment;
import pk.edu.uaf.linkify.Fragments.ChatsFragment;
import pk.edu.uaf.linkify.Fragments.ConnectFragment;
import pk.edu.uaf.linkify.Utils.PrefUtils;

import static pk.edu.uaf.linkify.Utils.AppConstant.SIGNED_UP_STATUS;

public class MainActivity2 extends AppCompatActivity {

    Toolbar toolbar;
    ViewPagerAdapter adapter;
    ViewPager viewPager;
    TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if (!PrefUtils.getBooleanPref(this,SIGNED_UP_STATUS)){
            Intent signUpIntent = new Intent(this, SignUpActivity.class);
            startActivityForResult(signUpIntent,0);
        }
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.viewPager_id);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ConnectFragment(), "Connect");
        adapter.addFragment(new ChatsFragment(), "Chats");
        adapter.addFragment(new CallsFragment(), "Calls");

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1,true);
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

    }


}

