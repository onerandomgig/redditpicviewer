package in.continuousloop.redditpicviewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import in.continuousloop.redditpicviewer.adapters.MusicListAdapter;
import in.continuousloop.redditpicviewer.constants.AppConstants;

/**
 * The music selection activity to select the music for video.
 */
public class MusicSelectionActivity extends AppCompatActivity {

    private MusicListAdapter mMusicListAdapter;
    private Button mStartSlideshowButton;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_select);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        ProgressBar lProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        mMusicListAdapter = new MusicListAdapter(getApplicationContext(), lProgressBar,R.layout.music_item);
        mMusicListAdapter.fetchMusic();

        ListView lMusicListView = (ListView)findViewById(R.id.musicGalleryList);
        lMusicListView.setAdapter(mMusicListAdapter);

        final int lCurrentSelectedTab = getIntent().getIntExtra(AppConstants.Extras.CURRENT_SELECTED_TAB, 0);

        // Start slideshow
        mStartSlideshowButton =  (Button) findViewById(R.id.toolbarStart);
        mStartSlideshowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), MusicSlideshowActivity.class);
                intent.putExtra(AppConstants.Extras.CURRENT_SELECTED_TAB, lCurrentSelectedTab);
                intent.putExtra(AppConstants.Extras.CURRENT_SELECTED_TRACK, mMusicListAdapter.getSelectedTrack());
                startActivity(intent);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void onPause() {
        mMusicListAdapter.stopAndReleaseAudioPlayer();
        super.onPause();
    }

    /**
     * {@inheritDoc}
     */
    public void onStop() {
        mMusicListAdapter.stopAndReleaseAudioPlayer();
        super.onStop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
