package org.wsd.agents.lecturer;

import jade.core.AID;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lecturer.behaviours.AwaitLockResponseBehaviour;
import org.wsd.agents.lecturer.gui.LecturerAgentGui;
import org.wsd.ontologies.otp.OTPMessageFactory;
import org.wsd.ontologies.otp.OTPOntology;
import org.wsd.ontologies.otp.OTPVocabulary;

import javax.swing.*;

@Slf4j
public class LecturerAgent extends GuiAgent {

    transient private LecturerAgentGui lecturerAgentGui;

    private final OTPMessageFactory otpMessageFactory = new OTPMessageFactory(this);

    @Override
    protected void setup() {
        getContentManager().registerLanguage(OTPOntology.codec);
        getContentManager().registerOntology(OTPOntology.instance);

        SwingUtilities.invokeLater(() -> lecturerAgentGui = new LecturerAgentGui(this));
    }

    @Override
    protected void onGuiEvent(final GuiEvent guiEvent) {
        final int commandType = guiEvent.getType();

        if (commandType == OTPVocabulary.NEW_OTP) {
            requestOTPFromLock((AID)guiEvent.getAllParameter().next());
        }
    }

    private void requestOTPFromLock(@NonNull final AID lockAgent) {
        log.info("Requesting OTP from lock: {}", lockAgent);

        otpMessageFactory.buildGenerateOTPRequest(lockAgent)
                .onSuccess(otpRequestAclMessage -> {
                    send(otpRequestAclMessage);
                    addBehaviour(new AwaitLockResponseBehaviour(this));
                    log.info("GenerateOTPRequest successfully sent!");
                })
                .onFailure(ex -> log.info("Could not send GenerateOTPRequest: {}", ex));
    }

    public void updateOtpCode(final String newOtpCode) {
        lecturerAgentGui.refreshOtp(newOtpCode);
    }
}
