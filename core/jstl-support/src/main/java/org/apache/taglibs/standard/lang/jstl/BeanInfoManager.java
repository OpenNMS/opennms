/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Portions Copyright Apache Software Foundation.
 */ 

package org.apache.taglibs.standard.lang.jstl;

import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * <p>Manages the BeanInfo for one class - contains the BeanInfo, and
 * also a mapping from property name to BeanInfoProperty.  There are
 * also static methods for accessing the BeanInfoManager for a class -
 * those mappings are cached permanently so that once the
 * BeanInfoManager is calculated, it doesn't have to be calculated
 * again.
 * 
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181181 $$DateTime: 2001/06/26 09:55:09 $$Author: kchung $
 **/

public class BeanInfoManager
{
  //-------------------------------------
  // Properties
  //-------------------------------------
  // property beanClass

  Class mBeanClass;
  public Class getBeanClass ()
  { return mBeanClass; }

  //-------------------------------------
  // Member variables
  //-------------------------------------

  // The BeanInfo
  BeanInfo mBeanInfo;

  // Mapping from property name to BeanInfoProperty
  Map mPropertyByName;

  // Mapping from property name to BeanInfoIndexedProperty
  Map mIndexedPropertyByName;

  // Mapping from event set name to event set descriptor
  Map mEventSetByName;

  // Flag if this is initialized
  boolean mInitialized;

  // The global mapping from class to BeanInfoManager
  static Map mBeanInfoManagerByClass = new HashMap ();

  //-------------------------------------
  /**
   *
   * Constructor
   **/
  BeanInfoManager (Class pBeanClass)
  {
    mBeanClass = pBeanClass;
  }

  //-------------------------------------
  /**
   *
   * Returns the BeanInfoManager for the specified class
   **/
  public static BeanInfoManager getBeanInfoManager (Class pClass)
  {
    BeanInfoManager ret = (BeanInfoManager) 
      mBeanInfoManagerByClass.get (pClass);
    if (ret == null) {
      ret = createBeanInfoManager (pClass);
    }
    return ret;
  }

  //-------------------------------------
  /**
   *
   * Creates and registers the BeanInfoManager for the given class if
   * it isn't already registered.
   **/
  static synchronized BeanInfoManager createBeanInfoManager (Class pClass)
  {
    // Because this method is synchronized statically, the
    // BeanInfoManager is not initialized at this time (otherwise it
    // could end up being a bottleneck for the entire system).  It is
    // put into the map in an uninitialized state.  The first time
    // someone tries to use it, it will be initialized (with proper
    // synchronizations in place to make sure it is only initialized
    // once).

    BeanInfoManager ret = (BeanInfoManager) 
      mBeanInfoManagerByClass.get (pClass);
    if (ret == null) {
      ret = new BeanInfoManager (pClass);
      mBeanInfoManagerByClass.put (pClass, ret);
    }
    return ret;
  }

  //-------------------------------------
  /**
   *
   * Returns the BeanInfoProperty for the specified property in the
   * given class, or null if not found.
   **/
  public static BeanInfoProperty getBeanInfoProperty
    (Class pClass,
     String pPropertyName,
     Logger pLogger)
    throws ELException
  {
    return getBeanInfoManager (pClass).getProperty (pPropertyName, pLogger);
  }

  //-------------------------------------
  /**
   *
   * Returns the BeanInfoIndexedProperty for the specified property in
   * the given class, or null if not found.
   **/
  public static BeanInfoIndexedProperty getBeanInfoIndexedProperty
    (Class pClass,
     String pIndexedPropertyName,
     Logger pLogger)
    throws ELException
  {
    return getBeanInfoManager 
      (pClass).getIndexedProperty (pIndexedPropertyName, pLogger);
  }

  //-------------------------------------
  /**
   *
   * Makes sure that this class has been initialized, and synchronizes
   * the initialization if it's required.
   **/
  void checkInitialized (Logger pLogger)
    throws ELException
  {
    if (!mInitialized) {
      synchronized (this) {
	if (!mInitialized) {
	  initialize (pLogger);
	  mInitialized = true;
	}
      }
    }
  }

