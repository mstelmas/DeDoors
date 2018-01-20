package org.wsd.agents.lock.gui;

import jade.gui.GuiEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.wsd.GuiLookAndFeelManager;
import org.wsd.agents.lock.LockAgent;
import org.wsd.agents.lock.LockGuiEvents;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

@Slf4j
public class LockAgentGui extends JFrame {

    private final static String PREFERRED_LOOK_AND_FEEL = "Nimbus";
    private final static String LOCK_APP_TITLE_FORMAT = "Lock Status (%s)";
    private final static Color LOCKED_COLOR = Color.RED;
    private final static Color UNLOCKED_COLOR = Color.GREEN;

    private final JPanel lockPanel = new JPanel();
    private final JButton unlockButton = new JButton();
    private final JTextField otpCodeTextField = new JTextField(10);
    private final JLabel lockStatusLabel = new JLabel();
    private final JPanel lockStatusPanel = new JPanel();

    private LockAgent lockAgent = null;

    public LockAgentGui(final LockAgent lockAgent) {
        this.lockAgent = lockAgent;

        new GuiLookAndFeelManager().setUpLookAndFeel(PREFERRED_LOOK_AND_FEEL)
                .onFailure((e) -> log.warn("Could not load system default theme! Oh well..."));

        buildGui();

        this.setTitle(String.format(LOCK_APP_TITLE_FORMAT, lockAgent.getLocalName()));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocationByPlatform(true);
        this.pack();
        /* TODO: Lock GUI temporary invisible for testing purposes */
        this.setVisible(true);
    }

    /* For easier GUI development */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LockAgentGui(new LockAgent()));
    }

    private void buildGui() {
        this.setLayout(new BorderLayout());

        updateLockState();

        lockStatusPanel.setLayout(new GridBagLayout());
        lockStatusPanel.add(lockStatusLabel);
        this.getContentPane().add(lockStatusPanel, BorderLayout.CENTER);

        unlockButton.addActionListener(actionEvent -> {
            final GuiEvent validateOtpEvent = new GuiEvent(this, LockGuiEvents.VALIDATE_OTP);
            validateOtpEvent.addParameter(otpCodeTextField.getText());
            lockAgent.postGuiEvent(validateOtpEvent);
        });

        lockPanel.add(otpCodeTextField);

        lockPanel.add(unlockButton);
        this.getContentPane().add(lockPanel, BorderLayout.SOUTH);
    }

    private Supplier<Pair<Color, String>> lockPanelStatus = () -> {
        if (lockAgent.getIsLocked().get()) {
            return Pair.of(LOCKED_COLOR, "Locked");
        } else {
            return Pair.of(UNLOCKED_COLOR, "Unlocked");
        }
    };

    private Supplier<String> unlockButtonStatus = () -> {
        if (lockAgent.getIsLocked().get()) {
            return "Unlock";
        } else {
            return "Lock";
        }
    };

    public void updateLockState() {
        unlockButton.setText(this.unlockButtonStatus.get());

        final Pair<Color, String> lockPanelStatus = this.lockPanelStatus.get();
        lockStatusPanel.setBackground(lockPanelStatus.getLeft());
        lockStatusLabel.setText(lockPanelStatus.getRight());
    }

}
