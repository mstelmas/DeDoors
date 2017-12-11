package org.wsd.agents.lecturer;

import jade.core.AID;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import lombok.extern.slf4j.Slf4j;
import org.wsd.AgentResolverService;
import org.wsd.agents.AgentTypes;
import org.wsd.agents.lecturer.behaviours.AwaitLockResponseBehaviour;
import org.wsd.agents.lecturer.gui.LecturerAgentGui;
import org.wsd.ontologies.otp.OTPMessageFactory;
import org.wsd.ontologies.otp.OTPOntology;
import org.wsd.ontologies.otp.OTPVocabulary;

import javax.swing.*;
import java.util.List;

@Slf4j
public class LecturerAgent extends GuiAgent {

    transient private LecturerAgentGui lecturerAgentGui;

    private final AgentResolverService agentResolverService = new AgentResolverService(this);

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
            requestOTP();
        }
    }

    private void requestOTP() {
        final List<AID> lockAgents = agentResolverService.agentsOfType(AgentTypes.LOCK)
                .getOrElseThrow(() -> new RuntimeException("Could not retrieve Lock Agents list"));

        if (lockAgents.isEmpty()) {
            log.info("No Lock Agents found, skipping...");
            return;
        }

        /* Send to first available lock agent. TODO: Temporary! */
        otpMessageFactory.buildGenerateOTPRequest(lockAgents.get(0))
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
