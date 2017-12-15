package org.wsd.ontologies.reservation;

public interface ReservationVocabulary {
	public static final String ASK_FOR_RESERVATION = "GenerateReservationRequest";
	public static final String CANCEL_RESERVATION = "CancelReservationRequest";
	public static final String CANCELED_RESERVATION = "CancelReservationResponse";
	public static final String REFUSE_RESERVATION_CANCELATION= "RefuseReservationCancelationResponse";
	public static final String SELECTED_RESERVATION = "SelectedReservationResponse";
	public static final String RESERVATION_ID = "reservationId";
	public static final String DATE_SINCE = "dateSince";
	public static final String DATE_TO = "dateTo";
	public static final String NUMBER_OF_PARTICIPANTS = "numberOfParticipants";
	public static final String NUMBER_OF_COMPUTERS = "numberOfComputers";
	public static final String IS_WEEKLY = "isWeekly";
	public static final String IS_LABORATORY = "isLaboratory";
	public static final String IS_SEMINARY_HALL = "isSeminaryHall";
	public static final String IS_MULTIMEDIA_PROJECTOR_REQUIRED = "isMultimediaProjectorRequired";
	public static final String IS_TV_REQUIRED = "numberOfComputers";
	public static final String SPECIFIC_ROOM_NUMBER = "specificRoomNumber";

	public static final String RESERVATION_OFFER = "ReservationOffer";
	public static final String RESERVATION_OFFER_SCORE = "score";
}
