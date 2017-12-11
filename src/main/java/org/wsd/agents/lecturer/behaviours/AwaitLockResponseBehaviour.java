package org.wsd.agents.lecturer.behaviours;

import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lecturer.LecturerAgent;

@Slf4j
public class AwaitLockResponseBehaviour extends ParallelBehaviour {

    public static final long LOCK_RESPONSE_TIMEOUT_MS = 5000L;

    public AwaitLockResponseBehaviour(final LecturerAgent agent) {
        super(agent, 1);

        addSubBehaviour(new LockResponseHandler(agent));

        addSubBehaviour(new WakerBehaviour(agent, LOCK_RESPONSE_TIMEOUT_MS) {
            @Override
            protected void onWake() {
                log.info("Timeout reached!");
            }
        });
    }
}