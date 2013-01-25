package org.opennms.features.topology.api.topo;

import java.util.Comparator;

/**
 * This comparator only cares about the tuple of namespace and id.
 */
public class RefComparator implements Comparator<Ref> {

	@Override
	public int compare(Ref a, Ref b) {
		if (a == null) {
			if (b == null) {
				return 0;
			} else {
				return 1;
			}
		} else if (b == null) {
			return -1;
		} else {
			if (a.getNamespace() == null) {
				if (b.getNamespace() == null) {
					if (a.getId() == null) {
						if (b.getId() == null) {
							return 0;
						} else {
							return 1;
						}
					} else if (b.getId() == null) {
						return -1;
					} else {
						return a.getId().compareTo(b.getId());
					}
				} else {
					return 1;
				}
			} else if (b.getNamespace() == null) {
				return -1;
			} else {
				int comparison = a.getNamespace().compareTo(b.getNamespace());
				if (comparison == 0) {
					if (a.getId() == null) {
						if (b.getId() == null) {
							return 0;
						} else {
							return 1;
						}
					} else if (b.getId() == null) {
						return -1;
					} else {
						return a.getId().compareTo(b.getId());
					}
				} else {
					return comparison;
				}
			}
		}
	}

}