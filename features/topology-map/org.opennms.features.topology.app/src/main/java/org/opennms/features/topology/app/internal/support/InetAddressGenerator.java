/**
 * 
 */
package org.opennms.features.topology.app.internal.support;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;

import com.vaadin.data.Property;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;

public class InetAddressGenerator implements ColumnGenerator {

	private static final long serialVersionUID = -3202605200928035972L;

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId) {
		Property property = source.getContainerProperty(itemId, columnId);
		if (property == null || property.getValue() == null) {
			return null;
		} else {
			return InetAddressUtils.str((InetAddress)property.getValue());
		}
	}
}
