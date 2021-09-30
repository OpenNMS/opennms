/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2016 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennms.reporting.jasperreports.compiler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JRReport;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.design.JRAbstractJavaCompiler;
import net.sf.jasperreports.engine.design.JRClassGenerator;
import net.sf.jasperreports.engine.design.JRCompilationSourceCode;
import net.sf.jasperreports.engine.design.JRCompilationUnit;
import net.sf.jasperreports.engine.design.JRSourceCompileTask;
import net.sf.jasperreports.engine.util.JRClassLoader;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.functions.FunctionsUtil;

/**
 * This class is a fork of net.sf.jasperreports.engine.design.JRJdtCompiler from JasperReports 6.3.0.
 *
 * It was modified to support JDT 4.2.2 which is required by some of the other
 * components in our class-path i.e. Drools and Jetty.
 *
 * @author jwhite
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 */
public class CustomJRJdtCompiler extends JRAbstractJavaCompiler {
    private static final Logger LOG = LoggerFactory.getLogger(CustomJRJdtCompiler.class);

    private static final String JDT_PROPERTIES_PREFIX = "org.eclipse.jdt.core.";

    public static final String EXCEPTION_MESSAGE_KEY_CLASS_LOADING_ERROR = "compilers.jdt.class.loading.error";
    public static final String EXCEPTION_MESSAGE_KEY_NAME_ENVIRONMENT_ANSWER_INSTANCE_ERROR = "compilers.jdt.name.environment.answer.instance.error";

    private final ClassLoader classLoader;

    public CustomJRJdtCompiler(JasperReportsContext jasperReportsContext) {
        super(jasperReportsContext, false);
        classLoader = getClassLoader();
    }

    @Override
    protected String compileUnits(final JRCompilationUnit[] units, String classpath, File tempDirFile) {
        final INameEnvironment env = getNameEnvironment(units);

        final IErrorHandlingPolicy policy = DefaultErrorHandlingPolicies.proceedWithAllProblems();

        final CompilerOptions options = getJdtSettings();

        final IProblemFactory problemFactory = new DefaultProblemFactory(Locale.getDefault());

        final CompilerRequestor requestor = getCompilerRequestor(units);

        final Compiler compiler = new Compiler(env, policy, options, requestor, problemFactory);

        do {
            CompilationUnit[] compilationUnits = requestor.processCompilationUnits();

            compiler.compile(compilationUnits);
        } while (requestor.hasMissingMethods());

        requestor.processProblems();

        return requestor.getFormattedProblems();
    }

    protected INameEnvironment getNameEnvironment(final JRCompilationUnit[] units) {
        final INameEnvironment env = new INameEnvironment() {
            @Override
            public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
                final StringBuilder result = new StringBuilder();
                String sep = "";
                for (int i = 0; i < compoundTypeName.length; i++) {
                    result.append(sep);
                    result.append(compoundTypeName[i]);
                    sep = ".";
                }
                return findType(result.toString());
            }

            @Override
            public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
                final StringBuilder result = new StringBuilder();
                String sep = "";
                for (int i = 0; i < packageName.length; i++) {
                    result.append(sep);
                    result.append(packageName[i]);
                    sep = ".";
                }
                result.append(sep);
                result.append(typeName);
                return findType(result.toString());
            }

            private int getClassIndex(String className) {
                int classIdx;
                for (classIdx = 0; classIdx < units.length; ++classIdx) {
                    if (className.equals(units[classIdx].getName())) {
                        break;
                    }
                }

                if (classIdx >= units.length) {
                    classIdx = -1;
                }

                return classIdx;
            }

