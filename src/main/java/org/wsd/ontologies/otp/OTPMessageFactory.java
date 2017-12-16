package org.wsd.ontologies.otp;

import io.vavr.control.Try;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.wsd.agents.ConversationIdGenerator;

@RequiredArgsConstructor
public class OTPMessageFactory {

    private final Agent agent;

    private final ConversationIdGenerator conversationIdGenerator = new ConversationIdGenerator();

    public Try<ACLMessage> buildGenerateOTPRequest(@NonNull final AID receiver, final String certificate, final Integer reservationId) {
        final ACLMessage otpRequestMessage = new ACLMessage(ACLMessage.REQUEST);

        otpRequestMessage.addReceiver(receiver);
        otpRequestMessage.setLanguage(OTPOntology.codec.getName());
        otpRequestMessage.setOntology(OTPOntology.instance.getName());
        otpRequestMessage.setConversationId(conversationIdGenerator.generate());

        return Try.of(() -> {
            agent.getContentManager().fillContent(otpRequestMessage, new Action(receiver,
                    new GenerateOTPRequest().withCertificate(certificate).withReservationId(reservationId)));
            return otpRequestMessage;
        });
    }

    public Try<ACLMessage> buildGenerateOTPResponse(@NonNull final ACLMessage otpRequestMessage, String otpCode, Integer reservationId) {
        final ACLMessage otpResponseMessage = otpRequestMessage.createReply();

        otpResponseMessage.setPerformative(ACLMessage.INFORM);
        otpResponseMessage.setLanguage(OTPOntology.codec.getName());
        otpResponseMessage.setOntology(OTPOntology.instance.getName());

        return Try.of(() -> {
            agent.getContentManager().fillContent(otpResponseMessage, new Action(otpRequestMessage.getSender(),
                    new GenerateOTPResponse().withOtpCode(otpCode).withReservationId(reservationId)));
            return otpResponseMessage;
        });
    }

    public Try<ACLMessage> buildRefuseOTPGenerationResponse(@NonNull final ACLMessage otpRequestMessage, String rejectionReason) {
        final ACLMessage otpResponseMessage = otpRequestMessage.createReply();

        otpResponseMessage.setPerformative(ACLMessage.REFUSE);
        otpResponseMessage.setLanguage(OTPOntology.codec.getName());
        otpResponseMessage.setOntology(OTPOntology.instance.getName());

        return Try.of(() -> {
            agent.getContentManager().fillContent(otpResponseMessage, new Action(otpRequestMessage.getSender(),
                    new RefuseOTPGenerationResponse().withRejectionReasons(rejectionReason)));
            return otpResponseMessage;
        });
    }
}
