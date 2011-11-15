/*
 * Copyright 2011 Herve Quiroz
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.trancecode.xproc.api;

import java.util.Iterator;

/**
 * Utility methods related to {@link Iterable}.
 * 
 * @author Herve Quiroz
 */
final class Iterables
{
    private Iterables()
    {
        // No instantiation
    }

    public static <T> T getFirst(final Iterable<T> sequence, final T defaultValue)
    {
        final Iterator<T> iterator = sequence.iterator();
        if (iterator.hasNext())
        {
            return iterator.next();
        }

        return defaultValue;
    }
}
