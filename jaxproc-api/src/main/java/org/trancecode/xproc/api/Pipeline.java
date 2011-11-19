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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * An instance of this abstract class can execute an XProc pipeline.
 * 
 * @author Herve Quiroz
 */
public abstract class Pipeline
{
    protected Pipeline()
    {
        // Protected constructor
    }

    public abstract void setParameter(QName name, Object value);

    public void setParameter(final String name, final Object value)
    {
        setParameter(new QName(name), value);
    }

    public void setParameters(final Map<QName, Object> parameters)
    {
        for (final Entry<QName, Object> parameter : parameters.entrySet())
        {
            setParameter(parameter.getKey(), parameter.getValue());
        }
    }

    public abstract void setOption(QName name, Object value);

    public final void setOption(final String name, final Object value)
    {
        setOption(new QName(name), value);
    }

    public void setOptions(final Map<QName, Object> options)
    {
        for (final Entry<QName, Object> option : options.entrySet())
        {
            setParameter(option.getKey(), option.getValue());
        }
    }

    public abstract void bindInputPort(String name, Iterable<Source> sources);

    public void bindInputPort(final String name, final Source... sources)
    {
        bindInputPort(name, Arrays.asList(sources));
    }

    public void bindInputPortToFile(final String name, final File file)
    {
        bindInputPortToFiles(name, file);
    }

    public void bindInputPortToFiles(final String name, final Iterable<File> files)
    {
        final List<Source> sources = new ArrayList<Source>();
        for (final File file : files)
        {
            sources.add(new StreamSource(file));
        }

        bindInputPort(name, sources);
    }

    public void bindInputPortToFiles(final String name, final File... files)
    {
        bindInputPortToFiles(name, Arrays.asList(files));
    }

    public void bindInputPortToResources(final String name, final Iterable<URI> resources)
    {
        final List<Source> sources = new ArrayList<Source>();
        for (final URI resource : resources)
        {
            sources.add(new StreamSource(resource.toASCIIString()));
        }

        bindInputPort(name, sources);
    }

    public void bindInputPortToResources(final String name, final URI... resources)
    {
        bindInputPortToResources(name, Arrays.asList(resources));
    }

    public void bindInputPortToResource(final String name, final URI resource)
    {
        bindInputPort(name, new StreamSource(resource.toASCIIString()));
    }

    public void bindInputPortToResource(final String name, final String resource)
    {
        bindInputPortToResource(name, URI.create(resource));
    }

    public abstract void bindOutputPort(String name, Result result);

    public void bindOutputPort(final String name, final File file)
    {
        bindOutputPort(name, new StreamResult(file));
    }

    public void bindOutputPort(final String name, final URI resource)
    {
        bindOutputPort(name, new StreamResult(resource.toString()));
    }

    public void bindOutputPort(final String name, final String resource)
    {
        bindOutputPort(name, new StreamResult(resource));
    }

    /**
     * @throws XProcException
     */
    public abstract PipelineResult execute();
}
