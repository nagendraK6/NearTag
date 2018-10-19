package com.neartag.in.models;

import java.util.ArrayList;

public class StoryBucket {


    ArrayList<StoryElement> storyElements;

    String creatorName;

    String creatorProfileUrl;

    String bucketCenterImageUrl;

    Integer centerImageWidth;

    Integer centerImageHeight;

    Boolean IsUserStory;

    Boolean IsLocalFile;

    public Boolean getLocalFile() {
        return IsLocalFile;
    }

    public void setLocalFile(Boolean localFile) {
        IsLocalFile = localFile;
    }

    public Boolean getUserStory() {
        return IsUserStory;
    }

    public void setUserStory(Boolean userStory) {
        IsUserStory = userStory;
    }

    public StoryBucket(ArrayList<StoryElement> storyElements, String creatorName, String creatorProfileUrl, String bucketCenterImageUrl, Integer centerImageWidth, Integer centerImageHeight) {
        this.storyElements = storyElements;
        this.creatorName = creatorName;
        this.creatorProfileUrl = creatorProfileUrl;
        this.bucketCenterImageUrl = bucketCenterImageUrl;
        this.centerImageWidth = centerImageWidth;
        this.centerImageHeight = centerImageHeight;
        this.IsUserStory = false;
        this.IsLocalFile = false;
    }

    public void addStoryElement(StoryElement element) {
        this.storyElements.add(element);
    }

    public void addStoryElementFront(StoryElement element) {
        this.storyElements.add(0, element);
    }

    public Integer getCenterImageHeight() {
        return centerImageHeight;
    }

    public void setCenterImageHeight(Integer centerImageHeight) {
        this.centerImageHeight = centerImageHeight;
    }

    public Integer getCenterImageWidth() {
        return centerImageWidth;
    }

    public void setCenterImageWidth(Integer centerImageWidth) {
        this.centerImageWidth = centerImageWidth;
    }

    public ArrayList<StoryElement> getStoryElements() {
        return storyElements;
    }

    public void setStoryElements(ArrayList<StoryElement> storyElements) {
        this.storyElements = storyElements;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorProfileUrl() {
        return creatorProfileUrl;
    }

    public void setCreatorProfileUrl(String creatorProfileUrl) {
        this.creatorProfileUrl = creatorProfileUrl;
    }

    public String getBucketCenterImageUrl() {
        return bucketCenterImageUrl;
    }

    public void setBucketCenterImageUrl(String bucketCenterImageUrl) {
        this.bucketCenterImageUrl = bucketCenterImageUrl;
    }
}
