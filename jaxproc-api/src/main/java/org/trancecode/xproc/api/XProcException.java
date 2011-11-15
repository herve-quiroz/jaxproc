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

import javax.xml.namespace.QName;
import javax.xml.stream.Location;

/**
 * @author Herve Quiroz
 */
public final class XProcException extends PipelineException
{
    private static final long serialVersionUID = -4241443703311041795L;

    public static enum Type {
        STATIC
        {
            @Override
            public String toString()
            {
                return "XS";
            }
        },
        DYNAMIC
        {
            @Override
            public String toString()
            {
                return "XD";
            }
        },
        STEP
        {
            @Override
            public String toString()
            {
                return "XC";
            }
        }
    };

    private final Type type;
    private final int code;
    private final Location location;
    private final QName name;

    public XProcException(final Type type, final int code, final Location location, final String message,
            final Object... parameters)
    {
        this(new QName("http://www.w3.org/ns/xproc-error", getLabel(type, code), "err"), type, code, location, message,
                parameters);
    }

    private XProcException(final QName name, final Type type, final int code, final Location location,
            final String message, final Object... parameters)
    {
        super(message, parameters);

        this.type = type;
        this.code = code;
        this.location = location;
        this.name = name;
    }

    public QName getName()
    {
        return name;
    }

    public int getCode()
    {
        return code;
    }

    public Location getLocation()
    {
        return location;
    }

    private static String getLabel(final Type type, final int code)
    {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(type);
        buffer.append(Integer.toString(10000 + code).substring(1));

        return buffer.toString();
    }

    public String getLabel()
    {
        return getLabel(type, code);
    }

    @Override
    public String getMessage()
    {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(getLabel());
        if (getLocation() != null)
        {
            buffer.append(" @ ").append(getLocation());
        }
        buffer.append(": ");
        buffer.append(super.getMessage());
        return buffer.toString();
    }

    public XProcException withCause(final Throwable cause)
    {
        this.initCause(cause);
        return this;
    }
}
