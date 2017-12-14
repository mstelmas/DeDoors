package org.wsd.agents.lock.configuration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LockConfiguration {
    private Integer numberOfSeats;
    private Integer numberOfComputers;
    private Boolean isSeminaryHall;
    private Boolean isLaboratory;
    private Boolean hasMultimediaProjector;
    private Boolean hasTV;
}
