package org.wsd.agents.lock.behaviours;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.wsd.AgentResolverService;
import org.wsd.agents.AgentTypes;
import org.wsd.agents.lock.LockAgent;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationMessageFactory;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PerformReservationCNPBehaviour extends OneShotBehaviour {

	private final LockAgent agent;
	private final ReservationMessageFactory reservationMessageFactory;
	private final ReservationDataRequest reservationRequestData;
    private final AgentResolverService agentResolverService;
    private final ACLMessage reservationRequestMessage;

	public PerformReservationCNPBehaviour(final LockAgent agent, final ACLMessage reservationRequestMessage, final ReservationDataRequest reservationRequest) {
		super(agent);
		this.agent = agent;
		this.reservationMessageFactory = new ReservationMessageFactory(agent);
		this.reservationRequestData = reservationRequest;
        this.agentResolverService = new AgentResolverService(agent);
        this.reservationRequestMessage = reservationRequestMessage;
	}

	@Override
	public void action() {
		log.info("Agent {} becomes a NEGOTIATOR and starts reservation CNP", agent.getLocalName());
		log.info("ReservationRequestData {}", reservationRequestData);
		log.info("Start date {}", reservationRequestData.getDateSince());
		log.info("End date {}", reservationRequestData.getDateTo());

        final List<AID> candidateLockAgents = agentResolverService.agentsOfType(AgentTypes.LOCK)
                .stream()
                .filter(lock -> !StringUtils.equals(lock.getLocalName(), agent.getLocalName()))
                .collect(Collectors.toList());

        /* TODO: Reply to LECTURER that reservation is not possible */
        if (candidateLockAgents.isEmpty()) {
            log.info("No candidate agents for CFP available, skipping...");
            return;
        }

        log.info("Sending reservationCFP to lock candidate agents: {}",
                candidateLockAgents.stream().map(AID::getLocalName).collect(Collectors.joining(",")));

        reservationMessageFactory.buildCallForProposalRequest(candidateLockAgents, reservationRequestData)
                .onSuccess(cfpMessage -> {
                    agent.addBehaviour(new ReservationCNPNegotiatorBehaviour(agent, reservationRequestMessage, cfpMessage));
                    log.info("Successfully sent reservation CFP messages");
                })
                .onFailure((ex) -> log.info("Could not build reservation CFP message :( {}", ex));
    }
}
