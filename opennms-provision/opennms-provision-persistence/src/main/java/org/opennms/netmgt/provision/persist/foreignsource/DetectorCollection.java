package org.opennms.netmgt.provision.persist.foreignsource;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>DetectorCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="detectors")
public class DetectorCollection extends LinkedList<PluginConfig> {

	/**
     * 
     */
    private static final long serialVersionUID = -3054011579260606775L;

    /**
	 * <p>Constructor for DetectorCollection.</p>
	 */
	public DetectorCollection() {
        super();
    }

    /**
     * <p>Constructor for DetectorCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public DetectorCollection(Collection<? extends PluginConfig> c) {
        super(c);
    }

    /**
     * <p>getDetectors</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="detector")
    public List<PluginConfig> getDetectors() {
        return this;
    }

    /**
     * <p>setDetectors</p>
     *
     * @param detectors a {@link java.util.List} object.
     */
    public void setDetectors(List<PluginConfig> detectors) {
        clear();
        addAll(detectors);
    }
}

