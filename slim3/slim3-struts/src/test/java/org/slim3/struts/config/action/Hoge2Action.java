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
package org.slim3.struts.config.action;

import org.apache.struts.action.ActionForward;
import org.slim3.struts.annotation.Execute;

/**
 * @author higa
 * 
 */
public class Hoge2Action {

    /**
     * 
     */
    public String aaa;

    /**
     * @return action forward.
     */
    @Execute(validate = false, input = "hoge.html", reset = "resetForIndex", roles = {
            "aaa", "bbb" })
    public ActionForward submit() {
        return null;
    }

    /**
     * @return action forward.
     */
    @Execute(input = "hoge.html")
    public ActionForward submit2() {
        return null;
    }

    /**
     * 
     */
    public void resetForIndex() {
    }
}