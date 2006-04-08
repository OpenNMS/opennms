package org.opennms.netmgt.dao.jdbc;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;


public class LazySet extends JdbcSet {
	
	public static interface Loader {
		Set load();
	}

	private Loader m_loader;
	private boolean m_loaded = false;

	public LazySet(Loader loader) {
		m_loader = loader;
	}

	public Iterator iterator() {
		load();
		return super.iterator();
	}

	public int size() {
		load();
		return super.size();
	}

	public boolean removeAll(Collection arg0) {
		load();
		return super.removeAll(arg0);
	}

	public boolean addAll(Collection arg0) {
		load();
		return super.addAll(arg0);
	}

	public void clear() {
		load();
		super.clear();
	}

	public boolean contains(Object o) {
		load();
		return super.contains(o);
	}

	public boolean containsAll(Collection arg0) {
		load();
		return super.containsAll(arg0);
	}

	public boolean isEmpty() {
		load();
		return super.isEmpty();
	}

	public boolean remove(Object o) {
		load();
		return super.remove(o);
	}

	public boolean retainAll(Collection arg0) {
		load();
		return super.retainAll(arg0);
	}

	public Object[] toArray() {
		load();
		return super.toArray();
	}

	public Object[] toArray(Object[] arg0) {
		load();
		return super.toArray(arg0);
	}

	public String toString() {
		load();
		return super.toString();
	}

	private void load() {
		if (!m_loaded) {
			setElements(m_loader.load());
			m_loaded = true;
		}
	}
	
	public boolean isLoaded() {
		return m_loaded;
	}
	
	

}
