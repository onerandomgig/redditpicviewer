package in.continuousloop.redditpicviewer.api.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import in.continuousloop.redditpicviewer.constants.AppConstants;

/**
 * A simple wrapper around {@link StringRequest} to make an API call to fetch iTunes music
 */
public class ITunesMusicAPIRequest extends StringRequest {

    private static final String ITUNES_MUSIC_API = "http://superscriptapp.com/mz/audio/itunes/music-search";

    public ITunesMusicAPIRequest(Response.Listener aSuccessListener, Response.ErrorListener aErrorListener) {
        super(Request.Method.POST, ITUNES_MUSIC_API, aSuccessListener, aErrorListener);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();
        params.put("query", AppConstants.ITUNES_QUERY);

        return params;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();
        params.put("X-MZ-Token", AppConstants.ACCESS_TOKEN);
        return params;
    }
}