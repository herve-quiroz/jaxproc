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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;

/**
 * A PipelineFactory can be used to create {@link Pipeline} objects.
 * <p>
 * It is similar in its approach to the {@link TransformerFactory} pattern.
 * 
 * @see TransformerFactory
 * @author Herve Quiroz
 */
public abstract class PipelineFactory
{
    private final Map<String, Object> properties = new HashMap<String, Object>();
    private final Map<String, Object> unmodifiableProperties = Collections.unmodifiableMap(properties);

    protected PipelineFactory()
    {
        // Protected constructor
    }

    public static final PipelineFactory newInstance()
    {
        final PipelineFactory instance = Iterables.getFirst(ServiceLoader.load(PipelineFactory.class), null);
        if (instance == null)
        {
            throw new IllegalStateException(String.format("no available %s implementation on the classpath",
                    PipelineFactory.class.getName()));
        }

        return instance;
    }

    public static final PipelineFactory newInstance(final String className, final ClassLoader classLoader)
    {
        try
        {
            @SuppressWarnings("unchecked")
            final Class<PipelineFactory> factoryClass = (Class<PipelineFactory>) Class.forName(className, true,
                    classLoader);
            return factoryClass.newInstance();
        }
        catch (final InstantiationException e)
        {
            throw new IllegalStateException("cannot instantiate a new instance of class " + className, e);
        }
        catch (final IllegalAccessException e)
        {
            throw new IllegalStateException("cannot instantiate a new instance of class " + className, e);
        }
        catch (final ClassNotFoundException e)
        {
            throw new IllegalArgumentException("no such class: " + className, e);
        }
    }

    protected final Map<String, Object> getProperties()
    {
        return unmodifiableProperties;
    }

    public final void setProperty(final String property, final Object value)
    {
        properties.put(property, value);
    }

    public final void setProperties(final Map<String, Object> properties)
    {
        this.properties.putAll(properties);
    }

    /**
     * @throws XProcException
     */
    public abstract Pipeline newPipeline(Source pipelineSource);

    public abstract String getVersion();

    public abstract String getXProcVersion();

    public abstract String getXPathVersion();

    public abstract String getProductName();

    public abstract String getVendor();

    public abstract String getVendorUri();

    @Override
    public final String toString()
    {
        return String.format("%s version %s (c) %s\n%s\nXProc version: %s\nXPath version: %s", getProductName(),
                getVersion(), getVendor(), getVendorUri(), getXProcVersion(), getXPathVersion());
    }
}
