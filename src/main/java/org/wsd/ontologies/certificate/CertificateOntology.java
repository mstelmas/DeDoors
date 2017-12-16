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
            {
                add(new AgentActionSchema(ASK_FOR_CERIFICATE_REQUEST), AskForCertificateRequest.class);
                AgentActionSchema aas = (AgentActionSchema) getSchema(ASK_FOR_CERIFICATE_REQUEST);
                aas.add(EMAIL, (PrimitiveSchema) getSchema(BasicOntology.STRING));
                aas.add(PASSOWRD, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            }
            {
                add(new AgentActionSchema(ASK_FOR_CERIFICATE_RESPONSE), AskForCertificateResponse.class);
                AgentActionSchema aas = (AgentActionSchema) getSchema(ASK_FOR_CERIFICATE_RESPONSE);
                aas.add(CERIFICATE, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
            }
            add(new AgentActionSchema(ASK_FOR_PERMISSIONS_REQUEST), AskForPermissionsRequest.class);
            {
                add(new AgentActionSchema(ASK_FOR_PERMISSIONS_RESPONSE), AskForPermissionsResponse.class);
                AgentActionSchema aas = (AgentActionSchema) getSchema(ASK_FOR_PERMISSIONS_RESPONSE);
                aas.add(PERSMISSIONS, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
            }
        }).onFailure(ex -> log.error("Could not create Reservation ontology schema {}", ex));
    }
}
