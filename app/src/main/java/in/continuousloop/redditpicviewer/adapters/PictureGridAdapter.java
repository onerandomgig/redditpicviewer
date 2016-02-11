package in.continuousloop.redditpicviewer.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import in.continuousloop.redditpicviewer.R;
import in.continuousloop.redditpicviewer.constants.AppConstants;
import in.continuousloop.redditpicviewer.listeners.IImageSelectionObserver;
import in.continuousloop.redditpicviewer.model.SubredditPicItem;
import in.continuousloop.redditpicviewer.model.SubredditSection;

/**
 * Adapter to handle fetching of pictures.
 */
public class PictureGridAdapter extends PictureListAdapter {

    private IImageSelectionObserver imgSelectionObserver;
    private List<SubredditPicItem> selectedPicItems;

    public PictureGridAdapter(Context context, ProgressBar progressBar, int resource, SubredditSection aSection,
                              IImageSelectionObserver aImageSelectionObserver) {
        super(context, progressBar, resource, aSection);

        imgSelectionObserver = aImageSelectionObserver;
        selectedPicItems = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View lPicItemView;
        if (convertView == null) {
            lPicItemView = mInflater.inflate(mItemLayout, null);
        } else {
            lPicItemView = convertView;
        }

        final SubredditPicItem lCurrentItem = getItem(position);

        // Mask the selected picture and show a check mark
        final RelativeLayout lSelectionLayout = (RelativeLayout) lPicItemView.findViewById(R.id.selectionLayout);
        if (selectedPicItems.contains(lCurrentItem)) {
            lSelectionLayout.setVisibility(View.VISIBLE);
        } else {
            lSelectionLayout.setVisibility(View.GONE);
        }

        // Enable / Disable next button.
        if (selectedPicItems.size() >= AppConstants.MIN_SELECT_IMAGE) {
            imgSelectionObserver.enableNextAction(true);
        } else {
            imgSelectionObserver.enableNextAction(false);
        }

        // Thumbnail image view
        SimpleDraweeView lPicThumbnailView = (SimpleDraweeView) lPicItemView.findViewById(R.id.picThumbnailImg);
        lPicThumbnailView.setImageURI(Uri.parse(lCurrentItem.getThumbNail()));

        // Select image for video
        lPicThumbnailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (selectedPicItems.contains(lCurrentItem)) {
                    selectedPicItems.remove(position);
                    notifyDataSetChanged();
                } else {
                    selectedPicItems.add(lCurrentItem);
                    notifyDataSetChanged();
                }
            }
        });

        // Fetch the next page if we have reached the bottom
        if (position >= getCount() - 2) {
            fetchPics();
        }

        return lPicItemView;
    }

    /**
     * Get the list of pictures that were selected.
     * @return - List of {@link SubredditPicItem}
     */
    public List<SubredditPicItem> getSelectedPictures() {
        return selectedPicItems;
    }
}
