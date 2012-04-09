package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
public class PrimaryType implements Comparable<PrimaryType>, Serializable {
    private static final long serialVersionUID = -647348487361201657L;
    private static final char[] s_order = { 'N', 'S', 'P' };
    private char m_collType;

    protected PrimaryType() {
        this('N');
    }

    PrimaryType(final char collType) {
        m_collType = collType;
    }

    @Transient
    public String getCode() {
        return String.valueOf(m_collType);
    }

    @Column(name="isSnmpPrimary")
    public char getCharCode() {
        return m_collType;
    }

    public void setCharCode(final char collType) {
        m_collType = collType;
    }

    /**
     * Hibernate objects should not have any specific hashCode() implementation
     * since it should always give the same object for the same row anyways.
     */
    @Override
    public int hashCode() {
    	return super.hashCode();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o instanceof PrimaryType) {
            return this.compareTo((PrimaryType)o) == 0;
        } else return false;
    }

    public int compareTo(final PrimaryType collType) {
        return getIndex(m_collType) - getIndex(collType.m_collType);
    }

    private static int getIndex(final char code) {
        for (int i = 0; i < s_order.length; i++) {
            if (s_order[i] == code) {
                return i;
            }
        }
        throw new IllegalArgumentException("illegal collType code '"+code+"'");
    }

    @Override
    public String toString() {
        return String.valueOf(m_collType);
    }

    public boolean isLessThan(final PrimaryType collType) {
        return compareTo(collType) < 0;
    }

    public boolean isGreaterThan(final PrimaryType collType) {
        return compareTo(collType) > 0;
    }

    public PrimaryType max(final PrimaryType collType) {
        return this.isLessThan(collType) ? collType : this;
    }

    public PrimaryType min(final PrimaryType collType) {
        return this.isLessThan(collType) ? this : collType;
    }

    public static PrimaryType get(final char code) {
        switch (code) {
        case 'P': return PRIMARY;
        case 'S': return SECONDARY;
        case 'N': return NOT_ELIGIBLE;
        default:
            throw new IllegalArgumentException("Cannot create collType from code "+code);
        }
    }

    public static PrimaryType get(final String code) {
        if (code == null) {
            return NOT_ELIGIBLE;
        }
        final String codeText = code.trim();
        if (codeText.length() < 1) {
            return NOT_ELIGIBLE;
        } else if (codeText.length() > 1) {
            throw new IllegalArgumentException("Cannot convert string '"+codeText+"' to a collType");
        } else {
            return get(codeText.charAt(0));
        }
    }

    public static List<PrimaryType> getAllTypes() {
        final List<PrimaryType> types = new ArrayList<PrimaryType>();
        for (final char c : s_order) {
            types.add(PrimaryType.get(c));
        }
        return types;
    }
    
    public static final PrimaryType PRIMARY = new PrimaryType('P');
    public static final PrimaryType SECONDARY = new PrimaryType('S');
    public static final PrimaryType NOT_ELIGIBLE = new PrimaryType('N');
}