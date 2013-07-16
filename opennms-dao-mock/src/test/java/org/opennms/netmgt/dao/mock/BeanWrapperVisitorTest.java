package org.opennms.netmgt.dao.mock;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.OnmsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanWrapperVisitorTest {
    private static final Logger LOG = LoggerFactory.getLogger(BeanWrapperVisitorTest.class);

    public static class TestBean {
        public Object getNullValue() {
            return null;
        }
        public String getString() {
            return "I am a string.";
        }
        public Boolean getTrueValue() {
            return Boolean.valueOf(true);
        }
        public Boolean getFalseValue() {
            return Boolean.valueOf(false);
        }
        public Double getOne() {
            return 1.0d;
        }
        public Float getTwo() {
            return 2.0f;
        }
        public Integer getThree() {
            return 3;
        }
        public Long getFour() {
            return 4L;
        }
        public TestBean getSubBean() {
            return new TestBean();
        }
        public CompareMe getEnumFirst() {
            return CompareMe.FIRST;
        }
        public CompareMe getEnumSecond() {
            return CompareMe.SECOND;
        }
        public CompareMe getEnumThird() {
            return CompareMe.THIRD;
        }
    }

    BeanWrapperCriteriaVisitor m_visitor = null;
    TestBean m_testBean = new TestBean();

    @Before
    public void setUp() {
        final List<TestBean> beans = new ArrayList<TestBean>();
        beans.add(m_testBean);
        m_visitor = new BeanWrapperCriteriaVisitor(beans);
    }

    @Test
    public void testCaseInsensitive() {
        new CriteriaBuilder(TestBean.class).isNotNull("truevalue").toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
    }

    @Test
    public void testNullRestriction() {
        new CriteriaBuilder(TestBean.class).isNull("nullValue").toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
    }

    @Test
    public void testNotNullFromNullRestriction() {
        new CriteriaBuilder(TestBean.class).isNull("string").toCriteria().visit(m_visitor);
        assertEquals(0, m_visitor.getMatches().size());
    }

    @Test
    public void testNotNullRestriction() {
        new CriteriaBuilder(TestBean.class).isNotNull("string").toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
    }

    @Test
    public void testNullFromNotNullRestriction() {
        new CriteriaBuilder(TestBean.class).isNotNull("nullValue").toCriteria().visit(m_visitor);
        assertEquals(0, m_visitor.getMatches().size());
    }
    
    @Test
    public void testEqualsRestriction() {
        new CriteriaBuilder(TestBean.class).eq("string", "I am a string.").toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
        m_visitor.reset();
        new CriteriaBuilder(TestBean.class).eq("trueValue", Boolean.valueOf(true)).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
        m_visitor.reset();
        new CriteriaBuilder(TestBean.class).eq("falseValue", Boolean.valueOf(false)).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
    }

    @Test
    public void testFailedEqualsRestriction() {
        new CriteriaBuilder(TestBean.class).eq("string", false).toCriteria().visit(m_visitor);
        assertEquals(0, m_visitor.getMatches().size());
    }

    @Test
    public void testGreaterThan() {
        new CriteriaBuilder(TestBean.class).gt("one", 0.0f).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
        m_visitor.reset();
        new CriteriaBuilder(TestBean.class).gt("one", 0.99999d).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
        m_visitor.reset();
        new CriteriaBuilder(TestBean.class).gt("four", 3.9999f).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
    }

    @Test
    public void testFailedGreaterThanWithEqualsComparison() {
        new CriteriaBuilder(TestBean.class).gt("four", 4).toCriteria().visit(m_visitor);
        assertEquals(0, m_visitor.getMatches().size());
    }

    @Test
    public void testFailedGreaterThanWithLessComparison() {
        new CriteriaBuilder(TestBean.class).gt("four", 17d).toCriteria().visit(m_visitor);
        assertEquals(0, m_visitor.getMatches().size());
    }
    
    @Test
    public void testGreaterEqual() {
        new CriteriaBuilder(TestBean.class).ge("one", 0.0f).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
        m_visitor.reset();
        new CriteriaBuilder(TestBean.class).ge("one", 0.99999d).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
        m_visitor.reset();
        new CriteriaBuilder(TestBean.class).ge("four", 3.9999f).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
    }

    @Test
    public void testGreaterEqualWithEqualsComparison() {
        new CriteriaBuilder(TestBean.class).ge("four", 4.0f).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
        m_visitor.reset();
        new CriteriaBuilder(TestBean.class).ge("four", 4).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
    }

    @Test
    public void testFailedGreaterEqualWithLessComparison() {
        new CriteriaBuilder(TestBean.class).ge("four", 17d).toCriteria().visit(m_visitor);
        assertEquals(0, m_visitor.getMatches().size());
    }

    @Test
    public void testLessThan() {
        new CriteriaBuilder(TestBean.class).lt("one", 2.0f).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
        m_visitor.reset();
        new CriteriaBuilder(TestBean.class).lt("one", 2.99999d).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
        m_visitor.reset();
        new CriteriaBuilder(TestBean.class).lt("four", 5.9999f).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
    }

    @Test
    public void testFailedLessThanWithEqualsComparison() {
        new CriteriaBuilder(TestBean.class).lt("four", 4).toCriteria().visit(m_visitor);
        assertEquals(0, m_visitor.getMatches().size());
    }

    @Test
    public void testFailedLessThanWithGreaterComparison() {
        new CriteriaBuilder(TestBean.class).lt("four", 1d).toCriteria().visit(m_visitor);
        assertEquals(0, m_visitor.getMatches().size());
    }
    
    @Test
    public void testLessEqual() {
        new CriteriaBuilder(TestBean.class).le("one", 2.0f).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
        m_visitor.reset();
        new CriteriaBuilder(TestBean.class).le("one", 2.99999d).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
        m_visitor.reset();
        new CriteriaBuilder(TestBean.class).le("four", 5.9999f).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
    }

    @Test
    public void testLessEqualWithEqualsComparison() {
        new CriteriaBuilder(TestBean.class).le("four", 4.0f).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
        m_visitor.reset();
        new CriteriaBuilder(TestBean.class).le("four", 4).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
    }

    @Test
    public void testFailedLessEqualWithGreaterComparison() {
        new CriteriaBuilder(TestBean.class).le("four", 1d).toCriteria().visit(m_visitor);
        assertEquals(0, m_visitor.getMatches().size());
    }
    
    @Test
    public void testAlias() {
        new CriteriaBuilder(TestBean.class).alias("subBean", "foo").eq("foo.one", 1d).toCriteria().visit(m_visitor);
        assertEquals(1, m_visitor.getMatches().size());
    }
    
    @Test
    public void testNot() {
        final Criteria criteria = new CriteriaBuilder(TestBean.class).not().eq("string", "I am a string.").toCriteria();
        LOG.debug("criteria = {}", criteria);
        criteria.visit(m_visitor);
        assertEquals(0, m_visitor.getMatches().size());
    }
    
    @Test
    public void testEventsMatching() {
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsEvent.class);
        cb.eq("eventuei", EventConstants.NODE_DOWN_EVENT_UEI);
        cb.isNull("ipaddr");
        
        final List<OnmsEvent> events = new ArrayList<OnmsEvent>();
        events.add(createEvent(1, "uei.opennms.org/test"));
        events.add(createEvent(2, EventConstants.NODE_DOWN_EVENT_UEI));
        events.add(createEvent(3, EventConstants.NODE_DOWN_EVENT_UEI));
        final BeanWrapperCriteriaVisitor visitor = new BeanWrapperCriteriaVisitor(events);
        cb.toCriteria().visit(visitor);
        assertEquals(2, visitor.getMatches().size());
    }

    @Test
    public void testEnumComparisonGt() {
        final BeanWrapperCriteriaVisitor visitor = new BeanWrapperCriteriaVisitor(m_testBean);
        new CriteriaBuilder(TestBean.class).gt("enumFirst", CompareMe.FIRST).toCriteria().visit(visitor);
        assertEquals(0, visitor.getMatches().size());

        visitor.reset();
        new CriteriaBuilder(TestBean.class).gt("enumSecond", CompareMe.FIRST).toCriteria().visit(visitor);
        assertEquals(1, visitor.getMatches().size());
    }
    
    @Test
    public void testEnumComparisonGe() {
        final BeanWrapperCriteriaVisitor visitor = new BeanWrapperCriteriaVisitor(m_testBean);
        new CriteriaBuilder(TestBean.class).ge("enumFirst", CompareMe.FIRST).toCriteria().visit(visitor);
        assertEquals(1, visitor.getMatches().size());

        visitor.reset();
        new CriteriaBuilder(TestBean.class).ge("enumSecond", CompareMe.FIRST).toCriteria().visit(visitor);
        assertEquals(1, visitor.getMatches().size());
    }
    
    @Test
    public void testEnumComparisonEq() {
        final BeanWrapperCriteriaVisitor visitor = new BeanWrapperCriteriaVisitor(m_testBean);
        new CriteriaBuilder(TestBean.class).eq("enumFirst", CompareMe.FIRST).toCriteria().visit(visitor);
        assertEquals(1, visitor.getMatches().size());

        visitor.reset();
        new CriteriaBuilder(TestBean.class).eq("enumSecond", CompareMe.FIRST).toCriteria().visit(visitor);
        assertEquals(0, visitor.getMatches().size());
    }
    
    @Test
    public void testEnumComparisonNe() {
        final BeanWrapperCriteriaVisitor visitor = new BeanWrapperCriteriaVisitor(m_testBean);
        new CriteriaBuilder(TestBean.class).ne("enumFirst", CompareMe.FIRST).toCriteria().visit(visitor);
        assertEquals(0, visitor.getMatches().size());

        visitor.reset();
        new CriteriaBuilder(TestBean.class).ne("enumSecond", CompareMe.FIRST).toCriteria().visit(visitor);
        assertEquals(1, visitor.getMatches().size());
    }
    
    @Test
    public void testEnumComparisonLt() {
        final BeanWrapperCriteriaVisitor visitor = new BeanWrapperCriteriaVisitor(m_testBean);
        new CriteriaBuilder(TestBean.class).lt("enumFirst", CompareMe.FIRST).toCriteria().visit(visitor);
        assertEquals(0, visitor.getMatches().size());

        visitor.reset();
        new CriteriaBuilder(TestBean.class).lt("enumSecond", CompareMe.FIRST).toCriteria().visit(visitor);
        assertEquals(0, visitor.getMatches().size());

        visitor.reset();
        new CriteriaBuilder(TestBean.class).lt("enumFirst", CompareMe.THIRD).toCriteria().visit(visitor);
        assertEquals(1, visitor.getMatches().size());

        visitor.reset();
        new CriteriaBuilder(TestBean.class).lt("enumSecond", CompareMe.THIRD).toCriteria().visit(visitor);
        assertEquals(1, visitor.getMatches().size());

        visitor.reset();
        new CriteriaBuilder(TestBean.class).lt("enumThird", CompareMe.THIRD).toCriteria().visit(visitor);
        assertEquals(0, visitor.getMatches().size());
    }
    
    @Test
    public void testEnumComparisonLe() {
        final BeanWrapperCriteriaVisitor visitor = new BeanWrapperCriteriaVisitor(m_testBean);
        new CriteriaBuilder(TestBean.class).le("enumFirst", CompareMe.FIRST).toCriteria().visit(visitor);
        assertEquals(1, visitor.getMatches().size());

        visitor.reset();
        new CriteriaBuilder(TestBean.class).le("enumSecond", CompareMe.FIRST).toCriteria().visit(visitor);
        assertEquals(0, visitor.getMatches().size());

        visitor.reset();
        new CriteriaBuilder(TestBean.class).le("enumFirst", CompareMe.THIRD).toCriteria().visit(visitor);
        assertEquals(1, visitor.getMatches().size());

        visitor.reset();
        new CriteriaBuilder(TestBean.class).le("enumSecond", CompareMe.THIRD).toCriteria().visit(visitor);
        assertEquals(1, visitor.getMatches().size());

        visitor.reset();
        new CriteriaBuilder(TestBean.class).le("enumThird", CompareMe.THIRD).toCriteria().visit(visitor);
        assertEquals(1, visitor.getMatches().size());
    }
    
    private static enum CompareMe {
        FIRST,
        SECOND,
        THIRD
    }

    private OnmsEvent createEvent(final int id, final String uei) {
        final OnmsEvent event = new OnmsEvent();
        event.setId(id);
        event.setEventUei(uei);
        return event;
    }
}