            private NameEnvironmentAnswer findType(String className) {
                try {
                    int classIdx = getClassIndex(className);

                    if (classIdx >= 0) {
                        ICompilationUnit compilationUnit = new CompilationUnit(units[classIdx].getSourceCode(),
                                className);
                        return new NameEnvironmentAnswer(compilationUnit, null);
                    }

                    String resourceName = className.replace('.', '/') + ".class";
                    InputStream is = getResource(resourceName);
                    if (is != null) {
                        try {
                            byte[] classBytes = JRLoader.loadBytes(is);
                            char[] fileName = className.toCharArray();
                            ClassFileReader classFileReader = new ClassFileReader(classBytes, fileName, true);
                            return new NameEnvironmentAnswer(classFileReader, null);
                        } finally {
                            try {
                                is.close();
                            } catch (IOException e) {
                                // ignore
                            }
                        }
                    }
                } catch (JRException e) {
                    LOG.error("Compilation error", e);
                } catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException exc) {
                    LOG.error("Compilation error", exc);
                } catch (IllegalArgumentException e) {
                    throw new JRRuntimeException(EXCEPTION_MESSAGE_KEY_NAME_ENVIRONMENT_ANSWER_INSTANCE_ERROR,
                            (Object[]) null, e);
                }
                return null;
            }

            private boolean isPackage(String result) {
                int classIdx = getClassIndex(result);
                if (classIdx >= 0) {
                    return false;
                }

                String resourceName = result.replace('.', '/') + ".class";

                boolean isPackage = true;

                InputStream is = getResource(resourceName);
                if (is != null) {
                    // cannot just test for null; need to read from "is" to
                    // avoid bug with sun.plugin.cache.EmptyInputStream on JRE
                    // 1.5 plugin
                    try {
                        isPackage = (is.read() > 0);
                    } catch (IOException e) {
                        // ignore
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }

                return isPackage;
            }

            @Override
            public boolean isPackage(char[][] parentPackageName, char[] packageName) {
                final StringBuilder result = new StringBuilder();
                String sep = "";
                if (parentPackageName != null) {
                    for (int i = 0; i < parentPackageName.length; i++) {
                        result.append(sep);
                        result.append(parentPackageName[i]);
                        sep = ".";
                    }
                }
                if (Character.isUpperCase(packageName[0])) {
                    if (!isPackage(result.toString())) {
                        return false;
                    }
                }
                result.append(sep);
                result.append(packageName);
                return isPackage(result.toString());
            }

            @Override
            public void cleanup() {
            }

        };

        return env;
    }

    protected CompilerRequestor getCompilerRequestor(final JRCompilationUnit[] units) {
        return new CompilerRequestor(jasperReportsContext, this, units);
    }

    protected CompilerOptions getJdtSettings() {
        final Map<String, String> settings = new HashMap<String, String>();
        settings.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.GENERATE);
        settings.put(CompilerOptions.OPTION_SourceFileAttribute, CompilerOptions.GENERATE);
        settings.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);

        List<JRPropertiesUtil.PropertySuffix> properties = JRPropertiesUtil.getInstance(jasperReportsContext)
                .getProperties(JDT_PROPERTIES_PREFIX);
        for (Iterator<JRPropertiesUtil.PropertySuffix> it = properties.iterator(); it.hasNext();) {
            JRPropertiesUtil.PropertySuffix property = it.next();
            String propVal = property.getValue();
            if (propVal != null && propVal.length() > 0) {
                settings.put(property.getKey(), propVal);
            }
        }

        Properties systemProps = System.getProperties();
        for (@SuppressWarnings("unchecked")
        Enumeration<String> it = (Enumeration<String>) systemProps.propertyNames(); it.hasMoreElements();) {
            String propName = it.nextElement();
            if (propName.startsWith(JDT_PROPERTIES_PREFIX)) {
                String propVal = systemProps.getProperty(propName);
                if (propVal != null && propVal.length() > 0) {
                    settings.put(propName, propVal);
                }
            }
        }

        return new CompilerOptions(settings);
    }

    private ClassLoader getClassLoader() {
        ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();

        if (clsLoader != null) {
            try {
                Class.forName(CustomJRJdtCompiler.class.getName(), true, clsLoader);
            } catch (ClassNotFoundException e) {
                clsLoader = null;
            }
        }

        if (clsLoader == null) {
            clsLoader = JRClassLoader.class.getClassLoader();
        }

        return clsLoader;
    }

    protected InputStream getResource(String resourceName) {
        if (classLoader == null) {
            return CustomJRJdtCompiler.class.getResourceAsStream("/" + resourceName);
        }
        return classLoader.getResourceAsStream(resourceName);
    }

    protected Class<?> loadClass(String className) throws ClassNotFoundException {
        if (classLoader == null) {
            return Class.forName(className);
        }
        return classLoader.loadClass(className);
    }

    @Override
    protected void checkLanguage(String language) throws JRException {
        if (!JRReport.LANGUAGE_JAVA.equals(language)) {
            throw new JRException(EXCEPTION_MESSAGE_KEY_EXPECTED_JAVA_LANGUAGE,
                    new Object[] { language, JRReport.LANGUAGE_JAVA });
        }
    }

    protected JRCompilationUnit recreateCompileUnit(JRCompilationUnit compilationUnit, Set<Method> missingMethods) {
        String unitName = compilationUnit.getName();

        JRSourceCompileTask sourceTask = compilationUnit.getCompileTask();
        JRCompilationSourceCode sourceCode = JRClassGenerator.modifySource(sourceTask, missingMethods,
                compilationUnit.getSourceCode());

        File sourceFile = compilationUnit.getSourceFile();
        File saveSourceDir = sourceFile == null ? null : sourceFile.getParentFile();
        sourceFile = getSourceFile(saveSourceDir, unitName, sourceCode);

        return new JRCompilationUnit(unitName, sourceCode, sourceFile, compilationUnit.getExpressions(), sourceTask);
    }

    @Override
    protected JRCompilationSourceCode generateSourceCode(JRSourceCompileTask sourceTask) throws JRException {
        return JRClassGenerator.generateClass(sourceTask);
    }

    @Override
    protected String getSourceFileName(String unitName) {
        return unitName + ".java";
    }

    @Override
    protected String getCompilerClass() {
        return CustomJRJdtCompiler.class.getCanonicalName();
    }

    public static class CompilerRequestor implements ICompilerRequestor {
        public static final String EXCEPTION_MESSAGE_KEY_METHOD_INVOKING_ERROR = "compilers.jdt.method.invoking.error";
        public static final String EXCEPTION_MESSAGE_KEY_METHOD_RESOLVING_ERROR = "compilers.jdt.method.resolving.error";

        private final JasperReportsContext jasperReportsContext;
        protected final CustomJRJdtCompiler compiler;
        protected final JRCompilationUnit[] units;
        protected final CompilationUnitResult[] unitResults;

        public CompilerRequestor(final JasperReportsContext jasperReportsContext, final CustomJRJdtCompiler compiler,
                final JRCompilationUnit[] units) {
            this.jasperReportsContext = jasperReportsContext;
            this.compiler = compiler;
            this.units = units;
            this.unitResults = new CompilationUnitResult[units.length];

            reset();
        }

        @Override
        public void acceptResult(CompilationResult result) {
            String className = ((CompilationUnit) result.getCompilationUnit()).className;

            int classIdx;
            for (classIdx = 0; classIdx < units.length; ++classIdx) {
                if (className.equals(units[classIdx].getName())) {
                    break;
                }
            }

            if (result.hasErrors()) {
                // IProblem[] problems = result.getErrors();
                IProblem[] problems = getJavaCompilationErrors(result);

                unitResults[classIdx].problems = problems;

                String sourceCode = units[classIdx].getSourceCode();

                for (int i = 0; i < problems.length; i++) {
                    IProblem problem = problems[i];

                    if (IProblem.UndefinedMethod == problem.getID()) {
                        if (problem.getSourceStart() >= 0 && problem.getSourceEnd() >= 0) {
                            String methodName = sourceCode.substring(problem.getSourceStart(),
                                    problem.getSourceEnd() + 1);

                            Method method = FunctionsUtil.getInstance(jasperReportsContext)
                                    .getMethod4Function(methodName);
                            if (method != null) {
                                unitResults[classIdx].addMissingMethod(method);
                                // continue;
                            }
                        }
                    }
                }
            } else {
                ClassFile[] resultClassFiles = result.getClassFiles();
                for (int i = 0; i < resultClassFiles.length; i++) {
                    units[classIdx].setCompileData(resultClassFiles[i].getBytes());
                }
            }
        }

        public void processProblems() {
            // nothing to do here
        }

        public String getFormattedProblems() {
            final StringBuilder problemBuilder = new StringBuilder();

            for (int u = 0; u < units.length; u++) {
                String sourceCode = units[u].getSourceCode();

                IProblem[] problems = unitResults[u].problems;

                if (problems != null && problems.length > 0) {
                    for (int i = 0; i < problems.length; i++) {
                        IProblem problem = problems[i];

                        problemBuilder.append(i + 1);
                        problemBuilder.append(". ");
                        problemBuilder.append(problem.getMessage());

                        if (problem.getSourceStart() >= 0 && problem.getSourceEnd() >= 0) {
                            int problemStartIndex = sourceCode.lastIndexOf("\n", problem.getSourceStart()) + 1;
                            int problemEndIndex = sourceCode.indexOf("\n", problem.getSourceEnd());
                            if (problemEndIndex < 0) {
                                problemEndIndex = sourceCode.length();
                            }

                            problemBuilder.append("\n");
                            problemBuilder.append(sourceCode.substring(problemStartIndex, problemEndIndex));
                            problemBuilder.append("\n");
                            for (int j = problemStartIndex; j < problem.getSourceStart(); j++) {
                                problemBuilder.append(" ");
                            }
                            if (problem.getSourceStart() == problem.getSourceEnd()) {
                                problemBuilder.append("^");
                            } else {
                                problemBuilder.append("<");
                                for (int j = problem.getSourceStart() + 1; j < problem.getSourceEnd(); j++) {
                                    problemBuilder.append("-");
                                }
                                problemBuilder.append(">");
                            }

                            problemBuilder.append("\n");
                        }
                    }

                    problemBuilder.append(problems.length);
                    problemBuilder.append(" errors\n");
                }
            }

            return problemBuilder.length() > 0 ? problemBuilder.toString() : null;
        }

        public boolean hasMissingMethods() {
            for (CompilationUnitResult unitResult : unitResults) {
                if (unitResult.hasMissingMethods()) {
                    return true;
                }
            }
            return false;
        }

        public CompilationUnit[] processCompilationUnits() {
            final CompilationUnit[] compilationUnits = new CompilationUnit[units.length];

            for (int i = 0; i < compilationUnits.length; i++) {
                if (unitResults[i].hasMissingMethods()) {
                    units[i] = compiler.recreateCompileUnit(units[i], unitResults[i].getMissingMethods());
                    unitResults[i].resolveMissingMethods();
                }

                compilationUnits[i] = new CompilationUnit(units[i].getSourceCode(), units[i].getName());
            }

            reset();

            return compilationUnits;
        }

        protected void reset() {
            for (int i = 0; i < unitResults.length; i++) {
                if (unitResults[i] == null) {
                    unitResults[i] = new CompilationUnitResult();
                }
                unitResults[i].reset();
            }
        }

        protected IProblem[] getJavaCompilationErrors(CompilationResult result) {
            try {
                Method getErrorsMethod = result.getClass().getMethod("getErrors", (Class[]) null);
                return (IProblem[]) getErrorsMethod.invoke(result, (Object[]) null);
            } catch (SecurityException e) {
                throw new JRRuntimeException(EXCEPTION_MESSAGE_KEY_METHOD_RESOLVING_ERROR, (Object[]) null, e);
            } catch (NoSuchMethodException e) {
                throw new JRRuntimeException(EXCEPTION_MESSAGE_KEY_METHOD_RESOLVING_ERROR, (Object[]) null, e);
            } catch (IllegalArgumentException e) {
                throw new JRRuntimeException(EXCEPTION_MESSAGE_KEY_METHOD_INVOKING_ERROR, (Object[]) null, e);
            } catch (IllegalAccessException e) {
                throw new JRRuntimeException(EXCEPTION_MESSAGE_KEY_METHOD_INVOKING_ERROR, (Object[]) null, e);
            } catch (InvocationTargetException e) {
                throw new JRRuntimeException(EXCEPTION_MESSAGE_KEY_METHOD_INVOKING_ERROR, (Object[]) null, e);
            }
        }
    }

    public static class CompilationUnit implements ICompilationUnit {
        protected String srcCode;
        protected String className;

        public CompilationUnit(String srcCode, String className) {
            this.srcCode = srcCode;
            this.className = className;
        }

        @Override
        public char[] getFileName() {
            return className.toCharArray();
        }

        @Override
        public char[] getContents() {
            return srcCode.toCharArray();
        }

        @Override
        public char[] getMainTypeName() {
            return className.toCharArray();
        }

        @Override
        public char[][] getPackageName() {
            return new char[0][0];
        }

        @Override
        public boolean ignoreOptionalProblems() {
            return false;
        }
    }

    public static class CompilationUnitResult {
        private Set<Method> resolvedMethods;
        private Set<Method> missingMethods;
        private IProblem[] problems;

        public boolean hasMissingMethods() {
            return missingMethods != null && missingMethods.size() > 0;
        }

        public Set<Method> getMissingMethods() {
            return missingMethods;
        }

        public void addMissingMethod(Method missingMethod) {
            if (resolvedMethods == null || !resolvedMethods.contains(missingMethod)) {
                if (missingMethods == null) {
                    missingMethods = new HashSet<>();
                }

                missingMethods.add(missingMethod);
            }
        }

        public IProblem[] getProblems() {
            return problems;
        }

        public void setProblems(IProblem[] problems) {
            this.problems = problems;
        }

        public void resolveMissingMethods() {
            if (missingMethods != null && missingMethods.size() > 0) {
                if (resolvedMethods == null) {
                    resolvedMethods = new HashSet<>();
                }
                resolvedMethods.addAll(missingMethods);
            }
        }

        public void reset() {
            missingMethods = null;
            problems = null;
        }
    }
}
