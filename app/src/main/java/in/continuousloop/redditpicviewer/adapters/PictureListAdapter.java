package in.continuousloop.redditpicviewer.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import in.continuousloop.redditpicviewer.ImageViewActivity;
import in.continuousloop.redditpicviewer.R;
import in.continuousloop.redditpicviewer.WebViewActivity;
import in.continuousloop.redditpicviewer.api.APIManager;
import in.continuousloop.redditpicviewer.constants.AppConstants;
import in.continuousloop.redditpicviewer.listeners.PicSubredditResponseListener;
import in.continuousloop.redditpicviewer.model.SubredditPicItem;
import in.continuousloop.redditpicviewer.model.SubredditPicsWrapper;
import in.continuousloop.redditpicviewer.model.SubredditSection;

/**
 * Adapter to handle fetching of pictures.
 */
public class PictureListAdapter extends ArrayAdapter<SubredditPicItem> {

    protected int mItemLayout;
    protected LayoutInflater mInflater;
    protected ProgressBar mProgressbar;

    protected boolean isFetching;
    protected SubredditSection mSubredditSection;

    protected String mNextPageId;
    protected List<SubredditPicItem> subredditPicItems;

    protected APIManager mAPIManager;

    public PictureListAdapter(Context context, ProgressBar progressBar, int resource, SubredditSection aSection) {
        super(context, resource);

        mAPIManager = APIManager.getInstance();
        subredditPicItems = new ArrayList<>();
        mInflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mProgressbar = progressBar;
        mItemLayout = resource;

        mSubredditSection = aSection;
    }

    /**
     * Fetch the pictures for the specified section
     */
    public void fetchPics() {

        mProgressbar.setVisibility(View.VISIBLE);

        if (isFetching) {
            return;
        }

        isFetching = true;
        mAPIManager.getPicsSubredditData(mSubredditSection, mNextPageId, new PicSubredditResponseListener() {
            @Override
            public void handleSuccess(SubredditPicsWrapper picsResponse) {

                mProgressbar.setVisibility(View.GONE);
                isFetching = false;

                mNextPageId = picsResponse.getNext();
                subredditPicItems.addAll(picsResponse.getPictures());
                PictureListAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void handleError(String aErrorMsg) {
                mProgressbar.setVisibility(View.GONE);
                isFetching = false;
            }
        });
    }

    /**
     * Get the number of pictures in the gallery
     *
     * @return - Number of pictures
     */
    @Override
    public int getCount() {
        return subredditPicItems.size();
    }

    /**
     * Get the picture item at the specified position
     *
     * @param position - The index of the picture item
     *
     * @return {@link SubredditPicItem}
     */
    @Override
    public SubredditPicItem getItem(int position) {
        return subredditPicItems.get(position);
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

        SubredditPicItem lCurrentItem = getItem(position);

        // Thumbnail image view
        SimpleDraweeView lPicThumbnailView = (SimpleDraweeView)lPicItemView.findViewById(R.id.picThumbnailImg);
        lPicThumbnailView.setImageURI(Uri.parse(lCurrentItem.getThumbNail()));

        // Load the high resolution image in a view pager activity
        lPicThumbnailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ImageViewActivity.class);
                intent.putExtra(AppConstants.Extras.PIC_LIST, (ArrayList)subredditPicItems);
                intent.putExtra(AppConstants.Extras.PIC_ITEM_POSITION, position);

                getContext().startActivity(intent);
            }
        });

        // Title view
        TextView lTitleView  = (TextView) lPicItemView.findViewById(R.id.picTitleLabel);
        lTitleView.setText(lCurrentItem.getTitle());

        // Set the permalink in the title field as a tag.
        if (lCurrentItem.getPermaLink() != null) {
            lTitleView.setTag(lCurrentItem.getPermaLink());
        }

        // Load the permalink in a webview when the title is clicked.
        lTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View aView) {

                Object lUrlToLoad = aView.getTag();
                if (lUrlToLoad != null) {
                    Intent intent = new Intent(getContext(), WebViewActivity.class);
                    intent.putExtra(AppConstants.Extras.WEB_URL, lUrlToLoad.toString());
                    getContext().startActivity(intent);
                } else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.no_permalink), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Submitted time view
        TextView lSubmittedOnView  = (TextView) lPicItemView.findViewById(R.id.picSubmittedTimeLabel);
        lSubmittedOnView.setText(lCurrentItem.getCreatedOn());

        // Author view
        TextView lAuthorView  = (TextView) lPicItemView.findViewById(R.id.authorLabel);
        lAuthorView.setText(lCurrentItem.getAuthor());

        // Number of comments view
        TextView lCommentsView  = (TextView) lPicItemView.findViewById(R.id.commentsLabel);
        lCommentsView.setText(lCurrentItem.getNumComments() + " comments");

        // Points view
        TextView lScoreView  = (TextView) lPicItemView.findViewById(R.id.pointsLabel);
        lScoreView.setText(lCurrentItem.getScore() + " points");

        // Fetch the next page if we have reached the bottom
        if (position >= getCount() - 2) {
            fetchPics();
        }

        return lPicItemView;
    }
}
