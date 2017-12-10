package org.wsd.agents.lock.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenerateOTPBehaviour extends OneShotBehaviour {

    private final ACLMessage otpRequest;

    public GenerateOTPBehaviour(final Agent agent, final ACLMessage otpRequest) {
        super(agent);
        this.otpRequest = otpRequest;
    }

    @Override
    public void action() {
        log.info("Generating OTP code for request: {}", otpRequest);
    }

}
