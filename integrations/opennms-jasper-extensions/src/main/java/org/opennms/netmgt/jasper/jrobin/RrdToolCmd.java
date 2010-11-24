package org.opennms.netmgt.jasper.jrobin;

import java.io.IOException;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDbPool;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;

abstract class RrdToolCmd {

    public class EmptyJRDataSource implements JRDataSource {

		public Object getFieldValue(JRField arg0) throws JRException {
			return null;
		}

		public boolean next() throws JRException {
			return false;
		}

	}

	private RrdCmdScanner cmdScanner;

    abstract String getCmdType();

    abstract JRDataSource execute() throws RrdException, IOException;

    JRDataSource executeCommand(String command) throws RrdException {
        cmdScanner = new RrdCmdScanner(command);
        try {
        	return execute();
        }catch(IOException e) {
        	return new EmptyJRDataSource();
        }
        
    }

    String getOptionValue(String shortForm, String longForm) throws RrdException {
        return cmdScanner.getOptionValue(shortForm, longForm);
    }

    String getOptionValue(String shortForm, String longForm, String defaultValue) throws RrdException {
        return cmdScanner.getOptionValue(shortForm, longForm, defaultValue);
    }

    String[] getMultipleOptionValues(String shortForm, String longForm) throws RrdException {
        return cmdScanner.getMultipleOptions(shortForm, longForm);
    }

    boolean getBooleanOption(String shortForm, String longForm) {
        return cmdScanner.getBooleanOption(shortForm, longForm);
    }

    String[] getRemainingWords() {
        return cmdScanner.getRemainingWords();
    }

    static boolean rrdDbPoolUsed = true;
    static boolean standardOutUsed = true;

    static boolean isRrdDbPoolUsed() {
        return rrdDbPoolUsed;
    }

    static void setRrdDbPoolUsed(boolean rrdDbPoolUsed) {
        RrdToolCmd.rrdDbPoolUsed = rrdDbPoolUsed;
    }

    static boolean isStandardOutUsed() {
        return standardOutUsed;
    }

    static void setStandardOutUsed(boolean standardOutUsed) {
        RrdToolCmd.standardOutUsed = standardOutUsed;
    }

    static long parseLong(String value) throws RrdException {
        try {
            return Long.parseLong(value);
        }
        catch (NumberFormatException nfe) {
            throw new RrdException(nfe);
        }
    }

    static int parseInt(String value) throws RrdException {
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException nfe) {
            throw new RrdException(nfe);
        }
    }

    static double parseDouble(String value) throws RrdException {
        if (value.equals("U")) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException nfe) {
            throw new RrdException(nfe);
        }
    }

    static void print(String s) {
        if (standardOutUsed) {
            System.out.print(s);
        }
    }

    static void println(String s) {
        if (standardOutUsed) {
            System.out.println(s);
        }
    }

    static RrdDb getRrdDbReference(String path) throws IOException, RrdException {
        if (rrdDbPoolUsed) {
            return RrdDbPool.getInstance().requestRrdDb(path);
        }
        else {
            return new RrdDb(path);
        }
    }

    static RrdDb getRrdDbReference(String path, String xmlPath) throws IOException, RrdException {
        if (rrdDbPoolUsed) {
            return RrdDbPool.getInstance().requestRrdDb(path, xmlPath);
        }
        else {
            return new RrdDb(path, xmlPath);
        }
    }

    static RrdDb getRrdDbReference(RrdDef rrdDef) throws IOException, RrdException {
        if (rrdDbPoolUsed) {
            return RrdDbPool.getInstance().requestRrdDb(rrdDef);
        }
        else {
            return new RrdDb(rrdDef);
        }
    }

    static void releaseRrdDbReference(RrdDb rrdDb) throws IOException, RrdException {
        if (rrdDbPoolUsed) {
            RrdDbPool.getInstance().release(rrdDb);
        }
        else {
            rrdDb.close();
        }
    }
}
