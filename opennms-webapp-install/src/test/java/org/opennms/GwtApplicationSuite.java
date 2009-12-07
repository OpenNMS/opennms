package org.opennms;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.google.gwt.junit.tools.GWTTestSuite;
import org.opennms.client.GwtApplicationTestCase;

public class GwtApplicationSuite extends GWTTestSuite {

    public GwtApplicationSuite() {
        this.addTestSuite(GwtApplicationTestCase.class);
    }

    public static Test suite() {
        return new GwtApplicationSuite();
    }
}
