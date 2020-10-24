package com.example.openreden.activity.core;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.openreden.R;
import com.example.openreden.adapter.TabAdapter;
import com.google.android.material.tabs.TabLayout;

public class CoreActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

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
        viewPager = findViewById(R.id.viewPager);
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
        updateIcons(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateIcons(position);
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

}
