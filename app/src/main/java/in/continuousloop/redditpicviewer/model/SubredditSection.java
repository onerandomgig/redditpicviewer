package in.continuousloop.redditpicviewer.model;

import lombok.Getter;
import lombok.Setter;

/**
 * A wrapper around a subreddit section (Hot, New, Rising etc)
 */
public @Getter @Setter class SubredditSection {

    private String tag;
    private String title;
    private String sectionAPIUrl;

    public SubredditSection(String aTag, String aTitle, String aAPIUrl) {
        tag = aTag;

        title = aTitle;
        sectionAPIUrl = aAPIUrl;
    }
}
