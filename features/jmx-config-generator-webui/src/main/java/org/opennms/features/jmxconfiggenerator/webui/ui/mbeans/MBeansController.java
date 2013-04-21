/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.jmxconfiggenerator.webui.ui.mbeans;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.jmxconfiggenerator.webui.data.JmxCollectionCloner;
import org.opennms.features.jmxconfiggenerator.webui.data.MetaMBeanItem;
import org.opennms.features.jmxconfiggenerator.webui.data.UiModel;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeNotifier;
import org.opennms.features.jmxconfiggenerator.webui.data.SelectableBeanItemContainer;
import org.opennms.features.jmxconfiggenerator.webui.data.StringRenderer;
import org.opennms.features.jmxconfiggenerator.webui.ui.ModelChangeRegistry;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompMember;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxDatacollectionConfig;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

/**
 * Controls the "MbeansView".
 * 
 * @author Markus von Rüden
 */
public class MBeansController implements ModelChangeNotifier, ViewStateChangedListener, ModelChangeListener<UiModel>,
		NameProvider {
	public static interface Callback {
		Container getContainer();
	}

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
	public static class AttributesContainerCache<ATTRIBUTETYPE, PARENTTYPE> {

		/**
		 * We do not want to handle NullPointers, so there is a NULL-Container
		 * if we need one.
		 */
		public static final SelectableBeanItemContainer NULL = new SelectableBeanItemContainer(Object.class);

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

		private AttributesContainerCache(Class<? super ATTRIBUTETYPE> type,
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
			if (bean == null) return NULL;
			if (containerMap.get(bean) != null) return containerMap.get(bean);
			containerMap.put(bean, new SelectableBeanItemContainer<ATTRIBUTETYPE>(type));
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
		public static interface AttributeCollector<ATTRIBUTETYPE, PARENTTYPE> {

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
	}

	/**
	 * Vaadin container for the MbeansTree
	 */
	private final MbeansHierarchicalContainer mbeansContainer = new MbeansHierarchicalContainer(this);
	/**
	 * Registry to notify underlying components on modelChange events
	 */
	private final ModelChangeRegistry registry = new ModelChangeRegistry();
	/**
	 * Collection to notify all view components if the ViewState changes. Any
	 * underlying component can invoke a viewStateChange
	 */
	private final Collection<ViewStateChangedListener> viewStateListener = new ArrayList<ViewStateChangedListener>();
	private final MBeansItemStrategyHandler itemStrategyHandler = new MBeansItemStrategyHandler();
	/**
	 * the Mbean which is currently selected in the MBeanTree
	 */
	private Mbean currentlySelected = null;
	/**
	 * The state in which the view is currently
	 */
	private ViewState currentState = ViewState.Init; // this would be default,
														// but we set it
														// nevertheless
	private AttributesContainerCache<Attrib, Mbean> attribContainerCache = new AttributesContainerCache<Attrib, Mbean>(
			Attrib.class, new AttributesContainerCache.AttributeCollector<Attrib, Mbean>() {
				@Override
				public List<Attrib> getAttributes(Mbean outer) {
					return outer.getAttrib();
				}
			});

	// TODO mvonrued -> this is not correct, because we do not want all members,
	// we just want specific ones
	private AttributesContainerCache<CompAttrib, Mbean> compAttribContainerCache = new AttributesContainerCache<CompAttrib, Mbean>(
			CompAttrib.class, new AttributesContainerCache.AttributeCollector<CompAttrib, Mbean>() {
				@Override
				public List<CompAttrib> getAttributes(Mbean outer) {
					return outer.getCompAttrib();
				}
			});

	private AttributesContainerCache<CompMember, CompAttrib> compMemberContainerCache = new AttributesContainerCache<CompMember, CompAttrib>(
			CompMember.class, new AttributesContainerCache.AttributeCollector<CompMember, CompAttrib>() {
				@Override
				public List<CompMember> getAttributes(CompAttrib outer) {
					return outer.getCompMember();
				}
			});

	@Override
	public void registerListener(Class clazz, ModelChangeListener listener) {
		registry.registerListener(clazz, listener);
	}

	@Override
	public void notifyObservers(Class clazz, Object newModel) {
		registry.notifyObservers(clazz, newModel);
	}

	public MbeansHierarchicalContainer getMBeansHierarchicalContainer() {
		return mbeansContainer;
	}

	/**
	 * Updates the view when the selected MBean changes. At first each
	 * ModelChangeListener are told, that there is a new Mbean to take care of
	 * (in detail: change the view to list mbean details of new mbean). And of
	 * course set a new ViewState (e.g. a non Mbean was selected and now a Mbean
	 * is selected)
	 * 
	 * @param event
	 */
	protected void updateView(ItemClickEvent event) {
		if (currentlySelected == event.getItemId()) return; // no change made
		currentlySelected = event.getItemId() instanceof Mbean ? (Mbean) event.getItemId() : null;
		registry.notifyObservers(Item.class, event.getItem());
		registry.notifyObservers(event.getItemId().getClass(), event.getItemId());
		setState(event.getItemId());
	}

	/**
	 * Gets the next ViewState of the view.
	 * 
	 * @param itemId
	 * @return ViewState.Init if itemId is null, otherwise
	 *         ViewState.LeafSelected on Mbean selection and NonLeafSelected on
	 *         non-Mbean selection
	 */
	private ViewState getNextState(Object itemId) {
		if (itemId == null) return ViewState.Init;
		if (itemId instanceof Mbean) return ViewState.LeafSelected;
		if (!(itemId instanceof Mbean)) return ViewState.NonLeafSelected;
		return ViewState.Init;
	}

	private void setState(Object itemId) {
		ViewState nextState = getNextState(itemId);
		if (nextState == currentState) return; // nothing to do
		fireViewStateChanged(new ViewStateChangedEvent(currentState, nextState, this)); // tell
																						// the
																						// underlying
																						// views
																						// to
																						// handle
																						// the
																						// view
																						// state
																						// change
																						// :)
	}

	public void setItemProperties(Item item, Object itemId) {
		itemStrategyHandler.setItemProperties(item, itemId);
	}

	public StringRenderer getStringRenderer(Class<?> clazz) {
		return itemStrategyHandler.getStringRenderer(clazz);
	}

	@Override
	public void viewStateChanged(ViewStateChangedEvent event) {
		currentState = event.getNewState();
		if (event.getNewState() == ViewState.Init) {
			attribContainerCache.containerMap.clear();
			compAttribContainerCache.containerMap.clear();
			compMemberContainerCache.containerMap.clear();
		}
	}

	protected void addView(ViewStateChangedListener view) {
		viewStateListener.add(view);
	}

	@Override
	public void modelChanged(UiModel newModel) {
		fireViewStateChanged(new ViewStateChangedEvent(currentState, ViewState.Init, this));
	}

	protected void fireViewStateChanged(ViewState newState, Object source) {
		fireViewStateChanged(new ViewStateChangedEvent(currentState, newState, source));
	}

	private void fireViewStateChanged(ViewStateChangedEvent event) {
		for (ViewStateChangedListener listener : viewStateListener)
			listener.viewStateChanged(event);
	}

	void handleDeselect(HierarchicalContainer container, Object itemId) {
		handleSelectDeselect(container, container.getItem(itemId), itemId, false);
	}

	void handleSelect(HierarchicalContainer container, Object itemId) {
		handleSelectDeselect(container, container.getItem(itemId), itemId, true);
	}

	public void handleSelectDeselect(HierarchicalContainer container, Item item, Object itemId, boolean select) {
		itemStrategyHandler.getStrategy(itemId.getClass()).handleSelectDeselect(item, itemId, select);
		if (!container.hasChildren(itemId)) return;
		for (Object childItemId : container.getChildren(itemId)) {
			handleSelectDeselect(container, container.getItem(childItemId), childItemId, select);
		}
	}

	public void updateMBeanIcon() {
		itemStrategyHandler.getStrategy(Mbean.class).updateIcon(mbeansContainer.getItem(currentlySelected));
	}

	public SelectableBeanItemContainer<Attrib> getAttributeContainer(Mbean bean) {
		return attribContainerCache.getContainer(bean);
	}

	public void clearAttributesCache() {
		attribContainerCache.containerMap.clear();
	}

	protected void updateMBean() {
		itemStrategyHandler.getStrategy(Mbean.class).updateModel(mbeansContainer.getItem(currentlySelected),
				currentlySelected);
	}

	public SelectableBeanItemContainer<CompMember> getCompositeMemberContainer(CompAttrib attrib) {
		return compMemberContainerCache.getContainer(attrib);
	}

	public SelectableBeanItemContainer<CompAttrib> getCompositeAttributeContainer(Mbean mbean) {
		return compAttribContainerCache.getContainer(mbean);
	}

	@Override
	public Map<Object, String> getNames() {
		Map<Object, String> names = new HashMap<Object, String>();
		for (Mbean bean : mbeansContainer.getMBeans()) {
			for (Attrib att : bean.getAttrib()) {
				names.put(att, att.getAlias());
			}
			for (CompAttrib compAttrib : bean.getCompAttrib()) {
				for (CompMember compMember : compAttrib.getCompMember())
					names.put(compMember, compMember.getAlias());
			}
		}
		return names;

	}

	protected Mbean getSelectedMBean() {
		return currentlySelected;
	}

	/**
	 * The whole point was to select/deselect
	 * Mbeans/Attribs/CompMembers/CompAttribs. In this method we simply create a
	 * JmxDatacollectionConfig considering the choices we made in the gui. To do
	 * this, we simply clone the original <code>JmxDatacollectionConfig</code>
	 * loaded at the beginning. After that we remove all
	 * MBeans/Attribs/CompMembers/CompAttribs and add them manually with the
	 * changes made in the gui.
	 * 
	 * @param controller
	 *            the MBeansController of the MbeansView (is needed to determine
	 *            the changes made in gui)
	 * @return
	 */
	// TODO mvonrued -> I guess we do not need this clone-stuff at all ^^ and it
	// is too complicated for such a simple
	// task
	public JmxDatacollectionConfig createJmxDataCollectionAccordingToSelection(UiModel uiModel) {
		/**
		 * At First we clone the original collection. This is done, because if
		 * we make any modifications (e.g. deleting not selected elements) the
		 * data isn't available in the GUI, too. To avoid reloading the data
		 * from server, we just clone it.
		 */
		JmxDatacollectionConfig clone = JmxCollectionCloner.clone(uiModel.getRawModel());

		/**
		 * At second we remove all MBeans from original data and get only
		 * selected once.
		 */
		List<Mbean> exportBeans = clone.getJmxCollection().get(0).getMbeans().getMbean();
		exportBeans.clear();
		Iterable<Mbean> selectedMbeans = getSelectedMbeans(getMBeansHierarchicalContainer());
		for (Mbean mbean : selectedMbeans) {
			/**
			 * At 3.1. we remove all Attributes from Mbean, because we only want
			 * selected ones.
			 */
			Mbean exportBean = JmxCollectionCloner.clone(mbean);
			exportBean.getAttrib().clear(); // we only want selected ones :)
			for (Attrib att : getSelectedAttributes(mbean, getAttributeContainer(mbean))) {
				exportBean.getAttrib().add(JmxCollectionCloner.clone(att));
			}
			if (!exportBean.getAttrib().isEmpty()) {
				exportBeans.add(exportBean); // no attributes selected, don't
												// add bean
			}
			/*
			 * At 3.2. we remove all CompAttribs and CompMembers from MBean,
			 * because we only want selected ones :)
			 */
			exportBean.getCompAttrib().clear();
			for (CompAttrib compAtt : getSelectedCompositeAttributes(mbean, getCompositeAttributeContainer(mbean))) {
				CompAttrib cloneCompAtt = JmxCollectionCloner.clone(compAtt);
				cloneCompAtt.getCompMember().clear();
				for (CompMember compMember : getSelectedCompositeMembers(compAtt, getCompositeMemberContainer(compAtt))) {
					cloneCompAtt.getCompMember().add(JmxCollectionCloner.clone(compMember));
				}
				if (!cloneCompAtt.getCompMember().isEmpty()) {
					exportBean.getCompAttrib().add(cloneCompAtt);
				}
			}
		}
		// Last but not least, we need to update the service name
		clone.getJmxCollection().get(0).setName(uiModel.getServiceName());
		return clone;
	}

	/**
	 * Returns all mbeans which are selected.
	 * 
	 * @return all mbeans which are selected.
	 */
	protected Iterable<Mbean> getSelectedMbeans() {
		return getSelectedMbeans(mbeansContainer);
	}

	/**
	 * Returns all selected Attributes for the given mbean. The mbean should be
	 * also selected. There is no check if that is the case.
	 * 
	 * @param mbean
	 *            The mbean to get all selected attributes from. The mbean
	 *            should be also selected. There is no check if that is the
	 *            case.
	 * @return all selected attributes for the given mbean.
	 */
	protected Iterable<Attrib> getSelectedAttributes(Mbean mbean) {
		return getSelectedAttributes(mbean, getAttributeContainer(mbean));
	}

	/**
	 * Returns all selected composite attributes for the given mbean. The mbean
	 * should be also selected. There is no check if that is the case.
	 * 
	 * @param mbean
	 *            The mbean to get all selected composite attributes from. The
	 *            mbean should be also selected. There is no check if that is
	 *            the case.
	 * @return all selected attributes for the given mbean.
	 */
	protected Iterable<CompAttrib> getSelectedCompositeAttributes(Mbean mbean) {
		return getSelectedCompositeAttributes(mbean, getCompositeAttributeContainer(mbean));
	}

	/**
	 * Returns all selected composite members for the given composite attribute.
	 * The composite attribute should be also selected. There is no check if
	 * that is the case.
	 * 
	 * @param mbean
	 *            The composite attribute to get all selected composite members
	 *            from. The composite attribute should be also selected. There
	 *            is no check if that is the case.
	 * @return all selected composite members for the given composite attribute.
	 */
	protected Iterable<CompMember> getSelectedCompositeMembers(CompAttrib compAttrib) {
		return getSelectedCompositeMembers(compAttrib, getCompositeMemberContainer(compAttrib));
	}

	/**
	 * @param container
	 * @return all Mbeans which are selected
	 */
	private static Iterable<Mbean> getSelectedMbeans(final MbeansHierarchicalContainer container) {
		return Iterables.filter(container.getMBeans(), new Predicate<Mbean>() {
			@Override
			public boolean apply(final Mbean bean) {
				Item item = container.getItem(bean);
				Property itemProperty = item.getItemProperty(MetaMBeanItem.SELECTED);
				if (itemProperty != null && itemProperty.getValue() != null) {
					return (Boolean) itemProperty.getValue();
				}
				return false;
			}
		});
	}

	/**
	 * 
	 * @param mbean
	 * @param compAttribContainer
	 * @return all CompAttrib elements which are selected
	 */
	private static Iterable<CompAttrib> getSelectedCompositeAttributes(final Mbean mbean,
			final SelectableBeanItemContainer<CompAttrib> compAttribContainer) {
		if (AttributesContainerCache.NULL == compAttribContainer) {
			return mbean.getCompAttrib();
		}
		return Iterables.filter(mbean.getCompAttrib(), new Predicate<CompAttrib>() {
			@Override
			public boolean apply(CompAttrib compAtt) {
				return compAttribContainer.getItem(compAtt).isSelected();
			}
		});
	}

	/**
	 * 
	 * @param compAtt
	 * @param compMemberContainer
	 * @return all <code>CompMember</code>s which are selected.
	 */
	private static Iterable<CompMember> getSelectedCompositeMembers(final CompAttrib compAtt,
			final SelectableBeanItemContainer<CompMember> compMemberContainer) {
		if (AttributesContainerCache.NULL == compMemberContainer) {
			return compAtt.getCompMember();
		}
		return Iterables.filter(compAtt.getCompMember(), new Predicate<CompMember>() {
			@Override
			public boolean apply(CompMember compMember) {
				return compMemberContainer.getItem(compMember).isSelected();
			}
		});
	}

	/**
	 * 
	 * @param mbean
	 * @param attributesContainer
	 * @return all Attributes which are selected.
	 */
	private static Iterable<Attrib> getSelectedAttributes(final Mbean mbean,
			final SelectableBeanItemContainer<Attrib> attributesContainer) {
		if (AttributesContainerCache.NULL == attributesContainer) {
			return mbean.getAttrib(); // no change made, return all
		}
		return Iterables.filter(mbean.getAttrib(), new Predicate<Attrib>() {
			@Override
			public boolean apply(Attrib attrib) {
				return attributesContainer.getItem(attrib).isSelected();
			}
		});
	}
}
