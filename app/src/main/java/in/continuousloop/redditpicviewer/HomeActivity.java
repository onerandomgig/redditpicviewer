package in.continuousloop.redditpicviewer;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import in.continuousloop.redditpicviewer.api.APIManager;
import in.continuousloop.redditpicviewer.constants.AppConstants;
import in.continuousloop.redditpicviewer.fragments.PictureListingFragment;
import in.continuousloop.redditpicviewer.model.SubredditSection;

/**
 * The home activity to show the picture listing.
 */
public class HomeActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private APIManager mAPIManager;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.reddit_toolbar_icon));

        // Custom view for tab
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(_createTab(tabLayout, R.drawable.fa_fire, R.string.section_hot), 0);
        tabLayout.addTab(_createTab(tabLayout, R.drawable.fa_asterisk, R.string.section_new), 1);
        tabLayout.addTab(_createTab(tabLayout, R.drawable.fa_bolt, R.string.section_rising), 2);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mAPIManager = APIManager.getInstance();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Load the music selection activity to enable music selection for slideshow.
        FloatingActionButton lFab = (FloatingActionButton) findViewById(R.id.fab);
        lFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), MusicSelectionActivity.class);
                intent.putExtra(AppConstants.Extras.CURRENT_SELECTED_TAB, mViewPager.getCurrentItem());
                startActivity(intent);
            }
        });
    }

    /**
     * Create a tab and set the specified icon and text
     *
     * @param tabLayout     - The tablayout to which the tab has to be added
     * @param aIconDrawable - The icon drawable id for the tab icon
     * @param aTabTextId    - The text resource id for the tab text
     *
     * @return {@link android.support.design.widget.TabLayout.Tab}
     */
    private TabLayout.Tab _createTab(TabLayout tabLayout, int aIconDrawable, int aTabTextId) {
        TabLayout.Tab lTab = tabLayout.newTab();

        View lTabView = getLayoutInflater().inflate(R.layout.item_tabs,null);
        ImageView tabIcon = (ImageView) lTabView.findViewById(R.id.tabIcon);
        TextView tabText = (TextView) lTabView.findViewById(R.id.tabText);

        tabIcon.setImageResource(aIconDrawable);
        tabText.setText(getResources().getString(aTabTextId));
        lTab.setCustomView(lTabView);

        return lTab;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private List<SubredditSection> sections;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            sections = mAPIManager.getSubredditSections();
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            PictureListingFragment lFragment = new PictureListingFragment();
            lFragment.setFragmentSection(sections.get(position));

            return lFragment;
        }

        @Override
        public int getCount() {
            // Show as many sections as configured.
            return sections.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            if (position >= sections.size()) {
                return null;
            }

            return sections.get(position).getTitle();

        }
    }
}
