/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import com.vaadin.data.Item;
import org.opennms.features.vaadin.jmxconfiggenerator.data.SelectableBeanItemContainer;
import org.opennms.features.vaadin.jmxconfiggenerator.data.SelectionChangedListener;
import org.opennms.features.vaadin.jmxconfiggenerator.data.SelectionValueChangedListener;
import org.opennms.features.vaadin.jmxconfiggenerator.data.UiModel;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.ConfirmationDialog;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation.ValidationManager;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation.ValidationResult;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompMember;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Controls the "MbeansView".
 * 
 * @author Markus von Rüden
 */
public class MBeansController implements SelectionManager, NameProvider, SelectionValueChangedListener {

	/**
	 * Helper class to save information about selection.
	 */
	private static class Selection {
		private Item item;
		private String itemId;
		private Object bean;

		public String getItemId() {
			return itemId;
		}

		private Item getItem() {
			return item;
		}

		private void update(String itemId, Item item, Object bean) {
			this.itemId = itemId;
			this.item = item;
			this.bean = bean;
		}

		private void reset() {
			update(null, null, null);
		}

		private Object getBean() {
			return bean;
		}
	}

	private final IconUpdater iconUpdater = new IconUpdater();

	/** container for the items to show in the tree. */
	private final MbeansHierarchicalContainer mbeansContainer = new MbeansHierarchicalContainer();

	private final ValidationManager validationManager = new ValidationManager(this, this);

	private final Selection currentSelection = new Selection();

	private final List<SelectionChangedListener> selectionChangedListener = new ArrayList<>();

	private final List<SelectionValueChangedListener> selectionValueChangedListeners = new ArrayList<>();

	private final Map<Class<?>, AttributesContainerCache> attributesContainerCacheMap = new HashMap<>();

	private final DefaultNameProvider defaultNameProvider;

	private MBeansContentPanel mbeansContentPanel;

	private MBeansTree mbeansTree;

	public MBeansController() {
		defaultNameProvider = new DefaultNameProvider(this);

		attributesContainerCacheMap.put(Attrib.class, new AttributesContainerCache<>(Attrib.class, new AttributesContainerCache.AttributeCollector<Attrib, Mbean>() {
			@Override
			public List<Attrib> getAttributes(Mbean outer) {
				return outer.getAttrib();
			}
		}));
		attributesContainerCacheMap.put(CompAttrib.class, new AttributesContainerCache<>(
				CompAttrib.class, new AttributesContainerCache.AttributeCollector<CompAttrib, Mbean>() {
			@Override
			public List<CompAttrib> getAttributes(Mbean outer) {
				return outer.getCompAttrib();
			}
		}));
		attributesContainerCacheMap.put(CompMember.class, new AttributesContainerCache<>(
				CompMember.class, new AttributesContainerCache.AttributeCollector<CompMember, CompAttrib>() {
			@Override
			public List<CompMember> getAttributes(CompAttrib outer) {
				return outer.getCompMember();
			}
		}));

		registerSelectionValueChangedListener(this);
	}

	public void registerSelectionChangedListener(SelectionChangedListener listener) {
		if (!selectionChangedListener.contains(listener))
			selectionChangedListener.add(listener);
	}

	public void registerSelectionValueChangedListener(SelectionValueChangedListener listener) {
		if (!selectionValueChangedListeners.contains(listener)) {
			selectionValueChangedListeners.add(listener);
		}
	}

	private void fireTreeSelectionChanged(Selection currentSelection) {
		SelectionChangedListener.SelectionChangedEvent changeEvent = new SelectionChangedListener.SelectionChangedEvent(
				currentSelection.getItem(),
				currentSelection.getItemId(),
				currentSelection.getBean());
		for (SelectionChangedListener eachListener : selectionChangedListener)
			eachListener.selectionChanged(changeEvent);
	}

	public void fireSelectionValueChanged(Object bean, Object itemId, boolean selected) {
		final SelectionValueChangedListener.SelectionValueChangedEvent event = new SelectionValueChangedListener.SelectionValueChangedEvent();
		event.setItemId(itemId);
		event.setNewValue(selected);
		event.setBean(bean);
		for (SelectionValueChangedListener eachListener : selectionValueChangedListeners) {
			eachListener.selectionValueChanged(event);
		}
	}

	public void setMbeansTree(MBeansTree mbeansTree) {
		this.mbeansTree = mbeansTree;
	}

