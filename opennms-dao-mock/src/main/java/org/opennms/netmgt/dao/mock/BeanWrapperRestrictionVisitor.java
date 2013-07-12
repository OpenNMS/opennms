package org.opennms.netmgt.dao.mock;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.restrictions.AllRestriction;
import org.opennms.core.criteria.restrictions.AnyRestriction;
import org.opennms.core.criteria.restrictions.AttributeRestriction;
import org.opennms.core.criteria.restrictions.BaseRestrictionVisitor;
import org.opennms.core.criteria.restrictions.BetweenRestriction;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.GeRestriction;
import org.opennms.core.criteria.restrictions.GtRestriction;
import org.opennms.core.criteria.restrictions.IlikeRestriction;
import org.opennms.core.criteria.restrictions.InRestriction;
import org.opennms.core.criteria.restrictions.IplikeRestriction;
import org.opennms.core.criteria.restrictions.LeRestriction;
import org.opennms.core.criteria.restrictions.LikeRestriction;
import org.opennms.core.criteria.restrictions.LtRestriction;
import org.opennms.core.criteria.restrictions.NeRestriction;
import org.opennms.core.criteria.restrictions.NotNullRestriction;
import org.opennms.core.criteria.restrictions.NotRestriction;
import org.opennms.core.criteria.restrictions.NullRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.SqlRestriction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

final class BeanWrapperRestrictionVisitor extends BaseRestrictionVisitor {
    private static final Logger LOG = LoggerFactory.getLogger(BeanWrapperRestrictionVisitor.class);

    private final Object m_entity;
    private final List<Alias> m_aliases;
    private final BeanWrapper m_beanWrapper;

    private boolean m_matched = true;

    private boolean m_stillMatched;

    public BeanWrapperRestrictionVisitor(final Object obj) {
        this(obj, null);
    }

    public BeanWrapperRestrictionVisitor(final Object obj, final List<Alias> aliases) {
        m_entity = obj;
        m_aliases = aliases;
        m_beanWrapper = new BeanWrapperImpl(m_entity);
    }

    protected void fail(final Restriction restriction) {
        m_matched = false;
        LOG.debug("{} failed restriction: {}", m_entity, restriction);
    }

    public Object getProperty(final String attribute) {
        LOG.debug("getProperty({})", attribute);
        final String[] attributes = attribute.split("\\.", 2);
        LOG.debug("attributes = {}", Arrays.asList(attributes));
        if (attributes.length == 1) {
            for (final PropertyDescriptor pd : m_beanWrapper.getPropertyDescriptors()) {
                if (pd.getName().equalsIgnoreCase(attribute)) {
                    return m_beanWrapper.getPropertyValue(pd.getName());
                }
                for (final Alias alias : m_aliases) {
                    if (alias.getAlias().equalsIgnoreCase(attribute)) {
                        if (pd.getName().equalsIgnoreCase(alias.getAssociationPath())) {
                            return m_beanWrapper.getPropertyValue(pd.getName());
                        }
                    }
                }
            }
        } else if (attributes.length > 1) {
            LOG.debug("more than one attribute, try walking the tree");
            for (final PropertyDescriptor pd : m_beanWrapper.getPropertyDescriptors()) {
                final String attributeName = attributes[0];
                final String subAttributes = attributes[1];
                if (pd.getName().equalsIgnoreCase(attributeName)) {
                    final Object propertyValue = m_beanWrapper.getPropertyValue(pd.getName());
                    final BeanWrapperRestrictionVisitor subVisitor = new BeanWrapperRestrictionVisitor(propertyValue, m_aliases);
                    final Object property = subVisitor.getProperty(subAttributes);
                    LOG.debug("Found a sub-attribute: {} = {}", attribute, property);
                    return property;
                }
                for (final Alias alias : m_aliases) {
                    final String aliasName = alias.getAlias();
                    final String aliasPath = alias.getAssociationPath();

                    //LOG.debug("alias = {}, path = {}", aliasName, aliasPath);
                    if (aliasName.equalsIgnoreCase(attributeName)) {
                        // LOG.debug("property name = {}, aliasName = {}, aliasPath = {}, attributeName = {}", pd.getName(), aliasName, aliasPath, attributeName);
                        if (pd.getName().equalsIgnoreCase(aliasPath)) {
                            final Object propertyValue = m_beanWrapper.getPropertyValue(pd.getName());
                            final BeanWrapperRestrictionVisitor subVisitor = new BeanWrapperRestrictionVisitor(propertyValue, m_aliases);
                            final Object property = subVisitor.getProperty(subAttributes);
                            LOG.debug("Found a sub-attribute: {} = {}", attribute, property);
                            return property;
                        }
                    }
                }
            }
        } else {
            LOG.warn("Uhh... 0 attributes?  How did we get here?");
        }
        return null;
    }
    
    protected Object getProperty(final AttributeRestriction restriction) {
        return getProperty(restriction.getAttribute());
    }

