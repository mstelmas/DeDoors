package org.wsd.agents.lock.otp;

import io.vavr.control.Validation;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

public class OtpStateService {

    private final OtpCodeGenerator otpCodeGenerator = new OtpCodeGenerator();
    private String currentOtpCode;

    public String generate() {
        currentOtpCode = otpCodeGenerator.generate();
        return currentOtpCode;
    }

    public Validation<String, String> validate(@NonNull final String otpCode) {
        if (StringUtils.equals(currentOtpCode, otpCode)) {
            return Validation.valid(otpCode);
        } else {
            return Validation.invalid("invalid OTP");
        }
    }

    public void invalidate() {
        currentOtpCode = null;
    }
}
