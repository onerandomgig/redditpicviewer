package in.continuousloop.redditpicviewer.config;

import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;

import in.continuousloop.redditpicviewer.api.NetworkAccessor;

/**
 * This class initializes libraries that are used throughout the application.
 */
public class Configuration {

    private static boolean isInitialized = false;

    /**
     * Initializes libraries used throughout the application. Its safe to call init multiple times.
     *
     * @param aContext - Android application context
     */
    public static void init(Context aContext) {

        if (isInitialized) {
            return;
        }

        isInitialized = true;

        // Initialize Fresco library
        Fresco.initialize(aContext);

        // Initialize NetworkAccessor
        NetworkAccessor.getInstance().init(aContext);
    }
}
