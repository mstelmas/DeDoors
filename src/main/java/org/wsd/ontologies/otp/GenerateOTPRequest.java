package org.wsd.ontologies.otp;

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
public class GenerateOTPRequest implements AgentAction {
    private String certificate;
    @Getter private Integer reservationId;
}
