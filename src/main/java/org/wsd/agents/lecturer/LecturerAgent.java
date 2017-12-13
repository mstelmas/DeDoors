package org.wsd.agents.lecturer;

import jade.core.AID;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.wsd.AgentResolverService;
import org.wsd.agents.AgentTypes;
import org.wsd.agents.lecturer.behaviours.AwaitLockResponseBehaviour;
import org.wsd.agents.lecturer.gui.LecturerAgentGui;
import org.wsd.agents.lock.LockAgent;
import org.wsd.ontologies.otp.OTPMessageFactory;
import org.wsd.ontologies.otp.OTPOntology;
import org.wsd.ontologies.otp.OTPVocabulary;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationMessageFacotry;
import org.wsd.ontologies.reservation.ReservationOntology;

import javax.swing.*;

@Slf4j
public class LecturerAgent extends GuiAgent {

	transient private LecturerAgentGui lecturerAgentGui;

	private final OTPMessageFactory otpMessageFactory = new OTPMessageFactory(this);
	private final ReservationMessageFacotry reservationMessageFactory = new ReservationMessageFacotry(this);

	@Override
	protected void setup() {
		getContentManager().registerLanguage(OTPOntology.codec);
		getContentManager().registerOntology(OTPOntology.instance);
		getContentManager().registerLanguage(ReservationOntology.codec);
		getContentManager().registerOntology(ReservationOntology.instance);

		SwingUtilities.invokeLater(() -> lecturerAgentGui = new LecturerAgentGui(this));
	}

	@Override
	protected void onGuiEvent(final GuiEvent guiEvent) {
		final int commandType = guiEvent.getType();

		if (commandType == LecturerGuiEvents.NEW_OTP) {
			requestOTPFromLock((AID) guiEvent.getAllParameter().next());
		}

		if (commandType == LecturerGuiEvents.ASK_FOR_RESERVATION) {
			ReservationDataRequest data = (ReservationDataRequest) guiEvent.getAllParameter().next();
			askRandomLockForReservation(data);
			log.info("Reservatuon data: {}", data);
		}
	}

	private void requestOTPFromLock(@NonNull final AID lockAgent) {
		log.info("Requesting OTP from lock: {}", lockAgent);

		otpMessageFactory.buildGenerateOTPRequest(lockAgent).onSuccess(otpRequestAclMessage -> {
			send(otpRequestAclMessage);
			addBehaviour(new AwaitLockResponseBehaviour(this));
			log.info("GenerateOTPRequest successfully sent!");
		}).onFailure(ex -> log.info("Could not send GenerateOTPRequest: {}", ex));
	}

	public void updateOtpCode(final String newOtpCode) {
		lecturerAgentGui.refreshOtp(newOtpCode);
	}

	private void askRandomLockForReservation(@NonNull final ReservationDataRequest data) {
		AgentResolverService agentResolverService = new AgentResolverService(this);
		AID lockAgent = agentResolverService.getRandomAgent(AgentTypes.LOCK);
		log.info("Sending reservation request, negotiator is: {}", lockAgent);
		reservationMessageFactory.buildReservationRequest(lockAgent, data).onSuccess(reservationAclMessage -> {
			send(reservationAclMessage);
			log.info("Message succesfully sent!");
		}).onFailure(ex -> log.info("Could not send ReservationRequest: {}", ex));
	}
}
