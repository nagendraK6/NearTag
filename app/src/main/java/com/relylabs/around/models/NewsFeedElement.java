package com.relylabs.around.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

/**
 * Created by nagendra on 8/7/18.
 */

public class NewsFeedElement  extends Model {

        @Column(name = "postId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
        public Integer postId;

        @Column(name = "bannerImageURL")
        public String bannerImageURL;


        @Column(name = "profileImageURL")
        public String profileImageURL;


        @Column(name = "tag")
        public String tag;


        @Column(name = "hasPublished")
        public Boolean hasPublished;


        @Column(name = "userPostText")
        public String userPostText;

        @Column(name = "galleryImageFile")
        public String galleryImageFile;


        @Column(name = "hasLiked")
        public Boolean hasLiked;

        @Column(name = "userId")
        public Integer userId;

        @Column(name = "userName")
        public String userName;

        @Column(name = "timestamp")
        public Long timestamp;

        public NewsFeedElement() {
            super();
            this.postId = -1;
            this.bannerImageURL = "";
            this.tag = "";
            this.profileImageURL = "";
            this.hasPublished = false;
            this.userPostText =  "";
            this.galleryImageFile = "";
            this.hasLiked = false;
            this.userId = -1;
            this.userName = "";
            this.timestamp = new Long(0);
            this.save();
        }

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
                String userName,
                Long timestamp
        ) {
            super();
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
            this.timestamp = timestamp;
            this.save();
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

        public  Long getTimeStamp() { return timestamp; }
}

