package org.wsd.agents.lock.behaviours;

        import jade.core.behaviours.OneShotBehaviour;
        import jade.lang.acl.ACLMessage;
        import lombok.extern.slf4j.Slf4j;
        import org.wsd.agents.lock.LockAgent;
        import org.wsd.ontologies.MessageContentExtractor;
        import org.wsd.ontologies.certificate.AskForPermissionsResponse;

        import java.util.Optional;

@Slf4j
public class PermissionsBehaviour extends OneShotBehaviour {

    private final ACLMessage responseMessage;

    private final LockAgent agent;
    private final MessageContentExtractor messageContentExtractor;

    public PermissionsBehaviour(final LockAgent agent, final ACLMessage message) {
        super(agent);
        this.agent = agent;
        this.responseMessage = message;
        this.messageContentExtractor = new MessageContentExtractor(agent);
    }

    @Override
    public void action() {
        final Optional<AskForPermissionsResponse> response = messageContentExtractor.extract(responseMessage, AskForPermissionsResponse.class);

        if (!response.isPresent()) {
            log.info("Could not extract AskForPermissionsResponse content");
            return;
        }

        final String permissions = response.get().getPermissions();
        agent.setPermissions(permissions);

        log.info("Permissions from response: {} is: {}", responseMessage, permissions);
    }
}


