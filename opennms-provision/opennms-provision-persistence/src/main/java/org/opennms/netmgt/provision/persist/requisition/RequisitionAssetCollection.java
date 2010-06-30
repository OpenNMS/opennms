package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>RequisitionAssetCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="assets")
public class RequisitionAssetCollection extends LinkedList<RequisitionAsset> {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for RequisitionAssetCollection.</p>
	 */
	public RequisitionAssetCollection() {
        super();
    }

    /**
     * <p>Constructor for RequisitionAssetCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public RequisitionAssetCollection(Collection<? extends RequisitionAsset> c) {
        super(c);
    }

    /**
     * <p>getAssetFields</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="asset")
    public List<RequisitionAsset> getAssetFields() {
        return this;
    }

    /**
     * <p>setAssetFields</p>
     *
     * @param assets a {@link java.util.List} object.
     */
    public void setAssetFields(List<RequisitionAsset> assets) {
        clear();
        addAll(assets);
    }
    
    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
}

