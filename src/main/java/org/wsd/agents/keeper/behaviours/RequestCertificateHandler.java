package org.wsd.agents.keeper.behaviours;

    import io.vavr.control.Try;
    import jade.content.Concept;
    import jade.content.ContentElement;
    import jade.content.onto.basic.Action;
    import jade.core.behaviours.CyclicBehaviour;
    import jade.lang.acl.ACLMessage;
    import lombok.extern.slf4j.Slf4j;
    import org.wsd.agents.keeper.KeeperAgent;
    import org.wsd.ontologies.certificate.AskForCertificateRequest;
    import org.wsd.ontologies.certificate.CertificateOntology;

    import static com.google.common.base.Predicates.instanceOf;
    import static io.vavr.API.*;

@Slf4j
public class RequestCertificateHandler extends CyclicBehaviour {

    private final KeeperAgent agent;

    public RequestCertificateHandler(final KeeperAgent agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    public void action() {
        final ACLMessage message = agent.receive();

        if (message == null) {
            block();
            return;
        }

        Try.of(() -> agent.getContentManager().extractContent(message))
                .onSuccess(contentObject -> dispatchIncomingMessage(message, contentObject))
                .onFailure(Throwable::printStackTrace);
    }

    private void dispatchIncomingMessage(final ACLMessage message, ContentElement contentElement) {

        log.info("Processing message: {} with content: {}", message, contentElement);

        final Concept action = ((Action) contentElement).getAction();

        switch (message.getPerformative()) {
            case ACLMessage.REQUEST:
                Match(action).of(
                        Case($(instanceOf(AskForCertificateRequest.class)), run(() -> agent.addBehaviour(new GenerateCertificateBehaviour(agent, message)))),
                        Case($(), o -> run(() -> replyNotUnderstood(message)))
                );
                break;

            default:
                log.info("No handlers found for incoming message: {}", message);
        }
    }

    private void replyNotUnderstood(final ACLMessage message) {
        Try.run(() -> {
            final ACLMessage notUnderstoodReply = message.createReply();
            notUnderstoodReply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
            notUnderstoodReply.setContentObject(message.getContentObject());
            notUnderstoodReply.setOntology(CertificateOntology.instance.getName());
            notUnderstoodReply.setLanguage(CertificateOntology.codec.getName());
            myAgent.send(notUnderstoodReply);
        }).onFailure(ex -> log.error("Could not reply with Not Understood message: {}", ex));
    }
}
