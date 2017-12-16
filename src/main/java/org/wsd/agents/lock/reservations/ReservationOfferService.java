package org.wsd.agents.lock.reservations;

import java.util.Comparator;
import java.util.Random;

import org.wsd.agents.lock.configuration.LockConfiguration;
import org.wsd.agents.lock.configuration.OfferPriceConfigurationProvider;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationOffer;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReservationOfferService {
    private final Random random = new Random();

    /* TODO: come up with a better way of scoring reservation offers? :) */
    public int scoreOffer(@NonNull final ReservationDataRequest reservationDataRequest, @NonNull LockConfiguration lockConfiguration) {
        OfferPriceConfigurationProvider offerPriceConfigurationProvider = new OfferPriceConfigurationProvider();
        return offerPriceConfigurationProvider.provide().map((config)-> {
            int price = 0;
            if(lockConfiguration.getIsSeminaryHall())
                price += config.getSeminaryHallPrice();
            if(lockConfiguration.getIsLaboratory())
                price += config.getLaboratoryPrice();
            if(lockConfiguration.getHasTV())
                price += config.getTvPrice();
            if(lockConfiguration.getHasMultimediaProjector())
                price += config.getMultmediaProjectorPrice();
            price += config.getComputerPrice() * lockConfiguration.getNumberOfComputers();
            price += (lockConfiguration.getNumberOfSeats() - reservationDataRequest.getNumberOfParticipants()) * config.getEmptySeatPenalty();
            price += lockConfiguration.getArea() * config.getSquareMeterPrice();
            return price;
        }).orElseGet(() -> Integer.MAX_VALUE);
    }

    public static final Comparator<ReservationOffer> reservationOfferScoreComparator = Comparator.comparingInt(ReservationOffer::getScore);
}
