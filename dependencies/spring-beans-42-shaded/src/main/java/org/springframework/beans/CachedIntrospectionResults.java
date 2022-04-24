/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.SpringProperties;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

/**
 * Internal class that caches JavaBeans {@link java.beans.PropertyDescriptor}
 * information for a Java class. Not intended for direct use by application code.
 *
 * <p>Necessary for own caching of descriptors within the application's
 * ClassLoader, rather than rely on the JDK's system-wide BeanInfo cache
 * (in order to avoid leaks on ClassLoader shutdown).
 *
 * <p>Information is cached statically, so we don't need to create new
 * objects of this class for every JavaBean we manipulate. Hence, this class
 * implements the factory design pattern, using a private constructor and
 * a static {@link #forClass(Class)} factory method to obtain instances.
 *
 * <p>Note that for caching to work effectively, some preconditions need to be met:
 * Prefer an arrangement where the Spring jars live in the same ClassLoader as the
 * application classes, which allows for clean caching along with the application's
 * lifecycle in any case. For a web application, consider declaring a local
 * {@link org.springframework.web.util.IntrospectorCleanupListener} in {@code web.xml}
 * in case of a multi-ClassLoader layout, which will allow for effective caching as well.
 *
 * <p>In case of a non-clean ClassLoader arrangement without a cleanup listener having
 * been set up, this class will fall back to a weak-reference-based caching model that
 * recreates much-requested entries every time the garbage collector removed them. In
 * such a scenario, consider the {@link #IGNORE_BEANINFO_PROPERTY_NAME} system property.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 05 May 2001
 * @see #acceptClassLoader(ClassLoader)
 * @see #clearClassLoader(ClassLoader)
 * @see #forClass(Class)
 */
public class CachedIntrospectionResults {

	/**
	 * System property that instructs Spring to use the {@link Introspector#IGNORE_ALL_BEANINFO}
	 * mode when calling the JavaBeans {@link Introspector}: "spring.beaninfo.ignore", with a
	 * value of "true" skipping the search for {@code BeanInfo} classes (typically for scenarios
	 * where no such classes are being defined for beans in the application in the first place).
	 * <p>The default is "false", considering all {@code BeanInfo} metadata classes, like for
	 * standard {@link Introspector#getBeanInfo(Class)} calls. Consider switching this flag to
	 * "true" if you experience repeated ClassLoader access for non-existing {@code BeanInfo}
	 * classes, in case such access is expensive on startup or on lazy loading.
	 * <p>Note that such an effect may also indicate a scenario where caching doesn't work
	 * effectively: Prefer an arrangement where the Spring jars live in the same ClassLoader
	 * as the application classes, which allows for clean caching along with the application's
	 * lifecycle in any case. For a web application, consider declaring a local
	 * {@link org.springframework.web.util.IntrospectorCleanupListener} in {@code web.xml}
	 * in case of a multi-ClassLoader layout, which will allow for effective caching as well.
	 * @see Introspector#getBeanInfo(Class, int)
	 */
	public static final String IGNORE_BEANINFO_PROPERTY_NAME = "spring.beaninfo.ignore";


	private static final boolean shouldIntrospectorIgnoreBeaninfoClasses =
			SpringProperties.getFlag(IGNORE_BEANINFO_PROPERTY_NAME);

	/** Stores the BeanInfoFactory instances */
	private static List<BeanInfoFactory> beanInfoFactories = SpringFactoriesLoader.loadFactories(
			BeanInfoFactory.class, CachedIntrospectionResults.class.getClassLoader());

	private static final Log logger = LogFactory.getLog(CachedIntrospectionResults.class);

	/**
	 * Set of ClassLoaders that this CachedIntrospectionResults class will always
	 * accept classes from, even if the classes do not qualify as cache-safe.
	 */
	static final Set<ClassLoader> acceptedClassLoaders =
			Collections.newSetFromMap(new ConcurrentHashMap<ClassLoader, Boolean>(16));

	/**
	 * Map keyed by Class containing CachedIntrospectionResults, strongly held.
	 * This variant is being used for cache-safe bean classes.
	 */
	static final ConcurrentMap<Class<?>, CachedIntrospectionResults> strongClassCache =
			new ConcurrentHashMap<Class<?>, CachedIntrospectionResults>(64);

	/**
	 * Map keyed by Class containing CachedIntrospectionResults, softly held.
	 * This variant is being used for non-cache-safe bean classes.
	 */
	static final ConcurrentMap<Class<?>, CachedIntrospectionResults> softClassCache =
			new ConcurrentReferenceHashMap<Class<?>, CachedIntrospectionResults>(64);


