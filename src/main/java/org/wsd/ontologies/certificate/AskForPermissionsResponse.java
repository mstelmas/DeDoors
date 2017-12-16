package org.wsd.ontologies.certificate;

import jade.content.Concept;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@NoArgsConstructor
@AllArgsConstructor
@Wither
@Data
public class AskForPermissionsResponse implements Concept {
    private String permissions;
}

