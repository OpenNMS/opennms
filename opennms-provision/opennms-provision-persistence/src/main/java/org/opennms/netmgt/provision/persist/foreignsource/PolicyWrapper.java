package org.opennms.netmgt.provision.persist.foreignsource;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="policy")
public class PolicyWrapper extends PluginConfig {
    private static final long serialVersionUID = 1L;
    
    public PolicyWrapper() {
        super();
    }
    
    public PolicyWrapper(PluginConfig pc) {
        super(pc);
    }

}

