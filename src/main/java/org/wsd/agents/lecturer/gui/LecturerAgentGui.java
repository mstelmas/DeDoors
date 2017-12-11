package org.wsd.agents.lecturer.gui;

import jade.gui.GuiEvent;
import lombok.extern.slf4j.Slf4j;
import org.wsd.GuiLookAndFeelManager;
import org.wsd.agents.lecturer.LecturerAgent;
import org.wsd.ontologies.otp.OTPVocabulary;

import javax.swing.*;
import javax.swing.border.TitledBorder;

@Slf4j
public class LecturerAgentGui extends JFrame {

    private final static String PREFERRED_LOOK_AND_FEEL = "Nimbus";
    private final static String LECTURER_APP_TITLE_FORMAT = "Lecturer Reservation App (%s)";

    private final TitledBorder lockManagementPanelBorder = BorderFactory.createTitledBorder("Lock management");
    private final JTextField receivedOtpCodeTextField = new JTextField(10);
    private final JButton requestOTPButton = new JButton("Request OTP");

    private LecturerAgent lecturerAgent = null;

    public LecturerAgentGui(final LecturerAgent lecturerAgent) {
        this.lecturerAgent = lecturerAgent;

        new GuiLookAndFeelManager().setUpLookAndFeel(PREFERRED_LOOK_AND_FEEL)
                .onFailure((e) -> log.warn("Could not load system default theme! Oh well..."));

        buildGui();

        this.setTitle(String.format(LECTURER_APP_TITLE_FORMAT, lecturerAgent.getName()));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocationByPlatform(true);
        this.pack();
        this.setVisible(true);
    }

    /* For easier GUI development */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LecturerAgentGui(new LecturerAgent()));
    }

    private void buildGui() {
        requestOTPButton.addActionListener(actionEvent -> lecturerAgent.postGuiEvent(new GuiEvent(this, OTPVocabulary.NEW_OTP)));

        this.getContentPane().add(buildLockManagementPanel());
    }

    private JPanel buildLockManagementPanel() {
        final JPanel lockManagementPanel = new JPanel();

        lockManagementPanel.setBorder(lockManagementPanelBorder);
        lockManagementPanel.add(requestOTPButton);

        receivedOtpCodeTextField.setEnabled(false);
        receivedOtpCodeTextField.setEditable(false);

        lockManagementPanel.add(receivedOtpCodeTextField);

        return lockManagementPanel;
    }

    public void refreshOtp(final String otpCode) {
        receivedOtpCodeTextField.setText(otpCode);
    }

}
