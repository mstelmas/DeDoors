package org.wsd.agents.lecturer;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import org.wsd.agents.lecturer.gui.LecturerAgentGui;

import javax.swing.*;

public class LecturerAgent extends GuiAgent {

    transient private LecturerAgentGui lecturerAgentGui;

    @Override
    protected void setup() {
        SwingUtilities.invokeLater(() -> lecturerAgentGui = new LecturerAgentGui(this));
    }

    @Override
    protected void onGuiEvent(final GuiEvent guiEvent) {

    }

}
