package com.neartag.in.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.ArrayList;

public class StoryElement implements Parcelable {

    public Integer getStoryId() {
        return storyId;
    }

    public void setStoryId(Integer storyId) {
        this.storyId = storyId;
    }

    public String getBannerImageURLLow() {
        return bannerImageURLLow;
    }

    public void setBannerImageURLLow(String bannerImageURLLow) {
        this.bannerImageURLLow = bannerImageURLLow;
    }

    public String getBannerImageURLHigh() {
        return bannerImageURLHigh;
    }

    public void setBannerImageURLHigh(String bannerImageURLHigh) {
        this.bannerImageURLHigh = bannerImageURLHigh;
    }

    public Integer getWidth() {
        return Width;
    }

    public void setWidth(Integer width) {
        Width = width;
    }

    public Integer getHeight() {
        return Height;
    }

    public void setHeight(Integer height) {
        Height = height;
    }

    public String getLearnMoreLink() {
        return LearnMoreLink;
    }

    public void setLearnMoreLink(String learnMoreLink) {
        LearnMoreLink = learnMoreLink;
    }


    private Integer storyId;

    private String bannerImageURLLow;


    private String bannerImageURLHigh;

    private Integer Width;

    private Integer Height;

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String LearnMoreLink;

    private Boolean IsLocalFile;

    public Boolean getLocalFile() {
        return IsLocalFile;
    }

    public void setLocalFile(Boolean localFile) {
        IsLocalFile = localFile;
    }

    public StoryElement(
            Integer storyId,
            String bannerImageURLLow,
            String bannerImageURLHigh,
            Integer width,
            Integer height,
            String text,
            String learnMoreLink) {
        this.storyId = storyId;
        this.bannerImageURLLow = bannerImageURLLow;
        this.bannerImageURLHigh = bannerImageURLHigh;
        this.Width = width;
        this.Height = height;
        this.text = text;
        this.LearnMoreLink = learnMoreLink;
        this.IsLocalFile = false;
    }

    protected StoryElement(Parcel in) {
        storyId = in.readInt();
        bannerImageURLLow = in.readString();
        bannerImageURLHigh = in.readString();
        Width = in.readInt();
        Height = in.readInt();
        text = in.readString();
        LearnMoreLink = in.readString();
        IsLocalFile = in.readByte() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(storyId);
        dest.writeString(bannerImageURLLow);
        dest.writeString(bannerImageURLHigh);
        dest.writeInt(Width);
        dest.writeInt(Height);
        dest.writeString(text);
        dest.writeString(LearnMoreLink);
        dest.writeByte((byte) (this.IsLocalFile ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StoryElement> CREATOR = new Creator<StoryElement>() {
        @Override
        public StoryElement createFromParcel(Parcel in) {
            return new StoryElement(in);
        }

        @Override
        public StoryElement[] newArray(int size) {
            return new StoryElement[size];
        }
    };

}
