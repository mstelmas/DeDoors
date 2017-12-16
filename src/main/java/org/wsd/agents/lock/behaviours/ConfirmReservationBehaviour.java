package org.wsd.agents.lock.behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lock.LockAgent;
import org.wsd.ontologies.reservation.ConfirmReservationRequest;
import org.wsd.ontologies.reservation.ReservationMessageFactory;

@Slf4j
public class ConfirmReservationBehaviour extends OneShotBehaviour {

    private final ACLMessage confirmReservationMessage;
    private final LockAgent agent;
    private final ReservationMessageFactory reservationMessageFactory;
    private final ConfirmReservationRequest confirmReservationRequest;

    public ConfirmReservationBehaviour(final LockAgent agent, final ACLMessage confirmReservationMessage, final ConfirmReservationRequest confirmReservationRequest) {
        this.confirmReservationMessage = confirmReservationMessage;
        this.agent = agent;
        this.confirmReservationRequest = confirmReservationRequest;
        this.reservationMessageFactory = new ReservationMessageFactory(agent);
    }

    @Override
    public void action() {
        reservationMessageFactory.buildConfirmReservationInformResponse(confirmReservationMessage, confirmReservationRequest.getReservationId())
                .onSuccess(agent::send)
                .onFailure(ex -> {
                    log.error("Could not send ConfirmReservationMessage Inform: {}", ex);
                });
    }
}
