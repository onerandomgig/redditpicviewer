package in.continuousloop.redditpicviewer.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import in.continuousloop.redditpicviewer.fragments.ImageViewFragment;
import in.continuousloop.redditpicviewer.model.SubredditPicItem;

/**
 * Adapter to handle displaying of high resolution images
 */
public class ImageViewAdapter extends FragmentStatePagerAdapter {

    public List<SubredditPicItem> picItemsList;

    public ImageViewAdapter(FragmentManager fm, List<SubredditPicItem> aPicItemsList) {
        super(fm);
        this.picItemsList = aPicItemsList;
    }

    @Override
    public Fragment getItem(int position) {
        ImageViewFragment lImgFragment = new ImageViewFragment();
        lImgFragment.setPicItem(picItemsList.get(position));

        return lImgFragment;
    }

    @Override
    public int getCount() {
        return picItemsList.size();
    }
}
