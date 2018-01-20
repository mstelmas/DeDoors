package org.wsd.agents.lecturer;

import io.vavr.API;
import io.vavr.control.Either;
import jade.core.AID;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.wsd.AgentResolverService;
import org.wsd.agents.AgentTypes;
import org.wsd.agents.lecturer.behaviours.AwaitLockResponseBehaviour;
import org.wsd.agents.lecturer.behaviours.ReservationResponseHandler;
import org.wsd.agents.lecturer.behaviours.ResponseCertificateHandler;
import org.wsd.agents.lecturer.configuration.LecturerConfigurationProvider;
import org.wsd.agents.lecturer.gui.LecturerAgentGui;
import org.wsd.agents.lecturer.reservations.Reservation;
import org.wsd.agents.lecturer.reservations.ReservationsStateService;
import org.wsd.ontologies.certificate.CertificateMessageFactory;
import org.wsd.ontologies.certificate.CertificateOntology;
import org.wsd.ontologies.otp.OTPMessageFactory;
import org.wsd.ontologies.otp.OTPOntology;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationMessageFactory;
import org.wsd.ontologies.reservation.ReservationOffer;
import org.wsd.ontologies.reservation.ReservationOntology;

import javax.swing.*;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

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
		if (certificate == "") {
			LecturerConfigurationProvider lecturerConfigurationProvider = new LecturerConfigurationProvider(this);
			String email = lecturerConfigurationProvider.provide().get().getEmail();
			String password = lecturerConfigurationProvider.provide().get().getPassword();
			requestCertificateFromKeeper(email, password);
		}
	}

	@Override
	protected void onGuiEvent(final GuiEvent guiEvent) {
		final int commandType = guiEvent.getType();
		
		Match(guiEvent.getType()).of(
				Case($(LecturerGuiEvents.NEW_OTP_FOR_LECTURER), o -> API.run(() -> requestOTPFromLock((Reservation) guiEvent.getAllParameter().next(), UserAgentRoles.USER_LECTURER))),
				Case($(LecturerGuiEvents.NEW_OTP_FOR_TECHNICIAN), o -> API.run(() -> requestOTPFromLock(new Reservation(null, (AID)guiEvent.getAllParameter().next()), UserAgentRoles.USER_TECHNICIAN))),
				Case($(LecturerGuiEvents.ASK_FOR_RESERVATION), o -> API.run(() -> askRandomLockForReservation((ReservationDataRequest) guiEvent.getAllParameter().next()))),
				Case($(LecturerGuiEvents.CANCEL_RESERVATION), o -> API.run(() -> cancelReservation((Reservation) guiEvent.getAllParameter().next()))),
				Case($(LecturerGuiEvents.ACCEPT_RESERVATION_OFFER), o -> API.run(() -> confirmReservationOffer((Reservation) guiEvent.getAllParameter().next()))),
				Case($(LecturerGuiEvents.REJECT_RESERVATION_OFFER), o -> API.run(() -> rejectReservationOffer((Reservation) guiEvent.getAllParameter().next())))
		);
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

		otpMessageFactory.buildGenerateOTPRequest(reservation.getLock(), certificate,reservation.getId()).onSuccess(otpRequestAclMessage -> {
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
		data.setCertificate(certificate);

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

	public void confirmReservationOffer(@NonNull final Reservation reservation) {
		reservationMessageFactory.buildConfirmReservationRequest(reservation).onSuccess(confirmReservationAclMessage -> {
			send(confirmReservationAclMessage);
			log.info("Successfully send confirm reservation message!");
		}).onFailure(ex -> log.info("Could not send confirm reservation message: {}", ex));
	}

    private void rejectReservationOffer(final Reservation reservation) {
        log.info("Reservation offer {} rejected!", reservation);
    }

	public void updateReservations() {
		lecturerAgentGui.refreshAvailableReservations();
	}

	public void onReservationReceived(final ReservationOffer reservationOffer) {
		lecturerAgentGui.confirmReservationOffer(reservationOffer);
	}

	public void onReservationCanceled() {
		lecturerAgentGui.cancelReservationSucceesInfo();
	}

	public void onReservationNotCanceled() {
		lecturerAgentGui.cancelReservationFailureInfo();
	}
}
