package org.opennms.netmgt.collectd.tca;

import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.Persister;

/**
 * The Class TcaCollectionAttributeType.
 * 
 * <p>It is assumed that all metrics are not counters.</p>
 * <p>The metrics are fixed:</p>
 * <ul>
 * <li>delay local-remote ~ current inbound-delay</li>
 * <li>jitter local-remote ~ current inbound-jitter</li>
 * <li>delay remote-local ~ current outbound-delay</li>
 * <li>jitter remote-local ~ current outbound-jitter-</li>
 * <li>timesync status (1 = good, time is synced, 0 = bad, out-of sync)</li>
 * </ul>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class TcaCollectionAttributeType implements CollectionAttributeType {

	/** The Attribute Group Type. */
	private AttributeGroupType m_groupType;

	/** The m_name. */
	private String m_name;

	/**
	 * Instantiates a new TCA collection attribute type.
	 *
	 * @param groupType the group type
	 * @param name the name
	 */
	public TcaCollectionAttributeType(AttributeGroupType groupType, String name) {
		super();
		this.m_groupType = groupType;
		this.m_name = name;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.config.collector.AttributeDefinition#getType()
	 */
	@Override
	public String getType() {
		return "Integer32";
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.config.collector.AttributeDefinition#getName()
	 */
	@Override
	public String getName() {
		return m_name;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.config.collector.CollectionAttributeType#getGroupType()
	 */
	@Override
	public AttributeGroupType getGroupType() {
		return m_groupType;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.config.collector.CollectionAttributeType#storeAttribute(org.opennms.netmgt.config.collector.CollectionAttribute, org.opennms.netmgt.config.collector.Persister)
	 */
	@Override
	public void storeAttribute(CollectionAttribute attribute, Persister persister) {
		persister.persistNumericAttribute(attribute);
	}

    @Override
    public String getAttributeId() {
        return "Not supported yet._" + "TCA_" + getName();
    }

}
