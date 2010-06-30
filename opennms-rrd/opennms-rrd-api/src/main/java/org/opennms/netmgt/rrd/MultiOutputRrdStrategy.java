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
    public void setDelegates(List<RrdStrategy<Object, Object>> delegates) {
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
    public void closeFile(List<Object> rrd) throws Exception {
        for (int i = 0; i < rrd.size(); i++) {
            m_strategies.get(i).closeFile(rrd.get(i));
        }
    }

    /** {@inheritDoc} */
    public List<Object> createDefinition(String creator, String directory, String rrdName,
            int step, List<RrdDataSource> dataSources, List<String> rraList)
            throws Exception {
        List<Object> retval = new ArrayList<Object>();
        for (RrdStrategy<Object, Object> strategy : m_strategies) {
            retval.add(strategy.createDefinition(creator, directory, rrdName, step, dataSources, rraList));
        }
        return retval;
    }

    /**
     * <p>createFile</p>
     *
     * @param rrdDef a {@link java.util.List} object.
     * @throws java.lang.Exception if any.
     */
    public void createFile(List<Object> rrdDef) throws Exception {
        for (int i = 0; i < rrdDef.size(); i++) {
            m_strategies.get(i).createFile(rrdDef.get(i));
        }
    }

    /** {@inheritDoc} */
    public InputStream createGraph(String command, File workDir)
    throws IOException, RrdException {
        return m_strategies.get(m_graphStrategyIndex).createGraph(command, workDir);
    }

    /** {@inheritDoc} */
    public RrdGraphDetails createGraphReturnDetails(String command, File workDir)
    throws IOException, RrdException {
        return m_strategies.get(m_graphStrategyIndex).createGraphReturnDetails(command, workDir);
    }

    /** {@inheritDoc} */
    public Double fetchLastValue(String rrdFile, String ds, int interval)
    throws NumberFormatException, RrdException {
        return m_strategies.get(m_fetchStrategyIndex).fetchLastValue(rrdFile, ds, interval);
    }

    /** {@inheritDoc} */
    public Double fetchLastValue(String rrdFile, String ds,
            String consolidationFunction, int interval)
    throws NumberFormatException, RrdException {
        return m_strategies.get(m_fetchStrategyIndex).fetchLastValue(rrdFile, ds, consolidationFunction, interval);
    }

    /** {@inheritDoc} */
    public Double fetchLastValueInRange(String rrdFile, String ds,
            int interval, int range) throws NumberFormatException, RrdException {
        return m_strategies.get(m_fetchStrategyIndex).fetchLastValueInRange(rrdFile, ds, interval, range);
    }

    /**
     * <p>getDefaultFileExtension</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultFileExtension() {
        return m_strategies.get(m_fetchStrategyIndex).getDefaultFileExtension();
    }

    /**
     * <p>getGraphLeftOffset</p>
     *
     * @return a int.
     */
    public int getGraphLeftOffset() {
        return m_strategies.get(m_graphStrategyIndex).getGraphLeftOffset();
    }

    /**
     * <p>getGraphRightOffset</p>
     *
     * @return a int.
     */
    public int getGraphRightOffset() {
        return m_strategies.get(m_graphStrategyIndex).getGraphRightOffset();
    }

    /**
     * <p>getGraphTopOffsetWithText</p>
     *
     * @return a int.
     */
    public int getGraphTopOffsetWithText() {
        return m_strategies.get(m_graphStrategyIndex).getGraphTopOffsetWithText();
    }

    /**
     * <p>getStats</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStats() {
        StringBuffer retval = new StringBuffer();
        for (RrdStrategy<?, ?> strategy : m_strategies) {
            retval.append(strategy.getStats());
            retval.append("\n");
        }
        return retval.toString().trim();
    }

    /** {@inheritDoc} */
    public List<Object> openFile(String fileName) throws Exception {
        List<Object> retval = new ArrayList<Object>();
        for (RrdStrategy<Object, Object> strategy : m_strategies) {
            retval.add(strategy.openFile(fileName));
        }
        return retval;
    }

    /** {@inheritDoc} */
    public void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        for (RrdStrategy<Object, Object> strategy : m_strategies) {
            strategy.promoteEnqueuedFiles(rrdFiles);
        }
    }

    /** {@inheritDoc} */
    public void updateFile(List<Object> rrd, String owner, String data) throws Exception {
        for (int i = 0; i < rrd.size(); i++) {
            m_strategies.get(i).updateFile(rrd.get(i), owner, data);
        }
    }
}
