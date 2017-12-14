package org.wsd.ontologies.certificate;

import io.vavr.control.Try;
import jade.core.Agent;
import jade.content.onto.basic.Action;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CertificateMessageFactory {

    private final Agent agent;

    public Try<ACLMessage> buildNewCertificateRequest(@NonNull final AID receiver, String email, String passwordHash) {
        final ACLMessage requestMessage = new ACLMessage(ACLMessage.REQUEST);

        requestMessage.addReceiver(receiver);
        requestMessage.setLanguage(CertificateOntology.codec.getName());
        requestMessage.setOntology(CertificateOntology.instance.getName());

        return Try.of(() -> {
            agent.getContentManager().fillContent(requestMessage, new Action(receiver, new AskForCertificateRequest()
                    .withEmail(email).withPasswordHash(passwordHash)
            ));
            return requestMessage;
        });
    }

    public Try<ACLMessage> buildNewCertificateResponse(@NonNull final AID receiver, String certificate) {
        final ACLMessage responseMessage = new ACLMessage(ACLMessage.INFORM_IF);

        responseMessage.addReceiver(receiver);
        responseMessage.setLanguage(CertificateOntology.codec.getName());
        responseMessage.setOntology(CertificateOntology.instance.getName());

        return Try.of(() -> {
            agent.getContentManager().fillContent(responseMessage, new Action(receiver, new AskForCertificateResponse()
                    .withCertificate(certificate)));
            return responseMessage;
        });
    }
}
