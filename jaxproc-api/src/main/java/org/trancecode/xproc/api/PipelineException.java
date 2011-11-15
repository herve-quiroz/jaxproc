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

/**
 * Base {@link Exception} class.
 * 
 * @author Herve Quiroz
 */
public class PipelineException extends RuntimeException
{
    private static final long serialVersionUID = -6526395636375195539L;

    public PipelineException()
    {
        super();
    }

    public PipelineException(final String message, final Object... parameters)
    {
        super(format(message, parameters));
    }

    public PipelineException(final Throwable cause)
    {
        super(cause);
    }

    public PipelineException(final Throwable cause, final String message, final Object... parameters)
    {
        super(format(message, parameters), cause);
    }

    protected static String format(final String message, final Object... parameters)
    {
        if (parameters == null || parameters.length == 0)
        {
            return message;
        }

        try
        {
            return String.format(message, parameters);
        }
        catch (final Exception e)
        {
            return message;
        }
    }
}
