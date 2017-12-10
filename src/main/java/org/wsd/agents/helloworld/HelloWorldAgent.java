package org.wsd.agents.helloworld;

import jade.core.Agent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloWorldAgent extends Agent {

    @Override
    protected void setup() {
        log.info("Hello! I'm a simple agent {}. Remove me, if you don't need me anymore ;(", getAID());
    }

}
