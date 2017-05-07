/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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
        List<Object> retval = new ArrayList<Object>();
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
        StringBuffer retval = new StringBuffer();
        for (RrdStrategy<?, ?> strategy : m_strategies) {
            retval.append(strategy.getStats());
            retval.append("\n");
        }
        return retval.toString().trim();
    }

    /** {@inheritDoc} */
    @Override
    public List<Object> openFile(String fileName) throws Exception {
        List<Object> retval = new ArrayList<Object>();
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
