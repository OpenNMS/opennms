package org.opennms.core.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.opennms.core.utils.LogUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * <p>OpenNMSAbstractTestExecutionListener class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OpenNMSAbstractTestExecutionListener extends AbstractTestExecutionListener {
    /** {@inheritDoc} */
    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        super.beforeTestMethod(testContext);
        LogUtils.debugf(this, "starting test method", testContext.getTestMethod());
    }

    /** {@inheritDoc} */
    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        super.afterTestMethod(testContext);
        LogUtils.debugf(this, "finishing test method", testContext.getTestMethod());
    }

    /**
     * <p>findTestAnnotation</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @param testContext a {@link org.springframework.test.context.TestContext} object.
     * @param <T> a T object.
     * @return a T object.
     */
    protected <T extends Annotation> T findTestAnnotation(Class<T> clazz, TestContext testContext) {
        Method testMethod = testContext.getTestMethod();
        T config = testMethod.getAnnotation(clazz);
        if (config != null) {
            return config;
        }
        LogUtils.tracef(this, "unable to find method annotation for context %s", testContext.getApplicationContext().toString());

        config = ((Class<?>) testContext.getTestClass()).getAnnotation(clazz);
        if (config != null) {
            return config;
        }
        LogUtils.tracef(this, "unable to find class annotation for context %s", testContext.getApplicationContext().toString());
        
        return null;
    }
}
