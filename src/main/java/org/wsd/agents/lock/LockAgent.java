package org.wsd.agents.lock;

import jade.core.AID;
import jade.domain.FIPANames;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.wsd.AgentResolverService;
import org.wsd.agents.AgentTypes;
import org.wsd.agents.lock.behaviours.LockOTPMessageHandler;
import org.wsd.agents.lock.behaviours.LockReservationMessageHandler;
import org.wsd.agents.lock.behaviours.PermissionsHandler;
import org.wsd.agents.lock.behaviours.ReservationCNPLockBehaviour;
import org.wsd.agents.lock.configuration.LockConfigurationProvider;
import org.wsd.agents.lock.gui.LockAgentGui;
import org.wsd.agents.lock.otp.OtpStateService;
import org.wsd.ontologies.certificate.CertificateMessageFactory;
import org.wsd.ontologies.certificate.CertificateOntology;
import org.wsd.ontologies.otp.OTPOntology;
import org.wsd.ontologies.reservation.ReservationOntology;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class LockAgent extends GuiAgent {

    transient private LockAgentGui lockAgentGui;

    @Getter
    private final AtomicBoolean isLocked = new AtomicBoolean(true);

    @Getter
    private final OtpStateService otpStateService = new OtpStateService();

    private int nextReservationId = 0;

    @Setter
    private String permissions = "";
    @Getter
    private int requiredAuthorization = 999;

    private final MessageTemplate RESERVATION_CNP_MESSAGE_TEMPLATE = MessageTemplate.and(
            MessageTemplate.and(
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                    MessageTemplate.MatchPerformative(ACLMessage.CFP)
            ),
            MessageTemplate.and(
                    MessageTemplate.MatchLanguage(ReservationOntology.codec.getName()),
                    MessageTemplate.MatchOntology(ReservationOntology.instance.getName())
            )
    );

    @Override
    protected void setup() {
        LockConfigurationProvider lockConfigurationProvider = new LockConfigurationProvider(this);
        requiredAuthorization = lockConfigurationProvider.provide().get().getRequiredAuthorizationLevel();

        getContentManager().registerLanguage(OTPOntology.codec);
        getContentManager().registerOntology(OTPOntology.instance);

        getContentManager().registerLanguage(ReservationOntology.codec);
        getContentManager().registerOntology(ReservationOntology.instance);

        getContentManager().registerLanguage(CertificateOntology.codec);
        getContentManager().registerOntology(CertificateOntology.instance);
        
        addBehaviour(new LockOTPMessageHandler(this));
        addBehaviour(new LockReservationMessageHandler(this));
        addBehaviour(new ReservationCNPLockBehaviour(this, RESERVATION_CNP_MESSAGE_TEMPLATE));
        addBehaviour(new PermissionsHandler(this));

        SwingUtilities.invokeLater(() -> lockAgentGui = new LockAgentGui(this));

        if (permissions == "")
            requestPermissionsFromKeeper();
    }

    @Override
    protected void onGuiEvent(final GuiEvent guiEvent) {
        final int commandType = guiEvent.getType();

        if (commandType == LockGuiEvents.VALIDATE_OTP) {

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

    public int getNextId() {
        nextReservationId++;
        return nextReservationId;
    }

    private void requestPermissionsFromKeeper() {
        AgentResolverService agentResolverService = new AgentResolverService(this);
        AID agent = agentResolverService.getRandomAgent(AgentTypes.KEEPER);

        CertificateMessageFactory certificateMessageFactory = new CertificateMessageFactory(this);
        certificateMessageFactory.buildAskForPermissionsRequest(agent).onSuccess(requestAclMessage -> {
            send(requestAclMessage);

            log.info("AskForPermissionsRequest successfully sent!");
        }).onFailure(ex -> log.info("Could not send AskForPermissionsRequest: {}", ex));
    }
}
