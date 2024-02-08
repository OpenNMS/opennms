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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * <p>MultiOutputRrdStrategy class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MultiOutputRrdStrategy implements RrdStrategy<List<Object>,List<Object>> {

    private final List<RrdStrategy<Object,Object>> m_strategies = new ArrayList<RrdStrategy<Object,Object>>();
    private int m_graphStrategyIndex;
    private int m_fetchStrategyIndex;

    /** {@inheritDoc} */
    @Override
    public void setConfigurationProperties(Properties configurationParameters) {
        // We don't use any configuration properties
    }

    /**
     * <p>getDelegates</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<RrdStrategy<Object, Object>> getDelegates() {
        return m_strategies;
    }

    /**
     * <p>setDelegates</p>
     *
     * @param delegates a {@link java.util.List} object.
     */
    public void setDelegates(final List<RrdStrategy<Object, Object>> delegates) {
        if (m_strategies == delegates) return;
        m_strategies.clear();
        m_strategies.addAll(delegates);
    }

    /**
     * <p>getGraphStrategyIndex</p>
     *
     * @return a int.
     */
    public int getGraphStrategyIndex() {
        return m_graphStrategyIndex;
    }

    /**
     * <p>setGraphStrategyIndex</p>
     *
     * @param graphStrategyIndex a int.
     */
    public void setGraphStrategyIndex(int graphStrategyIndex) {
        this.m_graphStrategyIndex = graphStrategyIndex;
    }

    /**
     * <p>getFetchStrategyIndex</p>
     *
     * @return a int.
     */
    public int getFetchStrategyIndex() {
        return m_fetchStrategyIndex;
    }

    /**
     * <p>setFetchStrategyIndex</p>
     *
     * @param fetchStrategyIndex a int.
     */
    public void setFetchStrategyIndex(int fetchStrategyIndex) {
        this.m_fetchStrategyIndex = fetchStrategyIndex;
    }

    /**
     * <p>closeFile</p>
     *
     * @param rrd a {@link java.util.List} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public void closeFile(List<Object> rrd) throws Exception {
        for (int i = 0; i < rrd.size(); i++) {
            m_strategies.get(i).closeFile(rrd.get(i));
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Object> createDefinition(String creator, String directory, String rrdName,
            int step, List<RrdDataSource> dataSources, List<String> rraList)
            throws Exception {
        List<Object> retval = new ArrayList<>();
        for (RrdStrategy<Object, Object> strategy : m_strategies) {
            retval.add(strategy.createDefinition(creator, directory, rrdName, step, dataSources, rraList));
        }
        return retval;
    }

    /** {@inheritDoc} */
    @Override
    public void createFile(List<Object> rrdDef) throws Exception {
        for (int i = 0; i < rrdDef.size(); i++) {
            m_strategies.get(i).createFile(rrdDef.get(i));
        }
    }

    /** {@inheritDoc} */
    @Override
    public InputStream createGraph(String command, File workDir)
    throws IOException, RrdException {
        return m_strategies.get(m_graphStrategyIndex).createGraph(command, workDir);
    }

    /** {@inheritDoc} */
    @Override
    public RrdGraphDetails createGraphReturnDetails(String command, File workDir)
    throws IOException, RrdException {
        return m_strategies.get(m_graphStrategyIndex).createGraphReturnDetails(command, workDir);
    }

    /** {@inheritDoc} */
    @Override
    public Double fetchLastValue(String rrdFile, String ds, int interval)
    throws NumberFormatException, RrdException {
        return m_strategies.get(m_fetchStrategyIndex).fetchLastValue(rrdFile, ds, interval);
    }

    /** {@inheritDoc} */
    @Override
    public Double fetchLastValue(String rrdFile, String ds,
            String consolidationFunction, int interval)
    throws NumberFormatException, RrdException {
        return m_strategies.get(m_fetchStrategyIndex).fetchLastValue(rrdFile, ds, consolidationFunction, interval);
    }

    /** {@inheritDoc} */
    @Override
    public Double fetchLastValueInRange(String rrdFile, String ds,
            int interval, int range) throws NumberFormatException, RrdException {
        return m_strategies.get(m_fetchStrategyIndex).fetchLastValueInRange(rrdFile, ds, interval, range);
    }

    /**
     * <p>getDefaultFileExtension</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getDefaultFileExtension() {
        return m_strategies.get(m_fetchStrategyIndex).getDefaultFileExtension();
    }

    /**
     * <p>getGraphLeftOffset</p>
     *
     * @return a int.
     */
    @Override
    public int getGraphLeftOffset() {
        return m_strategies.get(m_graphStrategyIndex).getGraphLeftOffset();
    }

    /**
     * <p>getGraphRightOffset</p>
     *
     * @return a int.
     */
    @Override
    public int getGraphRightOffset() {
        return m_strategies.get(m_graphStrategyIndex).getGraphRightOffset();
    }

    /**
     * <p>getGraphTopOffsetWithText</p>
     *
     * @return a int.
     */
    @Override
    public int getGraphTopOffsetWithText() {
        return m_strategies.get(m_graphStrategyIndex).getGraphTopOffsetWithText();
    }

    /**
     * <p>getStats</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getStats() {
        final StringBuilder retval = new StringBuilder();
        for (RrdStrategy<?, ?> strategy : m_strategies) {
            retval.append(strategy.getStats());
            retval.append("\n");
        }
        return retval.toString().trim();
    }

    /** {@inheritDoc} */
    @Override
    public List<Object> openFile(String fileName) throws Exception {
        List<Object> retval = new ArrayList<>();
        for (RrdStrategy<Object, Object> strategy : m_strategies) {
            retval.add(strategy.openFile(fileName));
        }
        return retval;
    }

    /** {@inheritDoc} */
    @Override
    public void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        for (RrdStrategy<Object, Object> strategy : m_strategies) {
            strategy.promoteEnqueuedFiles(rrdFiles);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void updateFile(List<Object> rrd, String owner, String data) throws Exception {
        for (int i = 0; i < rrd.size(); i++) {
            m_strategies.get(i).updateFile(rrd.get(i), owner, data);
        }
    }
}
