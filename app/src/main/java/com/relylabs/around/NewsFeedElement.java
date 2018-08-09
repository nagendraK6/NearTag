package com.relylabs.around;

/**
 * Created by nagendra on 8/7/18.
 */

public class NewsFeedElement  {
        private String bannerImageURL;
        private String profileImageURL;
        private String tag;

        public NewsFeedElement(String tag, String bannerImageURL, String profileImageURL) {
            this.bannerImageURL = bannerImageURL;
            this.tag = tag;
            this.profileImageURL = profileImageURL;
        }



        public String getBanngerImageURL() {
            return bannerImageURL;
        }

        public String getProfileImageURL() {
        return profileImageURL;
    }

        public String getTag() {
            return tag;
        }
}

