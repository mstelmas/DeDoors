package org.wsd.ontologies.reservation;

import org.wsd.agents.lock.configuration.LockConfiguration;

import jade.content.Concept;
import jade.core.AID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReservationOffer implements Concept {
    private int score;
    private int numberOfSeats;
    private int numberOfComputers;
    private AID lockAID;
    private int id;

    public ReservationOffer(int score, LockConfiguration configuration, AID lockAID, int id){
        this.score = score;
        this.numberOfComputers = configuration.getNumberOfComputers();
        this.numberOfSeats = configuration.getNumberOfSeats();
        this.lockAID = lockAID;
        this.id = id;
    }
}
