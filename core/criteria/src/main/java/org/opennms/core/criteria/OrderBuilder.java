package org.opennms.core.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.opennms.core.utils.LogUtils;

public final class OrderBuilder {
	private final List<Order> m_order = new ArrayList<Order>();
	private String m_lastAttribute = null;

	/**
	 * Append an order object to the end of the order list.
	 * @param order the order object to append
	 * @return whether it was added (true if added, false if already exists/ignored)
	 */
	boolean append(final Order order) {
		synchronized (m_order) {
			final ListIterator<Order> i = m_order.listIterator();
			while (i.hasNext()) {
				final Order o = i.next();
				final String attribute = order.getAttribute();
				if (o.getAttribute().equals(attribute)) {
					LogUtils.debugf(this, "Attribute '%s' was already in this criteria, ignoring addition of %s", attribute, order);
					m_lastAttribute = null;
					return false;
				}
			}
			m_order.add(order);
			m_lastAttribute = order.getAttribute();
			return true;
		}
	}

	public void clear() {
		m_order.clear();
	}

	public Collection<Order> getOrderCollection() {
		// make a copy so the internal one can't be modified outside of the builder
		return new ArrayList<Order>(m_order);
	}

	public void asc() {
		synchronized (m_order) {
			if (m_order.isEmpty()) {
				LogUtils.debugf(this, "asc() called, but no orderBy has been specified."); 
			} else if (m_lastAttribute == null) {
				LogUtils.debugf(this, "asc() called on an attribute that can't be changed.");
			} else {
				final Order o = m_order.remove(m_order.size() - 1);
				m_order.add(Order.asc(o.getAttribute()));
			}
		}
	}

	public void desc() {
		synchronized (m_order) {
			if (m_order.isEmpty()) {
				LogUtils.debugf(this, "desc() called, but no orderBy has been specified."); 
			} else if (m_lastAttribute == null) {
				LogUtils.debugf(this, "desc() called on an attribute that can't be changed.");
			} else {
				final Order o = m_order.remove(m_order.size() - 1);
				m_order.add(Order.desc(o.getAttribute()));
			}
		}
	}

}