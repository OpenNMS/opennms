package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.persist.PluginConfig;

@XmlRootElement(name="detectors")
public class DetectorCollection extends LinkedList<PluginConfig> {

	private static final long serialVersionUID = 1L;

	public DetectorCollection() {
        super();
    }

    public DetectorCollection(Collection<? extends PluginConfig> c) {
        super(c);
    }

    @XmlElement(name="detector")
    public List<PluginConfig> getDetectors() {
        return this;
    }

    public void setDetectors(List<PluginConfig> detectors) {
        clear();
        addAll(detectors);
    }
}

