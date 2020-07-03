package com.relylabs.instahelo.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nagendra on 9/18/18.
 */

@Table(name = "Tags")
public class Tag extends Model {

    @Column(name = "Name", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String Name;


    public Tag() {
        super();
    }

    public static ArrayList<String> getTagsString(String pattern) {
        List<Tag> all_tag =
                new Select().from(Tag.class).where("Name Like ?",  pattern + "%").execute();
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < all_tag.size(); i++) {
            result.add("#" + all_tag.get(i).Name);
        }

        return result;
    }
}
