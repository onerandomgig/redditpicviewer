package in.continuousloop.redditpicviewer;

import android.app.Application;

import in.continuousloop.redditpicviewer.config.Configuration;

/**
 * Application singleton to perform app startup activities
 */
public class RedditPicViewerApplication extends Application {

    public void onCreate() {
        super.onCreate();

        Configuration.init(this);
    }
}
