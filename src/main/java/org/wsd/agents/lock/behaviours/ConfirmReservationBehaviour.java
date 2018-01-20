package org.wsd.agents.lock.behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lock.LockAgent;
import org.wsd.agents.lock.reservations.ReservationStateService;
import org.wsd.ontologies.reservation.ConfirmReservationRequest;
import org.wsd.ontologies.reservation.ReservationMessageFactory;

@Slf4j
public class ConfirmReservationBehaviour extends OneShotBehaviour {

    private final ACLMessage confirmReservationMessage;
    private final LockAgent agent;
    private final ReservationMessageFactory reservationMessageFactory;
    private final ConfirmReservationRequest confirmReservationRequest;
    private final ReservationStateService reservationStateService;

    public ConfirmReservationBehaviour(final LockAgent agent, final ACLMessage confirmReservationMessage, final ConfirmReservationRequest confirmReservationRequest, final ReservationStateService reservationStateService) {
        this.confirmReservationMessage = confirmReservationMessage;
        this.agent = agent;
        this.confirmReservationRequest = confirmReservationRequest;
        this.reservationMessageFactory = new ReservationMessageFactory(agent);
        this.reservationStateService = reservationStateService;
    }

    @Override
    public void action() {
        reservationMessageFactory.buildConfirmReservationInformResponse(confirmReservationMessage, confirmReservationRequest.getReservationId())
                .onSuccess(message -> {
                    agent.send(message);
                    reservationStateService.confirm(confirmReservationRequest.getReservationId());
                })
                .onFailure(ex -> {
                    log.error("Could not send ConfirmReservationMessage Inform: {}", ex);
                });
    }
}
