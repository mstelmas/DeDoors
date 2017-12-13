package org.wsd.agents.lock;

import lombok.NonNull;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationOffer;

import java.util.Comparator;
import java.util.Random;

public class ReservationOfferService {
    private final Random random = new Random();

    /* TODO: come up with a better way of scoring reservation offers? :) */
    public int scoreOffer(@NonNull final ReservationDataRequest reservationDataRequest) {
        return random.nextInt();
    }

    public static final Comparator<ReservationOffer> reservationOfferScoreComparator = Comparator.comparingInt(ReservationOffer::getScore);
}
