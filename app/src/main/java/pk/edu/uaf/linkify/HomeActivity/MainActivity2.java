package pk.edu.uaf.linkify.HomeActivity;

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import pk.edu.uaf.linkify.AboutActivity;
import pk.edu.uaf.linkify.Adapter.MyServicesRecyclerAdapter;
import pk.edu.uaf.linkify.Adapter.ViewPagerAdapter;
import pk.edu.uaf.linkify.Fragments.CallsFragment;
import pk.edu.uaf.linkify.Fragments.ChatsFragment;
import pk.edu.uaf.linkify.Fragments.ConnectFragment;
import pk.edu.uaf.linkify.Fragments.PeersDialog;
import pk.edu.uaf.linkify.Modal.LinkifyUser;
import pk.edu.uaf.linkify.R;
import pk.edu.uaf.linkify.SignUpActivity;
import pk.edu.uaf.linkify.Utils.PrefUtils;

import static pk.edu.uaf.linkify.Utils.AppConstant.SHOW_CONNECT_PAGE;
import static pk.edu.uaf.linkify.Utils.AppConstant.SIGNED_UP_STATUS;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener, MyServicesRecyclerAdapter.ClickListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.viewPager_id)
    ViewPager viewPager;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    private ViewPagerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);
        boolean isConnectPage = getIntent().getBooleanExtra(SHOW_CONNECT_PAGE, false);
        if (!PrefUtils.getBooleanPref(this, SIGNED_UP_STATUS)) {
            Intent signUpIntent = new Intent(this, SignUpActivity.class);
            startActivityForResult(signUpIntent, 0);
            finish();
            return;
        }
        setSupportActionBar(toolbar);
        fab.setOnClickListener(this);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ConnectFragment(), "Connect");
        adapter.addFragment(new ChatsFragment(), "Chats");
        adapter.addFragment(new CallsFragment(), "Calls");

        viewPager.setAdapter(adapter);
        if (isConnectPage) {
            viewPager.setCurrentItem(0,true);
        } else
            viewPager.setCurrentItem(1, true);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
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
    public void ItemClickListener(NsdServiceInfo info, LinkifyUser user) {
        ChatsFragment fragment = (ChatsFragment) adapter.getItem(1);
        fragment.onNewChat(info, user);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about) {
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}

