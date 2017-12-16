package org.wsd.agents.lecturer.configuration;

import io.vavr.control.Try;
import jade.core.Agent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class LecturerConfigurationProvider {
    private final String EMAIL = "email";
    private final String PASSWORD = "password";

    private final Configurations configurations = new Configurations();

    private final Agent agent;

    public Optional<LecturerConfiguration> provide() {
        if (agent.getArguments().length < 2) {
            log.warn("No configuration file for lock {} specified.", agent.getLocalName());
            return Optional.empty();
        }

        final String configurationPropertiesFile = (String)agent.getArguments()[1];

        return Try.of(() -> configurations.properties(getClass().getClassLoader().getResource(configurationPropertiesFile)))
                .map(propertiesConfiguration -> Optional.of(buildLecturerConfigurationFromProperties(propertiesConfiguration)))
                .getOrElse(() -> {
                    log.warn("Could not load configuration file {} for agent {}", configurationPropertiesFile, agent.getLocalName());
                    return Optional.empty();
                });
    }

    private LecturerConfiguration buildLecturerConfigurationFromProperties(final PropertiesConfiguration propertiesConfiguration) {
        return LecturerConfiguration.builder()
                .email(propertiesConfiguration.getString(EMAIL, ""))
                .password(propertiesConfiguration.getString(PASSWORD, ""))
                .build();
    }
}
