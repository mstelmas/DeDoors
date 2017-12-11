package org.wsd.agents.lock.otp;

import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OtpStateService {

    private static OtpStateService instance;

    private final OtpCodeGenerator otpCodeGenerator = new OtpCodeGenerator();
    private String currentOtpCode;

    public synchronized static OtpStateService instance() {
        if (instance == null) {
            instance = new OtpStateService();
        }
        return instance;
    }

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
