package org.opennms.netmgt.provision.persist.foreignsource;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>PolicyCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="policies")
public class PolicyCollection extends LinkedList<PluginConfig> {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for PolicyCollection.</p>
	 */
	public PolicyCollection() {
        super();
    }

    /**
     * <p>Constructor for PolicyCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public PolicyCollection(Collection<? extends PluginConfig> c) {
        super(c);
    }

    /**
     * <p>getPolicies</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="policy")
    public List<PluginConfig> getPolicies() {
        return this;
    }

    /**
     * <p>setPolicies</p>
     *
     * @param policies a {@link java.util.List} object.
     */
    public void setPolicies(List<PluginConfig> policies) {
        clear();
        addAll(policies);
    }
}

