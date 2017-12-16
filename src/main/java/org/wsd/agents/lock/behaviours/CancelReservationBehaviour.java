package org.wsd.agents.lock.behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lock.LockAgent;
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
        /* TODO: INFORM/REFUSE reservation cancelation based on validation */
        reservationMessageFactory.buildCancelReservationInformResponse(cancelReservationMessage, cancelReservationRequest.getReservationId())
                .onSuccess(agent::send)
                .onFailure(ex -> {
                    log.error("Could not send CancelReservationMessage Inform: {}", ex);
                });
    }
}
