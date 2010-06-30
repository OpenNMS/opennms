package org.opennms.netmgt.provision.persist.foreignsource;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>PolicyWrapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="policy")
public class PolicyWrapper extends PluginConfig {
    private static final long serialVersionUID = 1L;
    
    /**
     * <p>Constructor for PolicyWrapper.</p>
     */
    public PolicyWrapper() {
        super();
    }
    
    /**
     * <p>Constructor for PolicyWrapper.</p>
     *
     * @param pc a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public PolicyWrapper(PluginConfig pc) {
        super(pc);
    }

}

