package org.opennms.gwt.web.ui;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.google.gwt.junit.tools.GWTTestSuite;

public class GwtCoreWebTestSuite extends GWTTestSuite {
    
    public static Test suite() {
        TestSuite suite = new TestSuite("All GWT tests go here");
        
        return suite;
    }
}
