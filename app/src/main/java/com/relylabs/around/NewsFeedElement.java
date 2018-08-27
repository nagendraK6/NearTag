package com.relylabs.around;

/**
 * Created by nagendra on 8/7/18.
 */

public class NewsFeedElement  {
        private Integer postId;
        private String bannerImageURL;
        private String profileImageURL;
        private String tag;
        private Boolean hasPublished;
        private String userPostText;
        private String galleryImageFile;
        private Boolean hasLiked;

        public NewsFeedElement(
                Integer postId,
                String tag,
                String bannerImageURL,
                String profileImageURL,
                Boolean hasLiked,
                Boolean hasPublished,
                String messageText,
                String galleryImageFile
        ) {
            this.postId = postId;
            this.bannerImageURL = bannerImageURL;
            this.tag = tag;
            this.profileImageURL = profileImageURL;
            this.hasPublished = hasPublished;
            this.userPostText =  messageText;
            this.galleryImageFile = galleryImageFile;
            this.hasLiked = hasLiked;
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

        public void setBannerImageURL(String s) {
            bannerImageURL = s;
        }

        public  Integer getPostId() {
            return postId;
        }

        public void setHasLiked() {
            hasLiked = true;
        }

        public  Boolean getHasLiked() {
            return hasLiked;
        }
}

