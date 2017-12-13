package org.wsd.ontologies.otp;

import io.vavr.control.Try;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ConceptSchema;
import jade.content.schema.PrimitiveSchema;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OTPOntology extends Ontology implements OTPVocabulary {

    private static final String ONTOLOGY_NAME = "OTP-code-ontology";

    public static final Ontology instance = new OTPOntology();
    public static final Codec codec = new SLCodec();

    private OTPOntology() {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());

        Try.run(() -> {
           add(new AgentActionSchema(GENERATE_OTP), GenerateOTPRequest.class);

           add(new ConceptSchema(GENERATED_OTP), GenerateOTPResponse.class);

           final ConceptSchema generatedOTPConceptSchema = (ConceptSchema) getSchema(GENERATED_OTP);
           generatedOTPConceptSchema.add(GENERATED_OTP_CODE, (PrimitiveSchema) getSchema(BasicOntology.STRING));

        }).onFailure(ex -> log.error("Could not create OTP ontology schema {}", ex));
    }
}