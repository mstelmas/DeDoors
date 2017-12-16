package org.wsd.agents.lock.configuration;

import io.vavr.control.Try;
import jade.core.Agent;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;

import java.util.Optional;

@Slf4j
@NoArgsConstructor
public class OfferPriceConfigurationProvider {
    private final String EMPTY_SEAT_PENALTY_KEY = "empty.seat.penalty";
    private final String SEMINARY_HALL_PRICE_KEY = "seminary.hall.price";
    private final String STANDARD_ROOM_PRICE_KEY = "standard.room.price";
    private final String LABORATORY_PRICE_KEY = "laboratory.price";
    private final String MULTIMEDIA_PROJECTOR_PRICE_KEY = "multimedia.projector.price";
    private final String TV_PRICE_KEY = "tv.price";
    private final String SQUARE_METER_PRICE = "square.meter.price";
    private final String COMPUTER_PRICE_KEY = "computer.price";

    private final Configurations configurations = new Configurations();

    /* TODO: Maybe configuration caching if we don't plan on modifying it during runtime */
    public Optional<OfferPriceConfiguration> provide() {
        final String configurationPropertiesFile = "/resources/configurations/offer-price.properties";

        return Try.of(() -> configurations.properties(getClass().getClassLoader().getResource(configurationPropertiesFile)))
                .map(propertiesConfiguration -> Optional.of(buildOfferPriceConfigurationFromProperties(propertiesConfiguration)))
                .getOrElse(() -> {
                    log.warn("Could not load offer configuration file {}", configurationPropertiesFile);
                    return Optional.empty();
                });
    }

    private OfferPriceConfiguration buildOfferPriceConfigurationFromProperties(final PropertiesConfiguration propertiesConfiguration) {
        return OfferPriceConfiguration.builder()
                .emptySeatPenalty(propertiesConfiguration.getInt(EMPTY_SEAT_PENALTY_KEY, 50))
                .seminaryHallPrice(propertiesConfiguration.getInt(SEMINARY_HALL_PRICE_KEY, 1000))
                .standardRoomPrice(propertiesConfiguration.getInt(STANDARD_ROOM_PRICE_KEY, 0))
                .laboratoryPrice(propertiesConfiguration.getInt(LABORATORY_PRICE_KEY, 500))
                .multmediaProjectorPrice(propertiesConfiguration.getInt(MULTIMEDIA_PROJECTOR_PRICE_KEY, 200))
                .tvPrice(propertiesConfiguration.getInt(TV_PRICE_KEY, 100))
                .squareMeterPrice(propertiesConfiguration.getInt(SQUARE_METER_PRICE, 10))
                .computerPrice(propertiesConfiguration.getInt(COMPUTER_PRICE_KEY, 50))
                .build();
    }
}
