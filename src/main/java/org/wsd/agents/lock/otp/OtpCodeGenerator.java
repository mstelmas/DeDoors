package org.wsd.agents.lock.otp;

import java.util.Random;

class OtpCodeGenerator {

    private static final int MAX_OTP_CODE = 1000000;

    private Random random = new Random();

    public String generate() {
        return random.ints(0, MAX_OTP_CODE)
                .mapToObj(value -> String.format("%06d", value))
                .findFirst()
                .get();
    }

}
