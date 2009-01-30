package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="assets")
public class RequisitionAssetCollection extends LinkedList<RequisitionAsset> {

	private static final long serialVersionUID = 1L;

	public RequisitionAssetCollection() {
        super();
    }

    public RequisitionAssetCollection(Collection<? extends RequisitionAsset> c) {
        super(c);
    }

    @XmlElement(name="asset")
    public List<RequisitionAsset> getAssetFields() {
        return this;
    }

    public void setAssetFields(List<RequisitionAsset> assets) {
        clear();
        addAll(assets);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
}

