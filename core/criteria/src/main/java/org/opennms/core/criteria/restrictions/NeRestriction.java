package org.opennms.core.criteria.restrictions;

public class NeRestriction extends AttributeValueRestriction implements Restriction {

    public NeRestriction(final String attribute, final Object value) {
        super(RestrictionType.NE, attribute, value);
    }

    @Override
    public void visit(final RestrictionVisitor visitor) {
        visitor.visitNe(this);
    }

    @Override
    public String toString() {
        return "NeRestriction [attribute=" + getAttribute() + ", value=" + getValue() + "]";
    }
}
