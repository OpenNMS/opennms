/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
