package org.wsd.agents;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AgentTypes {
    LOCK("lock-agent"),
    LECTURER("lecturer-agent"),
    KEEPER("keeper-agent");

    @Getter
    private final String name;
}
