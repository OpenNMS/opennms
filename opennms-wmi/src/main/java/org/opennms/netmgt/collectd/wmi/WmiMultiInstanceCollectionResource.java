package org.opennms.netmgt.collectd.wmi;

import java.io.File;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.model.RrdRepository;

/**
 * <p>WmiMultiInstanceCollectionResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WmiMultiInstanceCollectionResource extends WmiCollectionResource {
    private String m_inst;
    private String m_name;

    /**
     * <p>Constructor for WmiMultiInstanceCollectionResource.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param instance a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     */
    public WmiMultiInstanceCollectionResource(final CollectionAgent agent, final String instance, final String name) {
        super(agent);
        m_inst = instance;
        m_name = name;
    }

    /** {@inheritDoc} */
    @Override
    public File getResourceDir(final RrdRepository repository) {
        final File rrdBaseDir = repository.getRrdBaseDir();
        final File nodeDir = new File(rrdBaseDir, String.valueOf(m_agent.getNodeId()));
        final File typeDir = new File(nodeDir, m_name);
        final File instDir = new File(typeDir, m_inst.replaceAll("\\s+", "_").replaceAll(":", "_").replaceAll("\\\\", "_").replaceAll("[\\[\\]]", "_"));
        LogUtils.debugf(this, "getResourceDir: %s", instDir);
        return instDir;
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceTypeName() {
        return m_name;
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInstance() {
        return m_inst;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Node[" + m_agent.getNodeId() + "]/type["+ m_name+"]/instance[" + m_inst +"]";
    }

}
