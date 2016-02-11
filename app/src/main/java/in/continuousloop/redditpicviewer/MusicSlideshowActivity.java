package in.continuousloop.redditpicviewer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.continuousloop.redditpicviewer.api.APIManager;
import in.continuousloop.redditpicviewer.constants.AppConstants;
import in.continuousloop.redditpicviewer.listeners.PicSubredditResponseListener;
import in.continuousloop.redditpicviewer.model.MusicTrackItem;
import in.continuousloop.redditpicviewer.model.SubredditPicItem;
import in.continuousloop.redditpicviewer.model.SubredditPicsWrapper;
import in.continuousloop.redditpicviewer.model.SubredditSection;

/**
 * This activity displays a list of images while playing a music track in the background.
 */
public class MusicSlideshowActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {

    private String mNextPicsPageTag;

    private boolean isPlaying;

    private SubredditSection mCurrentSection;
    private List<SubredditPicItem> imageList;
    private MusicTrackItem musicTrackItem;

    private SimpleDraweeView mImageView;
    private TextView mTitleView;

    private MediaPlayer audioPlayer;

    private Thread mSlideshowThread;

    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.fragment_imageview);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mImageView = (SimpleDraweeView) findViewById(R.id.imageView);
        mTitleView = (TextView) findViewById(R.id.title);

        imageList = new ArrayList<>();
        musicTrackItem = (MusicTrackItem) getIntent().getSerializableExtra(AppConstants.Extras.CURRENT_SELECTED_TRACK);

        int lCurrentSelectedTab = getIntent().getIntExtra(AppConstants.Extras.CURRENT_SELECTED_TAB, 0);
        mCurrentSection = APIManager.getInstance().getSubredditSections().get(lCurrentSelectedTab);
    }

    /**
     * {@inheritDoc}
     */
    public void onResume() {
        super.onResume();
        if (imageList.size() == 0) {
            _fetchImages(true);
        } else {
            _startSlideshow();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onPause() {
        audioPlayer.release();
        audioPlayer = null;

        isPlaying = false;
        mSlideshowThread.interrupt();

        super.onPause();
    }

    /**
     * {@inheritDoc}
     */
    public void onStop() {

        if (audioPlayer != null) {
            audioPlayer.release();
            audioPlayer = null;
            mSlideshowThread.interrupt();
        }

        super.onStop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        audioPlayer.start();
    }

    /**
     * Fetch the images for the current section and start the slideshow after the images are fetched
     *
     * @param startSlideShow - True to start, False otherwise.
     */
    private void _fetchImages(final boolean startSlideShow) {
        APIManager.getInstance().getPicsSubredditData(mCurrentSection, mNextPicsPageTag, new PicSubredditResponseListener() {
            @Override
            public void handleSuccess(SubredditPicsWrapper picsResponse) {
                mNextPicsPageTag = picsResponse.getNext();
                imageList.addAll(picsResponse.getPictures());

                if (startSlideShow) {
                    _startSlideshow();
                }
            }

            @Override
            public void handleError(String aErrorMsg) {
                Toast.makeText(getApplicationContext(), aErrorMsg, Toast.LENGTH_LONG);
            }
        });
    }

    /**
     * Start the slide show and switch the image every 2 seconds. As the slideshow reaches the end
     * of the list (last 5 images), a new set of images is fetched. If there are no more images,
     * the slideshow loops back to the first picture.
     */
    private void _startSlideshow() {

        try {
            _initMediaPlayer();

            audioPlayer.setDataSource(musicTrackItem.getUrl());
            audioPlayer.prepareAsync();

            isPlaying = true;
        } catch (IOException e) {
            Log.e(MusicSlideshowActivity.class.getName(), "_startSlideshow: Failed to play audio track for slideshow");
        }

        mSlideshowThread = new Thread(new Runnable() {
            @Override
            public void run() {

                int lCurrentImgIdx = 0;
                try {
                    while (isPlaying) {

                        final SubredditPicItem lPicItem = imageList.get(lCurrentImgIdx);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                // Image view
                                mImageView.setImageURI(Uri.parse(lPicItem.getImageUrlWithResolution(0, 0)));

                                // Title view
                                mTitleView.setText(lPicItem.getTitle());
                            }
                        });
                        Thread.sleep(2000);
                        lCurrentImgIdx++;

                        if (lCurrentImgIdx == imageList.size() - 5) {
                            _fetchImages(false);
                        } else if (lCurrentImgIdx >= imageList.size()) {
                            lCurrentImgIdx = 0;
                        }
                    }
                } catch (InterruptedException e) {
                    Log.i(MusicSlideshowActivity.class.getName(), "_startSlideshow: Slideshow interrupted.");
                }
            }
        });
        mSlideshowThread.start();
    }

    /**
     * Initialize the audio player.
     */
    private void _initMediaPlayer() {
        audioPlayer = new MediaPlayer();
        audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        audioPlayer.setLooping(true);
        audioPlayer.setOnPreparedListener(this);
    }
}
