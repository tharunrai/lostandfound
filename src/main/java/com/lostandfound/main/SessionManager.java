package com.lostandfound.main;

import com.lostandfound.models.Item;
import com.lostandfound.models.User;

public class SessionManager {
    private static User currentUser;
    private static Item currentSelectedItem;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static Item getCurrentSelectedItem() {
        return currentSelectedItem;
    }

    public static void setCurrentSelectedItem(Item item) {
        currentSelectedItem = item;
    }

    public static void clearSession() {
        currentUser = null;
        currentSelectedItem = null;
    }
}
