package org.wsd.ontologies.otp;

import jade.content.Concept;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@NoArgsConstructor
@AllArgsConstructor
@Wither
@Data
public class RefuseOTPGenerationResponse implements Concept {
    private String rejectionReasons;
}
