package org.opennms.util.ilr;

import static org.junit.Assert.*;
import org.opennms.util.ilr.Main;
import org.junit.Test;

public class MainTest {
    Main main = new Main();
    public void setup(String testFile, String sortFlag) {
        String [] args = new String[2];
        args[0] = testFile;
        args[1] = sortFlag;
        main.execute(args, System.out);
    }
    @Test
    public void testParseTotalCollectionSortFlag() {
        setup("TestLogFile.log", "-tc");
        assertEquals("TOTALCOLLECTS", main.getCollector().m_sortColumn.toString());
     }
    @Test
    public void testParseAverageCollectionTime() {
        setup("TestLogFile.log", "-act");
        assertEquals("AVGCOLLECTTIME", main.getCollector().m_sortColumn.toString());
     }
    @Test
    public void testParseTotalCollectionTime(){
        setup("TestLogFile.log", "-tct");
        assertEquals("TOTALCOLLECTTIME", main.getCollector().m_sortColumn.toString());
    }
    @Test
    public void testParseAverageTimeBetweenCollection() {
        setup("TestLogFile.log","-atbc");
        assertEquals("AVGTIMEBETWEENCOLLECTS", main.getCollector().m_sortColumn.toString());
     }
    @Test
    public void testParseAverageSuccessfulCollectionTime() {
        setup("TestLogFile.log","-asct");
        assertEquals("AVGSUCCESSCOLLECTTIME", main.getCollector().m_sortColumn.toString());
     }
    @Test
    public void testParseSuccessfulPercentage() {
        setup("TestLogFile.log","-sp");
        assertEquals("SUCCESSPERCENTAGE", main.getCollector().m_sortColumn.toString());
     }
    @Test
    public void testParseAverageUnsuccessfulCollectionTime() {
        setup("TestLogFile.log","-auct");
        assertEquals("AVGUNSUCCESSCOLLECTTIME", main.getCollector().m_sortColumn.toString());
     }
    @Test
    public void testParseUnsuccessfulPercentage() {
        setup("TestLogFile.log","-up");
        assertEquals("UNSUCCESSPERCENTAGE", main.getCollector().m_sortColumn.toString());
     }
    @Test
    public void testParseTotalPersistTime() {
        setup("TestLogFile.log","-tpt");
        assertEquals("TOTALPERSISTTIME", main.getCollector().m_sortColumn.toString());
     }
}