    @Override public void visitNull(final NullRestriction restriction) {
        if (getProperty(restriction) != null) fail(restriction);
    }
    @Override public void visitNullComplete(final NullRestriction restriction) {}
    @Override public void visitNotNull(final NotNullRestriction restriction) {
        if (getProperty(restriction) == null) fail(restriction);
    }
    @Override public void visitNotNullComplete(final NotNullRestriction restriction) {}
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override public void visitEq(final EqRestriction restriction) {
        final Object o = getProperty(restriction);
        if (o instanceof Comparable) {
            try {
                final Comparable oC = (Comparable)o;
                final Comparable oV = (Comparable)restriction.getValue();
                final int comparison = oC.compareTo(oV);
                LOG.debug("comparision = {}", comparison);
                if (comparison == 0) {
                    return;
                }
            } catch (final ClassCastException e) {
            }
        } else {
            if (restriction.getValue().equals(o)) return;
        }
        fail(restriction);
    }
    @Override public void visitEqComplete(final EqRestriction restriction) {
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override public void visitNe(final NeRestriction restriction) {
        final Object o = getProperty(restriction);
        if (o instanceof Comparable) {
            try {
                final Comparable oC = (Comparable)o;
                final Comparable oV = (Comparable)restriction.getValue();
                final int comparison = oC.compareTo(oV);
                if (comparison != 0) {
                    return;
                }
            } catch (final ClassCastException e) {}
        } else {
            if (!restriction.getValue().equals(o)) return;
        }
        fail(restriction);
    }
    @Override public void visitNeComplete(final NeRestriction restriction) {}
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override public void visitGt(final GtRestriction restriction) {
        final Object o = getProperty(restriction);
        if (o instanceof java.lang.Number && restriction.getValue() instanceof java.lang.Number) {
            final BigDecimal left = new BigDecimal(((Number)o).doubleValue());
            final BigDecimal right = new BigDecimal(((Number)restriction.getValue()).doubleValue());
            if (left.compareTo(right) == 1) return;
        } else if (o instanceof Comparable) {
            try {
                final Comparable oC = (Comparable)o;
                final Comparable oV = (Comparable)restriction.getValue();
                if (oC.compareTo(oV) > 0) {
                    return;
                }
            } catch (final ClassCastException e) {}
        }
        fail(restriction);
    }
    @Override public void visitGtComplete(final GtRestriction restriction) {}
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override public void visitGe(final GeRestriction restriction) {
        final Object o = getProperty(restriction);
        if (o instanceof java.lang.Number && restriction.getValue() instanceof java.lang.Number) {
            final BigDecimal left = new BigDecimal(((Number)o).doubleValue());
            final BigDecimal right = new BigDecimal(((Number)restriction.getValue()).doubleValue());
            if (left.compareTo(right) >= 0) return;
        } else if (o instanceof Comparable) {
            try {
                final Comparable oC = (Comparable)o;
                final Comparable oV = (Comparable)restriction.getValue();
                if (oC.compareTo(oV) >= 0) {
                    return;
                }
            } catch (final ClassCastException e) {}
        }
        fail(restriction);
    }
    @Override public void visitGeComplete(final GeRestriction restriction) {}
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override public void visitLt(final LtRestriction restriction) {
        final Object o = getProperty(restriction);
        if (o instanceof java.lang.Number && restriction.getValue() instanceof java.lang.Number) {
            final BigDecimal left = new BigDecimal(((Number)o).doubleValue());
            final BigDecimal right = new BigDecimal(((Number)restriction.getValue()).doubleValue());
            if (left.compareTo(right) == -1) return;
        } else if (o instanceof Comparable) {
            try {
                final Comparable oC = (Comparable)o;
                final Comparable oV = (Comparable)restriction.getValue();
                if (oC.compareTo(oV) < 0) {
                    return;
                }
            } catch (final ClassCastException e) {}
        }
        fail(restriction);
    }
    @Override public void visitLtComplete(final LtRestriction restriction) {}
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override public void visitLe(final LeRestriction restriction) {
        final Object o = getProperty(restriction);
        if (o instanceof java.lang.Number && restriction.getValue() instanceof java.lang.Number) {
            final BigDecimal left = new BigDecimal(((Number)o).doubleValue());
            final BigDecimal right = new BigDecimal(((Number)restriction.getValue()).doubleValue());
            if (left.compareTo(right) <= 0) return;
        } else if (o instanceof Comparable) {
            try {
                final Comparable oC = (Comparable)o;
                final Comparable oV = (Comparable)restriction.getValue();
                if (oC.compareTo(oV) <= 0) {
                    return;
                }
            } catch (final ClassCastException e) {}
        }
        fail(restriction);
    }
    @Override public void visitLeComplete(final LeRestriction restriction) {}
    @Override public void visitAll(final AllRestriction restriction) {
        for (final Restriction r : restriction.getRestrictions()) {
            r.visit(this);
        }
    }
    @Override public void visitAllComplete(final AllRestriction restriction) {}
    @Override public void visitAny(final AnyRestriction restriction) {
        boolean matched = false;
        for (final Restriction r : restriction.getRestrictions()) {
            try {
                r.visit(this);
                matched = true;
                break;
            } catch (final Exception e) {
            }
        }
        if (!matched) {
            fail(restriction);
        }
    }
    @Override public void visitAnyComplete(final AnyRestriction restriction) {}
    @Override public void visitLike(final LikeRestriction restriction) {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
    @Override public void visitLikeComplete(final LikeRestriction restriction) {}
    @Override public void visitIlike(final IlikeRestriction restriction) {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
    @Override public void visitIlikeComplete(final IlikeRestriction restriction) {}
    @Override public void visitIn(final InRestriction restriction) {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
    @Override public void visitInComplete(final InRestriction restriction) {}
    @Override public void visitNot(final NotRestriction restriction) {
        m_stillMatched = m_matched;
    }
    @Override public void visitNotComplete(final NotRestriction restriction) {
        if (m_stillMatched) {
            m_matched = !m_matched;
        }
    }
    @Override public void visitBetween(final BetweenRestriction restriction) {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
    @Override public void visitBetweenComplete(final BetweenRestriction restriction) {}
    @Override public void visitSql(final SqlRestriction restriction) {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
    @Override public void visitSqlComplete(final SqlRestriction restriction) {}
    @Override public void visitIplike(final IplikeRestriction restriction) {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
    @Override public void visitIplikeComplete(final IplikeRestriction restriction) {}

    public boolean matches() {
        return m_matched ;
    }
}