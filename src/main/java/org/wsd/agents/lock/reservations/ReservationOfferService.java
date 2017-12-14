package org.wsd.agents.lock.reservations;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lock.configuration.LockConfiguration;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationOffer;

import java.util.Comparator;
import java.util.Random;

@Slf4j
public class ReservationOfferService {
    private final Random random = new Random();

    /* TODO: come up with a better way of scoring reservation offers? :) */
    public int scoreOffer(@NonNull final ReservationDataRequest reservationDataRequest, @NonNull LockConfiguration lockConfiguration) {
        log.info("Calculating score based on lock configuration: {}", lockConfiguration);
        return random.nextInt();
    }

    public static final Comparator<ReservationOffer> reservationOfferScoreComparator = Comparator.comparingInt(ReservationOffer::getScore);
}
