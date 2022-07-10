package com.tgbot.skeleton.domain.service;

import com.tgbot.skeleton.domain.model.CheckPoint;
import com.tgbot.skeleton.domain.model.User;

public interface UserService {
    User save(String id, String chatId, String username, String firstName, String lastName);

    User findByUserId(String userId);

    User findByChatId(String chatId);

    void saveUsersCheckPoint(String userId, CheckPoint checkPoint);

    CheckPoint findUsersCheckPoint(String userId);
}
