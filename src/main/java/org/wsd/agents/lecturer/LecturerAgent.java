package org.wsd.agents.lecturer;

import io.vavr.control.Try;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.wsd.AgentResolverService;
import org.wsd.agents.AgentTypes;
import org.wsd.agents.lecturer.behaviours.AwaitLockResponseBehaviour;
import org.wsd.agents.lecturer.gui.LecturerAgentGui;
import org.wsd.ontologies.otp.GenerateOTPRequest;
import org.wsd.ontologies.otp.OTPOntology;
import org.wsd.ontologies.otp.OTPVocabulary;

import javax.swing.*;
import java.util.List;

@Slf4j
public class LecturerAgent extends GuiAgent {

    transient private LecturerAgentGui lecturerAgentGui;

    private final AgentResolverService agentResolverService = new AgentResolverService(this);

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
        final ACLMessage otpRequestMessage = new ACLMessage(ACLMessage.REQUEST);

        final List<AID> lockAgents = agentResolverService.agentsOfType(AgentTypes.LOCK)
                .getOrElseThrow(() -> new RuntimeException("Could not retrieve Lock Agents list"));

        if (lockAgents.isEmpty()) {
            log.info("No Lock Agents found, skipping...");
        }

        otpRequestMessage.addReceiver(lockAgents.get(0));
        otpRequestMessage.setLanguage(OTPOntology.codec.getName());
        otpRequestMessage.setOntology(OTPOntology.instance.getName());

        Try.run(() -> getContentManager().fillContent(otpRequestMessage, new Action(lockAgents.get(0), new GenerateOTPRequest())))
                .andThen(() -> {
                            send(otpRequestMessage);
                            addBehaviour(new AwaitLockResponseBehaviour(this));
                            log.info("GenerateOTPRequest successfully sent!");
                        })
                .onFailure(ex -> log.info("Could not send GenerateOTPRequest: {}", ex));
    }

    public void updateOtpCode(final String newOtpCode) {
        lecturerAgentGui.refreshOtp(newOtpCode);
    }

}
