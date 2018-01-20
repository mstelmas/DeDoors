package org.wsd.agents.lock.behaviours;

import org.joda.time.LocalDateTime;
import org.wsd.agents.lock.LockAgent;
import org.wsd.agents.lock.configuration.LockConfiguration;
import org.wsd.agents.lock.configuration.LockConfigurationProvider;
import org.wsd.agents.lock.reservations.Reservation;
import org.wsd.agents.lock.reservations.ReservationOfferService;
import org.wsd.agents.lock.reservations.ReservationState;
import org.wsd.agents.lock.reservations.ReservationStateService;
import org.wsd.agents.lock.validators.PermissionValidator;
import org.wsd.ontologies.MessageContentExtractor;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationMessageFactory;
import org.wsd.ontologies.reservation.ReservationOffer;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class ReservationCNPLockBehaviour extends ContractNetResponder {

    private final ReservationOfferService reservationOfferService = new ReservationOfferService();

    private final LockConfigurationProvider lockConfigurationProvider;
    private final ReservationMessageFactory reservationMessageFactory;
    private final MessageContentExtractor messageContentExtractor;
    private final LockAgent agent;

    public ReservationCNPLockBehaviour(final LockAgent agent, final MessageTemplate messageTemplate) {
        super(agent, messageTemplate);
        this.agent = agent;
        this.lockConfigurationProvider = new LockConfigurationProvider(agent);
        this.reservationMessageFactory = new ReservationMessageFactory(agent);
        this.messageContentExtractor = new MessageContentExtractor(agent);
    }

    @Override
    protected ACLMessage handleCfp(final ACLMessage cfp) {
        log.info("Handling reservation CFP from {} for reservation request: {}", cfp.getSender(), cfp);

        final Optional<ReservationDataRequest> reservationDataRequest = messageContentExtractor.extract(cfp, ReservationDataRequest.class);

        boolean meetsRequirements = reservationDataRequest.map(request -> {
                return lockConfigurationProvider.provide().map(lockConfig -> {
                    return isMeetingRequirements(request, lockConfig);
                }).orElseGet(() -> {
                    log.info("Could not load lock configuration");
                    return false; });
            }
        ).orElseGet(() -> {
            log.info("Could not load message content");
            return false; });
        
        /* TODO:
            1. refactor
        */
        if(meetsRequirements) {
            final ReservationStateService reservationStateService = agent.getReservationStateService();
            final int reservationId = agent.getNextId();

            final Reservation reservation = Reservation.builder()
                    .agent(agent.getAID())
                    .id(reservationId)
                    .reservationState(ReservationState.RESERVED)
                    .dateOfReservation(LocalDateTime.fromDateFields(reservationDataRequest.get().getDateSince()))
                    .build();

            reservationStateService.add(reservation);

            return lockConfigurationProvider.provide()
                    .map(lockConfiguration ->
                            reservationDataRequest
                                    .map(r -> {
                                        return reservationMessageFactory.buildReservationOfferReply(
                                                cfp,
                                                new ReservationOffer(reservationId, reservationOfferService.scoreOffer(r, lockConfiguration), lockConfiguration, agent.getAID())
                                        ).getOrElse(() -> null);
                                    })
                    .orElseGet(() -> null)).orElseGet(() -> null);
        } else {
            log.info("Refusing reservation");
            return reservationMessageFactory.buildRefuseReservationResponse(cfp);
        }
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
        log.info("HANDLE ACCEPT PROPOSAL");

        return null;
    }

    private boolean isMeetingRequirements(ReservationDataRequest request, LockConfiguration lockConfig) {
        log.info("Check if room meets reqirements: {}, lock configuration {}", request, lockConfig);

        if(!PermissionValidator.isPermissionsValid(request.getCertificate(), lockConfig.getRequiredAuthorizationLevel())) {
            return false;
        }

        if(request.getNumberOfParticipants() > lockConfig.getNumberOfSeats()) {
            return false;
        }
        if(null != request.getIsLaboratory()) {
            if(request.getIsLaboratory() && !lockConfig.getIsLaboratory())
                return false;
        }
        if(null != request.getIsSeminaryHall()) {
            if(request.getIsSeminaryHall() && !lockConfig.getIsSeminaryHall())
                return false;
        }
        if(null != request.getNumberOfComputers()) {
            if(request.getNumberOfComputers() > lockConfig.getNumberOfComputers())
                return false;
        }
        if(null != request.getIsTVRequired()) {
            if(request.getIsTVRequired() && !lockConfig.getHasTV())
                return false;
        }
        if(null != request.getIsMultimediaProjectorRequired()) {
            if(request.getIsMultimediaProjectorRequired() && !lockConfig.getHasMultimediaProjector())
                return false;
        }
        return true;
    }
}
