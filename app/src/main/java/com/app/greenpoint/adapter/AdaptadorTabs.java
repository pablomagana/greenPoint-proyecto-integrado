package com.app.greenpoint.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by pablo on 10/02/2016.
 */
public class AdaptadorTabs extends FragmentStatePagerAdapter {
    private int numTabs;
    private Fragment[] fragments;

    public AdaptadorTabs(FragmentManager fm, int numTabs,Fragment[] fragments){
        super(fm);
        this.numTabs=numTabs;
        this.fragments=fragments;

    }
    @Override
    public Fragment getItem(int position) {
        if(position == 0)
        {
            return fragments[0];
        }
        else
        {
            return fragments[1];
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
