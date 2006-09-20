package org.opennms.test.mock;

public class MockUtil {

    public static boolean printEnabled() {
        return "true".equals(System.getProperty("mock.debug", "true"));
    }

    /**
     * @param string
     */
    public static void println(String string) {
        if (MockUtil.printEnabled())
            System.err.println(string);
    }

}
