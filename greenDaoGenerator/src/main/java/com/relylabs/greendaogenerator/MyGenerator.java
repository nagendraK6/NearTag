package com.relylabs.greendaogenerator;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Schema;

public class MyGenerator {
    public static void main(String[] args) {
        Schema schema = new Schema(1, "com.relylabs.around.db");
        // Your app package name and the (.db) is the folder where the DAO files will be generated into.
        schema.enableKeepSectionsByDefault();

        addTables(schema);

        try {
            new DaoGenerator().generateAll(schema,"../app/src/main/java");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addTables(final Schema schema) {
        addUserEntities(schema);
    }

    // This is use to describe the colums of your table
    private static Entity addUserEntities(final Schema schema) {
        Entity user = schema.addEntity("User");
        user.addIdProperty().primaryKey().autoincrement();
        user.addIntProperty("UserId").notNull();
        user.addStringProperty("FirstName");
        user.addStringProperty("LastName");
        user.addStringProperty("Email");
        user.addStringProperty("PhoneNo");
        user.addStringProperty("Location");
        user.addStringProperty("ProfilePicURL");
        user.addBooleanProperty("IsOTPVerified");
        return user;
    }
}
