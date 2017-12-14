package org.wsd.agents.lock.validators;

import io.vavr.control.Validation;
import jade.content.AgentAction;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

public class PermissionValidator {

    /* TODO: implement (check if agent has permissions for a requested action) */
    public Validation<String, String> validateActionPermissions(@NonNull final ACLMessage message, @NonNull final AgentAction agentAction) {
        /* For testing purposes:
              Lock3 - returns invalid permissions
         */
        if (StringUtils.startsWith("lock-agent-3", ((AID)message.getAllReceiver().next()).getLocalName())) {
            return Validation.invalid("bad credentials");
        } else {
            return Validation.valid("ok");
        }
    }
}
