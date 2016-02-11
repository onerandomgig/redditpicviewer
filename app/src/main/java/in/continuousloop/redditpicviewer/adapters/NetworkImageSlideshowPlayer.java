package in.continuousloop.redditpicviewer.adapters;

import android.graphics.drawable.Animatable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.continuousloop.redditpicviewer.api.APIManager;
import in.continuousloop.redditpicviewer.listeners.PicSubredditResponseListener;
import in.continuousloop.redditpicviewer.model.MusicTrackItem;
import in.continuousloop.redditpicviewer.model.SubredditPicItem;
import in.continuousloop.redditpicviewer.model.SubredditPicsWrapper;
import in.continuousloop.redditpicviewer.model.SubredditSection;

/**
 * The class to start and stop the slideshow. This class also fetches the images that need to play
 * during the slideshow
 */
public class NetworkImageSlideshowPlayer implements MediaPlayer.OnPreparedListener {

    private String mNextPicsPageTag;

    private boolean isPlaying;

    private SubredditSection mCurrentSection;
    private MusicTrackItem musicTrackItem;

    private SimpleDraweeView mImageView;
    private TextView mTitleView;

    private List<SubredditPicItem> imageList;

    private MediaPlayer audioPlayer;

    private Thread mSlideshowThread;

    private final Object mImageFetchLock = new Object();

    private Handler mSlideDisplayHandler;
    private ControllerListener imageControllerListener;

    /**
     * Create an instance of the NetworkImageSlideshowPlayer
     *
     * @param draweeView      - The imageview to display the image
     * @param titleView       - The textview to display the title
     * @param aMusicTrackItem - The music track that should be played during the slideshow
     * @param aCurrentSection - The section to fetch the images from (HOT, NEW, RISING)
     */
    public NetworkImageSlideshowPlayer(SimpleDraweeView draweeView,
                                       TextView titleView,
                                       MusicTrackItem aMusicTrackItem,
                                       SubredditSection aCurrentSection) {

        mImageView = draweeView;
        mTitleView = titleView;

        musicTrackItem = aMusicTrackItem;
        mCurrentSection = aCurrentSection;

        imageList = new ArrayList<>();

        imageControllerListener = _getControllerListener();
        mSlideDisplayHandler = new ImageDisplayHandler(this);
    }

    /**
     * Fetch the images and start the slideshow.
     */
    public void startSlideshow() {
        if (imageList.size() == 0) {
            _fetchImages(true);
        } else {
            _startSlideshow();
        }
    }

    /**
     * Stop the slideshow
     */
    public void stopSlideshow() {

        if (audioPlayer != null) {
            audioPlayer.release();
            audioPlayer = null;

            isPlaying = false;
            mSlideshowThread.interrupt();
        }
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
                    // Start the slideshow
                    _startSlideshow();
                }
            }

            @Override
            public void handleError(String aErrorMsg) {

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
            Log.e(NetworkImageSlideshowPlayer.class.getName(), "_startSlideshow: Failed to play audio track for slideshow");
        }

        mSlideshowThread = _getSlideshowRunner();
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

    /**
     * Get the {@link ControllerListener} to track image download events.
     *
     * @return {@link ControllerListener}
     */
    private ControllerListener _getControllerListener() {
        return new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(
                    String id,
                    @Nullable ImageInfo imageInfo,
                    @Nullable Animatable anim) {
                Log.d(NetworkImageSlideshowPlayer.class.getName(), "onFinalImageSet: Image downloaded");

                // Notify to display the next image.
                synchronized (mImageFetchLock) {
                    mImageFetchLock.notifyAll();
                }
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {

                // Notify to display the next image.
                synchronized (mImageFetchLock) {
                    mImageFetchLock.notifyAll();
                }
            }

            @Override
            public void onFailure(String id, Throwable throwable) {

                // Notify to display the next image.
                synchronized (mImageFetchLock) {
                    mImageFetchLock.notifyAll();
                }
            }
        };
    }

    /**
     * The thread that schedules the slideshow
     *
     * @return {@link Thread}
     */
    private Thread _getSlideshowRunner() {
        return new Thread(new Runnable() {
            @Override
            public void run() {

                // Show the first image.
                SubredditPicItem lPicItem = imageList.get(0);
                Message lMsg = Message.obtain();
                lMsg.obj = lPicItem;
                mSlideDisplayHandler.sendMessage(lMsg);

                // Show images starting from 1
                int lCurrentImgIdx = 1;
                while (isPlaying) {

                    // Wait maximum 5s before displaying the next image.
                    // By then, the current image would have finished downloading.
                    synchronized (mImageFetchLock) {
                        try {
                            mImageFetchLock.wait(5000);
                        } catch (InterruptedException e) {
                            Log.e(NetworkImageSlideshowPlayer.class.getName(), "onFinalImageSet: Interrupted while waiting for image to download");
                        }
                    }

                    // Delay sending the message by 2 secs. This will give the previous image atleast 2 seconds viewer time.
                    lPicItem = imageList.get(lCurrentImgIdx);
                    lMsg = Message.obtain();
                    lMsg.obj = lPicItem;
                    mSlideDisplayHandler.sendMessageDelayed(lMsg, 2000);
                    lCurrentImgIdx++;

                    // Fetch the next page images when you are 5 images away from the last image.
                    // If the next page could not be fetched, reset the index to the beginning.
                    if (lCurrentImgIdx == imageList.size() - 5) {
                        _fetchImages(false);
                    } else if (lCurrentImgIdx >= imageList.size()) {
                        lCurrentImgIdx = 0;
                    }
                }
            }
        });
    }

    /**
     * Class to handle displaying of slideshow image.
     */
    static class ImageDisplayHandler extends Handler {

        private NetworkImageSlideshowPlayer slideshowPlayer;

        public ImageDisplayHandler(NetworkImageSlideshowPlayer slideshowPlayer) {
            this.slideshowPlayer = slideshowPlayer;
        }

        public void handleMessage(Message msg) {

            SubredditPicItem lPicItem = (SubredditPicItem) msg.obj;

            // Create and register a controller to listen to image download events.
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setControllerListener(slideshowPlayer.imageControllerListener)
                    .setUri(Uri.parse(lPicItem.getImageUrlWithResolution(0, 0)))
                    .build();
            slideshowPlayer.mImageView.setController(controller);

            // Title view
            slideshowPlayer.mTitleView.setText(lPicItem.getTitle());
        }
    }
}
