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
package org.slim3.gen.datastore;

/**
 * @author taedium
 * 
 */
public abstract class PrimitiveType extends AbstractDataType {

    protected final String wrapperClassName;

    /**
     * @param env
     * @param declaration
     * @param typeMirror
     */
    public PrimitiveType(String className, String wrapperClassName) {
        super(className, wrapperClassName);
        this.wrapperClassName = wrapperClassName;
    }

    /**
     * @return the wrapperClassName
     */
    public String getWrapperClassName() {
        return wrapperClassName;
    }

    public <R, P, TH extends Throwable> R accept(
            DataTypeVisitor<R, P, TH> visitor, P p) throws TH {
        return visitor.visitPrimitiveType(this, p);
    }
}