package com.relylabs.around;

/**
 * Created by nagendra on 8/11/18.
 */

public class ComposerImageElement {
    private String thumbnail_url;

    private String canvas_url;


    public ComposerImageElement(String thumbnail_url,
                                String canvas_url) {
        this.canvas_url = canvas_url;
        this.thumbnail_url = thumbnail_url;
    }

    public String getThumbnailURL() {
        return this.thumbnail_url;
    }

    public String getCanvasURL() {
        return this.canvas_url;
    }

}