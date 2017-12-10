package org.wsd.agents.lecturer.gui;

import io.vavr.control.Try;
import jade.gui.GuiEvent;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lecturer.LecturerAgent;
import org.wsd.ontologies.otp.OTPVocabulary;

import javax.swing.*;
import java.util.stream.Stream;

@Slf4j
public class LecturerAgentGui extends JFrame {

    private final static String PREFERRED_LOOK_AND_FEEL = "Nimbus";
    private final static String LECTURER_APP_TITLE = "Lecturer Reservation App";

    private final JButton requestOTPButton = new JButton("Request OTP");

    private LecturerAgent lecturerAgent = null;

    public LecturerAgentGui(final LecturerAgent lecturerAgent) {
        this.lecturerAgent = lecturerAgent;

        setUpLookAndFeel().onFailure((e) -> log.warn("Could not load system default theme! Oh well..."));

        buildGui();

        this.setTitle(LECTURER_APP_TITLE);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocationByPlatform(true);
        this.pack();
        this.setVisible(true);
    }

    private Try<Void> setUpLookAndFeel() {
        return loadLookAndFeelTheme(PREFERRED_LOOK_AND_FEEL)
                .onFailure((e) -> log.warn(String.format("Could not load theme: %s, loading system default theme...", PREFERRED_LOOK_AND_FEEL)))
                .orElse(this::loadSystemLookAndFeel);
    }

    private Try<Void> loadLookAndFeelTheme(@NonNull final String lookAndFeelTheme) {
        return Try.run(() -> UIManager.setLookAndFeel(
                Stream.of(UIManager.getInstalledLookAndFeels())
                        .filter(lookAndFeelInfo -> lookAndFeelInfo.getName().equals(lookAndFeelTheme))
                        .findAny()
                        .map(UIManager.LookAndFeelInfo::getClassName)
                        .orElseThrow(Exception::new)
        ));
    }

    private Try<Void> loadSystemLookAndFeel() {
        return Try.run(() -> UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()));
    }

    /* For easier GUI development */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LecturerAgentGui(null));
    }

    private void buildGui() {
        requestOTPButton.addActionListener(actionEvent -> lecturerAgent.postGuiEvent(new GuiEvent(this, OTPVocabulary.NEW_OTP)));

        this.getContentPane().add(requestOTPButton);
    }

}
