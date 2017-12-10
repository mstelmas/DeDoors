package org.wsd.agents.lecturer;

import io.vavr.control.Try;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lecturer.gui.LecturerAgentGui;
import org.wsd.ontologies.otp.GenerateOTPRequest;
import org.wsd.ontologies.otp.OTPOntology;
import org.wsd.ontologies.otp.OTPVocabulary;

import javax.swing.*;

@Slf4j
public class LecturerAgent extends GuiAgent {

    transient private LecturerAgentGui lecturerAgentGui;

    private final Codec codec = new SLCodec();

    @Override
    protected void setup() {
        getContentManager().registerLanguage(codec);
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
        final AID lockAgent1 = new AID("lock-agent-1", AID.ISLOCALNAME);
        otpRequestMessage.addReceiver(lockAgent1);
        otpRequestMessage.setLanguage(codec.getName());
        otpRequestMessage.setOntology(OTPOntology.instance.getName());

        Try.run(() -> getContentManager().fillContent(otpRequestMessage, new Action(lockAgent1, new GenerateOTPRequest())))
                .andThen(() -> {
                            send(otpRequestMessage);
                            log.info("GenerateOTPRequest successfully sent!");
                        })
                .onFailure(ex -> log.info("Could not send GenerateOTPRequest: {}", ex));
    }

}
