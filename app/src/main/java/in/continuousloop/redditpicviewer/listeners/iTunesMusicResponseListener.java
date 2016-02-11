package in.continuousloop.redditpicviewer.listeners;

import java.util.List;

import in.continuousloop.redditpicviewer.model.MusicTrackItem;

/**
 * Listener to handle pictures response.
 */
public interface iTunesMusicResponseListener {

    void handleSuccess(List<MusicTrackItem> musicTrackListResponse);
    void handleError(String aErrorMsg);
}
