package org.wsd.ontologies;

import io.vavr.control.Try;
import jade.content.Concept;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class MessageContentExtractor {

    private final Agent agent;

    public <T extends Concept> Optional<T> extract(@NonNull final ACLMessage aclMessage, final Class<T> clazz) {
        return Try.of(() -> agent.getContentManager().extractContent(aclMessage))
                .map(contentObject -> Optional.of((T) ((Action) contentObject).getAction()))
                .getOrElse(Optional::empty);
    }
}
