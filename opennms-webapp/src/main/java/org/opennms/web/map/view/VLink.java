/*
 * Created on 8-mag-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.view;

/**
 * @author antonio
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
final public class VLink {
	VElement elem1;

	VElement elem2;

	public VLink(VElement elem1, VElement elem2) {
		this.elem1 = elem1;
		this.elem2 = elem2;
	}

	public boolean equals(Object otherLink) {
		VLink link = (VLink) otherLink;
		if (((this.elem1.getId() == link.getFirst().getId())
				&& this.elem1.getType().equals(link.getFirst().getType())
				&& (this.elem2.getId() == link.getSecond().getId()) && this.elem2
				.getType().equals(link.getSecond().getType()))
				|| (this.elem1.getId() == link.getSecond().getId()
						&& this.elem1.getType().equals(
								link.getSecond().getType())
						&& this.elem2.getId() == link.getFirst().getId() && (this.elem2
						.getType().equals(link.getFirst().getType()))))
			return true;
		return false;
	}

	/*
	 * public boolean hasElement(VElement elem) { if (isFirstElement(elem) ||
	 * isSecondElement(elem)) return true; return false; }
	 * 
	 * private boolean isFirstElement(VElement elem) { if ( elem.getId() ==
	 * elem1.getId() && ( ( elem.isNode() && elem1.isNode() ) || (
	 * elem.isSubmap() && elem1.isSubmap()) )) return true; return false; }
	 * 
	 * private boolean isSecondElement(VElement elem) { if ( elem.getId() ==
	 * elem2.getId() && ( ( elem.isNode() && elem2.isNode() ) || (
	 * elem.isSubmap() && elem2.isSubmap()) )) return true; return false; }
	 */

	public VElement getFirst() {
		return elem1;
	}

	public VElement getSecond() {
		return elem2;
	}

}