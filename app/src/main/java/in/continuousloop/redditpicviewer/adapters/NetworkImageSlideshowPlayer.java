package in.continuousloop.redditpicviewer.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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

    private Object mImageFetchLock;
    private Context mContext;

    private ControllerListener imageControllerListener;

    /**
     * Create an instance of the NetworkImageSlideshowPlayer
     *
     * @param aContext        - The activity that displays the slideshow
     * @param draweeView      - The imageview to display the image
     * @param titleView       - The textview to display the title
     * @param aMusicTrackItem - The music track that should be played during the slideshow
     * @param aCurrentSection - The section to fetch the images from (HOT, NEW, RISING)
     */
    public NetworkImageSlideshowPlayer(Context aContext,
                                       SimpleDraweeView draweeView,
                                       TextView titleView,
                                       MusicTrackItem aMusicTrackItem,
                                       SubredditSection aCurrentSection) {

        mContext = aContext;

        mImageView = draweeView;
        mTitleView = titleView;

        musicTrackItem = aMusicTrackItem;
        mCurrentSection = aCurrentSection;

        mImageFetchLock = new Object();
        imageList = new ArrayList<>();

        imageControllerListener = _getControllerListener();
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
                Toast.makeText(mContext, aErrorMsg, Toast.LENGTH_LONG);
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

        mSlideshowThread = new Thread(new Runnable() {
            @Override
            public void run() {

                int lCurrentImgIdx = 0;
                try {
                    while (isPlaying) {

                        final SubredditPicItem lPicItem = imageList.get(lCurrentImgIdx);
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                // Create and register a controller to listen to image download events.
                                DraweeController controller = Fresco.newDraweeControllerBuilder()
                                        .setControllerListener(imageControllerListener)
                                        .setUri(Uri.parse(lPicItem.getImageUrlWithResolution(0, 0)))
                                        .build();
                                mImageView.setController(controller);

                                // Title view
                                mTitleView.setText(lPicItem.getTitle());
                            }
                        });

                        // Wait maximum 5s before displaying the next image.
                        // By then, the current image would have finished downloading.
                        synchronized (mImageFetchLock) {
                            mImageFetchLock.wait(5000);
                        }

                        // Sleep for 2secs after the image is fetched before notifying to display the net image.
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Log.e(NetworkImageSlideshowPlayer.class.getName(), "onFinalImageSet: Interrupted while allowing image to display");
                        }
                        lCurrentImgIdx++;

                        if (lCurrentImgIdx == imageList.size() - 5) {
                            _fetchImages(false);
                        } else if (lCurrentImgIdx >= imageList.size()) {
                            lCurrentImgIdx = 0;
                        }
                    }
                } catch (InterruptedException e) {
                    Log.i(NetworkImageSlideshowPlayer.class.getName(), "_startSlideshow: Slideshow interrupted.");
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

    /**
     * Get the {@link ControllerListener} to track image download events.
     * @return {@link ControllerListener}
     */
    private ControllerListener _getControllerListener() {
        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
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

        return controllerListener;
    }
}
