package org.opennms.netmgt.provision.persist.foreignsource;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="policies")
public class PolicyCollection extends LinkedList<PluginConfig> {

	private static final long serialVersionUID = 1L;

	public PolicyCollection() {
        super();
    }

    public PolicyCollection(Collection<? extends PluginConfig> c) {
        super(c);
    }

    @XmlElement(name="policy")
    public List<PluginConfig> getPolicies() {
        return this;
    }

    public void setPolicies(List<PluginConfig> policies) {
        clear();
        addAll(policies);
    }
}

