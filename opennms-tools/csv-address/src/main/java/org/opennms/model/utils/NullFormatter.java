package org.opennms.model.utils;

public class NullFormatter implements ForeignSourceFormatter {

	@Override
	public String formatForeignSource(String foreignSource) {
		return foreignSource;
	}

}
