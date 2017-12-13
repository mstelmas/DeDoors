package org.wsd.agents.lecturer.gui;

import io.vavr.API;
import jade.core.AID;
import jade.gui.GuiEvent;
import lombok.extern.slf4j.Slf4j;
import org.wsd.AgentResolverService;
import org.wsd.GuiLookAndFeelManager;
import org.wsd.agents.AgentTypes;
import org.wsd.agents.lecturer.LecturerAgent;
import org.wsd.agents.lecturer.LecturerGuiEvents;
import org.wsd.ontologies.reservation.ReservationDataRequest;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Date;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;
import static io.vavr.control.Try.run;

@Slf4j
public class LecturerAgentGui extends JFrame {

	private final static String PREFERRED_LOOK_AND_FEEL = "Nimbus";
	private final static String LECTURER_APP_TITLE_FORMAT = "Lecturer Reservation App (%s)";

	private final TitledBorder lockManagementPanelBorder = BorderFactory.createTitledBorder("Lock management");
	private final JTextField receivedOtpCodeTextField = new JTextField(10);
	private final JButton requestOTPButton = new JButton("Request OTP");
	/* TODO: Runtime lock agents discovery if really needed */
	private final JComboBox<AID> availableLockAgentsComboBox = new JComboBox<>();

	private final TitledBorder askReservationsPanelBorder = BorderFactory.createTitledBorder("Reservation management");
	private final JButton askForReservationButton = new JButton("Ask for reservation");
	private final JSpinner startReservationDateTimeSpinner = new JSpinner(new SpinnerDateModel());
	private final JSpinner endReservationDateTimeSpinner = new JSpinner(new SpinnerDateModel());
	private final JLabel startReservationSpinnerText = new JLabel("Reservation since:");
	private final JLabel endReservationSpinnerText = new JLabel("Reservation to:");
	private final JLabel numberOfParticipantsText = new JLabel("Number of participants:");
	private final JSpinner numberOfParticipantsSpinner = new JSpinner();
	private final JCheckBox weeklyReservationCheckBox = new JCheckBox("Reservation every week");

	private LecturerAgent lecturerAgent;
	private AgentResolverService agentResolverService;

	public LecturerAgentGui(final LecturerAgent lecturerAgent) {
		this.lecturerAgent = lecturerAgent;
		this.agentResolverService = new AgentResolverService(lecturerAgent);

		new GuiLookAndFeelManager().setUpLookAndFeel(PREFERRED_LOOK_AND_FEEL)
				.onFailure((e) -> log.warn("Could not load system default theme! Oh well..."));

		buildGui();

		this.setTitle(String.format(LECTURER_APP_TITLE_FORMAT, lecturerAgent.getLocalName()));
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
		requestOTPButton.addActionListener(actionEvent -> {
			final GuiEvent requestNewOtpEvent = new GuiEvent(this, LecturerGuiEvents.NEW_OTP);
			requestNewOtpEvent.addParameter(availableLockAgentsComboBox.getSelectedItem());
			lecturerAgent.postGuiEvent(requestNewOtpEvent);
		});
		askForReservationButton.addActionListener(actionEvent -> {
			final GuiEvent askForReservationEvent = new GuiEvent(this, LecturerGuiEvents.ASK_FOR_RESERVATION);
			ReservationDataRequest reservationData = readReservationData();
			askForReservationEvent.addParameter(reservationData);
			lecturerAgent.postGuiEvent(askForReservationEvent);
		});
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		this.getContentPane().add(buildReservationsPanel());
		this.getContentPane().add(buildLockManagementPanel());
	}

	private JPanel buildReservationsPanel() {
		final JPanel askReservationsPanel = new JPanel();

		askReservationsPanel.setBorder(askReservationsPanelBorder);
		askReservationsPanel.setLayout(new BoxLayout(askReservationsPanel, BoxLayout.Y_AXIS));
		askReservationsPanel.add(startReservationSpinnerText);
		askReservationsPanel.add(startReservationDateTimeSpinner);
		askReservationsPanel.add(endReservationSpinnerText);
		askReservationsPanel.add(endReservationDateTimeSpinner);
		askReservationsPanel.add(numberOfParticipantsText);
		askReservationsPanel.add(numberOfParticipantsSpinner);
		askReservationsPanel.add(weeklyReservationCheckBox);
		askReservationsPanel.add(askForReservationButton);

		startReservationDateTimeSpinner
				.setEditor(new JSpinner.DateEditor(startReservationDateTimeSpinner, "yyyy-MM-dd HH:mm"));
		endReservationDateTimeSpinner
				.setEditor(new JSpinner.DateEditor(endReservationDateTimeSpinner, "yyyy-MM-dd HH:mm"));
		return askReservationsPanel;
	}

	// TODO: read all reservation data
	private ReservationDataRequest readReservationData() {
		ReservationDataRequest data = new ReservationDataRequest();
		data.setDateSince((Date) startReservationDateTimeSpinner.getValue());
		data.setDateTo((Date) endReservationDateTimeSpinner.getValue());
		data.setNumberOfParticipants((Integer) numberOfParticipantsSpinner.getValue());
		data.setIsWeekly(weeklyReservationCheckBox.isSelected());
		return data;
	}

	private JPanel buildLockManagementPanel() {
		final JPanel lockManagementPanel = new JPanel();

		availableLockAgentsComboBox.setRenderer(availableLockAgentsComboBoxRenderer);
		availableLockAgentsComboBox.setModel(new DefaultComboBoxModel(
				agentResolverService.agentsOfType(AgentTypes.LOCK).toArray()));
		lockManagementPanel.add(availableLockAgentsComboBox);

		lockManagementPanel.setBorder(lockManagementPanelBorder);
		lockManagementPanel.add(requestOTPButton);

		receivedOtpCodeTextField.setEditable(false);
		lockManagementPanel.add(receivedOtpCodeTextField);

		return lockManagementPanel;
	}

	public void refreshOtp(final String otpCode) {
		receivedOtpCodeTextField.setText(otpCode);
	}

	private final ListCellRenderer availableLockAgentsComboBoxRenderer = new DefaultListCellRenderer() {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			API.Match(value).of(Case($(instanceOf(AID.class)), lockAID -> run(() -> setText(lockAID.getLocalName()))));

			return this;
		}
	};
}
