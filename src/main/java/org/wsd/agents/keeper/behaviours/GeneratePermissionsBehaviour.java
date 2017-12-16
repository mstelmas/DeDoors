package org.wsd.agents.keeper.behaviours;

        import jade.core.behaviours.OneShotBehaviour;
        import jade.lang.acl.ACLMessage;
        import lombok.extern.slf4j.Slf4j;
        import org.wsd.agents.keeper.KeeperAgent;
        import org.wsd.agents.keeper.authorization.CertificateProvider;
        import org.wsd.ontologies.MessageContentExtractor;
        import org.wsd.ontologies.certificate.AskForPermissionsRequest;
        import org.wsd.ontologies.certificate.CertificateMessageFactory;

        import java.util.Optional;

@Slf4j
public class GeneratePermissionsBehaviour extends OneShotBehaviour {

    private final ACLMessage requestMessage;

    private final KeeperAgent agent;
    private final CertificateMessageFactory messageFactory;
    private final CertificateProvider certificateProvider = new CertificateProvider();
    private final MessageContentExtractor messageContentExtractor;

    public GeneratePermissionsBehaviour(final KeeperAgent agent, final ACLMessage message) {
        super(agent);
        this.agent = agent;
        this.requestMessage = message;
        this.messageFactory = new CertificateMessageFactory(agent);
        this.messageContentExtractor = new MessageContentExtractor(agent);
    }

    @Override
    public void action() {
        final Optional<AskForPermissionsRequest> request = messageContentExtractor.extract(requestMessage, AskForPermissionsRequest.class);

        if (!request.isPresent()) {
            log.info("Could not extract AskForPermissionsRequest content");
            return;
        }

        final String permissions = certificateProvider.getPermissions();

        log.info("Generated permissions for request: {} is: {}", requestMessage, permissions);
        askForPermissionsResponse(permissions);
    }

    private void askForPermissionsResponse(final String permissions) {
        messageFactory.buildAskForPermissionsResponse(requestMessage.getSender(), permissions)
                .onSuccess(response -> {
                    agent.send(response);
                    log.info("AskForPermissionsResponse successfully sent!");
                })
                .onFailure(ex -> {
                    log.info("Could not send AskForCertificateResponse: {}", ex);
                });
    }
}

