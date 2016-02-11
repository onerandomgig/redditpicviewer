package in.continuousloop.redditpicviewer.listeners;

import in.continuousloop.redditpicviewer.model.SubredditPicsWrapper;

/**
 * Listener to handle pictures response.
 */
public interface PicSubredditResponseListener {

    void handleSuccess(SubredditPicsWrapper picsResponse);
    void handleError(String aErrorMsg);
}
