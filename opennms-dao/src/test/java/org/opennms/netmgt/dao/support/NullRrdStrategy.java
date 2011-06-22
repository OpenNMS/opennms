/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;

public class NullRrdStrategy implements RrdStrategy<Object,Object> {
	
	// THIS IS USED FOR TESTS SO RrdUtils can be initialized
	// but doesn't need to do anything

    public void setConfigurationProperties(Properties configurationParameters) {
        // Do nothing
    }

	public void closeFile(Object rrd) throws Exception {
	}

	public Object createDefinition(String creator, String directory,
			String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList)
			throws Exception {
		return null;
	}

	public void createFile(Object rrdDef) throws Exception {
	}

    public InputStream createGraph(String command, File workDir)
            throws IOException, RrdException {
        return null;
    }
    
    public RrdGraphDetails createGraphReturnDetails(String command, File workDir)
            throws IOException, RrdException {
        return null;
    }
    
	public Double fetchLastValue(String rrdFile, String ds, int interval)
			throws NumberFormatException, RrdException {
		return null;
	}

	public Double fetchLastValueInRange(String rrdFile, String ds, int interval, int range)
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

    public int getGraphLeftOffset() {
        return 0;
    }

    public int getGraphRightOffset() {
        return 0;
    }

    public int getGraphTopOffsetWithText() {
        return 0;
    }

    public String getDefaultFileExtension() {
        return ".nullRrd";
    }

    public Double fetchLastValue(String rrdFile, String ds,
            String consolidationFunction, int interval)
            throws NumberFormatException, RrdException {
        return null;
    }

    public void promoteEnqueuedFiles(Collection<String> rrdFiles) {
    }

}
