package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>OnmsCategoryCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name = "categories")
public class OnmsCategoryCollection extends LinkedList<OnmsCategory> {

    private static final long serialVersionUID = 4731486422555152257L;

    /**
     * <p>Constructor for OnmsCategoryCollection.</p>
     */
    public OnmsCategoryCollection() {
        super();
    }

    /**
     * <p>Constructor for OnmsCategoryCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public OnmsCategoryCollection(Collection<? extends OnmsCategory> c) {
        super(c);
    }

    /**
     * <p>getCategories</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name = "category")
    public List<OnmsCategory> getCategories() {
        return this;
    }

    /**
     * <p>setCategories</p>
     *
     * @param categories a {@link java.util.List} object.
     */
    public void setCategories(List<OnmsCategory> categories) {
        clear();
        addAll(categories);
    }
}
