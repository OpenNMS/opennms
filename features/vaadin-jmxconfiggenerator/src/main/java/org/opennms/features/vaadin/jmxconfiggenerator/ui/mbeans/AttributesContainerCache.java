package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import org.opennms.features.vaadin.jmxconfiggenerator.data.SelectableBeanItemContainer;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The MBeanTree shows all available MBeans. Each Mbean has one or more
 * attributes. Each attribute is selectable. The MBean's attributes are
 * shown in a table. The problem is, that we must store the "is selected"
 * state of each AttributeItem. So we have two choices:<br/>
 *
 * 1. add ALL attributes from ALL MBeans to the container of the table and
 * show only the one belonging to the selected Mbean.<br/>
 *
 * 2. only add selected MBean's attributes to the container and save the
 * container for later use.<br/>
 *
 * We stick to 2. So at the beginning this class simply maps each MBean to
 * its container. But further on we realized that there are more scenarios
 * where we have a parent object which has a list of attributes. So the
 * {@link AttributesContainerCache} got more generic. Therefore the
 * ATTRIBUTETYPE defines the type of the attribute (e.g. {@link Attrib} to
 * stick with the MBeans example) and the PARENTTYPE defines the type of the
 * parent object (e.g. {@link Mbean} to stick with the MBeans example).
 *
 * @param <ATTRIBUTETYPE>
 *            The type of the parent object's attributes.
 * @param <PARENTTYPE>
 *            The type of the parent object which holds the attributes.
 *
 * @author Markus von Rüden
 */
public class AttributesContainerCache<ATTRIBUTETYPE, PARENTTYPE> {

    /**
     * The AttributeCollector retrieves all attributes from the parent's
     * object.
     *
     * @author Markus von Rüden
     *
     * @param <ATTRIBUTETYPE>
     *            The type of the attributes.
     * @param <PARENTTYPE>
     *            The type of the parent's object.
     */
    public interface AttributeCollector<ATTRIBUTETYPE, PARENTTYPE> {

        /**
         * Retrieves all attributes from the parent's object. Usually should
         * do something like <code>return parent.getChildren()</code>
         *
         * @param parent
         *            The parent object.
         * @return all attributes from the parent's object.
         */
        List<ATTRIBUTETYPE> getAttributes(PARENTTYPE parent);
    }

    /**
     * The map to map the container to the parent's object.
     */
    private final Map<PARENTTYPE, SelectableBeanItemContainer<ATTRIBUTETYPE>> containerMap = new HashMap<PARENTTYPE, SelectableBeanItemContainer<ATTRIBUTETYPE>>();

    /**
     * The type of the attribute.
     */
    private final Class<? super ATTRIBUTETYPE> type;

    /**
     * The collector to get all Attributes from, e.g. to get all Attributes from a MBean.
     */
    private final AttributeCollector<ATTRIBUTETYPE, PARENTTYPE> attribCollector;

    AttributesContainerCache(Class<? super ATTRIBUTETYPE> type,
							 AttributeCollector<ATTRIBUTETYPE, PARENTTYPE> attribCollector) {
        this.type = type;
        this.attribCollector = attribCollector;
    }

    /**
     * Gets the container of the given bean. If there is no container a new
     * one is created, otherwise the earlier used container is returned.
     *
     * @param bean
     * @return
     */
    public SelectableBeanItemContainer<ATTRIBUTETYPE> getContainer(PARENTTYPE bean) {
        if (bean == null) return null;
        if (containerMap.get(bean) != null) return containerMap.get(bean);
        containerMap.put(bean, new SelectableBeanItemContainer<>(type));
        initContainer(containerMap.get(bean), bean);
        return containerMap.get(bean);
    }

    /**
     * Initializes the container. So the container must not be null. It simply adds all attributes to the container.
     * @param container The container.
     * @param bean The parent bean.
     */
    private void initContainer(SelectableBeanItemContainer<ATTRIBUTETYPE> container, PARENTTYPE bean) {
        for (ATTRIBUTETYPE att : attribCollector.getAttributes(bean)) {
            container.addItem(att);
        }
    }

    /**
     * Clears the containerMap.
     */
    public void clear() {
        containerMap.clear();
    }
}
