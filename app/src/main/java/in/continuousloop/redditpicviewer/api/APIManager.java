package in.continuousloop.redditpicviewer.api;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import in.continuousloop.redditpicviewer.api.requests.ITunesMusicAPIRequest;
import in.continuousloop.redditpicviewer.constants.AppConstants;
import in.continuousloop.redditpicviewer.listeners.PicSubredditResponseListener;
import in.continuousloop.redditpicviewer.listeners.iTunesMusicResponseListener;
import in.continuousloop.redditpicviewer.mapping.RedditPicResponseMapper;
import in.continuousloop.redditpicviewer.model.MusicTrackItem;
import in.continuousloop.redditpicviewer.model.SubredditPicsWrapper;
import in.continuousloop.redditpicviewer.model.SubredditSection;

/**
 * This class handles all API requests. It uses the {@link NetworkAccessor} to delegate handling of
 * requests.
 */
public class APIManager {

    private static APIManager mSingletonInstance;

    private NetworkAccessor mNetworkAccessor;

    private static final String BASE_URL = "http://reddit.com";

    private static final String HOT_PICS_SUBREDDIT_API = "http://www.reddit.com/r/pics/hot.json";
    private static final String NEW_PICS_SUBREDDIT_API = "http://www.reddit.com/r/pics/new.json";
    private static final String RISING_PICS_SUBREDDIT_API = "http://www.reddit.com/r/pics/rising.json";

    private APIManager() {
        mNetworkAccessor = NetworkAccessor.getInstance();
    }

    /**
     * Get the singleton instance of the APIManager.
     *
     * @return
     */
    public static synchronized APIManager getInstance() {
        if (mSingletonInstance == null) {
            mSingletonInstance = new APIManager();
        }

        return mSingletonInstance;
    }

    public String getBaseUrl() {
        return BASE_URL;
    }

    /**
     * Get the supported sections.
     *
     * @return
     */
    public List<SubredditSection> getSubredditSections() {

        List<SubredditSection> subredditSections = new ArrayList<>();
        subredditSections.add(new SubredditSection("hot", "Reddit: Hot /r/pics", HOT_PICS_SUBREDDIT_API));
        subredditSections.add(new SubredditSection("new", "Reddit: New /r/pics", NEW_PICS_SUBREDDIT_API));
        subredditSections.add(new SubredditSection("rising", "Reddit: Rising /r/pics", RISING_PICS_SUBREDDIT_API));

        return subredditSections;
    }

    /**
     * Get pictures data from the pics subreddit for the specified section (Hot, New, Rising etc)
     *
     * @param aSection - The subreddit section to fetch the pictures for
     * @param afterId  - The id for the next page
     */
    public void getPicsSubredditData(SubredditSection aSection, String afterId, final PicSubredditResponseListener aListener) {

        // Sanity check.
        if (aSection == null || aListener == null) {
            return;
        }

        // Check if we have to fetch the first page or the next page.
        String lAPI = aSection.getSectionAPIUrl();
        if (afterId != null) {
            lAPI = lAPI + "?after=" + afterId;
        }

        // Fetch the data and notify the listener with the fetched data.
        JsonObjectRequest picsRequest =
                new JsonObjectRequest(Request.Method.GET, lAPI, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        SubredditPicsWrapper lPicsWrapper = RedditPicResponseMapper.mapPicsResponse(response);
                        aListener.handleSuccess(lPicsWrapper);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Deal with the error here
                        String lMsg;
                        if (error != null && error.getMessage() != null) {
                            lMsg = error.getMessage();
                        } else {
                            lMsg = "Failed to get pictures.";
                        }

                        aListener.handleError(lMsg);
                    }
                });

        picsRequest.setShouldCache(false);
        picsRequest.setRetryPolicy(new DefaultRetryPolicy(
                30 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Enqueue network requests.
        mNetworkAccessor.addRequest(picsRequest, aSection.getTag());

    }

    public void getITunesAudio(final iTunesMusicResponseListener aListener, String aRequestTag) {
        // Sanity check.
        if (aListener == null) {
            return;
        }

        final ObjectMapper objectMapper = new ObjectMapper();

        ITunesMusicAPIRequest iTunesRequest = new ITunesMusicAPIRequest(new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                List<MusicTrackItem> audioList;
                try {
                    audioList = objectMapper.readValue(response,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, MusicTrackItem.class));
                    aListener.handleSuccess(audioList);
                } catch (IOException e) {
                    aListener.handleError("Failed to fetch music tracks.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                aListener.handleError(error.toString());
            }
        });
        iTunesRequest.setShouldCache(true);

        // Enqueue network requests.
        mNetworkAccessor.addRequest(iTunesRequest, aRequestTag);
    }
}
