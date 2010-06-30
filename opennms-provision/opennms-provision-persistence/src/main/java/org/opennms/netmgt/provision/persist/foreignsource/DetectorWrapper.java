package org.opennms.netmgt.provision.persist.foreignsource;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>DetectorWrapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="detector")
public class DetectorWrapper extends PluginConfig {
    private static final long serialVersionUID = 1L;
    
    /**
     * <p>Constructor for DetectorWrapper.</p>
     */
    public DetectorWrapper() {
    }
    
    /**
     * <p>Constructor for DetectorWrapper.</p>
     *
     * @param pc a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public DetectorWrapper(PluginConfig pc) {
        super(pc);
    }

}

