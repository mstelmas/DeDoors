package org.wsd.agents.lock.behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lock.LockAgent;
import org.wsd.agents.lock.otp.OtpStateService;
import org.wsd.ontologies.otp.OTPMessageFactory;

@Slf4j
public class GenerateOTPBehaviour extends OneShotBehaviour {

    private final ACLMessage otpRequest;

    private final LockAgent agent;
    private final OTPMessageFactory otpMessageFactory;

    public GenerateOTPBehaviour(final LockAgent agent, final ACLMessage otpRequest) {
        super(agent);
        this.agent = agent;
        this.otpRequest = otpRequest;
        this.otpMessageFactory = new OTPMessageFactory(agent);
    }

    @Override
    public void action() {
        final String otpCode = OtpStateService.instance().generate();

        log.info("Generated OTP code for request: {} is: {}", otpRequest, otpCode);

        otpMessageFactory.buildGenerateOTPResponse(otpRequest.getSender(), otpCode)
                .onSuccess(otpResponseAclMessage -> {
                    agent.send(otpResponseAclMessage);
                    log.info("GenerateOTPResponse successfully sent!");
                })
                .onFailure(ex -> {
                    log.info("Could not send GenerateOTPResponse: {}", ex);
                    OtpStateService.instance().invalidate();
                });
    }
}
