package in.continuousloop.redditpicviewer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import in.continuousloop.redditpicviewer.api.APIManager;
import in.continuousloop.redditpicviewer.constants.AppConstants;
import in.continuousloop.redditpicviewer.adapters.NetworkImageSlideshowPlayer;
import in.continuousloop.redditpicviewer.model.MusicTrackItem;
import in.continuousloop.redditpicviewer.model.SubredditSection;

/**
 * This activity displays a list of images while playing a music track in the background.
 */
public class MusicSlideshowActivity extends AppCompatActivity {

    private NetworkImageSlideshowPlayer slideshowPlayer;

    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.fragment_imageview);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Image view and title view to show the image and title
        SimpleDraweeView lImageView = (SimpleDraweeView) findViewById(R.id.imageView);
        TextView lTitleView = (TextView) findViewById(R.id.title);

        // The music track to play during the slide show
        MusicTrackItem musicTrackItem = (MusicTrackItem) getIntent().getSerializableExtra(AppConstants.Extras.CURRENT_SELECTED_TRACK);

        // The current tab that is selected (to fetch the images to show in the slide show)
        int lCurrentSelectedTab = getIntent().getIntExtra(AppConstants.Extras.CURRENT_SELECTED_TAB, 0);
        SubredditSection lCurrentSection = APIManager.getInstance().getSubredditSections().get(lCurrentSelectedTab);

        // The slide show player instance.
        slideshowPlayer = new NetworkImageSlideshowPlayer(lImageView, lTitleView, musicTrackItem, lCurrentSection);
    }

    /**
     * {@inheritDoc}
     */
    public void onResume() {
        super.onResume();
        slideshowPlayer.startSlideshow();
    }

    /**
     * {@inheritDoc}
     */
    public void onPause() {
        slideshowPlayer.stopSlideshow();
        super.onPause();
    }

    /**
     * {@inheritDoc}
     */
    public void onStop() {
        slideshowPlayer.stopSlideshow();
        super.onStop();
    }
}