  //-------------------------------------
  /**
   *
   * Initializes by mapping property names to BeanInfoProperties
   **/
  void initialize (Logger pLogger)
    throws ELException
  {
    try {
      mBeanInfo = Introspector.getBeanInfo (mBeanClass);

      mPropertyByName = new HashMap ();
      mIndexedPropertyByName = new HashMap ();
      PropertyDescriptor [] pds = mBeanInfo.getPropertyDescriptors ();
      for (int i = 0; pds != null && i < pds.length; i++) {
	// Treat as both an indexed property and a normal property
	PropertyDescriptor pd = pds [i];
	if (pd instanceof IndexedPropertyDescriptor) {
	  IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
	  Method readMethod = getPublicMethod (ipd.getIndexedReadMethod ());
	  Method writeMethod = getPublicMethod (ipd.getIndexedWriteMethod ());
	  BeanInfoIndexedProperty property = new BeanInfoIndexedProperty
	    (readMethod,
	     writeMethod,
	     ipd);

	  mIndexedPropertyByName.put (ipd.getName (), property);
	}

	Method readMethod = getPublicMethod (pd.getReadMethod ());
	Method writeMethod = getPublicMethod (pd.getWriteMethod ());
	BeanInfoProperty property = new BeanInfoProperty
	  (readMethod,
	   writeMethod,
	   pd);

	mPropertyByName.put (pd.getName (), property);
      }

      mEventSetByName = new HashMap ();
      EventSetDescriptor [] esds = mBeanInfo.getEventSetDescriptors ();
      for (int i = 0; esds != null && i < esds.length; i++) {
	EventSetDescriptor esd = esds [i];
	mEventSetByName.put (esd.getName (), esd);
      }
    }
    catch (IntrospectionException exc) {
      if (pLogger.isLoggingWarning ()) {
	pLogger.logWarning
	  (Constants.EXCEPTION_GETTING_BEANINFO,
	   exc,
	   mBeanClass.getName ());
      }
    }
  }

  //-------------------------------------
  /**
   *
   * Returns the BeanInfo for the class
   **/
  BeanInfo getBeanInfo (Logger pLogger)
    throws ELException
  {
    checkInitialized (pLogger);
    return mBeanInfo;
  }

  //-------------------------------------
  /**
   *
   * Returns the BeanInfoProperty for the given property name, or null
   * if not found.
   **/
  public BeanInfoProperty getProperty (String pPropertyName,
				       Logger pLogger)
    throws ELException
  {
    checkInitialized (pLogger);
    return (BeanInfoProperty) mPropertyByName.get (pPropertyName);
  }

  //-------------------------------------
  /**
   *
   * Returns the BeanInfoIndexedProperty for the given property name,
   * or null if not found.
   **/
  public BeanInfoIndexedProperty getIndexedProperty 
    (String pIndexedPropertyName,
     Logger pLogger)
    throws ELException
  {
    checkInitialized (pLogger);
    return (BeanInfoIndexedProperty) 
      mIndexedPropertyByName.get (pIndexedPropertyName);
  }

  //-------------------------------------
  /**
   *
   * Returns the EventSetDescriptor for the given event set name, or
   * null if not found.
   **/
  public EventSetDescriptor getEventSet (String pEventSetName,
					 Logger pLogger)
    throws ELException
  {
    checkInitialized (pLogger);
    return (EventSetDescriptor) mEventSetByName.get (pEventSetName);
  }

  //-------------------------------------
  // Finding the public version of a method - if a PropertyDescriptor
  // is obtained for a non-public class that implements a public
  // interface, the read/write methods will be for the class, and
  // therefore inaccessible.  To correct this, a version of the same
  // method must be found in a superclass or interface.
  //-------------------------------------
  /**
   *
   * Returns a publicly-accessible version of the given method, by
   * searching for a public declaring class.
   **/
  static Method getPublicMethod (Method pMethod)
  {
    if (pMethod == null) {
      return null;
    }

    // See if the method is already available from a public class
    Class cl = pMethod.getDeclaringClass ();
    if (Modifier.isPublic (cl.getModifiers ())) {
      return pMethod;
    }

    // Otherwise, try to find a public class that declares the method
    Method ret = getPublicMethod (cl, pMethod);
    if (ret != null) {
      return ret;
    }
    else {
      return pMethod;
    }
  }

  //-------------------------------------
  /**
   *
   * If the given class is public and has a Method that declares the
   * same name and arguments as the given method, then that method is
   * returned.  Otherwise the superclass and interfaces are searched
   * recursively.
   **/
  static Method getPublicMethod (Class pClass,
				 Method pMethod)
  {
    // See if this is a public class declaring the method
    if (Modifier.isPublic (pClass.getModifiers ())) {
      try {
        Method m;
        try {
	  m = pClass.getDeclaredMethod (pMethod.getName (),
					     pMethod.getParameterTypes ());
        } catch (java.security.AccessControlException ex) {
	  // kludge to accommodate J2EE RI's default settings
	  // TODO: see if we can simply replace
          //       getDeclaredMethod() with getMethod() ...?
          m = pClass.getMethod(pMethod.getName (),
                                             pMethod.getParameterTypes ());
        }
	if (Modifier.isPublic (m.getModifiers ())) {
	  return m;
	}
      }
      catch (NoSuchMethodException exc) {}
    }

    // Search the interfaces
    {
      Class [] interfaces = pClass.getInterfaces ();
      if (interfaces != null) {
	for (int i = 0; i < interfaces.length; i++) {
	  Method m = getPublicMethod (interfaces [i], pMethod);
	  if (m != null) {
	    return m;
	  }
	}
      }
    }

    // Search the superclass
    {
      Class superclass = pClass.getSuperclass ();
      if (superclass != null) {
	Method m = getPublicMethod (superclass, pMethod);
	if (m != null) {
	  return m;
	}
      }
    }

    return null;
  }

  //-------------------------------------
}
