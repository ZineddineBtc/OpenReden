package com.example.openreden.activity.core;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.openreden.R;
import com.example.openreden.StaticClass;
import com.example.openreden.activity.core.fragment.ChatsFragment;
import com.example.openreden.activity.core.fragment.ExploreFragment;
import com.example.openreden.activity.core.fragment.ProfileFragment;
import com.example.openreden.adapter.TabAdapter;
import com.google.android.material.tabs.TabLayout;

import static com.example.openreden.activity.core.fragment.ProfileFragment.photoOptionsLL;
import static com.example.openreden.activity.core.fragment.ProfileFragment.photoOptionsShown;
import static com.example.openreden.activity.core.fragment.ProfileFragment.shadeLL;

public class CoreActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView searchIV;
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);
        findViewsByIds();
        setTabAdapter();
        setUI();

    }
    public void findViewsByIds(){
        toolbar = findViewById(R.id.toolbar);
        searchIV = toolbar.findViewById(R.id.searchIV);
        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
            }
        });
        viewPager = findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(3);
        tabLayout = findViewById(R.id.tabLayout);
    }
    public void setTabAdapter(){
        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new ExploreFragment(), "Tab 1");
        adapter.addFragment(new ChatsFragment(), "Tab 2");
        adapter.addFragment(new ProfileFragment(), "Tab 3");
    }
    public void setUI(){
        toolbar.setTitle("OpenReden");
        toolbar.setTitleTextColor(getColor(R.color.special));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        openFragment();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateIcons(position);
                getFragmentManager()
                        .beginTransaction()
                        .commit();
            }
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }@Override public void onPageScrollStateChanged(int state) {}
        });
    }
    public void updateIcons(int position){
        int[] tabIcons = new int[3];
        switch (position){
            case 0:
                tabIcons[0] = R.drawable.ic_explore_special;
                tabIcons[1] = R.drawable.ic_message_black_24dp;
                tabIcons[2] = R.drawable.ic_account_circle_black;
                break;
            case 1:
                tabIcons[0] = R.drawable.ic_explore_black;
                tabIcons[1] = R.drawable.ic_message_special_24dp;
                tabIcons[2] = R.drawable.ic_account_circle_black;
                break;
            case 2:
                tabIcons[0] = R.drawable.ic_explore_black;
                tabIcons[1] = R.drawable.ic_message_black_24dp;
                tabIcons[2] = R.drawable.ic_account_circle_special;
                break;
        }
        try {
            tabLayout.getTabAt(0).setIcon(tabIcons[0]);
            tabLayout.getTabAt(1).setIcon(tabIcons[1]);
            tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        }catch (NullPointerException e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public void openFragment(){
        int position = 0;
        to = getIntent().getStringExtra(StaticClass.TO);
        if(to != null) {
            if (to.equals(StaticClass.PROFILE_FRAGMENT)){
                position = 2;
            }else if(to.equals(StaticClass.CHATS_FRAGMENT)) {
                position = 1;
            }
        }
        viewPager.setCurrentItem(position);
        getFragmentManager()
                .beginTransaction()
                .commit();
        updateIcons(position);
    }
    @Override
    public void onBackPressed() {
        if (photoOptionsShown){
            shadeLL.setVisibility(View.GONE);
            photoOptionsLL.setVisibility(View.GONE);
            photoOptionsShown = false;
        }else{
            moveTaskToBack(true);
        }
    }
}
