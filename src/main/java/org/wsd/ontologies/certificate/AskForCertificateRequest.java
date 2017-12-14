package org.wsd.ontologies.certificate;

import jade.content.AgentAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@NoArgsConstructor
@AllArgsConstructor
@Wither
@Data
public class AskForCertificateRequest implements AgentAction {
    private String email;
    private String passwordHash;
}
