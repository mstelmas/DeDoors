package org.wsd.agents.lecturer.gui;

import io.vavr.API;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jade.core.AID;
import jade.gui.GuiEvent;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;
import org.wsd.AgentResolverService;
import org.wsd.GuiLookAndFeelManager;
import org.wsd.agents.AgentTypes;
import org.wsd.agents.lecturer.LecturerAgent;
import org.wsd.agents.lecturer.LecturerGuiEvents;
import org.wsd.agents.lecturer.reservations.Reservation;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationOffer;

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
    private final JLabel lecturerReceiveOTPLabel = new JLabel("Lock opening code: ");
    private final JTextField lecturerReceivedOtpCodeTextField = new JTextField(10);
    private final JButton lecturerRequestOTPButton = new JButton("Request OTP for reservation");
    private final JButton cancelReservationButton = new JButton("Cancel reservation");
	/* TODO: Runtime lock agents discovery if really needed */
	private final JComboBox<AID> availableLockAgentsComboBox = new JComboBox<>();
    private final JList<Reservation> agentReservationsList = new JList<>();
    private final JScrollPane agentReservationsScrollPanel = new JScrollPane();

    /* Lock management GUI for USER_TECHNICIAN */
    private final TitledBorder technicianLockManagementPanelBorder = BorderFactory.createTitledBorder("Lock management (USER_TECHNICIAN)");
    private final JTextField technicianReceivedOtpCodeTextField = new JTextField(10);
    private final JButton technicianRequestOTPButton = new JButton("Open lock");
    private final JLabel technicianRequestOTPLabel = new JLabel("Lock opening code: ");

	private final TitledBorder askReservationsPanelBorder = BorderFactory.createTitledBorder("Reservation management (USER_LECTURER)");
    private final TitledBorder reservationRequirementsPanelBorder = BorderFactory.createTitledBorder("Reservation requirements");
	private final JButton askForReservationButton = new JButton("Ask for reservation");
	private final JSpinner startReservationDateTimeSpinner = new JSpinner(new SpinnerDateModel());
	private final JSpinner endReservationDateTimeSpinner = new JSpinner(new SpinnerDateModel());
	private final JLabel startReservationSpinnerText = new JLabel("Reservation since:");
	private final JLabel endReservationSpinnerText = new JLabel("Reservation to:");
	private final JLabel numberOfParticipantsText = new JLabel("Number of participants:");
	private final JSpinner numberOfParticipantsSpinner = new JSpinner();
	private final JCheckBox weeklyReservationCheckBox = new JCheckBox("Reservation every week");
	private final JCheckBox isLaboratoryCheckBox = new JCheckBox("Laboratory");
	private final JCheckBox isSeminaryHallCheckBox = new JCheckBox("Seminary Hall");
	private final JCheckBox multimediaProjectorCheckBox = new JCheckBox("Multimedia projector");
	private final JCheckBox tvCheckBox = new JCheckBox("TV");
	private final JLabel numberOfComputersLabel = new JLabel("Number of computers: ");
    private final JTextField numberOfComputersTextField = new JTextField(5);
    private final JLabel specificRoomNumberLabel = new JLabel("Specific room number: ");
    private final JTextField specificRoomNumberTextField = new JTextField(5);
    private final TitledBorder roomRequirementsPanelBorder = BorderFactory.createTitledBorder("Room requirements");

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

        cancelReservationButton.addActionListener(actionEvent -> {
            if (agentReservationsList.getSelectedValue() == null) {
                JOptionPane.showMessageDialog(this, "No reservation selected from list");
                return;
            }

            final GuiEvent cancelReservationEvent = new GuiEvent(this, LecturerGuiEvents.CANCEL_RESERVATION);
            cancelReservationEvent.addParameter(agentReservationsList.getSelectedValue());
            lecturerAgent.postGuiEvent(cancelReservationEvent);
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

		this.getContentPane().setLayout(new MigLayout());
		this.getContentPane().add(buildReservationsPanel(), "dock north, wrap, push, grow");
		this.getContentPane().add(buildLockManagementPanel(), "dock south, push, grow");
	}

	private JPanel buildReservationsPanel() {
		final JPanel askReservationsPanel = new JPanel(new MigLayout());
        askReservationsPanel.setBorder(askReservationsPanelBorder);

        askReservationsPanel.add(buildReservationRequirementsPanel(), "wrap, growx, pushx");

        askReservationsPanel.add(buildRoomRequirementsPanel(), "wrap, growx, pushx");

        askReservationsPanel.add(askForReservationButton, "right");

        return askReservationsPanel;
	}

	private JPanel buildReservationRequirementsPanel() {
        final JPanel reservationRequirementsPanel = new JPanel(new MigLayout());

        reservationRequirementsPanel.setBorder(reservationRequirementsPanelBorder);
        reservationRequirementsPanel.add(startReservationSpinnerText);
        reservationRequirementsPanel.add(startReservationDateTimeSpinner);
        reservationRequirementsPanel.add(endReservationSpinnerText);
        reservationRequirementsPanel.add(endReservationDateTimeSpinner, "wrap");
        reservationRequirementsPanel.add(numberOfParticipantsText);
        reservationRequirementsPanel.add(numberOfParticipantsSpinner);
        reservationRequirementsPanel.add(weeklyReservationCheckBox);

        startReservationDateTimeSpinner
                .setEditor(new JSpinner.DateEditor(startReservationDateTimeSpinner, "yyyy-MM-dd HH:mm"));
        endReservationDateTimeSpinner
                .setEditor(new JSpinner.DateEditor(endReservationDateTimeSpinner, "yyyy-MM-dd HH:mm"));


        return reservationRequirementsPanel;
    }

	private JPanel buildRoomRequirementsPanel() {
        final JPanel roomRequirementsPanel = new JPanel(new MigLayout());

        roomRequirementsPanel.setBorder(roomRequirementsPanelBorder);
        roomRequirementsPanel.add(isLaboratoryCheckBox);
        roomRequirementsPanel.add(isSeminaryHallCheckBox);
        roomRequirementsPanel.add(multimediaProjectorCheckBox);
        roomRequirementsPanel.add(tvCheckBox, "wrap");

        numberOfComputersLabel.setLabelFor(numberOfComputersTextField);
        roomRequirementsPanel.add(numberOfComputersLabel);
        roomRequirementsPanel.add(numberOfComputersTextField);

        specificRoomNumberLabel.setLabelFor(specificRoomNumberTextField);
        roomRequirementsPanel.add(specificRoomNumberLabel);
        roomRequirementsPanel.add(specificRoomNumberTextField);

        return roomRequirementsPanel;
    }

	// TODO: read all reservation data
	private ReservationDataRequest readReservationData() {
		ReservationDataRequest data = new ReservationDataRequest();
		data.setDateSince((Date) startReservationDateTimeSpinner.getValue());
		data.setDateTo((Date) endReservationDateTimeSpinner.getValue());
		data.setNumberOfParticipants((Integer) numberOfParticipantsSpinner.getValue());
		data.setIsWeekly(weeklyReservationCheckBox.isSelected());
		data.setIsLaboratory(isLaboratoryCheckBox.isSelected());
		data.setIsSeminaryHall(isSeminaryHallCheckBox.isSelected());
		data.setIsMultimediaProjectorRequired(multimediaProjectorCheckBox.isSelected());
		data.setIsTVRequired(tvCheckBox.isSelected());
        Try.of(() -> Integer.valueOf(specificRoomNumberTextField.getText())).onSuccess(data::setSpecificRoomNumber);
        Try.of(() -> Integer.valueOf(numberOfComputersTextField.getText())).onSuccess(data::setNumberOfComputers);
		return data;
	}

	private JPanel buildLockManagementPanel() {
		final JPanel mainLockManagementPanel = new JPanel(new MigLayout());

        mainLockManagementPanel.add(buildLecturerLockManagementPanel(), "pushx, growx");
        mainLockManagementPanel.add(buildTechnicianLockManagementPanel(), "pushx, growx");

		return mainLockManagementPanel;
	}

	private JPanel buildLecturerLockManagementPanel() {
        final JPanel lecturerLockManagementPanel = new JPanel(new MigLayout());

        agentReservationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        agentReservationsList.setCellRenderer(agentReservationsListRenderer);
        agentReservationsScrollPanel.setViewportView(agentReservationsList);
        lecturerLockManagementPanel.add(agentReservationsScrollPanel, "span2, wrap, growx, pushx");

        refreshAvailableReservations();

        lecturerLockManagementPanel.setBorder(lecturerLockManagementPanelBorder);
        lecturerLockManagementPanel.add(cancelReservationButton, "wrap, pushx, growx");
        lecturerLockManagementPanel.add(lecturerRequestOTPButton, "wrap, pushx, growx");

        lecturerReceiveOTPLabel.setLabelFor(lecturerReceivedOtpCodeTextField);
        lecturerLockManagementPanel.add(lecturerReceiveOTPLabel, "left, sg 1, split");
        lecturerReceivedOtpCodeTextField.setEditable(false);
        lecturerLockManagementPanel.add(lecturerReceivedOtpCodeTextField, "wrap, pushx, growx");

        return lecturerLockManagementPanel;
    }

    private JPanel buildTechnicianLockManagementPanel() {
        final JPanel technicianLockManagementPanel = new JPanel(new MigLayout());

        availableLockAgentsComboBox.setRenderer(availableLockAgentsComboBoxRenderer);
        availableLockAgentsComboBox.setModel(new DefaultComboBoxModel(agentResolverService.agentsOfType(AgentTypes.LOCK).toArray()));
        technicianLockManagementPanel.add(availableLockAgentsComboBox);

        technicianLockManagementPanel.setBorder(technicianLockManagementPanelBorder);
        technicianLockManagementPanel.add(technicianRequestOTPButton, "wrap, pushx, growx");

        technicianRequestOTPLabel.setLabelFor(technicianReceivedOtpCodeTextField);
        technicianLockManagementPanel.add(technicianRequestOTPLabel);
        technicianReceivedOtpCodeTextField.setEditable(false);
        technicianLockManagementPanel.add(technicianReceivedOtpCodeTextField, "pushx, growx");

        return technicianLockManagementPanel;
    }

	public void refreshAvailableReservations() {
        final DefaultListModel<Reservation> availableReservationsListModel = new DefaultListModel<>();
        lecturerAgent.getReservationsStateService().findAll().forEach(availableReservationsListModel::addElement);
        agentReservationsList.setModel(availableReservationsListModel);
    }

    public void confirmReservationOffer(final ReservationOffer reservationOffer) {
	    final Object[] choices = { "Accept", "Reject" };
	    final int choice = JOptionPane.showOptionDialog(
	            this,
	            String.format(
	                    "Reservation offer received from %s\n\n" +
                        "Offer ID: %d\n" +
                        "Seats: %d\n" +
                        "Computers: %d\n" +
                        "Cost: %d\n\n",
                        reservationOffer.getLockAID().getLocalName(), reservationOffer.getId(),
                        reservationOffer.getNumberOfSeats(), reservationOffer.getNumberOfComputers(),
                        reservationOffer.getScore()),
                "Reservation offer",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                choices,
                choices[0]
        );

        final Reservation reservation = new Reservation(reservationOffer.getId(), reservationOffer.getLockAID());
	    if (choice == JOptionPane.YES_OPTION) {
            final GuiEvent acceptReservationOfferEvent = new GuiEvent(this, LecturerGuiEvents.ACCEPT_RESERVATION_OFFER);
            acceptReservationOfferEvent.addParameter(reservation);
            lecturerAgent.postGuiEvent(acceptReservationOfferEvent);
        } else if (choice == JOptionPane.NO_OPTION) {
            final GuiEvent rejectReservationOfferEvent = new GuiEvent(this, LecturerGuiEvents.REJECT_RESERVATION_OFFER);
            rejectReservationOfferEvent.addParameter(reservation);
            lecturerAgent.postGuiEvent(rejectReservationOfferEvent);
        }
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

    public final void cancelReservationSucceesInfo() {
        JOptionPane.showMessageDialog(this, "Reservation succesfully cancelled");
    }
}
