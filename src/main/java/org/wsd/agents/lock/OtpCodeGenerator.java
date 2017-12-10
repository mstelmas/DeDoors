package org.wsd.agents.lock;

import java.util.Random;

public class OtpCodeGenerator {

    public static final int MAX_OTP_CODE = 1000000;

    private Random random = new Random();

    public String generate() {
        return random.ints(0, MAX_OTP_CODE)
                .mapToObj(value -> String.format("%06d", value))
                .findFirst()
                .get();
    }

}
