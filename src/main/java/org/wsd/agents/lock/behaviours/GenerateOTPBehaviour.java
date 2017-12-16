package org.wsd.agents.lock.behaviours;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lock.LockAgent;
import org.wsd.agents.lock.LockValidationService;
import org.wsd.ontologies.MessageContentExtractor;
import org.wsd.ontologies.otp.GenerateOTPRequest;
import org.wsd.ontologies.otp.OTPMessageFactory;

import java.util.Optional;

import static io.vavr.API.*;
import static io.vavr.Patterns.$Invalid;
import static io.vavr.Patterns.$Valid;

@Slf4j
public class GenerateOTPBehaviour extends OneShotBehaviour {

    private final ACLMessage otpRequestMessage;

    private final LockAgent agent;
    private final OTPMessageFactory otpMessageFactory;
    private final LockValidationService lockValidationService = new LockValidationService();
    private final MessageContentExtractor messageContentExtractor;

    public GenerateOTPBehaviour(final LockAgent agent, final ACLMessage otpRequestMessage) {
        super(agent);
        this.agent = agent;
        this.otpRequestMessage = otpRequestMessage;
        this.otpMessageFactory = new OTPMessageFactory(agent);
        this.messageContentExtractor = new MessageContentExtractor(agent);
    }

    @Override
    public void action() {
        final Optional<GenerateOTPRequest> maybeGenerateOTPRequest = messageContentExtractor.extract(otpRequestMessage, GenerateOTPRequest.class);

        if (!maybeGenerateOTPRequest.isPresent()) {
            log.info("Could not extract GenerateOTPRequest content");
            return;
        }

        final GenerateOTPRequest generateOTPRequest = maybeGenerateOTPRequest.get();

        final Validation<Seq<String>, String> otpValidationResult = lockValidationService.validateGenerateOTPRequest(agent, otpRequestMessage, generateOTPRequest);

        Match(otpValidationResult).of(
                Case($Valid($()), o -> run(() -> acceptOTPGenerationRequest(generateOTPRequest))),
                Case($Invalid($()), validationErrors -> run(() -> refuseOTPGenerationRequest(validationErrors)))
        );
    }

    private void acceptOTPGenerationRequest(final GenerateOTPRequest generateOTPRequest) {
        final String otpCode = agent.getOtpStateService().generate();

        log.info("Generated OTP code for request: {} is: {}", otpRequestMessage, otpCode);

        otpMessageFactory.buildGenerateOTPResponse(otpRequestMessage, otpCode, generateOTPRequest.getReservationId())
                .onSuccess(agent::send)
                .onFailure(ex -> {
                    log.info("Could not send generated OTP response: {}", ex);
                    agent.getOtpStateService().invalidate();
                });
    }

    private void refuseOTPGenerationRequest(final Seq<String> rejectionReasons) {
        log.info("Refusing OTP code generation because of: {} for request {}", rejectionReasons, otpRequestMessage);

        otpMessageFactory.buildRefuseOTPGenerationResponse(otpRequestMessage, rejectionReasons.mkString(","))
                .onSuccess(agent::send)
                .onFailure(ex -> {
                    log.error("Could not send OTP generation rejection response: {}", ex);
                });
    }
}
