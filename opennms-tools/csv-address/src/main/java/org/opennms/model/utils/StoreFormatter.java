package org.opennms.model.utils;

import org.apache.commons.lang.StringUtils;

public class StoreFormatter implements ForeignSourceFormatter {

	@Override
	public String formatForeignSource(String foreignSource) {
		return "Store" + StringUtils.leftPad(foreignSource, 4, '0');
	}

}
