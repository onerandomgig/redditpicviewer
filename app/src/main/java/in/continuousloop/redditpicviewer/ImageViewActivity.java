package in.continuousloop.redditpicviewer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.List;

import in.continuousloop.redditpicviewer.adapters.ImageViewAdapter;
import in.continuousloop.redditpicviewer.constants.AppConstants;
import in.continuousloop.redditpicviewer.model.SubredditPicItem;

/**
 * An image view activity to show the high resolution image.
 */
public class ImageViewActivity extends AppCompatActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        List<SubredditPicItem> lPicsList = (List<SubredditPicItem>)getIntent().getSerializableExtra(AppConstants.Extras.PIC_LIST);
        int lPosition = getIntent().getIntExtra(AppConstants.Extras.PIC_ITEM_POSITION, 0);

        ViewPager lViewPager = (ViewPager) findViewById(R.id.imageViewPager);
        ImageViewAdapter lImageViewAdapter = new ImageViewAdapter(getSupportFragmentManager(), lPicsList);
        lViewPager.setAdapter(lImageViewAdapter);
        lViewPager.setCurrentItem(lPosition);
    }
}
