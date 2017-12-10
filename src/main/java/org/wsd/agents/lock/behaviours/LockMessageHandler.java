package org.wsd.agents.lock.behaviours;

import io.vavr.control.Try;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.extern.slf4j.Slf4j;
import org.wsd.ontologies.otp.GenerateOTPRequest;
import org.wsd.ontologies.otp.OTPOntology;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@Slf4j
public class LockMessageHandler extends CyclicBehaviour {

    private final SLCodec codec = new SLCodec();

    private final MessageTemplate OTP_ONTOLOGY_MESSAGE_TEMPLATE = MessageTemplate.and(
            MessageTemplate.MatchLanguage(codec.getName()),
            MessageTemplate.MatchOntology(OTPOntology.instance.getName())
    );

    public LockMessageHandler(final Agent agent) {
        super(agent);
    }

    @Override
    public void action() {
        final ACLMessage message = myAgent.receive(OTP_ONTOLOGY_MESSAGE_TEMPLATE);

        if (message == null) {
            block();
            return;
        }

        Try.of(() -> myAgent.getContentManager().extractContent(message))
                .onSuccess(contentObject -> dispatchIncomingMessage(message, contentObject))
                .onFailure(Throwable::printStackTrace);
    }

    private void dispatchIncomingMessage(final ACLMessage message, ContentElement contentElement) {

        log.info("Processing message: {} with content: {}", message, contentElement);

        final Concept action = ((Action) contentElement).getAction();

        switch (message.getPerformative()) {
            case ACLMessage.REQUEST:
                Match(action).of(
                        Case($(instanceOf(GenerateOTPRequest.class)), run(() -> myAgent.addBehaviour(new GenerateOTPBehaviour(myAgent, message)))),
                        Case($(), o -> run(() -> replyNotUnderstood(message)))
                );
                break;
            case ACLMessage.CFP:
                Match(action).of(
                        Case($(), o -> run(() -> replyNotUnderstood(message)))
                );
                break;
            default:
                replyNotUnderstood(message);
        }
    }

    private void replyNotUnderstood(final ACLMessage message) {
        Try.run(() -> {
            final ACLMessage notUnderstoodReply = message.createReply();
            notUnderstoodReply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
            notUnderstoodReply.setContentObject(message.getContentObject());
            notUnderstoodReply.setOntology(OTPOntology.instance.getName());
            notUnderstoodReply.setLanguage(codec.getName());
            myAgent.send(notUnderstoodReply);
        }).onFailure(ex -> log.error("Could not reply with Not Understood message: {}", ex));
    }

}
