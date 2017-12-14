package org.wsd.ontologies.certificate;

import io.vavr.control.Try;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PrimitiveSchema;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CertificateOntology extends Ontology implements CertificateVocabulary {
    private static final String ONTOLOGY_NAME = "Certificate-ontology";

    public static final Ontology instance = new CertificateOntology();
    public static final Codec codec = new SLCodec();

    private CertificateOntology() {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());

        Try.run(() -> {
            AgentActionSchema as;
            add(as = new AgentActionSchema(ASK_FOR_CERIFICATE), AskForCertificateRequest.class);
            as.add(EMAIL, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
            as.add(PASSOWRD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

            add(as = new AgentActionSchema(ASK_FOR_CERIFICATE), AskForCertificateResponse.class);
            as.add(CERIFICATE, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

            add(as = new AgentActionSchema(ASK_FOR_PERMISSIONS), AskForPermissionsRequest.class);

            add(as = new AgentActionSchema(ASK_FOR_PERMISSIONS), AskForPermissionsResponse.class);
            as.add(PERSMISSIONS, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

        }).onFailure(ex -> log.error("Could not create Reservation ontology schema {}", ex));
    }
}
