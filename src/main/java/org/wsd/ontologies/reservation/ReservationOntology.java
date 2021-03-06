package org.wsd.ontologies.reservation;

import io.vavr.control.Try;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PrimitiveSchema;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReservationOntology extends Ontology implements ReservationVocabulary {

	private static final String ONTOLOGY_NAME = "Reservation-ontology";

	public static final Ontology instance = new ReservationOntology();
	public static final Codec codec = new SLCodec();

	private ReservationOntology() {
		super(ONTOLOGY_NAME, BasicOntology.getInstance());

		Try.run(() -> {
			AgentActionSchema as;
			add(as = new AgentActionSchema(ASK_FOR_RESERVATION), ReservationDataRequest.class);
			as.add(DATE_SINCE, (PrimitiveSchema) getSchema(BasicOntology.DATE), ObjectSchema.MANDATORY);
			as.add(DATE_TO, (PrimitiveSchema) getSchema(BasicOntology.DATE), ObjectSchema.MANDATORY);
			as.add(NUMBER_OF_PARTICIPANTS, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.MANDATORY);
			as.add(NUMBER_OF_COMPUTERS, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
			as.add(IS_WEEKLY, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN), ObjectSchema.MANDATORY);
			as.add(IS_LABORATORY, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN), ObjectSchema.OPTIONAL);
			as.add(IS_SEMINARY_HALL, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN), ObjectSchema.OPTIONAL);
			as.add(IS_MULTIMEDIA_PROJECTOR_REQUIRED, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN), ObjectSchema.OPTIONAL);
			as.add(IS_TV_REQUIRED, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN), ObjectSchema.OPTIONAL);
			as.add(SPECIFIC_ROOM_NUMBER, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
			as.add(CERTIFICATE, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

			add(new ConceptSchema(RESERVATION_OFFER), ReservationOffer.class);

			final ConceptSchema reservationOfferConceptSchema = (ConceptSchema) getSchema(RESERVATION_OFFER);
			reservationOfferConceptSchema.add(RESERVATION_OFFER_SCORE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			reservationOfferConceptSchema.add(RESERVATION_ID, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			reservationOfferConceptSchema.add(NUMBER_OF_COMPUTERS, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
			reservationOfferConceptSchema.add(NUMBER_OF_SEATS, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
			reservationOfferConceptSchema.add(LOCK_AID, (ConceptSchema) getSchema(BasicOntology.AID), ObjectSchema.MANDATORY);
			add(new AgentActionSchema(CANCEL_RESERVATION), CancelReservationRequest.class);
			final AgentActionSchema cancelReservationActionSchema = (AgentActionSchema) getSchema(CANCEL_RESERVATION);
			cancelReservationActionSchema.add(RESERVATION_ID, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));

			add(new ConceptSchema(CANCELED_RESERVATION), CancelReservationResponse.class);
			final ConceptSchema canceledReservationConceptSchema = (ConceptSchema) getSchema(CANCELED_RESERVATION);
			canceledReservationConceptSchema.add(RESERVATION_ID, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));

			add(new ConceptSchema(REFUSE_RESERVATION_CANCELATION), RefuseReservationCancelationResponse.class);
			final ConceptSchema refusedReservationCancelationSchema = (ConceptSchema) getSchema(REFUSE_RESERVATION_CANCELATION);
			refusedReservationCancelationSchema.add(RESERVATION_ID, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));

			add(new ConceptSchema(CONFIRM_RESERVATION_RESPONSE), ConfirmReservationResponse.class);
			final ConceptSchema confirmReservationConceptSchema = (ConceptSchema) getSchema(CONFIRM_RESERVATION_RESPONSE);
			confirmReservationConceptSchema.add(RESERVATION_ID, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));

			add(new AgentActionSchema(CONFIRM_RESERVATION_REQUEST), ConfirmReservationRequest.class);
			final AgentActionSchema confirmReservationAfentActionSchema = (AgentActionSchema) getSchema(CONFIRM_RESERVATION_REQUEST);
			confirmReservationAfentActionSchema.add(RESERVATION_ID, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));

		}).onFailure(ex -> log.error("Could not create Reservation ontology schema {}", ex));
	}

}
