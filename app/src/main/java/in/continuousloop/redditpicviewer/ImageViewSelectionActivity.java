package in.continuousloop.redditpicviewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import in.continuousloop.redditpicviewer.adapters.PictureGridAdapter;
import in.continuousloop.redditpicviewer.api.APIManager;
import in.continuousloop.redditpicviewer.constants.AppConstants;
import in.continuousloop.redditpicviewer.listeners.IImageSelectionObserver;
import in.continuousloop.redditpicviewer.model.SubredditPicItem;
import in.continuousloop.redditpicviewer.model.SubredditSection;

/**
 * The imageview selection activity to select pictures to create video.
 */
public class ImageViewSelectionActivity extends AppCompatActivity implements IImageSelectionObserver {

    private List<SubredditPicItem> mSelectedPics;
    private Button mNextButton;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);

        mSelectedPics = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        // Load the music selection activity
        mNextButton = (Button) findViewById(R.id.toolbarNext);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), MusicSelectionActivity.class);
                intent.putExtra(AppConstants.Extras.PIC_LIST, (ArrayList) mSelectedPics);
                startActivity(intent);
            }
        });

        int lCurrentSelectedTabIdx = getIntent().getIntExtra(AppConstants.Extras.CURRENT_SELECTED_TAB, 0);
        SubredditSection lCurrentSelectedTab = APIManager.getInstance().getSubredditSections().get(lCurrentSelectedTabIdx);

        ProgressBar lProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        GridView lPictureGridView = (GridView) findViewById(R.id.pictureGalleryGrid);
        PictureGridAdapter lPicGridAdapter = new PictureGridAdapter(this, lProgressBar, R.layout.picture_grid_item, lCurrentSelectedTab, this);
        lPictureGridView.setAdapter(lPicGridAdapter);

        lPicGridAdapter.fetchPics();
    }

    /**
     * Set the visibility of the next button
     * @param shouldEnable - Display next button when true. Hide when false.
     */
    public void enableNextAction (boolean shouldEnable) {
        if (shouldEnable) {
            mNextButton.setVisibility(View.VISIBLE);
        } else {
            mNextButton.setVisibility(View.GONE);
        }
    }

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
