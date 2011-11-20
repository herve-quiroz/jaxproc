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

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.trancecode.xml.saxon.Saxon;
import org.trancecode.xproc.api.PipelineResult;

/**
 * @author Herve Quiroz
 */
public final class TubularPipelineResult extends PipelineResult
{
    private final org.trancecode.xproc.PipelineResult pipelineResult;

    TubularPipelineResult(final org.trancecode.xproc.PipelineResult result)
    {
        this.pipelineResult = result;
    }

    @Override
    public void readDocument(final String portName, final Result result)
    {
        pipelineResult.readNode(portName, result);
    }

    @Override
    public Source readDocument(final String portName)
    {
        return pipelineResult.readNode(portName).asSource();
    }

    @Override
    public Iterable<Source> readDocuments(final String portName)
    {
        return Saxon.asSources(pipelineResult.readNodes(portName));
    }
}
