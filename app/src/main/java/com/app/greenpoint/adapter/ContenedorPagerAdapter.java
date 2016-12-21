package com.app.greenpoint.adapter;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ContenedorPagerAdapter extends FragmentPagerAdapter {

    public final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[]{"Tab1", "Tab2"};
    private Fragment[] fragments;
    private Context context;

    public ContenedorPagerAdapter(FragmentManager fragmentManager, Context context, Fragment[] fragments) {
        super(fragmentManager);
        this.context = context;
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}
