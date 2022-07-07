package com.tgbot.skeleton.domain.service;

import com.tgbot.skeleton.domain.model.CheckPoint;
import com.tgbot.skeleton.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    // replace with necessary storage
    private static final Map<String, User> TEMP_USERS = new HashMap<>();
    private static final Map<String, CheckPoint> TEMP_CHECK_POINTS = new HashMap<>();

    public User save(final String id, final String chatId, final String username, final String firstName, final String lastName) {
        final User user = new User(id, chatId, username, firstName, lastName);
        TEMP_USERS.put(id, user);
        return user;
    }

    public User findByUserId(final String userId) {
        return TEMP_USERS.get(userId);
    }

    public User findByChatId(final String chatId) {
        // temp solution
        return TEMP_USERS.get(chatId);
    }

    public void saveUsersCheckPoint(final String userId, final CheckPoint checkPoint) {
        TEMP_CHECK_POINTS.put(userId, checkPoint);
    }

    public CheckPoint findUsersCheckPoint(final String userId) {
        return TEMP_CHECK_POINTS.get(userId);
    }
}
