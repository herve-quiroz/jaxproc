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

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;

/**
 * Result of a pipeline execution.
 * 
 * @author Herve Quiroz
 */
public abstract class PipelineResult
{
    protected PipelineResult()
    {
        // Protected constructor
    }

    public abstract void readDocument(String portName, Result result);

    public void readDocument(final String name, final File file)
    {
        readDocument(name, new StreamResult(file));
    }

    public void readDocument(final String name, final URI resource)
    {
        readDocument(name, new StreamResult(resource.toString()));
    }

    public void readDocument(final String name, final String resource)
    {
        readDocument(name, new StreamResult(resource));
    }

    public abstract Source readDocument(String portName);

    public abstract Iterable<Source> readDocuments(String portName);
}
