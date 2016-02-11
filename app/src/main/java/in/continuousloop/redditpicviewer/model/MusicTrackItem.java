package in.continuousloop.redditpicviewer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * Model for a single audio item
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public @Getter @Setter class MusicTrackItem implements Serializable {

    private String id;
    private String img;
    private String url;
    private String title;
}
