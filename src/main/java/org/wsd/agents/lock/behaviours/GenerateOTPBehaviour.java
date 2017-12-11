package org.wsd.agents.lock.behaviours;

import io.vavr.control.Try;
import jade.content.onto.basic.Action;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lock.LockAgent;
import org.wsd.agents.lock.otp.OtpStateService;
import org.wsd.ontologies.otp.GenerateOTPResponse;
import org.wsd.ontologies.otp.OTPOntology;

@Slf4j
public class GenerateOTPBehaviour extends OneShotBehaviour {

    private final ACLMessage otpRequest;

    private final LockAgent agent;

    public GenerateOTPBehaviour(final LockAgent agent, final ACLMessage otpRequest) {
        super(agent);
        this.agent = agent;
        this.otpRequest = otpRequest;
    }

    @Override
    public void action() {
        final String otpCode = OtpStateService.instance().generate();

        log.info("Generated OTP code for request: {} is: {}", otpRequest, otpCode);

        final ACLMessage otpResponseMessage = new ACLMessage(ACLMessage.INFORM_IF);

        otpResponseMessage.addReceiver(otpRequest.getSender());
        otpResponseMessage.setLanguage(OTPOntology.codec.getName());
        otpResponseMessage.setOntology(OTPOntology.instance.getName());

        Try.run(() -> agent.getContentManager().fillContent(otpResponseMessage, new Action(otpRequest.getSender(), new GenerateOTPResponse().withOtpCode(otpCode))))
                .andThen(() -> {
                    agent.send(otpResponseMessage);
                    log.info("GenerateOTPResponse successfully sent!");
                })
                .onFailure(ex -> {
                    log.info("Could not send GenerateOTPResponse: {}", ex);
                    OtpStateService.instance().invalidate();
                });
    }
}
