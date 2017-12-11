package org.wsd.agents.lecturer.behaviours;

import io.vavr.control.Try;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lecturer.LecturerAgent;
import org.wsd.ontologies.otp.GenerateOTPResponse;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@Slf4j
public class LockResponseHandler extends SimpleBehaviour {

    private boolean isFinished = false;
    /* TODO: add sender filter */

    private final LecturerAgent agent;

    public LockResponseHandler(final LecturerAgent agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    public void action() {
        final ACLMessage message = agent.receive();

        if (message == null) {
            block();
            return;
        }

        Try.of(() -> agent.getContentManager().extractContent(message))
                .onSuccess(contentObject -> dispatchIncomingMessage(message, contentObject))
                .onFailure(Throwable::printStackTrace);
    }

    private void dispatchIncomingMessage(final ACLMessage message, ContentElement contentElement) {

        log.info("Processing message: {} with content: {}", message, contentElement);

        final Concept action = ((Action) contentElement).getAction();

        switch (message.getPerformative()) {
            case ACLMessage.INFORM_IF:
                Match(action).of(
                        Case($(instanceOf(GenerateOTPResponse.class)), run(() -> handleReceivedOtpCode((GenerateOTPResponse)action))),
                        Case($(), o -> run(() -> log.info("No handlers found for incoming message: {}", message)))
                );
                break;

            default:
                log.info("No handlers found for incoming message: {}", message);
        }

        isFinished = true;
    }

    private void handleReceivedOtpCode(final GenerateOTPResponse generateOTPResponse) {
        log.info("Received OTP: {}", generateOTPResponse.getOtpCode());
        agent.updateOtpCode(generateOTPResponse.getOtpCode());
    }

    @Override
    public boolean done() {
        return isFinished;
    }
}
