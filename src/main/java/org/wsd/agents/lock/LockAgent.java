package org.wsd.agents.lock;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import org.wsd.agents.lock.behaviours.LockMessageHandler;
import org.wsd.ontologies.otp.OTPOntology;

public class LockAgent extends Agent {

    private final Codec codec = new SLCodec();

    @Override
    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(OTPOntology.instance);

        final SequentialBehaviour sequentialBehaviour = new SequentialBehaviour();
        sequentialBehaviour.addSubBehaviour(new LockMessageHandler(this));
        addBehaviour(sequentialBehaviour);
    }

}
