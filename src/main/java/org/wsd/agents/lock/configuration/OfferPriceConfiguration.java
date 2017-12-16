package org.wsd.agents.lock.configuration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OfferPriceConfiguration {
    private Integer emptySeatPenalty;
    private Integer standardRoomPrice;
    private Integer seminaryHallPrice;
    private Integer laboratoryPrice;
    private Integer multmediaProjectorPrice;
    private Integer computerPrice;
    private Integer tvPrice;
    private Integer squareMeterPrice;
}
