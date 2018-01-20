package org.wsd.agents.lock.behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;
import org.wsd.agents.lock.LockAgent;
import org.wsd.agents.lock.reservations.ReservationStateService;
import org.wsd.ontologies.reservation.CancelReservationRequest;
import org.wsd.ontologies.reservation.ReservationMessageFactory;

@Slf4j
public class CancelReservationBehaviour extends OneShotBehaviour {

    private final ACLMessage cancelReservationMessage;
    private final LockAgent agent;
    private final ReservationMessageFactory reservationMessageFactory;
    private final CancelReservationRequest cancelReservationRequest;

    public CancelReservationBehaviour(final LockAgent agent, final ACLMessage cancelReservationMessage, final CancelReservationRequest cancelReservationRequest) {
        this.cancelReservationMessage = cancelReservationMessage;
        this.agent = agent;
        this.cancelReservationRequest = cancelReservationRequest;
        this.reservationMessageFactory = new ReservationMessageFactory(agent);
    }

    @Override
    public void action() {
        final ReservationStateService reservationStateService = agent.getReservationStateService();
        final LocalDateTime reservationStartDate = reservationStateService.getReservationStartDate(cancelReservationRequest.getReservationId());

        if (LocalDateTime.now().isAfter(reservationStartDate)) {
            reservationMessageFactory.buildCancelReservationRefuseResponse(cancelReservationMessage, cancelReservationRequest.getReservationId())
                    .onSuccess(agent::send)
                    .onFailure(ex -> {
                        log.error("Could not send CancelReservationRefuseMessage Cancel: {}", ex);
                    });
        } else {
            reservationMessageFactory.buildCancelReservationInformResponse(cancelReservationMessage, cancelReservationRequest.getReservationId())
                    .onSuccess(agent::send)
                    .onFailure(ex -> {
                        log.error("Could not send CancelReservationMessage Inform: {}", ex);
                    });
        }
    }
}
