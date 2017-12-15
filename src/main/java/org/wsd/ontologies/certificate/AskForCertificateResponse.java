package org.wsd.ontologies.certificate;

import jade.content.Concept;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@NoArgsConstructor
@AllArgsConstructor
@Wither
@Data
public class AskForCertificateResponse implements Concept {
    @Getter
    private String certificate;
}
