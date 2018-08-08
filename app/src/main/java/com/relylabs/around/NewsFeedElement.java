package com.relylabs.around;

/**
 * Created by nagendra on 8/7/18.
 */

public class NewsFeedElement  {
        private String bannerImageURL;
        private String tag;

        public NewsFeedElement(String tag, String bannerImageURL) {
            this.bannerImageURL = bannerImageURL;
            this.tag = tag;
        }

        public String getBanngerImageURL() {
            return bannerImageURL;
        }

        public String getTag() {
            return tag;
        }
}

