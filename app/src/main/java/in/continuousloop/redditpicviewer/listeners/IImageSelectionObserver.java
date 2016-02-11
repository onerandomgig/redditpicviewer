package in.continuousloop.redditpicviewer.listeners;

/**
 * An interface for image listing views that want to provide image selection functionality
 */
public interface IImageSelectionObserver {
    void enableNextAction(boolean shouldEnable);
}
