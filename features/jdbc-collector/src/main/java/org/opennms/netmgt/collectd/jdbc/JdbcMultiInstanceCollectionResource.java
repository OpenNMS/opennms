package org.opennms.netmgt.collectd.jdbc;

import java.io.File;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.model.RrdRepository;

public class JdbcMultiInstanceCollectionResource extends JdbcCollectionResource {
    private String m_inst;
    private String m_name;
    
    public JdbcMultiInstanceCollectionResource(CollectionAgent agent, String instance, String name) {
        super(agent);
        m_inst = instance;
        m_name = name;
    }
    
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    @Override
    public File getResourceDir(RrdRepository repository) {
        File rrdBaseDir = repository.getRrdBaseDir();
        File nodeDir = new File(rrdBaseDir, String.valueOf(m_agent.getNodeId()));
        File typeDir = new File(nodeDir, m_name);
        File instDir = new File(typeDir, m_inst.replaceAll("\\s+", "_").replaceAll(":", "_").replaceAll("\\\\", "_").replaceAll("[\\[\\]]", "_"));
        if (log().isDebugEnabled()) {
            log().debug("getResourceDir: " + instDir.toString());
        }
        return instDir;
    }
    
    @Override
    public String getResourceTypeName() {
        return m_name;
    }

    @Override
    public String getInstance() {
        return m_inst;
    }

    @Override
    public String toString() {
        return "Node[" + m_agent.getNodeId() + "]/type["+ m_name+"]/instance[" + m_inst +"]";
    }
}
