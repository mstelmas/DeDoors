package org.wsd.ontologies.otp;

public interface OTPVocabulary {
    public static final int NEW_OTP = 1;
    public static final int VALIDATE_OTP = 2;

    public static final String GENERATE_OTP = "GenerateOTPRequest";
    public static final String GENERATED_OTP = "GenerateOTPResponse";
    public static final String GENERATED_OTP_CODE = "otpCode";
}