	/**
	 * Accept the given ClassLoader as cache-safe, even if its classes would
	 * not qualify as cache-safe in this CachedIntrospectionResults class.
	 * <p>This configuration method is only relevant in scenarios where the Spring
	 * classes reside in a 'common' ClassLoader (e.g. the system ClassLoader)
	 * whose lifecycle is not coupled to the application. In such a scenario,
	 * CachedIntrospectionResults would by default not cache any of the application's
	 * classes, since they would create a leak in the common ClassLoader.
	 * <p>Any {@code acceptClassLoader} call at application startup should
	 * be paired with a {@link #clearClassLoader} call at application shutdown.
	 * @param classLoader the ClassLoader to accept
	 */
	public static void acceptClassLoader(ClassLoader classLoader) {
		if (classLoader != null) {
			acceptedClassLoaders.add(classLoader);
		}
	}

	/**
	 * Clear the introspection cache for the given ClassLoader, removing the
	 * introspection results for all classes underneath that ClassLoader, and
	 * removing the ClassLoader (and its children) from the acceptance list.
	 * @param classLoader the ClassLoader to clear the cache for
	 */
	public static void clearClassLoader(ClassLoader classLoader) {
		for (Iterator<ClassLoader> it = acceptedClassLoaders.iterator(); it.hasNext();) {
			ClassLoader registeredLoader = it.next();
			if (isUnderneathClassLoader(registeredLoader, classLoader)) {
				it.remove();
			}
		}
		for (Iterator<Class<?>> it = strongClassCache.keySet().iterator(); it.hasNext();) {
			Class<?> beanClass = it.next();
			if (isUnderneathClassLoader(beanClass.getClassLoader(), classLoader)) {
				it.remove();
			}
		}
		for (Iterator<Class<?>> it = softClassCache.keySet().iterator(); it.hasNext();) {
			Class<?> beanClass = it.next();
			if (isUnderneathClassLoader(beanClass.getClassLoader(), classLoader)) {
				it.remove();
			}
		}
	}

	/**
	 * Create CachedIntrospectionResults for the given bean class.
	 * @param beanClass the bean class to analyze
	 * @return the corresponding CachedIntrospectionResults
	 * @throws BeansException in case of introspection failure
	 */
	@SuppressWarnings("unchecked")
	static CachedIntrospectionResults forClass(Class<?> beanClass) throws BeansException {
		CachedIntrospectionResults results = strongClassCache.get(beanClass);
		if (results != null) {
			return results;
		}
		results = softClassCache.get(beanClass);
		if (results != null) {
			return results;
		}

		results = new CachedIntrospectionResults(beanClass);
		ConcurrentMap<Class<?>, CachedIntrospectionResults> classCacheToUse;

		if (ClassUtils.isCacheSafe(beanClass, CachedIntrospectionResults.class.getClassLoader()) ||
				isClassLoaderAccepted(beanClass.getClassLoader())) {
			classCacheToUse = strongClassCache;
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Not strongly caching class [" + beanClass.getName() + "] because it is not cache-safe");
			}
			classCacheToUse = softClassCache;
		}