	public void setMbeansContentPanel(MBeansContentPanel mbeansContentPanel) {
		this.mbeansContentPanel = mbeansContentPanel;
	}

	/**
	 * Updates the view when the selected MBean changes. At first each
	 * SelectionChangedListener are told, that there is a new Mbean to take care of
	 * (in detail: change the view to list mbean details of new mbean). And of
	 * course set a new ViewState (e.g. a non Mbean was selected and now a Mbean
	 * is selected)
	 *
	 * @param itemId the ItemId (Object Id) to select in the tree.
	 */
	protected void selectItemInTree(final String itemId) {
		if (currentSelection.getItemId() != itemId) {
			if (mbeansContentPanel.isDirty()) {
				new ConfirmationDialog()
						.withOkAction(new ConfirmationDialog.Action() {
							@Override
							public void execute(ConfirmationDialog window) {
								mbeansContentPanel.discard();
								currentSelection.update(itemId, mbeansContainer.getItem(itemId), mbeansContainer.getDataFor(itemId));
								fireTreeSelectionChanged(currentSelection);
							}
						})
						.withCancelAction(new ConfirmationDialog.Action() {
							@Override
							public void execute(ConfirmationDialog window) {
								// abort, do not continue -> revert selection
								// (the change is already visible to the client)
								mbeansTree.select(currentSelection.getItemId());
							}
						})
						.withOkLabel("yes")
						.withCancelLabel("no")
						.withCaption("Validation errors")
						.withDescription("The current view contains validation errors.<br/>The values cannot be saved.<br/>If you continue, they are lost.<br/><br/>Do you want to switch selection?")
						.open();
			} else {
				currentSelection.update(itemId, mbeansContainer.getItem(itemId), mbeansContainer.getDataFor(itemId));
				fireTreeSelectionChanged(currentSelection);
			}
		}
	}

	private void updateValidState(final ValidationResult result) {
		Collection<ValidationResult.ValidationError> validationErrors = result.getValidationErrors(Mbean.class);
		for (ValidationResult.ValidationError eachError : validationErrors) {
			updateValidState(eachError.getErrorObject(), false);
		}
		validationErrors = result.getValidationErrors(Attrib.class);
		for (ValidationResult.ValidationError eachError : validationErrors) {
			Mbean parent = (Mbean) attributesContainerCacheMap.get(Attrib.class).getParentFor(eachError.getErrorObject());
			updateValidState(parent, false);
		}
		validationErrors = result.getValidationErrors(CompMember.class);
		for (ValidationResult.ValidationError eachError : validationErrors) {
			CompAttrib parent = (CompAttrib) attributesContainerCacheMap.get(CompMember.class).getParentFor(eachError.getErrorObject());
			updateValidState(parent, false);
		}
		mbeansTree.markAsDirtyRecursive();
	}

	private void updateValidState(Object data, boolean valid) {
		final String itemId = mbeansContainer.getItemIdFor(data);
		final Item theItem = mbeansContainer.getItem(itemId);
		updateValidState(theItem, valid);
		if (!valid) {
			// make sure the error element is visible
			mbeansTree.expandItemsUpToParent(itemId);
		}
	}

	private void updateValidState(Item item, boolean valid) {
		if (item != null && !Objects.equals(item.getItemProperty("valid").getValue(), valid)) {
			item.getItemProperty("valid").setValue(Boolean.valueOf(valid)); // set the new validity
		}
	}

	/**
	 * In the content panel you can edit the MBean/CompAttrib and the respective attributes (Attrib/CompMember).
	 * Changes in both may affect the overall validity. Therefore we have to validate the changed element again to ensure
	 * it still is valid.
	 */
	protected void validateCurrentSelection() {
		updateValidState(currentSelection.bean, mbeansContentPanel.isValid());
		mbeansTree.markAsDirtyRecursive();
	}

	protected void handleSelectDeselect(Item item, Object itemId, boolean select) {
		handleSelectDeselect(item, select);
		handleSelectDeselectForSelectableBeanItemContainer(itemId, select);
		if (!mbeansContainer.hasChildren(itemId)) return;
		// we are not done yet in the tree hierarchy, so continue
		for (Object childItemId : mbeansContainer.getChildren(itemId)) {
			handleSelectDeselect(mbeansContainer.getItem(childItemId), childItemId, select);
		}
		validate(); // we changed the amount of elements considered for validation, so we have to update everything
	}

