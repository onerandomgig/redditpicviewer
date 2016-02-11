package in.continuousloop.redditpicviewer.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import in.continuousloop.redditpicviewer.R;
import in.continuousloop.redditpicviewer.constants.AppConstants;
import in.continuousloop.redditpicviewer.model.SubredditPicItem;

/**
 * ImageViewFragment to show the high resolution image and the title.
 */
public class ImageViewFragment extends Fragment {

    private SubredditPicItem picItem;

    private SimpleDraweeView mSimpleDraweeView;
    private TextView mTitleTextView;

    /**
     * The picture item to display
     *
     * @param aPicItem - The picture item.
     */
    public void setPicItem(SubredditPicItem aPicItem) {
        picItem = aPicItem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_imageview, container, false);

        mSimpleDraweeView = (SimpleDraweeView) rootView.findViewById(R.id.imageView);
        mTitleTextView = (TextView) rootView.findViewById(R.id.title);

        if(picItem.getImageUrlWithResolution(0, 0) != null) {
            mSimpleDraweeView.setImageURI(Uri.parse(picItem.getImageUrlWithResolution(0, 0)));
        }

        String lTitle = picItem.getTitle();
        int lMaxTitleLength = Math.min(AppConstants.MAX_IMAGE_TITLE_LENGTH, picItem.getTitle().length());
        if (lMaxTitleLength < picItem.getTitle().length()) {
            lTitle = lTitle.substring(0, lMaxTitleLength) + " ...";
        }

        mTitleTextView.setText(lTitle);

        return rootView;
    }
}
