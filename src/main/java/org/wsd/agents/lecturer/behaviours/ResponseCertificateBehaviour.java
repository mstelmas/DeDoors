package org.wsd.agents.lecturer.behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lecturer.LecturerAgent;
import org.wsd.ontologies.MessageContentExtractor;
import org.wsd.ontologies.certificate.AskForCertificateResponse;

import java.util.Optional;

@Slf4j
public class ResponseCertificateBehaviour extends OneShotBehaviour {

    private final ACLMessage responseMessage;

    private final LecturerAgent agent;
    private final MessageContentExtractor messageContentExtractor;

    public ResponseCertificateBehaviour(final LecturerAgent agent, final ACLMessage message) {
        super(agent);
        this.agent = agent;
        this.responseMessage = message;
        this.messageContentExtractor = new MessageContentExtractor(agent);
    }

    @Override
    public void action() {
        final Optional<AskForCertificateResponse> response = messageContentExtractor.extract(responseMessage, AskForCertificateResponse.class);

        if (!response.isPresent()) {
            log.info("Could not extract GenerateOTPRequest content");
            return;
        }

        final String certificate = response.get().getCertificate();
        agent.setCertificate(certificate);

        log.info("Certificate from response: {} is: {}", responseMessage, certificate);
    }
}

