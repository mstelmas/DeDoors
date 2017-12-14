package org.wsd.agents.keeper;

import jade.core.Agent;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.keeper.behaviours.RequestCertificateHandler;
import org.wsd.ontologies.certificate.CertificateOntology;

@Slf4j
public class KeeperAgent extends Agent {

    @Override
    protected void setup() {
        getContentManager().registerLanguage(CertificateOntology.codec);
        getContentManager().registerOntology(CertificateOntology.instance);

        // TODO create and add behaviours
        addBehaviour(new RequestCertificateHandler(this));
//        addBehaviour(new RequestPermissionsMessageHandler(this));
    }
}
