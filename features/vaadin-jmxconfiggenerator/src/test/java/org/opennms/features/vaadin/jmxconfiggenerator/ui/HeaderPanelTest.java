package org.opennms.features.vaadin.jmxconfiggenerator.ui;

import org.junit.Assert;
import org.junit.Test;

public class HeaderPanelTest {
    @Test
    public void testGenerateLabel() {
        HeaderPanel panel = new HeaderPanel();
        String label = panel.generateLabel(UiState.ResultView.name());
        Assert.assertNotNull(label);

        for (UiState eachState : UiState.values()) {
            if (eachState.hasUi()) {
                Assert.assertTrue(
                        String.format("The label '%s' does not contain the ui state '%s'", label, eachState),
                        label.contains(eachState.getDescription()));
            }
        }

    }
}
