package org.opennms.gwt.web.ui.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class CoreWebTestGwt {

	@Test
    public void testUpperCasingLabel() {
        UpperCasingLabel upperCasingLabel = new UpperCasingLabel();
        
        upperCasingLabel.setText("foo");
        assertEquals("FOO", upperCasingLabel.getText());
        
        upperCasingLabel.setText("BAR");
        assertEquals("BAR", upperCasingLabel.getText());
        
        upperCasingLabel.setText("BaZ");
        assertEquals("BAZ", upperCasingLabel.getText());
    }

}
