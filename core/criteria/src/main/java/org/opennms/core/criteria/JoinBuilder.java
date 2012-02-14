package org.opennms.core.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.criteria.Join.JoinType;
import org.opennms.core.utils.LogUtils;

public class JoinBuilder {
	final Map<String,Join> m_joins = new HashMap<String,Join>();
	
	public JoinBuilder join(final String associationPath, final String alias, final JoinType type) {
		if (m_joins.containsKey(alias)) {
			LogUtils.debugf(this, "alias '%s' already joined with associationPath '%s', skipping.", alias, associationPath);
		} else {
			m_joins.put(alias, new Join(associationPath, alias, type));
		}
		return this;
	}

	public Collection<Join> getJoinCollection() {
		// make a copy so the internal one can't be modified outside of the builder
		return new ArrayList<Join>(m_joins.values());
	}

}
