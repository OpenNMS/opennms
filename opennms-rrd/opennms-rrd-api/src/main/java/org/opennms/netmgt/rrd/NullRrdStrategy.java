/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.rrd;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * A {@link org.opennms.netmgt.rrd.RrdStrategy} implementation that does nothing.
 *
 * Used in cases where an instance of {@link org.opennms.netmgt.rrd.RrdStrategy} is required 
 * but no implementations are available.
 *
 */
public class NullRrdStrategy implements RrdStrategy<Object,Object> {

    @Override
    public void setConfigurationProperties(Properties configurationParameters) {
        // pass
    }

    @Override
	public void closeFile(Object rrd) {
        // pass
	}

    @Override
	public Object createDefinition(String creator, String directory,
			String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList) {
		return null;
	}

    @Override
	public void createFile(Object rrdDef) {
        // pass
	}

    @Override
    public InputStream createGraph(String command, File workDir) {
        return null;
    }

    @Override
    public RrdGraphDetails createGraphReturnDetails(String command, File workDir) {
        return null;
    }

    @Override
	public Double fetchLastValue(String rrdFile, String ds, int interval) {
		return null;
	}

    @Override
	public Double fetchLastValueInRange(String rrdFile, String ds, int interval, int range) {
		return null;
	}

    @Override
	public String getStats() {
		return null;
	}

    @Override
	public Object openFile(String fileName) {
		return null;
	}

    @Override
	public void updateFile(Object rrd, String owner, String data) {
        // pass
	}

    @Override
    public int getGraphLeftOffset() {
        return 0;
    }

    @Override
    public int getGraphRightOffset() {
        return 0;
    }

    @Override
    public int getGraphTopOffsetWithText() {
        return 0;
    }

    @Override
    public String getDefaultFileExtension() {
        return ".nullRrd";
    }

    @Override
    public Double fetchLastValue(String rrdFile, String ds,
            String consolidationFunction, int interval) {
        return null;
    }

    @Override
    public void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        // pass
    }

}
