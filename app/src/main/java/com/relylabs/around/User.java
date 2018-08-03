package com.relylabs.around;

/**
 * Created by nagendra on 8/2/18.
 */

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import  com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;


@Table(name = "Users")
public class User extends Model {

    @Column(name = "UserID", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public Integer UserID;

    @Column(name = "Name")
    public String Name;

    @Column(name = "Location")
    public String Location;

    @Column(name = "ProfilePicURL")
    public String ProfilePicURL;


    @Column(name = "IsLoggedInUser")
    public Boolean IsLoggedInUser;

    @Column(name = "PhoneNo")
    public String PhoneNo;

    @Column(name = "IsOTPVerified")
    public Boolean IsOTPVerified;

    @Column(name = "CountryCode")
    public String CountryCode;

    @Column(name = "AccessToken")
    public String AccessToken;


    public User() {
        super();
        this.IsOTPVerified = false;
        this.IsLoggedInUser = false;
        this.Name = "";
        this.Location = "";
        this.ProfilePicURL = "";
    }


    public static User getRandom() {
        return new Select().from(User.class).orderBy("RANDOM()").executeSingle();
    }

    public static User getLoggedInUser() {
        User user = new Select().from(User.class).where("IsLoggedInUser = ?", true).executeSingle();
        if (user != null) {
            Boolean updated = false;

            if (user.IsLoggedInUser == null) {
                user.IsLoggedInUser = false;
                updated = true;
            }

            if (user.ProfilePicURL == null) {
                user.ProfilePicURL = "";
                updated = true;
            }

            if (updated) {
                user.save();
            }
        }

        return user;
    }

    public static Integer getLoggedInUserID() {
        User current_user = User.getLoggedInUser();
        if (current_user == null) {
            return -1;
        }

        return current_user.UserID;
    }

    public String getFormattedNo() {
        return this.CountryCode + "-" + this.PhoneNo;
    }
}

