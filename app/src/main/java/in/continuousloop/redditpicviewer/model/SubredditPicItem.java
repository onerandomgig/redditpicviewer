package in.continuousloop.redditpicviewer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Model for a single picture item in the pics subreddit
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public @Getter @Setter class SubredditPicItem implements Serializable {

    private String id;
    private String title;
    private String thumbNail;
    private String permaLink;
    private String author;
    private String domain;

    // List of all image urls with different resolutions.
    private List<PicResolution> imageUrls;

    private int score;
    private int numComments;
    private String createdOn;

    public SubredditPicItem() {
        imageUrls = new ArrayList<>(0);
    }

    /**
     * Get an image url with the specified resolution.
     * If an image with the specified resolution is not available, return the first one in the list.
     *
     * @param aWidth   - Width of image
     * @param aHeight  - Height of image
     *
     * @return Image URL. NULL if no images are available.
     */
    public String getImageUrlWithResolution(int aWidth, int aHeight) {
        PicResolution lPic = new PicResolution(aWidth, aHeight, null);

        if (imageUrls.contains(lPic)) {
            lPic = imageUrls.get(imageUrls.indexOf(lPic));

        } else if (imageUrls != null && imageUrls.size() != 0){
            lPic = imageUrls.get(0);
        }

        return lPic.getUrl();
    }

    /**
     * Add a picture url to the list of preview images.
     *
     * @param aWidth  - The width of the image
     * @param aHeight - The height of the image
     * @param aPicUrl - The image url
     */
    public void addPictureWithResolution(int aWidth, int aHeight, String aPicUrl) {
        PicResolution lPic = new PicResolution(aWidth, aHeight, aPicUrl);
        imageUrls.add(lPic);
    }

    /**
     * Wrapper class to wrap pic url with resolution
     */
    class PicResolution implements Serializable {
        private String resolutionIdentifier;
        private String url;

        public PicResolution(int aWidth, int aHeight, String aUrl) {
            resolutionIdentifier = aWidth + "x" + aHeight;
            url = aUrl;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PicResolution that = (PicResolution) o;

            return resolutionIdentifier.equals(that.resolutionIdentifier);

        }

        @Override
        public int hashCode() {
            return resolutionIdentifier.hashCode();
        }
    }
}
