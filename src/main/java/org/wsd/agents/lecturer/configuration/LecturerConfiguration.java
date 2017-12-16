package org.wsd.agents.lecturer.configuration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LecturerConfiguration {
    private String email;
    private String password;
}
