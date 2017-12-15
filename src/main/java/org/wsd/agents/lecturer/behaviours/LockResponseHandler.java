package org.wsd.agents.lecturer.behaviours;

import io.vavr.control.Either;
import io.vavr.control.Try;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lecturer.LecturerAgent;
import org.wsd.agents.lecturer.UserAgentRoles;
import org.wsd.agents.lecturer.reservations.Reservation;
import org.wsd.ontologies.otp.GenerateOTPResponse;
import org.wsd.ontologies.otp.RefuseOTPGenerationResponse;
import org.wsd.ontologies.reservation.CancelReservationResponse;
import org.wsd.ontologies.reservation.RefuseReservationCancelationResponse;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@Slf4j
public class LockResponseHandler extends SimpleBehaviour {

    private boolean isFinished = false;
    /* TODO: add sender filter */

    private final LecturerAgent agent;
    private final UserAgentRoles agentRole;

    public LockResponseHandler(final LecturerAgent agent, final UserAgentRoles userAgentRole) {
        super(agent);
        this.agent = agent;
        this.agentRole = userAgentRole;
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
            case ACLMessage.INFORM:
                Match(action).of(
                        Case($(instanceOf(GenerateOTPResponse.class)), o -> run(() -> handleReceivedOtpCode((GenerateOTPResponse)action))),
                        Case($(instanceOf(CancelReservationResponse.class)), o -> run(() -> handleCanceledReservation(message.getSender(), (CancelReservationResponse)action))),
                        Case($(), o -> run(() -> log.info("No handlers found for incoming message: {}", message)))
                );
                break;
            case ACLMessage.REFUSE:
                Match(action).of(
                        Case($(instanceOf(RefuseOTPGenerationResponse.class)), o -> run(() -> handleRefusedOtpCodeGeneration((RefuseOTPGenerationResponse) action))),
                        Case($(instanceOf(RefuseReservationCancelationResponse.class)), o -> run(() -> handleCanceledReservation(message.getSender(), (RefuseReservationCancelationResponse) action))),
                        Case($(), o -> run(() -> log.info("No handlers found for incoming message: {}", message)))
                );
                break;


            default:
                log.info("No handlers found for incoming message: {}", message);
        }

        isFinished = true;
    }

    private void handleCanceledReservation(final AID lock, final CancelReservationResponse cancelReservationResponse) {
        log.info("Reservation {} on {} successfully canceled!", cancelReservationResponse.getReservationId(), lock.getLocalName());
        agent.getReservationsStateService().remove(new Reservation(cancelReservationResponse.getReservationId(), lock));
        agent.updateReservations();
    }

    private void handleCanceledReservation(final AID lock, final RefuseReservationCancelationResponse refuseReservationCancelationResponse) {
        log.info("Lock {} refused to cancel reservation {}!", lock.getLocalName(), refuseReservationCancelationResponse.getReservationId());
        /* TODO: implement */
    }

    private void handleReceivedOtpCode(final GenerateOTPResponse generateOTPResponse) {
        log.info("Received OTP: {}", generateOTPResponse.getOtpCode());
        agent.updateOtpCode(Either.right(generateOTPResponse.getOtpCode()), agentRole);
    }

    private void handleRefusedOtpCodeGeneration(final RefuseOTPGenerationResponse refuseOTPGenerationResponse) {
        log.info("OTP code generation request refused because of: {}", refuseOTPGenerationResponse.getRejectionReasons());
        agent.updateOtpCode(Either.left(refuseOTPGenerationResponse.getRejectionReasons()), agentRole);
    }

    @Override
    public boolean done() {
        return isFinished;
    }
}
