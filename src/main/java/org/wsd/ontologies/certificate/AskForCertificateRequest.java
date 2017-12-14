package org.wsd.ontologies.certificate;

import jade.content.AgentAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@NoArgsConstructor
@AllArgsConstructor
@Wither
@Data
public class AskForCertificateRequest implements AgentAction {
    @Getter
    private String email;
    @Getter
    private String password;
}
