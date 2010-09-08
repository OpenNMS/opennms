package org.opennms.rrd.utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for Performance data receiver.
 */
public class PerfDataReceiverTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PerfDataReceiverTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( PerfDataReceiverTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testReceiver()
    {
        assertTrue( true );
    }
}
