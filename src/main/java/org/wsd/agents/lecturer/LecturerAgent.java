package org.wsd.agents.lecturer;

import io.vavr.control.Either;
import jade.core.AID;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.wsd.AgentResolverService;
import org.wsd.agents.AgentTypes;
import org.wsd.agents.lecturer.behaviours.AwaitLockResponseBehaviour;
import org.wsd.agents.lecturer.gui.LecturerAgentGui;
import org.wsd.agents.lecturer.reservations.Reservation;
import org.wsd.agents.lecturer.reservations.ReservationsStateService;
import org.wsd.ontologies.otp.OTPMessageFactory;
import org.wsd.ontologies.otp.OTPOntology;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationMessageFactory;
import org.wsd.ontologies.reservation.ReservationOntology;

import javax.swing.*;

@Slf4j
public class LecturerAgent extends GuiAgent {

	transient private LecturerAgentGui lecturerAgentGui;

    @Getter
    private final ReservationsStateService reservationsStateService = new ReservationsStateService();

	private final OTPMessageFactory otpMessageFactory = new OTPMessageFactory(this);
	private final ReservationMessageFactory reservationMessageFactory = new ReservationMessageFactory(this);

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

		if (commandType == LecturerGuiEvents.NEW_OTP_FOR_LECTURER) {
			requestOTPFromLock((Reservation) guiEvent.getAllParameter().next(), UserAgentRoles.USER_LECTURER);
		} else if (commandType == LecturerGuiEvents.NEW_OTP_FOR_TECHNICIAN) {
		    /*
		       TODO: This is a temporal hack to reuse existing metod for generating
		             OTP for USER_LECTURER.

		             We create a dummy reservation for USER_TECHNICIAN, but when
		             the permissions are done this should not be necessary because
		             user role will be checked by a lock
		     */
            requestOTPFromLock(new Reservation(null, (AID)guiEvent.getAllParameter().next()), UserAgentRoles.USER_TECHNICIAN);
        } else if (commandType == LecturerGuiEvents.ASK_FOR_RESERVATION) {
			ReservationDataRequest data = (ReservationDataRequest) guiEvent.getAllParameter().next();
			askRandomLockForReservation(data);
			log.info("Reservatuon data: {}", data);
		}
	}

	/* TODO: Refactor ugly enum passing... */
	private void requestOTPFromLock(@NonNull final Reservation reservation, final UserAgentRoles userAgentRole) {
		log.info("Requesting OTP for reservation: {}", reservation);

		/* TODO: Picking specific reservation for OTP code request */
		otpMessageFactory.buildGenerateOTPRequest(reservation.getLock(), reservation.getId()).onSuccess(otpRequestAclMessage -> {
			send(otpRequestAclMessage);
			addBehaviour(new AwaitLockResponseBehaviour(this, userAgentRole));
			log.info("GenerateOTPRequest successfully sent!");
		}).onFailure(ex -> log.info("Could not send GenerateOTPRequest: {}", ex));
	}

	/* TODO: Refactor ;/ */
	public void updateOtpCode(final Either<String, String> otpCodeOrRefusalReasons, final UserAgentRoles userAgentRole) {
	    if (userAgentRole == UserAgentRoles.USER_LECTURER) {
            lecturerAgentGui.refreshLecturerOtp(otpCodeOrRefusalReasons);
        } else if (userAgentRole == UserAgentRoles.USER_TECHNICIAN){
	        lecturerAgentGui.refreshTechnicianOtp(otpCodeOrRefusalReasons);
        }
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
