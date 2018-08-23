package com.relylabs.around;

/**
 * Created by nagendra on 8/7/18.
 */

public class NewsFeedElement  {
        private String bannerImageURL;
        private String profileImageURL;
        private String tag;
        private Boolean hasPublished;
        private String userPostText;
        private String galleryImageFile;

        public NewsFeedElement(
                String tag,
                String bannerImageURL,
                String profileImageURL,
                Boolean hasPublished,
                String messageText,
                String galleryImageFile
        ) {
            this.bannerImageURL = bannerImageURL;
            this.tag = tag;
            this.profileImageURL = profileImageURL;
            this.hasPublished = hasPublished;
            this.userPostText =  messageText;
            this.galleryImageFile = galleryImageFile;
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

        public String getUserPostText() {
        return userPostText;
    }

        public String getGalleryImageFile() {
        return galleryImageFile;
    }

        public Boolean getHasPublished() { return hasPublished; }

        public  void  setHasPublished(boolean b) {
            hasPublished = true;
        }
}

