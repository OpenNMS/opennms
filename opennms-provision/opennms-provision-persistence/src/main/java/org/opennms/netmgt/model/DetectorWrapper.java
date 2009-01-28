package org.opennms.netmgt.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.persist.PluginConfig;

@XmlRootElement(name="detector")
public class DetectorWrapper extends PluginConfig {
    private static final long serialVersionUID = 1L;
    
    public DetectorWrapper() {
    }
    
    public DetectorWrapper(PluginConfig pc) {
        super(pc.getName(), pc.getPluginClass());
        this.setParameters(pc.getParameters());
    }

}

