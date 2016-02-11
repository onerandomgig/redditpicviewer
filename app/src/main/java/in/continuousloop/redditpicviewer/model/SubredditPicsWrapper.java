package in.continuousloop.redditpicviewer.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * A wrapper class for the pics subreddit response.
 */
public @Getter @Setter class SubredditPicsWrapper {

    private List<SubredditPicItem> pictures;
    private String next;

    public SubredditPicsWrapper() {
        pictures = new ArrayList<>();
    }

    /**
     * Add a picture item to the list
     * @param aPicture - The picture to be added
     */
    public void addPicture(SubredditPicItem aPicture) {
        pictures.add(aPicture);
    }
}