		CachedIntrospectionResults existing = classCacheToUse.putIfAbsent(beanClass, results);
		return (existing != null ? existing : results);
	}

	/**
	 * Check whether this CachedIntrospectionResults class is configured
	 * to accept the given ClassLoader.
	 * @param classLoader the ClassLoader to check
	 * @return whether the given ClassLoader is accepted
	 * @see #acceptClassLoader
	 */
	private static boolean isClassLoaderAccepted(ClassLoader classLoader) {
		for (ClassLoader acceptedLoader : acceptedClassLoaders) {
			if (isUnderneathClassLoader(classLoader, acceptedLoader)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the given ClassLoader is underneath the given parent,
	 * that is, whether the parent is within the candidate's hierarchy.
	 * @param candidate the candidate ClassLoader to check
	 * @param parent the parent ClassLoader to check for
	 */
	private static boolean isUnderneathClassLoader(ClassLoader candidate, ClassLoader parent) {
		if (candidate == parent) {
			return true;
		}
		if (candidate == null) {
			return false;
		}
		ClassLoader classLoaderToCheck = candidate;
		while (classLoaderToCheck != null) {
			classLoaderToCheck = classLoaderToCheck.getParent();
			if (classLoaderToCheck == parent) {
				return true;
			}
		}
		return false;
	}


	/** The BeanInfo object for the introspected bean class */
	private final BeanInfo beanInfo;

	/** PropertyDescriptor objects keyed by property name String */
	private final Map<String, PropertyDescriptor> propertyDescriptorCache;

	/** TypeDescriptor objects keyed by PropertyDescriptor */
	private final ConcurrentMap<PropertyDescriptor, TypeDescriptor> typeDescriptorCache;


	/**
	 * Create a new CachedIntrospectionResults instance for the given class.
	 * @param beanClass the bean class to analyze
	 * @throws BeansException in case of introspection failure
	 */
	private CachedIntrospectionResults(Class<?> beanClass) throws BeansException {
		try {
			if (logger.isTraceEnabled()) {
				logger.trace("Getting BeanInfo for class [" + beanClass.getName() + "]");
			}

			BeanInfo beanInfo = null;
			for (BeanInfoFactory beanInfoFactory : beanInfoFactories) {
				beanInfo = beanInfoFactory.getBeanInfo(beanClass);
				if (beanInfo != null) {
					break;
				}
			}
			if (beanInfo == null) {
				// If none of the factories supported the class, fall back to the default
				beanInfo = (shouldIntrospectorIgnoreBeaninfoClasses ?
						Introspector.getBeanInfo(beanClass, Introspector.IGNORE_ALL_BEANINFO) :
						Introspector.getBeanInfo(beanClass));
			}
			this.beanInfo = beanInfo;

			if (logger.isTraceEnabled()) {
				logger.trace("Caching PropertyDescriptors for class [" + beanClass.getName() + "]");
			}
			this.propertyDescriptorCache = new LinkedHashMap<String, PropertyDescriptor>();

			// This call is slow so we do it once.
			PropertyDescriptor[] pds = this.beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				if (Class.class == beanClass && !("name".equals(pd.getName()) ||
						(pd.getName().endsWith("Name") && String.class == pd.getPropertyType()))) {
					// Only allow all name variants of Class properties
					continue;
				}
				if (pd.getWriteMethod() == null && pd.getPropertyType() != null &&
					(ClassLoader.class.isAssignableFrom(pd.getPropertyType()) ||
						ProtectionDomain.class.isAssignableFrom(pd.getPropertyType()))) {
					// Ignore ClassLoader and ProtectionDomain read-only properties - no need to bind to those
					continue;
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Found bean property '" + pd.getName() + "'" +
							(pd.getPropertyType() != null ? " of type [" + pd.getPropertyType().getName() + "]" : "") +
							(pd.getPropertyEditorClass() != null ?
									"; editor [" + pd.getPropertyEditorClass().getName() + "]" : ""));
				}
				pd = buildGenericTypeAwarePropertyDescriptor(beanClass, pd);
				if (pd.getWriteMethod() == null && pd.getPropertyType() != null &&
					(ClassLoader.class.isAssignableFrom(pd.getPropertyType()) ||
						ProtectionDomain.class.isAssignableFrom(pd.getPropertyType()))) {
					// Ignore ClassLoader and ProtectionDomain read-only properties - no need to bind to those
					continue;
				}
				this.propertyDescriptorCache.put(pd.getName(), pd);
			}

			this.typeDescriptorCache = new ConcurrentReferenceHashMap<PropertyDescriptor, TypeDescriptor>();
		}
		catch (IntrospectionException ex) {
			throw new FatalBeanException("Failed to obtain BeanInfo for class [" + beanClass.getName() + "]", ex);
		}
	}

	BeanInfo getBeanInfo() {
		return this.beanInfo;
	}

	Class<?> getBeanClass() {
		return this.beanInfo.getBeanDescriptor().getBeanClass();
	}

	PropertyDescriptor getPropertyDescriptor(String name) {
		PropertyDescriptor pd = this.propertyDescriptorCache.get(name);
		if (pd == null && StringUtils.hasLength(name)) {
			// Same lenient fallback checking as in PropertyTypeDescriptor...
			pd = this.propertyDescriptorCache.get(name.substring(0, 1).toLowerCase() + name.substring(1));
			if (pd == null) {
				pd = this.propertyDescriptorCache.get(name.substring(0, 1).toUpperCase() + name.substring(1));
			}
		}
		return (pd == null || pd instanceof GenericTypeAwarePropertyDescriptor ? pd :
				buildGenericTypeAwarePropertyDescriptor(getBeanClass(), pd));
	}

	PropertyDescriptor[] getPropertyDescriptors() {
		PropertyDescriptor[] pds = new PropertyDescriptor[this.propertyDescriptorCache.size()];
		int i = 0;
		for (PropertyDescriptor pd : this.propertyDescriptorCache.values()) {
			pds[i] = (pd instanceof GenericTypeAwarePropertyDescriptor ? pd :
					buildGenericTypeAwarePropertyDescriptor(getBeanClass(), pd));
			i++;
		}
		return pds;
	}

	private PropertyDescriptor buildGenericTypeAwarePropertyDescriptor(Class<?> beanClass, PropertyDescriptor pd) {
		try {
			return new GenericTypeAwarePropertyDescriptor(beanClass, pd.getName(), pd.getReadMethod(),
					pd.getWriteMethod(), pd.getPropertyEditorClass());
		}
		catch (IntrospectionException ex) {
			throw new FatalBeanException("Failed to re-introspect class [" + beanClass.getName() + "]", ex);
		}
	}

	TypeDescriptor addTypeDescriptor(PropertyDescriptor pd, TypeDescriptor td) {
		TypeDescriptor existing = this.typeDescriptorCache.putIfAbsent(pd, td);
		return (existing != null ? existing : td);
	}

	TypeDescriptor getTypeDescriptor(PropertyDescriptor pd) {
		return this.typeDescriptorCache.get(pd);
	}

}
