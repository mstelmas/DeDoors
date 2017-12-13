package org.wsd.agents.lock.behaviours;

import java.awt.image.SinglePixelPackedSampleModel;

import org.wsd.agents.lock.LockAgent;
import org.wsd.ontologies.otp.OTPMessageFactory;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationMessageFacotry;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PerformCNPbehaviour extends OneShotBehaviour {

	private final LockAgent agent;
	private final ReservationMessageFacotry reservationMessageFactory;
	private final ReservationDataRequest reservationRequestData;

	public PerformCNPbehaviour(final LockAgent agent, final ReservationDataRequest reservationRequest) {
		super(agent);
		this.agent = agent;
		this.reservationMessageFactory = new ReservationMessageFacotry(agent);
		this.reservationRequestData = reservationRequest;
		log.info("reservation request: {}", reservationRequest);
	}

	@Override
	public void action() {
		log.info("PerformCNPBehaviour.action()");
		log.info("ReservationRequestData {}", reservationRequestData);
		log.info("Start date {}", reservationRequestData.getDateSince());
		log.info("End date {}", reservationRequestData.getDateTo());
	}
}
