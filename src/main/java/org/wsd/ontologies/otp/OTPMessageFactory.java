package org.wsd.ontologies.otp;

import io.vavr.control.Try;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OTPMessageFactory {

    private final Agent agent;

    public Try<ACLMessage> buildGenerateOTPRequest(@NonNull final AID receiver) {
        final ACLMessage otpRequestMessage = new ACLMessage(ACLMessage.REQUEST);

        otpRequestMessage.addReceiver(receiver);
        otpRequestMessage.setLanguage(OTPOntology.codec.getName());
        otpRequestMessage.setOntology(OTPOntology.instance.getName());

        return Try.of(() -> {
            agent.getContentManager().fillContent(otpRequestMessage, new Action(receiver, new GenerateOTPRequest()));
            return otpRequestMessage;
        });
    }

    public Try<ACLMessage> buildGenerateOTPResponse(@NonNull final AID receiver, String otpCode) {
        final ACLMessage otpResponseMessage = new ACLMessage(ACLMessage.INFORM_IF);

        otpResponseMessage.addReceiver(receiver);
        otpResponseMessage.setLanguage(OTPOntology.codec.getName());
        otpResponseMessage.setOntology(OTPOntology.instance.getName());

        return Try.of(() -> {
            agent.getContentManager().fillContent(otpResponseMessage, new Action(receiver, new GenerateOTPResponse().withOtpCode(otpCode)));
            return otpResponseMessage;
        });
    }
}