package in.continuousloop.redditpicviewer.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.squareup.okhttp.OkHttpClient;

/**
 * This class provides methods to queue and process network requests.
 * The underlying network access library is Volley. OkHttp is used as the Http client.
 */
public class NetworkAccessor {

    private static boolean isInitialized;
    private static NetworkAccessor mSingletonInstance;

    private RequestQueue mRequestQueue;

    private NetworkAccessor() {}

    /**
     * Get an instance (Singleton) of the NetworkAccessor.
     *
     * @return {@link NetworkAccessor}
     */
    public static synchronized NetworkAccessor getInstance() {
        if (mSingletonInstance == null) {
            mSingletonInstance = new NetworkAccessor();
        }

        return mSingletonInstance;
    }

    /**
     * Initializes the underlying request queues to handle requests. It is safe to call init multiple times.
     *
     * @param aContext - {@link Context} Android application context
     */
    public void init(Context aContext) {
        if (!isInitialized && mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(aContext, new OkHttpStack(new OkHttpClient()));
            isInitialized = true;
        }
    }

    /**
     * Returns a Volley request queue for creating network requests
     *
     * @return {@link com.android.volley.RequestQueue}
     * @throws IllegalStateException - If init is not called
     */
    public RequestQueue getVolleyRequestQueue() throws IllegalStateException {

        if (!isInitialized) {
            throw new IllegalStateException("NetworkAccessor is not initialized. Are you sure you called init?");
        }

        return mRequestQueue;
    }

    /**
     * Adds a request to the Volley request queue with a given tag
     *
     * @param request - the request to be added
     * @param tag     - the tag identifying the request
     */
    public void addRequest(Request<?> request, String tag) {
        request.setTag(tag);
        getVolleyRequestQueue().add(request);
    }

    /**
     * Cancels all the request in the Volley queue for a given tag
     *
     * @param tag associated with the Volley requests to be cancelled
     */
    public void cancelAllRequests(String tag) {
        getVolleyRequestQueue().cancelAll(tag);
    }
}
