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
        private Integer userId;
        private String userName;

        public NewsFeedElement(
                Integer postId,
                String tag,
                String bannerImageURL,
                String profileImageURL,
                Boolean hasLiked,
                Boolean hasPublished,
                String messageText,
                String galleryImageFile,
                Integer userId,
                String userName
        ) {
            this.postId = postId;
            this.bannerImageURL = bannerImageURL;
            this.tag = tag;
            this.profileImageURL = profileImageURL;
            this.hasPublished = hasPublished;
            this.userPostText =  messageText;
            this.galleryImageFile = galleryImageFile;
            this.hasLiked = hasLiked;
            this.userId = userId;
            this.userName = userName;
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

        public  Integer getUserId() { return userId; }

        public  String getUserName() { return userName; }
}

