package com.example.asannet.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/*
    TAD
    Class that stores fragments for tabs
 */

public class SessionPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "SessionPagerAdapter";

    private final List<Fragment> mFragmentList = new ArrayList<>();

    public SessionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return mFragmentList.get(i);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment) {
        mFragmentList.add(fragment);
    }
}
