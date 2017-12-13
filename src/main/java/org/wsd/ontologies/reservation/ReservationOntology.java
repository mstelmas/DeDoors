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
			as.add(IS_WEEKLY, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN), ObjectSchema.MANDATORY);

			add(new ConceptSchema(RESERVATION_OFFER), ReservationOffer.class);

			final ConceptSchema reservationOfferConceptSchema = (ConceptSchema) getSchema(RESERVATION_OFFER);
			reservationOfferConceptSchema.add(RESERVATION_OFFER_SCORE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
		}).onFailure(ex -> log.error("Could not create Reservation ontology schema {}", ex));
	}

}
