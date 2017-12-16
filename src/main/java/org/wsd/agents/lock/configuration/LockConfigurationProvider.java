package org.wsd.agents.lock.configuration;

import io.vavr.control.Try;
import jade.core.Agent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class LockConfigurationProvider {
    private final String NUM_OF_SEATS_KEY = "available.seats";
    private final String NUM_OF_COMPUTERS_KEY = "available.computers";
    private final String IS_SEMINARY_HALL_KEY = "is.seminary.hall";
    private final String IS_LABORATORY_KEY = "is.laboratory";
    private final String HAS_MULTIMEDIA_PROJECTOR_KEY = "has.multimedia.projector";
    private final String HAS_TV_KEY = "has.tv";
    private final String AREA_KEY = "area";
    private final String REQUIRED_AUTHORIZATION_LEVEL = "required.authorization.level";

    private final Configurations configurations = new Configurations();

    private final Agent agent;

    /* TODO: Maybe configuration caching if we don't plan on modifying it during runtime */
    public Optional<LockConfiguration> provide() {
        if (agent.getArguments().length < 2) {
            log.warn("No configuration file for lock {} specified.", agent.getLocalName());
            return Optional.empty();
        }

        final String configurationPropertiesFile = (String)agent.getArguments()[1];

        return Try.of(() -> configurations.properties(getClass().getClassLoader().getResource(configurationPropertiesFile)))
                .map(propertiesConfiguration -> Optional.of(buildLockConfigurationFromProperties(propertiesConfiguration)))
                .getOrElse(() -> {
                    log.warn("Could not load configuration file {} for agent {}", configurationPropertiesFile, agent.getLocalName());
                    return Optional.empty();
                });
    }

    private LockConfiguration buildLockConfigurationFromProperties(final PropertiesConfiguration propertiesConfiguration) {
        return LockConfiguration.builder()
                .numberOfSeats(propertiesConfiguration.getInt(NUM_OF_SEATS_KEY, 0))
                .numberOfComputers(propertiesConfiguration.getInteger(NUM_OF_COMPUTERS_KEY, 0))
                .isSeminaryHall(propertiesConfiguration.getBoolean(IS_SEMINARY_HALL_KEY, false))
                .isLaboratory(propertiesConfiguration.getBoolean(IS_LABORATORY_KEY, false))
                .hasMultimediaProjector(propertiesConfiguration.getBoolean(HAS_MULTIMEDIA_PROJECTOR_KEY, false))
                .hasTV(propertiesConfiguration.getBoolean(HAS_TV_KEY, false))
                .area(propertiesConfiguration.getInteger(AREA_KEY, 0))
                .requiredAuthorizationLevel(propertiesConfiguration.getInteger(REQUIRED_AUTHORIZATION_LEVEL, 1))
                .build();
    }
}
