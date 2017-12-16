package org.wsd.agents;

import org.apache.commons.lang3.RandomStringUtils;

import java.sql.Timestamp;

public class ConversationIdGenerator {

    public String generate() {
        return RandomStringUtils.randomAlphanumeric(16).concat(new Timestamp(System.currentTimeMillis()).toString());
    }
}
