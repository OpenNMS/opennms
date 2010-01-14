package org.opennms.netmgt.rrd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class MultiOutputRrdStrategy implements RrdStrategy<List<Object>,List<Object>> {

    private final List<RrdStrategy> m_strategies = new ArrayList<RrdStrategy>();
    private int m_graphStrategyIndex;
    private int m_fetchStrategyIndex;

    public void setConfigurationProperties(Properties configurationParameters) {
        // We don't use any configuration properties
    }

    public List<RrdStrategy> getDelegates() {
        return m_strategies;
    }

    public void setDelegates(List<RrdStrategy> delegates) {
        m_strategies.clear();
        m_strategies.addAll(delegates);
    }

    public int getGraphStrategyIndex() {
        return m_graphStrategyIndex;
    }

    public void setGraphStrategyIndex(int graphStrategyIndex) {
        this.m_graphStrategyIndex = graphStrategyIndex;
    }

    public int getFetchStrategyIndex() {
        return m_fetchStrategyIndex;
    }

    public void setFetchStrategyIndex(int fetchStrategyIndex) {
        this.m_fetchStrategyIndex = fetchStrategyIndex;
    }

    public void closeFile(List<Object> rrd) throws Exception {
        for (int i = 0; i < rrd.size(); i++) {
            m_strategies.get(i).closeFile(rrd.get(i));
        }
    }

    public List<Object> createDefinition(String creator, String directory, String rrdName,
            int step, List<RrdDataSource> dataSources, List<String> rraList)
            throws Exception {
        List<Object> retval = new ArrayList<Object>();
        for (RrdStrategy strategy : m_strategies) {
            retval.add(strategy.createDefinition(creator, directory, rrdName, step, dataSources, rraList));
        }
        return retval;
    }

    public void createFile(List<Object> rrdDef) throws Exception {
        for (int i = 0; i < rrdDef.size(); i++) {
            m_strategies.get(i).createFile(rrdDef.get(i));
        }
    }

    public InputStream createGraph(String command, File workDir)
    throws IOException, RrdException {
        return m_strategies.get(m_graphStrategyIndex).createGraph(command, workDir);
    }

    public RrdGraphDetails createGraphReturnDetails(String command, File workDir)
    throws IOException, RrdException {
        return m_strategies.get(m_graphStrategyIndex).createGraphReturnDetails(command, workDir);
    }

    public Double fetchLastValue(String rrdFile, String ds, int interval)
    throws NumberFormatException, RrdException {
        return m_strategies.get(m_fetchStrategyIndex).fetchLastValue(rrdFile, ds, interval);
    }

    public Double fetchLastValue(String rrdFile, String ds,
            String consolidationFunction, int interval)
    throws NumberFormatException, RrdException {
        return m_strategies.get(m_fetchStrategyIndex).fetchLastValue(rrdFile, ds, consolidationFunction, interval);
    }

    public Double fetchLastValueInRange(String rrdFile, String ds,
            int interval, int range) throws NumberFormatException, RrdException {
        return m_strategies.get(m_fetchStrategyIndex).fetchLastValueInRange(rrdFile, ds, interval, range);
    }

    public String getDefaultFileExtension() {
        return m_strategies.get(m_fetchStrategyIndex).getDefaultFileExtension();
    }

    public int getGraphLeftOffset() {
        return m_strategies.get(m_graphStrategyIndex).getGraphLeftOffset();
    }

    public int getGraphRightOffset() {
        return m_strategies.get(m_graphStrategyIndex).getGraphRightOffset();
    }

    public int getGraphTopOffsetWithText() {
        return m_strategies.get(m_graphStrategyIndex).getGraphTopOffsetWithText();
    }

    @Override
    public String getStats() {
        StringBuffer retval = new StringBuffer();
        for (RrdStrategy strategy : m_strategies) {
            retval.append(strategy.getStats());
            retval.append("\n");
        }
        return retval.toString().trim();
    }

    public List<Object> openFile(String fileName) throws Exception {
        List<Object> retval = new ArrayList<Object>();
        for (RrdStrategy strategy : m_strategies) {
            retval.add(strategy.openFile(fileName));
        }
        return retval;
    }

    public void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        for (RrdStrategy strategy : m_strategies) {
            strategy.promoteEnqueuedFiles(rrdFiles);
        }
    }

    public void updateFile(List<Object> rrd, String owner, String data) throws Exception {
        for (int i = 0; i < rrd.size(); i++) {
            m_strategies.get(i).updateFile(rrd.get(i), owner, data);
        }
    }
}
