package org.wsd.agents.lock;

import jade.core.behaviours.SequentialBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lock.behaviours.LockMessageHandler;
import org.wsd.agents.lock.gui.LockAgentGui;
import org.wsd.agents.lock.otp.OtpStateService;
import org.wsd.ontologies.otp.OTPOntology;
import org.wsd.ontologies.otp.OTPVocabulary;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class LockAgent extends GuiAgent {

    transient private LockAgentGui lockAgentGui;

    @Getter
    private final AtomicBoolean isLocked = new AtomicBoolean(true);

    @Getter
    private final OtpStateService otpStateService = new OtpStateService();

    @Override
    protected void setup() {
        getContentManager().registerLanguage(OTPOntology.codec);
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

            if (isLocked.get()) {
                final String enteredOtpCode = (String) guiEvent.getAllParameter().next();

                if (otpStateService.validate(enteredOtpCode).isValid()) {
                    log.info("OTP code match - unlocking lock!");
                    isLocked.set(false);
                }
            } else {
                    isLocked.set(true);
                    otpStateService.invalidate();
            }

            lockAgentGui.updateLockState();
        }
    }
}
