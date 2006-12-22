package org.opennms.netmgt.correlation;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractSequentialList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class SoftReferenceList<T> extends AbstractSequentialList<T> {
	
	private final List<SoftReference<T>> m_contents = new LinkedList<SoftReference<T>>();
	private final ReferenceQueue<T> queue = new ReferenceQueue<T>();

	@Override
	public ListIterator<T> listIterator(int index) {
		processQueue();
		return new SoftReferenceListIterator<T>(m_contents.listIterator(index), queue);
	}
	
	public void removeCollected() {
		processQueue();
		for (Iterator<SoftReference<T>> iter = m_contents.iterator(); iter.hasNext();) {
			SoftReference<T> ref = iter.next();
			if (ref.get() == null) {
				iter.remove();
			}
		}
	}
	
	private void processQueue() {
		Set<Reference<? extends T>> removed = new HashSet<Reference<? extends T>>();
		Reference<? extends T> ref;
		while((ref = queue.poll()) != null) {
			removed.add(ref);
		}
		m_contents.removeAll(removed);
	}

	@Override
	public int size() {
		processQueue();
		return m_contents.size();
	}
	
	private static class SoftReferenceListIterator<E> implements ListIterator<E> {
		ListIterator<SoftReference<E>> m_it;
		ReferenceQueue<E> m_queue;
		
		public SoftReferenceListIterator(ListIterator<SoftReference<E>> it, ReferenceQueue<E> queue) {
			m_it = it;
			m_queue = queue;
		}

		public void add(E o) {
			assertNotNull(o);
			m_it.add(createRef(o));
		}

		public boolean hasNext() {
			return m_it.hasNext();
		}

		public boolean hasPrevious() {
			return m_it.hasPrevious();
		}

		public E next() {
			SoftReference<E> ref = m_it.next();
			return ref.get();
		}

		public int nextIndex() {
			return m_it.nextIndex();
		}

		public E previous() {
			SoftReference<E> ref = m_it.previous();
			return ref.get();
		}

		public int previousIndex() {
			return m_it.previousIndex();
		}

		public void remove() {
			m_it.remove();
		}

		public void set(E o) {
			assertNotNull(o);
			m_it.set(createRef(o));
		}

		private SoftReference<E> createRef(E element) {
			return new SoftReference<E>(element, m_queue);
		}
		
		private void assertNotNull(E o) {
			if (o == null) {
				throw new NullPointerException("null cannot be added to SoftReferenceLists");
			}
		}
		
		
	}


}
