package org.wsd.agents.lecturer.behaviours;

import io.vavr.control.Either;
import io.vavr.control.Try;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lecturer.LecturerAgent;
import org.wsd.agents.lecturer.UserAgentRoles;
import org.wsd.ontologies.otp.GenerateOTPResponse;
import org.wsd.ontologies.otp.OTPOntology;
import org.wsd.ontologies.otp.RefuseOTPGenerationResponse;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@Slf4j
public class OTPLockResponseHandler extends SimpleBehaviour {

    private final MessageTemplate OTP_MESSAGE_TEMPLATE = MessageTemplate.and(
            MessageTemplate.MatchLanguage(OTPOntology.codec.getName()),
            MessageTemplate.MatchOntology(OTPOntology.instance.getName())
    );

    private boolean isFinished = false;

    private final LecturerAgent agent;
    private final UserAgentRoles agentRole;

    public OTPLockResponseHandler(final LecturerAgent agent, final UserAgentRoles userAgentRole) {
        super(agent);
        this.agent = agent;
        this.agentRole = userAgentRole;
    }

    @Override
    public void action() {
        final ACLMessage message = agent.receive(OTP_MESSAGE_TEMPLATE);

        if (message == null) {
            block();
            return;
        }

        Try.of(() -> agent.getContentManager().extractContent(message))
                .onSuccess(contentObject -> dispatchIncomingMessage(message, contentObject))
                .onFailure(Throwable::printStackTrace);
    }

    private void dispatchIncomingMessage(final ACLMessage message, ContentElement contentElement) {
        log.info("Processing incoming message: {} with content: {}", message, contentElement);

        final Concept action = ((Action) contentElement).getAction();

        switch (message.getPerformative()) {
            case ACLMessage.INFORM:
                Match(action).of(
                        Case($(instanceOf(GenerateOTPResponse.class)), o -> run(() -> handleReceivedOtpCode((GenerateOTPResponse)action))),
                        Case($(), o -> run(() -> log.info("No handlers found for incoming message: {}", message)))
                );
                break;
            case ACLMessage.REFUSE:
                Match(action).of(
                        Case($(instanceOf(RefuseOTPGenerationResponse.class)), o -> run(() -> handleRefusedOtpCodeGeneration((RefuseOTPGenerationResponse) action))),
                        Case($(), o -> run(() -> log.info("No handlers found for incoming message: {}", message)))
                );
                break;


            default:
                log.warn("No handlers found for incoming message: {}", message);
        }

        isFinished = true;
    }

    private void handleReceivedOtpCode(final GenerateOTPResponse generateOTPResponse) {
        log.info("Received OTP: {}", generateOTPResponse.getOtpCode());
        agent.updateOtpCode(Either.right(generateOTPResponse.getOtpCode()), agentRole);
    }

    private void handleRefusedOtpCodeGeneration(final RefuseOTPGenerationResponse refuseOTPGenerationResponse) {
        log.info("OTP code generation request refused because of: {}", refuseOTPGenerationResponse.getRejectionReasons());
        agent.updateOtpCode(Either.left(refuseOTPGenerationResponse.getRejectionReasons()), agentRole);
    }

    @Override
    public boolean done() {
        return isFinished;
    }
}
