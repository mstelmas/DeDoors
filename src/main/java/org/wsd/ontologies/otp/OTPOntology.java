package org.wsd.ontologies.otp;

import io.vavr.control.Try;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.schema.AgentActionSchema;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OTPOntology extends Ontology implements OTPVocabulary {

    private static final String ONTOLOGY_NAME = "OTP-code-ontology";

    public static final Ontology instance = new OTPOntology();

    private OTPOntology() {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());

        Try.run(() -> {
           add(new AgentActionSchema(GENERATE_OTP), GenerateOTPRequest.class);
        }).onFailure(ex -> log.error("Could not create OTP ontology schema {}", ex));
    }
}
