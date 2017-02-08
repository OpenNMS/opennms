/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.data.Container;
import com.vaadin.data.util.AbstractBeanContainer;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.GraphContainer.ChangeListener;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

@SuppressWarnings("serial")
public class GCFilterableContainer extends AbstractBeanContainer<VertexRef, Vertex> implements Container.Hierarchical {

	/**
	 * Mapping from Item ID to parent Item ID for items included in the filtered
	 * container.
	 */
	private Map<VertexRef, VertexRef> filteredParent = null;

	/**
	 * Mapping from Item ID to a list of child IDs when filtered
	 */
	private Map<VertexRef, LinkedList<VertexRef>> filteredChildren = null;

	/**
	 * List that contains all filtered root elements of the container.
	 */
	private List<VertexRef> filteredRoots = null;

	/**
	 * Determines how filtering of the container is done.
	 */
	private boolean includeParentsWhenFiltering = true;

	private Set<Vertex> filterOverride = null;
	 
	private final GraphContainer m_graphContainer;

	public GCFilterableContainer(GraphContainer graphContainer) {
		super(Vertex.class);
		setBeanIdResolver(identityResolver());
		m_graphContainer = graphContainer;
		m_graphContainer.addChangeListener(new ChangeListener() {

			@Override
			public void graphChanged(GraphContainer graphContainer) {
				GCFilterableContainer.this.removeAllItems();
				GCFilterableContainer.this.addAll(m_graphContainer.getBaseTopology().getVertices());
			}
		});
		super.addAll(m_graphContainer.getBaseTopology().getVertices());

	}

	private static BeanIdResolver<VertexRef, Vertex> identityResolver() {
		return new BeanIdResolver<VertexRef, Vertex>() {

			@Override
			public VertexRef getIdForBean(Vertex bean) {
				return bean;
			}
		};
	}

	private static VertexRef vRef(Object itemId) {
		return (VertexRef) itemId;
	}

	/**
	 * Can the specified Item have any children? Don't add a JavaDoc comment
	 * here, we use the default documentation from implemented interface.
	 */
	@Override
	public boolean areChildrenAllowed(Object itemId) {
		boolean allowed = internalAreChildrenAllowed(vRef(itemId));
		return allowed;
	}

	private boolean internalAreChildrenAllowed(VertexRef itemId) {
		if (containsId(itemId)) {
			return m_graphContainer.getBaseTopology().hasChildren(itemId);
		}
		return false;
	}

	/**
	 * Gets the IDs of the children of the specified Item. Don't add a JavaDoc
	 * comment here, we use the default documentation from implemented
	 * interface.
	 */
	@Override
	public Collection<?> getChildren(Object itemId) {
		Collection<?> children = internalGetChildren(vRef(itemId));
		return children;

	}

	private Collection<? extends VertexRef> internalGetChildren(VertexRef itemId) {
		if (filteredChildren != null) {
			return filteredChildren.get(itemId);
		} else {
			Collection<Vertex> emptyList = Collections.emptyList(); 
			return !containsId(itemId) ? emptyList : m_graphContainer.getBaseTopology().getChildren(itemId, m_graphContainer.getCriteria());
		}
	}

	/**
	 * Gets the ID of the parent of the specified Item. Don't add a JavaDoc
	 * comment here, we use the default documentation from implemented
	 * interface.
	 */
	@Override
	public Object getParent(Object itemId) {
		Object parent = internalGetParent(vRef(itemId));
		return parent;
	}

	private Object internalGetParent(VertexRef itemId) {
		if (filteredParent != null) {
			return filteredParent.get(itemId);
		}
		
		if (containsId(itemId)) {
			return m_graphContainer.getBaseTopology().getParent(itemId);
		}
		
		return null;
	}

	/**
	 * Is the Item corresponding to the given ID a leaf node? Don't add a
	 * JavaDoc comment here, we use the default documentation from implemented
	 * interface.
	 */
	@Override
	public boolean hasChildren(Object itemId) {
		boolean hasChildren = internalHasChildren(vRef(itemId));
		return hasChildren;
	}

	private boolean internalHasChildren(VertexRef itemId) {
		if (filteredChildren != null) {
			return filteredChildren.containsKey(itemId);
		} else { 
			return containsId(itemId) && m_graphContainer.getBaseTopology().hasChildren(itemId);
		}
	}

	/**
	 * Is the Item corresponding to the given ID a root node? Don't add a
	 * JavaDoc comment here, we use the default documentation from implemented
	 * interface.
	 */
	@Override
	public boolean isRoot(Object itemId) {
		// If the container is filtered the itemId must be among filteredRoots
		// to be a root.
		if (filteredRoots != null) {
			return filteredRoots.contains(itemId) && containsId(itemId);
		} else {
			return containsId(itemId) && m_graphContainer.getBaseTopology().getParent(vRef(itemId)) == null;
		}


	}

