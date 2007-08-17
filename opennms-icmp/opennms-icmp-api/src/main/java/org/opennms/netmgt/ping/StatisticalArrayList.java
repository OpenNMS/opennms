package org.opennms.netmgt.ping;

import java.util.ArrayList;
import java.util.Collection;

public class StatisticalArrayList<E> extends ArrayList<E> {
	private static final long serialVersionUID = 1L;

	public StatisticalArrayList() {
		super();
	}
	
	public StatisticalArrayList(Collection<E> c) {
		super(c);
	}
	
	/**
	 * Get the percent of non-null entries in the list.
	 * @return the percent of non-null entries as an {@link Integer}
	 */
	public Integer percentNotNull() {
		if (this.size() == 0) {
			return null;
		}

		Float retval = new Float(this.countNotNull()) / new Float(this.size()) * (float)100;
		// System.out.println("passed = " + this.passed() + ", size = " + this.size() + ", percent passed = " + retval);
		return new Integer(retval.intValue());
	}
	
	/**
	 * Get the percent of null entries in the list.
	 * @return the percent of null entries as an {@link Integer}
	 */
	public Integer percentNull() {
		if (this.size() == 0) {
			return null;
		}

		Float retval = new Float(this.countNull()) / new Float(this.size()) * (float)100;
		// System.out.println("failed = " + this.passed() + ", size = " + this.size() + ", percent failed = " + retval);
		return new Integer(retval.intValue());
	}
	
	/**
	 * Get the number of non-null entries in the list.
	 * @return the count of non-null entries
	 */
	public int countNotNull() {
		int count = 0;
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i) != null) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Get the number of null entries in the list.
	 * @return the count of null entries
	 */
	public int countNull() {
		return this.size() - this.countNotNull();
	}

	/**
	 * Get the average of all non-null entries.
	 * @return the average
	 */
	public int averageAsInt() {
		return new Float(this.averageAsFloat()).intValue();
	}
	
	public float averageAsFloat() {
		Long total = new Long(0);
		int count = 0;

		for (int i = 0; i < this.size(); i++) {
			Long value = (Long)this.get(i);
			if (value != null) {
				total += value;
				count++;
			}
		}
		
		if (total > 0 && count > 1) {
			Float average = (float)total / (float)count;
			return average.floatValue();
		}
	
		return total.floatValue();
	}
	
	/**
	 * Get the mean of all non-null entries
	 * @return the mean value, or null if there were no entries
	 */
	public Long median() {
		if (this.countNotNull() == 0) {
			return null;
		}
		
		ArrayList<Long> al = new ArrayList<Long>();
		Long value1, value2;
		for (int i =0; i < this.size(); i++) {
			value1 = (Long)this.get(i);
			if (value1 != null) {
				al.add(value1);
			}
		}

		if (al.size() % 2 == 0) {
			// even number of entries, take the mean of the 2 center ones
			value1 = (Long)al.get(al.size() / 2);
			value2 = (Long)al.get((al.size() / 2) - 1);
			return (value1/value2);
		} else {
			// odd number of entries, take the true median
			return al.get((int)(al.size() / 2));
		}
	}
}
