package org.opennms.netmgt.rrd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MultiOutputRrdStrategy implements RrdStrategy<List<Object>,List<Object>> {

    private final List<RrdStrategy> m_strategies = new ArrayList<RrdStrategy>();
    private int m_graphIndex;
    private int m_readIndex;

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
        return m_strategies.get(m_graphIndex).createGraph(command, workDir);
    }

    public RrdGraphDetails createGraphReturnDetails(String command, File workDir)
    throws IOException, RrdException {
        return m_strategies.get(m_graphIndex).createGraphReturnDetails(command, workDir);
    }

    public Double fetchLastValue(String rrdFile, String ds, int interval)
    throws NumberFormatException, RrdException {
        return m_strategies.get(m_readIndex).fetchLastValue(rrdFile, ds, interval);
    }

    public Double fetchLastValue(String rrdFile, String ds,
            String consolidationFunction, int interval)
    throws NumberFormatException, RrdException {
        return m_strategies.get(m_readIndex).fetchLastValue(rrdFile, ds, consolidationFunction, interval);
    }

    public Double fetchLastValueInRange(String rrdFile, String ds,
            int interval, int range) throws NumberFormatException, RrdException {
        return m_strategies.get(m_readIndex).fetchLastValueInRange(rrdFile, ds, interval, range);
    }

    public String getDefaultFileExtension() {
        return m_strategies.get(m_readIndex).getDefaultFileExtension();
    }

    public int getGraphLeftOffset() {
        return m_strategies.get(m_graphIndex).getGraphLeftOffset();
    }

    public int getGraphRightOffset() {
        return m_strategies.get(m_graphIndex).getGraphRightOffset();
    }

    public int getGraphTopOffsetWithText() {
        return m_strategies.get(m_graphIndex).getGraphTopOffsetWithText();
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

    public void graphicsInitialize() throws Exception {
        m_strategies.get(m_graphIndex).graphicsInitialize();
    }

    /**
     * Initialize the array of RrdStrategy delegates.
     * TODO: Use a configuration file to initialize this
     * list.
     */
    public void initialize() throws Exception {
        // TODO Auto-generated method stub
        m_strategies.add((RrdStrategy)Class.forName("org.opennms.netmgt.rrd.rrdtool.JniRrdStrategy").newInstance());
        m_strategies.add((RrdStrategy)Class.forName("org.opennms.netmgt.rrd.tcp.TcpRrdStrategy").newInstance());
        m_graphIndex = 0;
        m_readIndex = 0;
        for (RrdStrategy strategy : m_strategies) {
            strategy.initialize();
        }
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
