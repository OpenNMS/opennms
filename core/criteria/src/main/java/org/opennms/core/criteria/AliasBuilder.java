package org.opennms.core.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.utils.LogUtils;

public class AliasBuilder {
	final Map<String,Alias> m_aliases = new HashMap<String,Alias>();
	
	public AliasBuilder alias(final String associationPath, final String alias, final JoinType type) {
		if (m_aliases.containsKey(alias)) {
			LogUtils.debugf(this, "alias '%s' already associated with associationPath '%s', skipping.", alias, associationPath);
		} else {
			m_aliases.put(alias, new Alias(associationPath, alias, type));
		}
		return this;
	}

	public Collection<Alias> getAliasCollection() {
		// make a copy so the internal one can't be modified outside of the builder
		return new ArrayList<Alias>(m_aliases.values());
	}

}
