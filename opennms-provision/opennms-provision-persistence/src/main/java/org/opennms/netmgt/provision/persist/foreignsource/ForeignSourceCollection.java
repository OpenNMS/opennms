package org.opennms.netmgt.provision.persist.foreignsource;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>ForeignSourceCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="foreign-sources")
public class ForeignSourceCollection extends LinkedList<ForeignSource> {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for ForeignSourceCollection.</p>
	 */
	public ForeignSourceCollection() {
        super();
    }

    /**
     * <p>Constructor for ForeignSourceCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public ForeignSourceCollection(Collection<? extends ForeignSource> c) {
        super(c);
    }

    /**
     * <p>getForeignSources</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="foreign-source")
    public List<ForeignSource> getForeignSources() {
        return this;
    }

    /**
     * <p>setForeignSources</p>
     *
     * @param foreignSources a {@link java.util.List} object.
     */
    public void setForeignSources(List<ForeignSource> foreignSources) {
        clear();
        addAll(foreignSources);
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

