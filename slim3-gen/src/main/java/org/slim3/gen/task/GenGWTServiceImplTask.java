/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.slim3.gen.task;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.slim3.gen.ClassConstants;
import org.slim3.gen.Constants;
import org.slim3.gen.desc.GWTServiceImplDesc;
import org.slim3.gen.desc.GWTServiceImplDescFactory;
import org.slim3.gen.generator.GWTServiceImplGenerator;
import org.slim3.gen.generator.GWTServiceImplTestCaseGenerator;
import org.slim3.gen.generator.Generator;

/**
 * Represents a task to generate a GWT service implementation java file.
 * 
 * @author taedium
 * @since 3.0
 */
public class GenGWTServiceImplTask extends AbstractGenJavaFileTask {

    /** the packageName */
    protected String packageName;

    /** the superclass name of testcase */
    protected String testCaseSuperclassName = ClassConstants.JDOTestCase;

    /** the serviceClassName */
    protected String serviceClassName;

    /** the serviceRelativeClassName */
    protected String serviceRelativeClassName;

    /**
     * Sets the packageName.
     * 
     * @param packageName
     *            the packageName to set
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Sets the serviceClassName.
     * 
     * @param serviceClassName
     *            the serviceClassName to set
     */
    public void setServiceClassName(String serviceClassName) {
        this.serviceClassName = serviceClassName;
    }

    @Override
    public void doExecute() throws Exception {
        super.doExecute();
        if (serviceClassName == null) {
            throw new IllegalStateException(
                "The serviceClassName parameter is null.");
        }

        ClassNameBuilder nameBuilder = getClassNameBuilder();

        GWTServiceImplDescFactory factory =
            createServiceImplDescFactory(nameBuilder.getPackageName());
        GWTServiceImplDesc serviceImplDesc = factory.createServiceImplDesc();

        JavaFile javaFile = createJavaFile(serviceImplDesc);
        Generator generator = createServiceImplGenerator(serviceImplDesc);
        generateJavaFile(generator, javaFile);

        JavaFile testCaseJavaFile = createTestCaseJavaFile(serviceImplDesc);
        Generator testCaseGenerator =
            createServiceImplTestCaseGenerator(serviceImplDesc);
        generateJavaFile(testCaseGenerator, testCaseJavaFile);
    }

    /**
     * Creates a {@link ClassNameBuilder}.
     * 
     * @return a {@link ClassNameBuilder}
     * @throws IOException
     * @throws XPathExpressionException
     */
    protected ClassNameBuilder getClassNameBuilder() throws IOException,
            XPathExpressionException {
        ClassNameBuilder nameBuilder = new ClassNameBuilder();
        nameBuilder.append(getServiceImplBasePackageName());
        nameBuilder.append(serviceRelativeClassName);
        return nameBuilder;
    }

    /**
     * Returns the service implementation package name.
     * 
     * @return the service implementation package name.
     * @throws IOException
     * @throws XPathExpressionException
     */
    protected String getServiceImplBasePackageName() throws IOException,
            XPathExpressionException {
        if (packageName != null) {
            return packageName;
        }
        WebConfig config = createWebConfig();
        return config.getRootPackageName()
            + "."
            + Constants.SERVICE_IMPL_SUB_PACKAGE;
    }

    /**
     * Creates a {@link GWTServiceImplDescFactory}.
     * 
     * @param packageName
     *            the package name
     * @return a service implementation description factory.
     */
    protected GWTServiceImplDescFactory createServiceImplDescFactory(
            String packageName) {
        return new GWTServiceImplDescFactory(
            packageName,
            testCaseSuperclassName,
            serviceClassName);
    }

    /**
     * Creates a {@link Generator}.
     * 
     * @param serviceImplDesc
     *            the service implementation description
     * @return a generator
     */
    protected Generator createServiceImplGenerator(
            GWTServiceImplDesc serviceImplDesc) {
        return new GWTServiceImplGenerator(serviceImplDesc);
    }

    /**
     * Creates a {@link Generator} for a test case.
     * 
     * @param serviceImplDesc
     *            the service implementation description
     * @return a generator
     */
    protected Generator createServiceImplTestCaseGenerator(
            GWTServiceImplDesc serviceImplDesc) {
        return new GWTServiceImplTestCaseGenerator(serviceImplDesc);
    }
}