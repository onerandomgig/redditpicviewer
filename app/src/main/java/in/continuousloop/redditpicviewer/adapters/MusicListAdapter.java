package in.continuousloop.redditpicviewer.adapters;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import in.continuousloop.redditpicviewer.R;
import in.continuousloop.redditpicviewer.api.APIManager;
import in.continuousloop.redditpicviewer.listeners.iTunesMusicResponseListener;
import in.continuousloop.redditpicviewer.model.MusicTrackItem;

/**
 * Adapter to handle fetching of music tracks.
 */
public class MusicListAdapter extends ArrayAdapter<MusicTrackItem> implements MediaPlayer.OnPreparedListener {

    private int mItemLayout;
    private LayoutInflater mInflater;
    private ProgressBar mProgressbar;

    private boolean isFetching;
    private MusicTrackItem selectedTrack;
    private List<MusicTrackItem> musicTrackItems;

    private APIManager mAPIManager;
    private static final String ITUNES_MUSIC_REQUEST_TAG = "iTunes";

    private View currentlySelectedView;
    private MediaPlayer musicTrackPlayer;

    public MusicListAdapter(Context context, ProgressBar progressBar, int resource) {
        super(context, resource);

        mAPIManager = APIManager.getInstance();
        musicTrackItems = new ArrayList<>();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mProgressbar = progressBar;
        mItemLayout = resource;

        musicTrackPlayer = new MediaPlayer();
        musicTrackPlayer.setLooping(true);
        musicTrackPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    /**
     * Fetch the music tracks from iTunes
     */
    public void fetchMusic() {

        mProgressbar.setVisibility(View.VISIBLE);

        if (isFetching) {
            return;
        }
        isFetching = true;

        mAPIManager.getITunesAudio(new iTunesMusicResponseListener() {
            @Override
            public void handleSuccess(List<MusicTrackItem> musicTrackListResponse) {

                mProgressbar.setVisibility(View.GONE);
                isFetching = false;

                if (musicTrackListResponse != null && musicTrackListResponse.size() > 0) {
                    selectedTrack = musicTrackListResponse.get(0);
                    musicTrackItems.addAll(musicTrackListResponse);
                    MusicListAdapter.this.notifyDataSetChanged();
                }
            }

            @Override
            public void handleError(String aErrorMsg) {
                mProgressbar.setVisibility(View.GONE);
                isFetching = false;
            }
        }, ITUNES_MUSIC_REQUEST_TAG);
    }

    /**
     * Get the number of music tracks
     *
     * @return - Number of music tracks
     */
    @Override
    public int getCount() {
        return musicTrackItems.size();
    }

    /**
     * Get the music track item at the specified position
     *
     * @param position - The index of the music track item
     * @return {@link MusicTrackItem}
     */
    @Override
    public MusicTrackItem getItem(int position) {
        return musicTrackItems.get(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View lMusicItemView;
        if (convertView == null) {
            lMusicItemView = mInflater.inflate(mItemLayout, null);
        } else {
            lMusicItemView = convertView;
        }
        lMusicItemView.setTag(new Boolean(false));

        final View lThisView = lMusicItemView;
        final MusicTrackItem lCurrentItem = getItem(position);

        // Thumbnail image view.
        SimpleDraweeView lMusicThumbnailView = (SimpleDraweeView) lMusicItemView.findViewById(R.id.trackIcon);
        lMusicThumbnailView.setImageURI(Uri.parse(lCurrentItem.getImg()));

        lMusicItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View aView) {

                // If this track is currently playing, pause it.
                if (((Boolean)aView.getTag()).booleanValue()) {
                    ((ImageView)aView.findViewById(R.id.playButton)).setImageResource(R.drawable.play);
                    aView.setTag(new Boolean(false));
                    pauseAudioPlayer();
                }
                // Play this track.
                else {

                    // If this is a new track that has to be played, then unset the currently playing track.
                    _resetPreviouslySelectedItemView();

                    // This is the currently playing track now
                    currentlySelectedView = lThisView;

                    // Update the view for the currently selected track.
                    _updateCurrentlySelectedItemView();

                    // Play the currently selected track
                    selectedTrack = lCurrentItem;
                    _playSelectedTrack();
                }
            }
        });

        // Track Title view
        TextView lTitleView = (TextView) lMusicItemView.findViewById(R.id.trackName);
        lTitleView.setText(lCurrentItem.getTitle());

        return lMusicItemView;
    }

    /**
     * Get the selected music track. The first track is selected by default.
     */
    public MusicTrackItem getSelectedTrack() {
        return selectedTrack;
    }

    /**
     * Update the image icon to show a pause button when the player is about to play
     * @param mp - The MediaPlayer instance that is ready to play
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        musicTrackPlayer.start();
    }

    /**
     * Pause the current track the media player is playing
     */
    public void pauseAudioPlayer() {
        musicTrackPlayer.pause();
    }

    /**
     * Stop and release the audio player
     */
    public void stopAndReleaseAudioPlayer() {
        if (musicTrackPlayer != null) {
            musicTrackPlayer.release();
        }
    }

    /**
     * Play the currently selected music track.
     */
    private void _playSelectedTrack() {

        try {
            musicTrackPlayer.reset();
            musicTrackPlayer.setDataSource(selectedTrack.getUrl());
            musicTrackPlayer.prepareAsync();
            musicTrackPlayer.setOnPreparedListener(this);
        } catch (Exception e) {
            Log.e(MusicListAdapter.class.getName(), "_playSelectedTrack: Error playing selected track " + selectedTrack.getUrl());
        }
    }

    private void _resetPreviouslySelectedItemView() {
        // If this is a new track that has to be played, then unset the currently playing track.
        if (currentlySelectedView != null) {
            // Reset the border color of the previously selected track thumbnail
            RoundingParams lRoundingParams = RoundingParams.fromCornersRadius(2);
            lRoundingParams.setBorder(getContext().getResources().getColor(R.color.shadowColor), 1.0f);
            ((SimpleDraweeView) currentlySelectedView.findViewById(R.id.trackIcon)).getHierarchy().setRoundingParams(lRoundingParams);

            ImageView lTrackPlayIcon = (ImageView)currentlySelectedView.findViewById(R.id.playButton);
            lTrackPlayIcon.setImageResource(R.drawable.play);
            lTrackPlayIcon.setTag(new Boolean(false));
        }
    }

    private void _updateCurrentlySelectedItemView() {
        // Change the border color of the selected track thumbnail
        RoundingParams lRoundingParams = RoundingParams.fromCornersRadius(2);
        lRoundingParams.setBorder(getContext().getResources().getColor(R.color.colorAccent), 4.0f);
        ((SimpleDraweeView)currentlySelectedView.findViewById(R.id.trackIcon)).getHierarchy().setRoundingParams(lRoundingParams);

        ImageView lTrackPlayIcon = (ImageView)currentlySelectedView.findViewById(R.id.playButton);
        lTrackPlayIcon.setImageResource(R.drawable.pause);
        lTrackPlayIcon.setTag(new Boolean(true));
    }
}