	void handleSelectDeselect(Item item, boolean selected) {
		item.getItemProperty(MBeansTree.MetaMBeansTreeItem.SELECTED).setValue(selected);
		iconUpdater.updateIcon(item, selected);
		// if we deselected and have a valid flag, we have to set the element to valid
		// to hide the error icons.
		if (!selected && item.getItemProperty("valid") != null) {
			updateValidState(item, true);
		}
	}

	private void handleSelectDeselectForSelectableBeanItemContainer(Object itemId, boolean select) {
		final Object data = mbeansContainer.getDataFor((String) itemId);
		// we are now at the end of the hierarchy, handleSelectDeselect for attributes/composite members
		if (data instanceof Mbean) {
			SelectableBeanItemContainer<Attrib> attribContainer = getContainer(Attrib.class, data);
			attribContainer.selectAllItems(select);
		}
		if (data instanceof CompAttrib) {
			SelectableBeanItemContainer<CompMember> compMemberContainer = getContainer(CompMember.class, data);
			compMemberContainer.selectAllItems(select);
			forwardSelectionToCompAttribute(itemId, data, select);
		}
	}

	// forward the selection of the CompAttrib to the Attributes Container as well
	private void forwardSelectionToCompAttribute(Object itemId, Object data, boolean select) {
		if (data instanceof CompAttrib) {
			String parentItemId = (String) getMBeansHierarchicalContainer().getParent(itemId);
			Mbean parent = (Mbean) getMBeansHierarchicalContainer().getDataFor(parentItemId);
			getContainer(CompAttrib.class, parent).getItem(data).setSelected(select);
		}
	}

	protected <T, X> SelectableBeanItemContainer<T> getContainer(Class<T> type, X bean) {
		AttributesContainerCache containerCache = attributesContainerCacheMap.get(type);
		Objects.requireNonNull(containerCache);
		return containerCache.getContainer(bean);
	}

	protected MbeansHierarchicalContainer getMBeansHierarchicalContainer() {
		return mbeansContainer;
	}

	private <T, X> Collection<T> getSelectedAttributes(Class<T> type, X bean) {
		SelectableBeanItemContainer<T> container = getContainer(type, bean);
		return container.getSelectedAttributes();
	}

	@Override
	public void selectionValueChanged(SelectionValueChangedEvent selectionValueChangedEvent) {
		Object itemId = selectionValueChangedEvent.getItemId();
		Item item = mbeansContainer.getItem(itemId);
		if (item != null) {
			handleSelectDeselect(item, selectionValueChangedEvent.getNewValue());
			Object data = mbeansContainer.getDataFor((String) itemId);
			forwardSelectionToCompAttribute(itemId, data, selectionValueChangedEvent.getNewValue());
			validateCurrentSelection();
		}
	}

	@Override
	public Collection<Attrib> getSelectedAttributes(Mbean mbean) {
		return getSelectedAttributes(Attrib.class, mbean);
	}

	@Override
	public Collection<CompMember> getSelectedCompositeMembers(CompAttrib compAttrib) {
		return getSelectedAttributes(CompMember.class, compAttrib);
	}

	@Override
	public Collection<CompAttrib> getSelectedCompositeAttributes(Mbean mbean) {
		return getSelectedAttributes(CompAttrib.class, mbean);
	}

	@Override
	public Collection<Mbean> getSelectedMbeans() {
		return getMBeansHierarchicalContainer().getSelectedMbeans();
	}

	public void updateDataSource(UiModel newModel) {
		reset();
		mbeansContainer.updateDataSource(newModel.getRawModel().getJmxCollection().get(0).getMbeans().getMbean());
		mbeansTree.expandAllItems();
		mbeansTree.select(mbeansContainer.firstItemId());
		validate();
	}

	private void reset()  {
		mbeansTree.setComponentError(null); // reset any errors which might be there
		currentSelection.reset();
		mbeansContentPanel.reset();
		for (AttributesContainerCache eachCache : attributesContainerCacheMap.values()) {
			eachCache.clear();
		}
	}

	private ValidationResult validate() {
		ValidationResult result = validationManager.validate(mbeansContainer);
		updateValidState(result);
		return result;
	}

	protected boolean isValid() {
		return validate().isValid();
	}

	@Override
	public Map<Object, String> getNamesMap() {
		return defaultNameProvider.getNamesMap();
	}
}
