package org.wsd.agents.lock;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.core.behaviours.SequentialBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lock.behaviours.LockMessageHandler;
import org.wsd.agents.lock.gui.LockAgentGui;
import org.wsd.ontologies.otp.OTPOntology;
import org.wsd.ontologies.otp.OTPVocabulary;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class LockAgent extends GuiAgent {

    transient private LockAgentGui lockAgentGui;

    @Getter
    private final AtomicBoolean isLocked = new AtomicBoolean(true);

    private final Codec codec = new SLCodec();

    @Override
    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(OTPOntology.instance);

        final SequentialBehaviour sequentialBehaviour = new SequentialBehaviour();
        sequentialBehaviour.addSubBehaviour(new LockMessageHandler(this));
        addBehaviour(sequentialBehaviour);

        SwingUtilities.invokeLater(() -> lockAgentGui = new LockAgentGui(this));
    }

    @Override
    protected void onGuiEvent(final GuiEvent guiEvent) {
        final int commandType = guiEvent.getType();

        if (commandType == OTPVocabulary.VALIDATE_OTP) {
            log.info("Validating OTP: {}", guiEvent.getAllParameter().next());
        }
    }
}
