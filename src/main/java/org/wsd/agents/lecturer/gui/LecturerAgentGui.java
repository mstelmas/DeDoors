package org.wsd.agents.lecturer.gui;

import io.vavr.API;
import io.vavr.control.Either;
import jade.core.AID;
import jade.gui.GuiEvent;
import lombok.extern.slf4j.Slf4j;
import org.wsd.AgentResolverService;
import org.wsd.GuiLookAndFeelManager;
import org.wsd.agents.AgentTypes;
import org.wsd.agents.lecturer.LecturerAgent;
import org.wsd.agents.lecturer.LecturerGuiEvents;
import org.wsd.agents.lecturer.reservations.Reservation;
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

    /* Lock management GUI for USER_LECTURER */
	private final TitledBorder lecturerLockManagementPanelBorder = BorderFactory.createTitledBorder("Lock management (USER_LECTURER)");
	private final JTextField lecturerReceivedOtpCodeTextField = new JTextField(10);
	private final JButton lecturerRequestOTPButton = new JButton("Request OTP");
	/* TODO: Runtime lock agents discovery if really needed */
	private final JComboBox<AID> availableLockAgentsComboBox = new JComboBox<>();
    private final JList<Reservation> agentReservationsList = new JList<>();
    private final JScrollPane agentReservationsScrollPanel = new JScrollPane();

    /* Lock management GUI for USER_TECHNICIAN */
    private final TitledBorder technicianLockManagementPanelBorder = BorderFactory.createTitledBorder("Lock management (USER_TECHNICIAN)");
    private final JTextField technicianReceivedOtpCodeTextField = new JTextField(10);
    private final JButton technicianRequestOTPButton = new JButton("Open lock");

	private final TitledBorder askReservationsPanelBorder = BorderFactory.createTitledBorder("Reservation management (USER_LECTURER)");
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
		lecturerRequestOTPButton.addActionListener(actionEvent -> {
		    if (agentReservationsList.getSelectedValue() == null) {
		        JOptionPane.showMessageDialog(this, "No reservation selected from list");
		        return;
            }

			final GuiEvent requestNewOtpEvent = new GuiEvent(this, LecturerGuiEvents.NEW_OTP_FOR_LECTURER);
			requestNewOtpEvent.addParameter(agentReservationsList.getSelectedValue());
			lecturerAgent.postGuiEvent(requestNewOtpEvent);
		});

		askForReservationButton.addActionListener(actionEvent -> {
			final GuiEvent askForReservationEvent = new GuiEvent(this, LecturerGuiEvents.ASK_FOR_RESERVATION);
			ReservationDataRequest reservationData = readReservationData();
			askForReservationEvent.addParameter(reservationData);
			lecturerAgent.postGuiEvent(askForReservationEvent);
		});

        technicianRequestOTPButton.addActionListener(actionEvent -> {
            final GuiEvent requestNewOtpEvent = new GuiEvent(this, LecturerGuiEvents.NEW_OTP_FOR_TECHNICIAN);
            requestNewOtpEvent.addParameter(availableLockAgentsComboBox.getSelectedItem());
            lecturerAgent.postGuiEvent(requestNewOtpEvent);
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
		final JPanel mainLockManagementPanel = new JPanel();
		mainLockManagementPanel.setLayout(new BoxLayout(mainLockManagementPanel, BoxLayout.Y_AXIS));

        mainLockManagementPanel.add(buildLecturerLockManagementPanel());
        mainLockManagementPanel.add(buildTechnicianLockManagementPanel());

		return mainLockManagementPanel;
	}

	private JPanel buildLecturerLockManagementPanel() {
        final JPanel lecturerLockManagementPanel = new JPanel();

        agentReservationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        agentReservationsList.setCellRenderer(agentReservationsListRenderer);
        agentReservationsScrollPanel.setViewportView(agentReservationsList);
        lecturerLockManagementPanel.add(agentReservationsScrollPanel);

        refreshAvailableReservations();

        lecturerLockManagementPanel.setBorder(lecturerLockManagementPanelBorder);
        lecturerLockManagementPanel.add(lecturerRequestOTPButton);

        lecturerReceivedOtpCodeTextField.setEditable(false);
        lecturerLockManagementPanel.add(lecturerReceivedOtpCodeTextField);

        return lecturerLockManagementPanel;
    }

    private JPanel buildTechnicianLockManagementPanel() {
        final JPanel technicianLockManagementPanel = new JPanel();

        availableLockAgentsComboBox.setRenderer(availableLockAgentsComboBoxRenderer);
        availableLockAgentsComboBox.setModel(new DefaultComboBoxModel(agentResolverService.agentsOfType(AgentTypes.LOCK).toArray()));
        technicianLockManagementPanel.add(availableLockAgentsComboBox);

        technicianLockManagementPanel.setBorder(technicianLockManagementPanelBorder);
        technicianLockManagementPanel.add(technicianRequestOTPButton);

        technicianReceivedOtpCodeTextField.setEditable(false);
        technicianLockManagementPanel.add(technicianReceivedOtpCodeTextField);

        return technicianLockManagementPanel;
    }

	public void refreshAvailableReservations() {
        final DefaultListModel<Reservation> availableReservationsListModel = new DefaultListModel<>();
        lecturerAgent.getReservationsStateService().findAll().forEach(availableReservationsListModel::addElement);
        agentReservationsList.setModel(availableReservationsListModel);
    }

	public void refreshLecturerOtp(final Either<String, String> otpCodeOrRefusalReasons) {
		refreshOtp(otpCodeOrRefusalReasons, lecturerReceivedOtpCodeTextField);
	}

	public void refreshTechnicianOtp(final Either<String, String> otpCodeOrRefusalReasons) {
        refreshOtp(otpCodeOrRefusalReasons, technicianReceivedOtpCodeTextField);
    }

    private void  refreshOtp(final Either<String, String> otpCodeOrRefusalReasons, final JTextField otpCodeTextField) {
        otpCodeTextField.setText("");
        otpCodeOrRefusalReasons
                .map(otpCode -> {
                    otpCodeTextField.setText(otpCode);
                    return otpCode;
                })
                .orElseRun(refusalReasons -> JOptionPane.showMessageDialog(this, "OTP generation refused because of: " + refusalReasons));
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

    private final ListCellRenderer agentReservationsListRenderer = new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            API.Match(value)
                    .of(Case($(instanceOf(Reservation.class)), reservation -> run(() -> setText(String.format("Reservation ID: %d@%s", reservation.getId(), reservation.getLock().getLocalName())))));

            return this;
        }
    };
}
