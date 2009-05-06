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
package org.slim3.commons.cleaner;

import java.beans.Introspector;
import java.util.LinkedList;

/**
 * A utility class to clean resources.
 * 
 * @author koichik
 * @author higa
 */
public final class Cleaner {

    static final LinkedList<Cleanable> cleanables = new LinkedList<Cleanable>();

    /**
     * Adds the cleanable resource.
     * 
     * @param cleanable
     *            the cleanable resource
     */
    public static void add(Cleanable cleanable) {
        cleanables.add(cleanable);
    }

    /**
     * Removes the cleanable resource.
     * 
     * @param cleanable
     *            the cleanable resource
     */
    public static void remove(Cleanable cleanable) {
        cleanables.remove(cleanable);
    }

    /**
     * Cleans all resources.
     */
    public static void cleanAll() {
        while (!cleanables.isEmpty()) {
            Cleanable cleanable = cleanables.removeLast();
            try {
                cleanable.clean();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        Introspector.flushCaches();
    }
}