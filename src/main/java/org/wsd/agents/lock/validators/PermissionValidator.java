package org.wsd.agents.lock.validators;

import io.vavr.control.Validation;
import jade.lang.acl.ACLMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lock.LockAgent;
import org.wsd.ontologies.otp.GenerateOTPRequest;

@Slf4j
public class PermissionValidator {

    public Validation<String, String> validateActionPermissions(LockAgent agent, @NonNull final ACLMessage message, @NonNull final GenerateOTPRequest generateOTPRequest) {
        final String certificate = generateOTPRequest.getCertificate();
        final int level = Integer.parseInt(certificate.replace("privateKeyLevel",""));

        final int requiredLevel = agent.getRequiredAuthorization();

        if (level >= requiredLevel) {
            return Validation.valid("ok");
        } else {
            return Validation.invalid("AuthorizationLevel is not enough");
        }
    }
}
