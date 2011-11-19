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
package org.trancecode.xproc.tubular;

import org.trancecode.xproc.api.XProcException;
import org.trancecode.xproc.api.XProcException.Type;

/**
 * Utility methods related to {@link Exception}.
 * 
 * @author Herve Quiroz
 */
public final class TubularExceptions
{
    private TubularExceptions()
    {
        // No instantiation
    }

    public static Type toXProcType(final org.trancecode.xproc.XProcException.Type type)
    {
        if (type == org.trancecode.xproc.XProcException.Type.DYNAMIC)
        {
            return Type.DYNAMIC;
        }

        if (type == org.trancecode.xproc.XProcException.Type.STATIC)
        {
            return Type.STATIC;
        }

        if (type == org.trancecode.xproc.XProcException.Type.STEP)
        {
            return Type.STEP;
        }

        throw new IllegalStateException(type.toString());
    }

    public static XProcException toXProcException(final org.trancecode.xproc.XProcException tubularException)
    {
        return new XProcException(toXProcType(tubularException.getType()), tubularException.getCode(),
                tubularException.getLocation(), tubularException.getMessage()).withCause(tubularException);
    }
}