	/**
	 * Gets the IDs of the root elements in the container. Don't add a JavaDoc
	 * comment here, we use the default documentation from implemented
	 * interface.
	 */
	@Override
	public Collection<?> rootItemIds() {
		Collection<?> rootItems = internalRootItems();
		return rootItems;
	}

	private Collection<?> internalRootItems() {
		if (filteredRoots != null) {
			return Collections.unmodifiableCollection(filteredRoots);
		} else {
			return Collections.unmodifiableCollection(m_graphContainer.getBaseTopology().getRootGroup());
		}
	}

	/**
	 * <p>
	 * Sets the given Item's capability to have children. If the Item identified
	 * with the itemId already has children and the areChildrenAllowed is false
	 * this method fails and <code>false</code> is returned; the children must
	 * be first explicitly removed with
	 * {@link #setParent(Object itemId, Object newParentId)} or
	 * {@link com.vaadin.data.Container#removeItem(Object itemId)}.
	 * </p>
	 * 
	 * @param itemId
	 *            the ID of the Item in the container whose child capability is
	 *            to be set.
	 * @param childrenAllowed
	 *            the boolean value specifying if the Item can have children or
	 *            not.
	 * @return <code>true</code> if the operation succeeded, <code>false</code>
	 *         if not
	 */
	@Override
	public boolean setChildrenAllowed(Object itemId, boolean childrenAllowed) {
		throw new UnsupportedOperationException("setChildrenAllowed is not supported");
	}

	/**
	 * <p>
	 * Sets the parent of an Item. The new parent item must exist and be able to
	 * have children. (<code>canHaveChildren(newParentId) == true</code>). It is
	 * also possible to detach a node from the hierarchy (and thus make it root)
	 * by setting the parent <code>null</code>.
	 * </p>
	 * 
	 * @param itemId
	 *            the ID of the item to be set as the child of the Item
	 *            identified with newParentId.
	 * @param newParentId
	 *            the ID of the Item that's to be the new parent of the Item
	 *            identified with itemId.
	 * @return <code>true</code> if the operation succeeded, <code>false</code>
	 *         if not
	 */
	@Override
	public boolean setParent(Object itemId, Object newParentId) {
		throw new UnsupportedOperationException("setParent is not supported");
	}

	 /**
	  * Removes the Item identified by given itemId and all its children from the
	  * given Container.
	  * 
	  * @param container
	  *            the container where the item is to be removed
	  * @param itemId
	  *            the identifier of the Item to be removed
	  * @return true if the operation succeeded
	  */
	 public static boolean removeItemRecursively(Container.Hierarchical container, Object itemId) {
		 boolean success = true;
		 Collection<?> children2 = container.getChildren(itemId);
		 if (children2 != null) {
			 Object[] array = children2.toArray();
			 for (int i = 0; i < array.length; i++) {
				 boolean removeItemRecursively = removeItemRecursively(
						 container, array[i]);
				 if (!removeItemRecursively) {
					 success = false;
				 }
			 }
		 }
		 // remove the root of subtree if children where succesfully removed
		 if (success) {
			 success = container.removeItem(itemId);
		 }
		 return success;

	 }


	 /**
	  * Controls how the filtering of the container works. Set this to true to
	  * make filtering include parents for all matched items in addition to the
	  * items themselves. Setting this to false causes the filtering to only
	  * include the matching items and make items with excluded parents into root
	  * items.
	  * 
	  * @param includeParentsWhenFiltering
	  *            true to include all parents for items that match the filter,
	  *            false to only include the matching items
	  */
	 public void setIncludeParentsWhenFiltering(boolean includeParentsWhenFiltering) {
		 this.includeParentsWhenFiltering = includeParentsWhenFiltering;
		 if (filteredRoots != null) {
			 // Currently filtered so needs to be re-filtered
			 doFilterContainer(true);
		 }
	 }

