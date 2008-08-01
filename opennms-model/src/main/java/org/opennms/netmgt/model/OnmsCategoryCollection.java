package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "categories")
public class OnmsCategoryCollection extends LinkedList<OnmsCategory> {

    private static final long serialVersionUID = 4731486422555152257L;

    public OnmsCategoryCollection() {
        super();
    }

    public OnmsCategoryCollection(Collection<? extends OnmsCategory> c) {
        super(c);
    }

    @XmlElement(name = "category")
    public List<OnmsCategory> getCategories() {
        return this;
    }

    public void setCategories(List<OnmsCategory> categories) {
        clear();
        addAll(categories);
    }
}
