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

package org.apache.taglibs.standard.lang.jstl.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.taglibs.standard.lang.jstl.Evaluator;
import org.apache.taglibs.standard.lang.jstl.test.beans.Factory;

/**
 *
 * <p>This runs a series of tests specifically for the evaluator.  It
 * parses and evaluates various expressions in the context of a test
 * PageContext containing preset data, and prints out the results of
 * the evaluations.
 *
 * <p>The expressions are stored in an input text file, where one line
 * contains the expression and the next line contains the expected
 * type.  Blank lines and lines that start with # are ignored.  The
 * results are written to an output file (blank lines and # lines are
 * included in the output file).  The output file may be compared
 * against an existing output file to do regression testing.
 * 
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181181 $$DateTime: 2001/06/26 09:55:09 $$Author: kchung $
 **/

public class EvaluationTest
{
  //-------------------------------------
  // Properties
  //-------------------------------------

  //-------------------------------------
  // Member variables
  //-------------------------------------

  //-------------------------------------
  /**
   *
   * Constructor
   **/
  public EvaluationTest ()
  {
  }

  //-------------------------------------
  /**
   *
   * Runs the tests, reading expressions from pIn and writing the
   * results to pOut.
   **/
  public static void runTests (DataInput pIn,
			       PrintStream pOut)
    throws IOException
  {
    PageContext context = createTestContext ();

    while (true) {
      String str = pIn.readLine ();
      if (str == null) break;
      if (str.startsWith ("#") ||
	  "".equals (str.trim ())) {
	pOut.println (str);
      }
      else {
	String typeStr = pIn.readLine ();
	pOut.println ("Expression: " + str);

	try {
	  Class cl = parseClassName (typeStr);
	  pOut.println ("ExpectedType: " + cl);
	  Evaluator e = new Evaluator ();
	  Object val = e.evaluate ("test", str, cl, null, context);
	  pOut.println ("Evaluates to: " + val);
	  if (val != null) {
	    pOut.println ("With type: " + val.getClass ().getName ());
	  }
	  pOut.println ();
	}
	catch (JspException exc) {
	  pOut.println ("Causes an error: " + exc);
	}
	catch (ClassNotFoundException exc) {
	  pOut.println ("Causes an error: " + exc);
	}
      }
    }

  }

  //-------------------------------------
  /**
   *
   * Finds the class for a class name, including primitive names
   **/
  static Class parseClassName (String pClassName)
    throws ClassNotFoundException
  {
    String c = pClassName.trim ();
    if ("boolean".equals (c)) {
      return Boolean.TYPE;
    }
    else if ("byte".equals (c)) {
      return Byte.TYPE;
    }
    else if ("char".equals (c)) {
      return Character.TYPE;
    }
    else if ("short".equals (c)) {
      return Short.TYPE;
    }
    else if ("int".equals (c)) {
      return Integer.TYPE;
    }
    else if ("long".equals (c)) {
      return Long.TYPE;
    }
    else if ("float".equals (c)) {
      return Float.TYPE;
    }
    else if ("double".equals (c)) {
      return Double.TYPE;
    }
    else {
      return Class.forName (pClassName);
    }
  }

  //-------------------------------------
  /**
   *
   * Runs the tests, reading from the given input file and writing to
   * the given output file.
   **/
  public static void runTests (File pInputFile,
			       File pOutputFile)
    throws IOException
  {
    FileInputStream fin = null;
    FileOutputStream fout = null;
    try {
      fin = new FileInputStream (pInputFile);
      BufferedInputStream bin = new BufferedInputStream (fin);
      DataInputStream din = new DataInputStream (bin);

      try {
	fout = new FileOutputStream (pOutputFile);
	BufferedOutputStream bout = new BufferedOutputStream (fout);
	PrintStream pout = new PrintStream (bout);

	runTests (din, pout);

	pout.flush ();
      }
      finally {
	if (fout != null) {
	  fout.close ();
	}
      }
    }
    finally {
      if (fin != null) {
	fin.close ();
      }
    }
  }

  //-------------------------------------
  /**
   *
   * Performs a line-by-line comparison of the two files, returning
   * true if the files are different, false if not.
   **/
  public static boolean isDifferentFiles (DataInput pIn1,
					  DataInput pIn2)
    throws IOException
  {
    while (true) {
      String str1 = pIn1.readLine ();
      String str2 = pIn2.readLine ();
      if (str1 == null &&
	  str2 == null) {
	return false;
      }
      else if (str1 == null ||
	       str2 == null) {
	return true;
      }
      else {
	if (!str1.equals (str2)) {
	  return true;
	}
      }
    }
  }

