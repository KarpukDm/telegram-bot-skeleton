package com.tgbot.skeleton.domain.service;

import com.tgbot.skeleton.domain.model.StoredMessage;

import java.util.List;

public interface MessageService {

    /**
     * Save user to the system
     *
     * @param userId        - user if from telegram
     * @param messageId     - message is from telegram
     * @param isInteractive - whether the system expects a click on the proposed answer option
     * @param botOwns       - true if this message is sent by a bot
     */
    void save(String userId, String messageId, boolean isInteractive, boolean botOwns);

    /**
     * Find the last user message
     *
     * @param userId - user id
     * @return last user message object
     */
    StoredMessage findLastUserMessage(String userId);

    /**
     * Find all user messages
     *
     * @param userId - user id
     * @return list of user messages
     */
    List<String> findByUserId(String userId);

    /**
     * Delete user messages by ids
     *
     * @param userId     - user id
     * @param messageIds - message ids
     */
    void deleteByIds(String userId, List<String> messageIds);
}
