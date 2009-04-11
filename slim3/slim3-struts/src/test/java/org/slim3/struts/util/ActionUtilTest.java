/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
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
package org.slim3.struts.util;

import junit.framework.TestCase;

import org.slim3.struts.unit.MockHttpServletRequest;
import org.slim3.struts.unit.MockServletContext;
import org.slim3.struts.web.WebLocator;

/**
 * @author higa
 * 
 */
public class ActionUtilTest extends TestCase {

    private MockServletContext servletContext = new MockServletContext();

    private MockHttpServletRequest request = new MockHttpServletRequest(
            servletContext, "/hoge.do");

    @Override
    protected void setUp() throws Exception {
        WebLocator.setRequest(request);
    }

    @Override
    protected void tearDown() throws Exception {
        WebLocator.setRequest(null);
    }

    /**
     * @throws Exception
     */
    public void testSetAndGetAction() throws Exception {
        Object action = new Object();
        ActionUtil.setAction(action);
        assertSame(action, ActionUtil.getAction());
    }

    /**
     * @throws Exception
     */
    public void testGetActionPath() throws Exception {
        assertEquals("/hoge/", ActionUtil.getActionPath());
    }

    /**
     * @throws Exception
     */
    public void testGetActionPathForJSP() throws Exception {
        request.setServletPath("/hoge/index.jsp");
        assertEquals("/hoge/", ActionUtil.getActionPath());
    }
}