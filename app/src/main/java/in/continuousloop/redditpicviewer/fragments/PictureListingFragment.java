package in.continuousloop.redditpicviewer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import in.continuousloop.redditpicviewer.R;
import in.continuousloop.redditpicviewer.adapters.PictureListAdapter;
import in.continuousloop.redditpicviewer.model.SubredditSection;

/**
 * A fragment to show the list of pictures.
 */
public class PictureListingFragment extends Fragment {

    private SubredditSection mSection;

    /**
     * The section which this picture listing fragment will showcase.
     *
     * @param aSection - The specified section
     */
    public void setFragmentSection(SubredditSection aSection) {
        mSection = aSection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_picture_gallery, container, false);

        ProgressBar lProgressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        ListView lPictureListView = (ListView)rootView.findViewById(R.id.pictureGalleryList);
        PictureListAdapter lPicListAdapter = new PictureListAdapter(getContext(), lProgressBar,R.layout.picture_item, mSection);
        lPicListAdapter.fetchPics();

        lPictureListView.setAdapter(lPicListAdapter);

        return rootView;
    }
}