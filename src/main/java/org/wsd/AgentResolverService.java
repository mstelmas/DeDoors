package org.wsd;

import io.vavr.control.Try;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.wsd.agents.AgentTypes;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.Collections;

@RequiredArgsConstructor
public class AgentResolverService {

    private final Agent agent;

    public Try<List<AID>> agentsOfType(final AgentTypes agentType) {
        final SearchConstraints searchConstraints = new SearchConstraints();
        searchConstraints.setMaxResults(-1L);

        return Try.of(() -> Arrays.asList(AMSService.search(agent, new AMSAgentDescription(), searchConstraints)))
                .map(amsAgentDescriptions ->
                    amsAgentDescriptions.stream()
                            .map(AMSAgentDescription::getName)
                            .filter(aid -> StringUtils.startsWith(aid.getName(), agentType.getName()))
                            .collect(Collectors.toList())
                );
    }

    public AID getRandomAgent(final AgentTypes agentType) {
        List<AID> agents = agentsOfType(agentType)
                    .getOrElse(Collections.emptyList());
        Random generator = new Random();
        return agents.get(generator.nextInt(agents.size()));
    }
}
