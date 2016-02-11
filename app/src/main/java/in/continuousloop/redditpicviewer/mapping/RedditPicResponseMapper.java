package in.continuousloop.redditpicviewer.mapping;

import android.text.format.DateUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import in.continuousloop.redditpicviewer.api.APIManager;
import in.continuousloop.redditpicviewer.model.SubredditPicItem;
import in.continuousloop.redditpicviewer.model.SubredditPicsWrapper;

/**
 * A mapper class to map the json response from the reddit pics api.
 *
 * NOTE: This response mapper handles mapping manually as opposed to using Jackson/GSON
 * because the api response is huge but only a few fields are required.
 */
public class RedditPicResponseMapper {

    private static final String TAG_DATA = "data";
    private static final String TAG_AFTER = "after";
    private static final String TAG_CHILDREN = "children";

    private static final String TAG_DOMAIN = "domain";
    private static final String TAG_ID = "id";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_SCORE = "score";
    private static final String TAG_NUM_COMMENTS = "num_comments";
    private static final String TAG_THUMBNAIL = "thumbnail";
    private static final String TAG_PERMALINK = "permalink";
    private static final String TAG_TITLE = "title";
    private static final String TAG_CREATED = "created";

    private static final String TAG_PREVIEW = "preview";
    private static final String TAG_IMAGES = "images";
    private static final String TAG_SOURCE = "source";
    private static final String TAG_URL = "url";
    private static final String TAG_WIDTH = "width";
    private static final String TAG_HEIGHT = "height";

    /**
     * Map the pictures reddit api response to app object model.
     *
     * @param aResponse - A JSONObject wrapper around the api response
     * @return {@link SubredditPicsWrapper}
     */
    public static SubredditPicsWrapper mapPicsResponse(JSONObject aResponse) {

        JSONObject lChild;
        SubredditPicItem lPicItem;
        SubredditPicsWrapper lWrapper = new SubredditPicsWrapper();

        try {
            JSONObject lData = aResponse.getJSONObject(TAG_DATA);

            // Next page for images.
            lWrapper.setNext(lData.getString(TAG_AFTER));

            // List of images in current page.
            JSONArray lPics = lData.getJSONArray(TAG_CHILDREN);
            for (int lIdx = 0; lIdx < lPics.length(); lIdx++) {

                lChild = lPics.getJSONObject(lIdx).getJSONObject(TAG_DATA);
                lPicItem = new SubredditPicItem();

                lPicItem.setDomain(lChild.getString(TAG_DOMAIN));
                lPicItem.setId(lChild.getString(TAG_ID));
                lPicItem.setAuthor(lChild.getString(TAG_AUTHOR));
                lPicItem.setScore(lChild.getInt(TAG_SCORE));
                lPicItem.setNumComments(lChild.getInt(TAG_NUM_COMMENTS));
                lPicItem.setThumbNail(lChild.getString(TAG_THUMBNAIL));
                lPicItem.setPermaLink(APIManager.getInstance().getBaseUrl() + lChild.getString(TAG_PERMALINK));
                lPicItem.setTitle(lChild.getString(TAG_TITLE));
                lPicItem.setCreatedOn(DateUtils.getRelativeTimeSpanString((long) lChild.getDouble(TAG_CREATED) * 1000).toString());

                if (lChild.has(TAG_PREVIEW)) {
                    JSONObject lPreview = lChild.getJSONObject(TAG_PREVIEW);
                    JSONArray lImages = lPreview.getJSONArray(TAG_IMAGES);
                    JSONObject lOriginalImage = lImages.getJSONObject(0).getJSONObject(TAG_SOURCE);

                    String lImgUrl = lOriginalImage.getString(TAG_URL);
                    int lImgWidth = lOriginalImage.getInt(TAG_WIDTH);
                    int lImgHeight = lOriginalImage.getInt(TAG_HEIGHT);

                    lPicItem.addPictureWithResolution(lImgWidth, lImgHeight, lImgUrl);
                } else {
                    lPicItem.addPictureWithResolution(0, 0, lPicItem.getThumbNail());
                }

                lWrapper.addPicture(lPicItem);
            }
        } catch (Exception ex) {
            Log.e(RedditPicResponseMapper.class.getName(), "mapPicsResponse: Error parsing pics response", ex);
        }

        return lWrapper;
    }
}
