package com.neartag.in.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by nagendra on 8/7/18.
 */
@Table(name = "NewsFeedElement")
public class NewsFeedElement  extends Model {

        @Column(name = "postId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
        public Integer postId;

        @Column(name = "bannerImageURLLow")
        public String bannerImageURLLow;


        @Column(name = "bannerImageURLHigh")
        public String bannerImageURLHigh;

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

        @Column(name = "userLocation")
        public String userLocation;

        @Column(name = "timestamp")
        public Long timestamp;

        @Column(name = "commentsCount")
        public String commentsCount;

        @Column(name = "likesCount")
        public String likesCount;

        @Column(name = "sharesCount")
        public String sharesCount;

        @Column(name = "Width")
        public Integer Width;

        @Column(name = "Height")
        public Integer Height;

        public String LearnMoreLink;

        public Integer totalDurationCount;

        public Boolean isSystemUser;

        public NewsFeedElement() {
            super();
            this.postId = -1;
            this.bannerImageURLLow = "";
            this.bannerImageURLHigh = "";
            this.tag = "";
            this.profileImageURL = "";
            this.hasPublished = false;
            this.userPostText =  "";
            this.galleryImageFile = "";
            this.hasLiked = false;
            this.userId = -1;
            this.userName = "";
            this.timestamp = Long.valueOf(0);
            this.commentsCount = "";
            this.sharesCount = "";
            this.likesCount = "";
            this.userLocation = "";
            this.totalDurationCount = 0;
            this.Width = 0;
            this.Height = 0;
            this.isSystemUser = false;
            this.LearnMoreLink = "";
        }

        public NewsFeedElement(
                Integer postId,
                String tag,
                String bannerImageURLLow,
                String bannerImageURLHigh,
                String profileImageURL,
                Boolean hasLiked,
                Boolean hasPublished,
                String messageText,
                String galleryImageFile,
                Integer userId,
                String userName,
                String userLocation,
                Long timestamp,
                String likesCount,
                String sharesCount,
                String commentsCount,
                Integer Width,
                Integer Height,
                String LearnMoreLink

                ) {
            super();
            this.postId = postId;
            this.bannerImageURLLow = bannerImageURLLow;
            this.bannerImageURLHigh = bannerImageURLHigh;
            this.tag = tag;
            this.profileImageURL = profileImageURL;
            this.hasPublished = hasPublished;
            this.userPostText =  messageText;
            this.galleryImageFile = galleryImageFile;
            this.hasLiked = hasLiked;
            this.userId = userId;
            this.userName = userName;
            this.timestamp = timestamp;
            this.likesCount = likesCount;
            this.sharesCount = sharesCount;
            this.commentsCount = commentsCount;
            this.userLocation = userLocation;
            this.totalDurationCount = 0;
            this.Width = Width;
            this.Height = Height;
            this.isSystemUser = false;
            this.LearnMoreLink = LearnMoreLink;
        }

        public void incrementDuration() {
          this.totalDurationCount = this.totalDurationCount + 1;
        }

        public boolean shouldShowComments() {
            return this.totalDurationCount > 10;
        }

        public String getBannerImageURLLow() {
            return bannerImageURLLow;
        }

        public String getBannerImageURLHigh() {
        return bannerImageURLHigh;
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

        public void setBannerImageURLLow(String s) {
            bannerImageURLLow = s;
        }

        public void setBannerImageURLHigh(String s) {bannerImageURLHigh = s;}

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

        public  String getUserLocation() { return userLocation; }

        public  Long getTimeStamp() { return timestamp; }

        public  String getLikesCount() { return likesCount; }

        public  String getSharesCount() { return sharesCount; }

        public  String getCommentsCount() { return commentsCount; }

        public  Integer getWidth() { return Width; }

        public  Integer getHeight() { return Height; }

        public  void setDimensions(Integer width, Integer height) {
            this.Width = width;
            this.Height = height;
        }

        public Boolean isWidthGt() {
            return this.Width > this.Height;
        }

        public void setIsSystemUser(Boolean isSystemUser) {
            this.isSystemUser = isSystemUser;
        }

        public  boolean getIsSystemUser() {
            return this.isSystemUser;
        }

        public  String getLearnMoreLink() { return this.LearnMoreLink; }
}
