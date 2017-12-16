package org.wsd.ontologies.certificate;

import io.vavr.control.Try;
import jade.core.Agent;
import jade.content.onto.basic.Action;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sun.util.logging.resources.logging;

@RequiredArgsConstructor
@Slf4j
public class CertificateMessageFactory {

    private final Agent agent;

    public Try<ACLMessage> buildAskForCertificateRequest(@NonNull final AID receiver, String email, String password) {
        final ACLMessage requestMessage = new ACLMessage(ACLMessage.REQUEST);

        requestMessage.addReceiver(receiver);
        requestMessage.setLanguage(CertificateOntology.codec.getName());
        requestMessage.setOntology(CertificateOntology.instance.getName());

        return Try.of(() -> {
            agent.getContentManager().fillContent(requestMessage,
                    new Action(receiver, new AskForCertificateRequest().withEmail(email).withPassword(password)));
            return requestMessage;
        });
    }

    public Try<ACLMessage> buildAskForCertificateResponse(@NonNull final AID receiver, String certificate) {
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

    public Try<ACLMessage> buildAskForPermissionsRequest(@NonNull final AID receiver) {
        final ACLMessage requestMessage = new ACLMessage(ACLMessage.REQUEST);

        requestMessage.addReceiver(receiver);
        requestMessage.setLanguage(CertificateOntology.codec.getName());
        requestMessage.setOntology(CertificateOntology.instance.getName());

        return Try.of(() -> {
            agent.getContentManager().fillContent(requestMessage,
                    new Action(receiver, new AskForPermissionsRequest()));
            return requestMessage;
        });
    }

    public Try<ACLMessage> buildAskForPermissionsResponse(@NonNull final AID receiver, String permissions) {
        final ACLMessage responseMessage = new ACLMessage(ACLMessage.INFORM_IF);

        responseMessage.addReceiver(receiver);
        responseMessage.setLanguage(CertificateOntology.codec.getName());
        responseMessage.setOntology(CertificateOntology.instance.getName());

        return Try.of(() -> {
            agent.getContentManager().fillContent(responseMessage, new Action(receiver, new AskForPermissionsResponse()
                    .withPermissions(permissions)));
            return responseMessage;
        });
    }
}
