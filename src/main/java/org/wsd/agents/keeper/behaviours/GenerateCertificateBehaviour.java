package org.wsd.agents.keeper.behaviours;


import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.keeper.KeeperAgent;
import org.wsd.agents.keeper.authorization.CertificateProvider;
import org.wsd.ontologies.MessageContentExtractor;
import org.wsd.ontologies.certificate.AskForCertificateRequest;
import org.wsd.ontologies.certificate.CertificateMessageFactory;

import java.util.Optional;

@Slf4j
public class GenerateCertificateBehaviour extends OneShotBehaviour {

    private final ACLMessage requestMessage;

    private final KeeperAgent agent;
    private final CertificateMessageFactory messageFactory;
    private final CertificateProvider certificateProvider = new CertificateProvider();
    private final MessageContentExtractor messageContentExtractor;

    public GenerateCertificateBehaviour(final KeeperAgent agent, final ACLMessage message) {
        super(agent);
        this.agent = agent;
        this.requestMessage = message;
        this.messageFactory = new CertificateMessageFactory(agent);
        this.messageContentExtractor = new MessageContentExtractor(agent);
    }

    @Override
    public void action() {
        final Optional<AskForCertificateRequest> request = messageContentExtractor.extract(requestMessage, AskForCertificateRequest.class);

        if (!request.isPresent()) {
            log.info("Could not extract GenerateOTPRequest content");
            return;
        }

        final String certificate
                = certificateProvider.generateCertificate(request.get().getEmail(), request.get().getPassword());

        log.info("Generated certificate for request: {} is: {}", requestMessage, certificate);
        askForCertificateResponse(certificate);
    }

    private void askForCertificateResponse(final String certificate) {
        log.info("building AskForCertificateResponse");
        messageFactory.buildAskForCertificateResponse(requestMessage.getSender(), certificate)
                .onSuccess(response -> {
                    agent.send(response);
                    log.info("AskForCertificateResponse successfully sent!");
                })
                .onFailure(ex -> {
                    log.info("Could not send AskForCertificateResponse: {}", ex);
                });
    }
}
