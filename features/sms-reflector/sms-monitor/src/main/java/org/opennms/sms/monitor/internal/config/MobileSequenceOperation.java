package org.opennms.sms.monitor.internal.config;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public abstract class MobileSequenceOperation {
	public Category log() {
		return ThreadCategory.getInstance(getClass());
	}
}
