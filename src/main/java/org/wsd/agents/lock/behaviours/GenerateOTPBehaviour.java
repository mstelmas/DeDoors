package org.wsd.agents.lock.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lock.OtpCodeGenerator;

@Slf4j
public class GenerateOTPBehaviour extends OneShotBehaviour {

    private final ACLMessage otpRequest;

    private final OtpCodeGenerator otpCodeGenerator = new OtpCodeGenerator();

    public GenerateOTPBehaviour(final Agent agent, final ACLMessage otpRequest) {
        super(agent);
        this.otpRequest = otpRequest;
    }

    @Override
    public void action() {
        log.info("Generated OTP code for request: {} is: {}", otpRequest, otpCodeGenerator.generate());
    }

}
