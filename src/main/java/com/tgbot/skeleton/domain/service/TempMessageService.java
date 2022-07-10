package com.tgbot.skeleton.domain.service;

import com.tgbot.skeleton.domain.model.StoredMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

@Service
public class TempMessageService implements MessageService {

    // replace with necessary storage
    private static final Map<String, List<StoredMessage>> TEMP_MESSAGES = new ConcurrentHashMap<>();

    @Override
    public void save(final String userId, final String messageId, final boolean isInteractive, final boolean botOwns) {
        if (TEMP_MESSAGES.containsKey(userId)) {
            TEMP_MESSAGES.get(userId).add(new StoredMessage(messageId, isInteractive, botOwns));
        } else {
            TEMP_MESSAGES.put(userId, newArrayList(new StoredMessage(messageId, isInteractive, botOwns)));
        }
    }

    @Override
    public StoredMessage findLastUserMessage(final String userId) {
        final List<StoredMessage> storedMessages = TEMP_MESSAGES.get(userId);
        if (CollectionUtils.isEmpty(storedMessages)) {
            return null;
        } else {
            return storedMessages.get(storedMessages.size() - 1);
        }
    }

    @Override
    public List<String> findByUserId(final String userId) {
        return TEMP_MESSAGES.getOrDefault(userId, newArrayList()).stream()
                .map(StoredMessage::getMessageId)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByIds(final String userId, final List<String> messageIds) {
        final List<StoredMessage> messages = TEMP_MESSAGES.getOrDefault(userId, newArrayList()).stream()
                .filter(m -> !messageIds.contains(m.getMessageId())).
                collect(Collectors.toList());

        TEMP_MESSAGES.put(userId, messages);
    }
}