  //-------------------------------------
  /**
   *
   * Performs a line-by-line comparison of the two files, returning
   * true if the files are different, false if not.
   **/
  public static boolean isDifferentFiles (File pFile1,
					  File pFile2)
    throws IOException
  {
    FileInputStream fin1 = null;
    try {
      fin1 = new FileInputStream (pFile1);
      BufferedInputStream bin1 = new BufferedInputStream (fin1);
      DataInputStream din1 = new DataInputStream (bin1);

      FileInputStream fin2 = null;
      try {
	fin2 = new FileInputStream (pFile2);
	BufferedInputStream bin2 = new BufferedInputStream (fin2);
	DataInputStream din2 = new DataInputStream (bin2);

	return isDifferentFiles (din1, din2);
      }
      finally {
	if (fin2 != null) {
	  fin2.close ();
	}
      }
    }
    finally {
      if (fin1 != null) {
	fin1.close ();
      }
    }
  }

  //-------------------------------------
  // Test data
  //-------------------------------------
  /**
   *
   * Creates and returns the test PageContext that will be used for
   * the tests.
   **/
  static PageContext createTestContext ()
  {
    PageContext ret = new PageContextImpl ();

    // Create some basic values for lookups
    ret.setAttribute ("val1a", "page-scoped1", PageContext.PAGE_SCOPE);
    ret.setAttribute ("val1b", "request-scoped1", PageContext.REQUEST_SCOPE);
    ret.setAttribute ("val1c", "session-scoped1", PageContext.SESSION_SCOPE);
    ret.setAttribute ("val1d", "app-scoped1", PageContext.APPLICATION_SCOPE);

    // Create a bean
    {
      Bean1 b1 = new Bean1 ();
      b1.setBoolean1 (true);
      b1.setByte1 ((byte) 12);
      b1.setShort1 ((short) -124);
      b1.setChar1 ('b');
      b1.setInt1 (4);
      b1.setLong1 (222423);
      b1.setFloat1 ((float) 12.4);
      b1.setDouble1 (89.224);
      b1.setString1 ("hello");
      b1.setStringArray1 (new String [] {
	"string1",
	"string2",
	"string3",
	"string4"
      });
      {
	List l = new ArrayList ();
	l.add (new Integer (14));
	l.add ("another value");
	l.add (b1.getStringArray1 ());
	b1.setList1 (l);
      }
      {
	Map m = new HashMap ();
	m.put ("key1", "value1");
	m.put (new Integer (14), "value2");
	m.put (new Long (14), "value3");
	m.put ("recurse", b1);
	b1.setMap1 (m);
      }
      ret.setAttribute ("bean1a", b1);

      Bean1 b2 = new Bean1 ();
      b2.setInt2 (new Integer (-224));
      b2.setString2 ("bean2's string");
      b1.setBean1 (b2);

      Bean1 b3 = new Bean1 ();
      b3.setDouble1 (1422.332);
      b3.setString2 ("bean3's string");
      b2.setBean2 (b3);
    }

    // Create the public/private beans
    {
      ret.setAttribute ("pbean1", Factory.createBean1 ());
      ret.setAttribute ("pbean2", Factory.createBean2 ());
      ret.setAttribute ("pbean3", Factory.createBean3 ());
      ret.setAttribute ("pbean4", Factory.createBean4 ());
      ret.setAttribute ("pbean5", Factory.createBean5 ());
      ret.setAttribute ("pbean6", Factory.createBean6 ());
      ret.setAttribute ("pbean7", Factory.createBean7 ());
    }

    // Create the empty tests
    {
      Map m = new HashMap ();
      m.put ("emptyArray", new Object [0]);
      m.put ("nonemptyArray", new Object [] {"abc"});
      m.put ("emptyList", new ArrayList ());
      {
	List l = new ArrayList ();
	l.add ("hello");
	m.put ("nonemptyList", l);
      }
      m.put ("emptyMap", new HashMap ());
      {
	Map m2 = new HashMap ();
	m2.put ("a", "a");
	m.put ("nonemptyMap", m2);
      }
      m.put ("emptySet", new HashSet ());
      {
	Set s = new HashSet ();
	s.add ("hello");
	m.put ("nonemptySet", s);
      }
      ret.setAttribute ("emptyTests", m);
    }

    return ret;
  }

  //-------------------------------------
  // Main method
  //-------------------------------------
  /**
   *
   * Runs the evaluation test
   **/
  public static void main (String [] pArgs)
    throws IOException
  {
    if (pArgs.length != 2 &&
	pArgs.length != 3) {
      usage ();
      System.exit (1);
    }

    File in = new File (pArgs [0]);
    File out = new File (pArgs [1]);

    runTests (in, out);

    if (pArgs.length > 2) {
      File compare = new File (pArgs [2]);
      if (isDifferentFiles (out, compare)) {
	System.out.println ("Test failure - output file " +
			    out +
			    " differs from expected output file " +
			    compare);
      }
      else {
	System.out.println ("tests passed");
      }
    }
  }

  //-------------------------------------
  static void usage ()
  {
    System.err.println ("usage: java org.apache.taglibs.standard.lang.jstl.test.EvaluationTest {input file} {output file} [{compare file}]");
  }

  //-------------------------------------

}
