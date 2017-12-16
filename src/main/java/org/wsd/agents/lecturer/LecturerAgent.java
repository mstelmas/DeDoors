package org.wsd.agents.lecturer;

import io.vavr.control.Either;
import jade.core.AID;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.wsd.AgentResolverService;
import org.wsd.agents.AgentTypes;
import org.wsd.agents.lecturer.behaviours.AwaitLockResponseBehaviour;
import org.wsd.agents.lecturer.behaviours.ReservationResponseHandler;
import org.wsd.agents.lecturer.gui.LecturerAgentGui;
import org.wsd.agents.lecturer.reservations.Reservation;
import org.wsd.agents.lecturer.reservations.ReservationsStateService;
import org.wsd.agents.lecturer.behaviours.ResponseCertificateHandler;
import org.wsd.ontologies.certificate.CertificateMessageFactory;
import org.wsd.ontologies.certificate.CertificateOntology;
import org.wsd.ontologies.otp.OTPMessageFactory;
import org.wsd.ontologies.otp.OTPOntology;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationMessageFactory;
import org.wsd.ontologies.reservation.ReservationOntology;

import javax.swing.*;

@Slf4j
public class LecturerAgent extends GuiAgent {

	transient private LecturerAgentGui lecturerAgentGui;

	@Setter
	private String certificate = "";
	public void setCertificate(final String cert) {
		certificate = cert;
	}

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

		getContentManager().registerLanguage(CertificateOntology.codec);
		getContentManager().registerOntology(CertificateOntology.instance);

		addBehaviour(new ReservationResponseHandler(this));
		addBehaviour(new ResponseCertificateHandler(this));

		SwingUtilities.invokeLater(() -> lecturerAgentGui = new LecturerAgentGui(this));

		// TODO refactor, integrate into GUI or somewhere into statup
		if (certificate == "")
			requestCertificateFromKeeper("lecturer1@elka.pw.edu.pl", "password1");
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
		} else if (commandType == LecturerGuiEvents.CANCEL_RESERVATION) {
			cancelReservation((Reservation) guiEvent.getAllParameter().next());
		}
	}

	private void requestCertificateFromKeeper(final String email, String password) {
		AgentResolverService agentResolverService = new AgentResolverService(this);
		AID agent = agentResolverService.getRandomAgent(AgentTypes.KEEPER);

		CertificateMessageFactory certificateMessageFactory = new CertificateMessageFactory(this);
		certificateMessageFactory.buildAskForCertificateRequest(agent, email, password).onSuccess(requestAclMessage -> {
			send(requestAclMessage);

			log.info("AskForCertificateRequest successfully sent!");
		}).onFailure(ex -> log.info("Could not send AskForCertificateRequest: {}", ex));
	}

	/* TODO: Refactor ugly enum passing... */
	private void requestOTPFromLock(@NonNull final Reservation reservation, final UserAgentRoles userAgentRole) {
		log.info("Requesting OTP for reservation: {}", reservation);

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

	private void cancelReservation(@NonNull final Reservation reservation) {
		reservationMessageFactory.buildCancelReservationRequest(reservation).onSuccess(cancelReservationAclMessage -> {
			send(cancelReservationAclMessage);
			addBehaviour(new AwaitLockResponseBehaviour(this, UserAgentRoles.USER_LECTURER));
			log.info("CancelReservation successfully sent!");
		}).onFailure(ex -> log.info("Could not send CancelReservation: {}", ex));
	}

	public void confirmReservation(@NonNull final Reservation reservation) {
		reservationMessageFactory.buildConfirmReservationRequest(reservation).onSuccess(confirmReservationAclMessage -> {
			send(confirmReservationAclMessage);
			// addBehaviour(new AwaitLockResponseBehaviour(this, UserAgentRoles.USER_LECTURER));
			log.info("Successfully send confirm reservation message!");
		}).onFailure(ex -> log.info("Could not send confirm reservation message: {}", ex));
	}

	public void addReservationOffer(@NonNull final Reservation offer) {
		//TODO: Add reservation offer to agent
		log.info("Adding reservation offer to {}", getAID());

		log.info("Sending confirm reservation message automaticly");
		confirmReservation(offer);
	}

	public void updateReservations() {
		lecturerAgentGui.refreshAvailableReservations();
	}
}
