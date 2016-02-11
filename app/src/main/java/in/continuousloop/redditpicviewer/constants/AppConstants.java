package in.continuousloop.redditpicviewer.constants;

/**
 * Constants to identify passing data between activities, app restrictions and other app wide constants.
 */
public class AppConstants {

    public static final int MAX_IMAGE_TITLE_LENGTH = 64;

    public static final int MAX_SELECT_IMAGE = 15;
    public static final int MIN_SELECT_IMAGE = 10;

    public static class Extras {
        public static final String WEB_URL = "web-url";
        public static final String PIC_LIST = "pics-list";
        public static final String PIC_ITEM_POSITION = "pic-item-pos";
        public static final String CURRENT_SELECTED_TAB = "current-selected-tab";
        public static final String CURRENT_SELECTED_TRACK = "current-selected-track";
    }

    public static final String ITUNES_QUERY = "popular";
    public static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ7XCJpZFwiOlwiOWVkNDMxZWItYTQ5Zi0xMWU1LWI3NDQtMGE5M2Q4ZDQwNjExXCJ9IiwiaXNzIjoibWV6ZXRoZXMtYXBwIiwiZXhwIjoxNDU2MTIwODg4LCJpYXQiOjE0NTQ2NDk2NTl9.N005p07kT8B018LnWgYzU2jnZ4dMtKanVpzIZvdM6UU";
}
