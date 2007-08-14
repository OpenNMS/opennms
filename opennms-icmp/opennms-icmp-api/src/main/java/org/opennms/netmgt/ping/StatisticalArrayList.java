package org.opennms.netmgt.ping;

import java.util.ArrayList;

public class StatisticalArrayList<E> extends ArrayList<E> {
	private static final long serialVersionUID = 1L;

	public Integer percentNotNull() {
		if (this.size() == 0) {
			return null;
		}

		Float retval = new Float(this.countNotNull()) / new Float(this.size()) * (float)100;
		// System.out.println("passed = " + this.passed() + ", size = " + this.size() + ", percent passed = " + retval);
		return new Integer(retval.intValue());
	}
	
	public Integer percentNull() {
		if (this.size() == 0) {
			return null;
		}

		Float retval = new Float(this.countNull()) / new Float(this.size()) * (float)100;
		// System.out.println("failed = " + this.passed() + ", size = " + this.size() + ", percent failed = " + retval);
		return new Integer(retval.intValue());
	}
	
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
	public int averageAsInteger() {
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
}
