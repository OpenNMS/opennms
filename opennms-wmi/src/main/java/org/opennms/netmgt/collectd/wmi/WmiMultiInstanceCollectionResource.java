package org.opennms.netmgt.collectd.wmi;

import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.dao.support.GenericIndexResourceType;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.config.StorageStrategy;
import org.opennms.netmgt.collectd.AbstractCollectionResource;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.ServiceParameters;
import org.opennms.netmgt.collectd.CollectionAttributeType;
import org.opennms.core.utils.ThreadCategory;
import org.apache.log4j.Category;

import java.util.List;
import java.io.File;

public class WmiMultiInstanceCollectionResource extends WmiCollectionResource {
    private String m_inst;
    private String m_name;

    public WmiMultiInstanceCollectionResource(CollectionAgent agent, String instance, String name) {
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

    public String getResourceTypeName() {
        return m_name;
    }

    public String getInstance() {
        return m_inst;
    }

    @Override
    public String toString() {
        return "Node[" + m_agent.getNodeId() + "]/type["+ m_name+"]/instance[" + m_inst +"]";
    }

}
