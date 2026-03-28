package com.omnicharge.notification.consumer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.amqp.core.Message;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageRetryHelper {

    public static long getDeathCount(Message message, String queueName) {
        Object xDeathHeader = message.getMessageProperties().getHeaders().get("x-death");

        if (!(xDeathHeader instanceof List<?> deathEntries)) {
            return 0L;
        }

        for (Object entry : deathEntries) {
            if (!(entry instanceof Map<?, ?> deathEntry)) {
                continue;
            }

            Object queue = deathEntry.get("queue");
            if (!queueName.equals(queue)) {
                continue;
            }

            Object count = deathEntry.get("count");
            if (count instanceof Number number) {
                return number.longValue();
            }
        }

        return 0L;
    }
}
