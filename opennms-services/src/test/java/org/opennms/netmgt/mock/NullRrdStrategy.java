package org.opennms.netmgt.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdStrategy;

public class NullRrdStrategy implements RrdStrategy {
	
	// THIS IS USED FOR TESTS SO RrdUtils can be initialized
	// but doesn't need to do anything

	public void closeFile(Object rrd) throws Exception {
	}

	public Object createDefinition(String creator, String directory,
			String rrdName, int step, List dataSources, List rraList)
			throws Exception {
		return null;
	}

	public void createFile(Object rrdDef) throws Exception {
	}

	public InputStream createGraph(String command, File workDir)
			throws IOException, RrdException {
		return null;
	}

	public Double fetchLastValue(String rrdFile, int interval)
			throws NumberFormatException, RrdException {
		return null;
	}

	public Double fetchLastValueInRange(String rrdFile, int interval, int range)
			throws NumberFormatException, RrdException {
		return null;
	}

	public String getStats() {
		return null;
	}

	public void graphicsInitialize() throws Exception {
	}

	public void initialize() throws Exception {
	}

	public Object openFile(String fileName) throws Exception {
		return null;
	}

	public void updateFile(Object rrd, String owner, String data)
			throws Exception {
	}

}
