package com.neartag.in.models;

/**
 * Created by nagendra on 9/8/18.
 */

public class Comment {

    private Integer user_id;
    private String user_name;
    private String user_profile_url;
    private String comment;
    private String date_created;

    public Comment() {

    }

    public Comment(Integer m_user_id,
                   String m_user_name,
                   String m_user_profile_image_url,
                   String m_comment_text,
                   String m_date_created) {
        this.user_id = m_user_id;
        this.user_name = m_user_name;
        this.user_profile_url = m_user_profile_image_url;
        this.comment = m_comment_text;
        this.date_created = m_date_created;

    }

    public String getCommentText() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommenterProfilePicUrl() {
        return user_profile_url;
    }

    public void setCommenterProfilePicUrl(String user_profile_url) {
        this.user_profile_url = user_profile_url;
    }


    public Integer getUserId() {
        return user_id;
    }

    public String getUserName() {
        return user_name;
    }


    public void setUserId(Integer user_id) {
        this.user_id = user_id;
    }


    public String getDateCreated() {
        return date_created;
    }

    public void setDateCreated(String date_created) {
        this.date_created = date_created;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "comment='" + comment + '\'' +
                ", user_id='" + user_id + '\'' +
                ", date_created='" + date_created + '\'' +
                '}';
    }
}