	 /**
	  * Overridden to provide filtering for root & children items.
	  * 
	  * (non-Javadoc)
	  * 
	  * @see com.vaadin.data.util.IndexedContainer#updateContainerFiltering()
	  */
	 @Override
	 protected boolean doFilterContainer(boolean hasFilters) {
		 if (!hasFilters) {
			 // All filters removed
			 filteredRoots = null;
			 filteredChildren = null;
			 filteredParent = null;

			 return super.doFilterContainer(hasFilters);
		 }

		 // Reset data structures
		 filteredRoots = new LinkedList<VertexRef>();
		 filteredChildren = new HashMap<VertexRef, LinkedList<VertexRef>>();
		 filteredParent = new HashMap<VertexRef, VertexRef>();

		 if (includeParentsWhenFiltering) {
			 // Filter so that parents for items that match the filter are also
			 // included
			 HashSet<Vertex> includedItems = new HashSet<Vertex>();
			 for (Vertex rootId : m_graphContainer.getBaseTopology().getRootGroup()) {
				 if (filterIncludingParents(rootId, includedItems)) {
					 filteredRoots.add(rootId);
					 addFilteredChildrenRecursively(rootId, includedItems);
				 }
			 }
			 // includedItemIds now contains all the item ids that should be
			 // included. Filter IndexedContainer based on this
			 filterOverride = includedItems;
			 super.doFilterContainer(hasFilters);
			 filterOverride = null;

			 return true;
		 } else {
			 // Filter by including all items that pass the filter and make items
			 // with no parent new root items

			 // Filter IndexedContainer first so getItemIds return the items that
			 // match
			 super.doFilterContainer(hasFilters);

			 LinkedHashSet<VertexRef> filteredItemIds = new LinkedHashSet<VertexRef>(getItemIds());

			 for (VertexRef itemId : filteredItemIds) {
				 Vertex itemParent = m_graphContainer.getBaseTopology().getParent(itemId);
				 if (itemParent == null || !filteredItemIds.contains(itemParent)) {
					 // Parent is not included or this was a root, in both cases
					 // this should be a filtered root
					 filteredRoots.add(itemId);
				 } else {
					 // Parent is included. Add this to the children list (create
					 // it first if necessary)
					 addFilteredChild(itemParent, itemId);
				 }
			 }

			 return true;
		 }
	 }

	 /**
	  * Adds the given childItemId as a filteredChildren for the parentItemId and
	  * sets it filteredParent.
	  * 
	  * @param parentItemId
	  * @param childItemId
	  */
	 private void addFilteredChild(VertexRef parentItemId, VertexRef childItemId) {
		 LinkedList<VertexRef> parentToChildrenList = filteredChildren
				 .get(parentItemId);
		 if (parentToChildrenList == null) {
			 parentToChildrenList = new LinkedList<VertexRef>();
			 filteredChildren.put(parentItemId, parentToChildrenList);
		 }
		 filteredParent.put(childItemId, parentItemId);
		 parentToChildrenList.add(childItemId);

	 }

	 /**
	  * Recursively adds all items in the includedItems list to the
	  * filteredChildren map in the same order as they are in the children map.
	  * Starts from parentItemId and recurses down as long as child items that
	  * should be included are found.
	  * 
	  * @param parentItemId
	  *            The item id to start recurse from. Not added to a
	  *            filteredChildren list
	  * @param includedItems
	  *            Set containing the item ids for the items that should be
	  *            included in the filteredChildren map
	  */
	 private void addFilteredChildrenRecursively(Vertex parentItemId, Set<Vertex> includedItems) {
		 Collection<Vertex> childList = m_graphContainer.getBaseTopology().getChildren(parentItemId, m_graphContainer.getCriteria());
		 if (childList == null) {
			 return;
		 }

		 for (Vertex childItemId : childList) {
			 if (includedItems.contains(childItemId)) {
				 addFilteredChild(parentItemId, childItemId);
				 addFilteredChildrenRecursively(childItemId, includedItems);
			 }
		 }
	 }

	 /**
	  * Scans the itemId and all its children for which items should be included
	  * when filtering. All items which passes the filters are included.
	  * Additionally all items that have a child node that should be included are
	  * also themselves included.
	  * 
	  * @param itemId
	  * @param includedItems
	  * @return true if the itemId should be included in the filtered container.
	  */
	 private boolean filterIncludingParents(Vertex itemId,
			 Set<Vertex> includedItems) {
		 boolean toBeIncluded = passesFilters(itemId);

		 Collection<Vertex> childList = m_graphContainer.getBaseTopology().getChildren(itemId, m_graphContainer.getCriteria());
		 if (childList != null) {
			 for (Vertex childItemId : childList) {
				 toBeIncluded |= filterIncludingParents(childItemId, includedItems);
			 }
		 }

		 if (toBeIncluded) {
			 includedItems.add(itemId);
		 }
		 return toBeIncluded;
	 }

	 /**
	  * (non-Javadoc)
	  * 
	  * @see
	  * com.vaadin.data.util.IndexedContainer#passesFilters(java.lang.Object)
	  */
	 @Override
	 protected boolean passesFilters(Object itemId) {
		 if (filterOverride != null) {
			 return filterOverride.contains(itemId);
		 } else {
			 return super.passesFilters(itemId);
		 }
	 }
	 
	 @Override
	 public void fireItemSetChange() {
		 super.fireItemSetChange();
	 }
}
