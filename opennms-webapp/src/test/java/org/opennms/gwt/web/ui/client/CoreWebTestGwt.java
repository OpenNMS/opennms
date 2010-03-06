package org.opennms.gwt.web.ui.client;

import com.google.gwt.junit.client.GWTTestCase;

public class CoreWebTestGwt extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.opennms.gwt.web.ui.CoreWeb";
    }
    
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
