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

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.trancecode.xproc.RunnablePipeline;
import org.trancecode.xproc.XProcException;
import org.trancecode.xproc.api.Pipeline;
import org.trancecode.xproc.api.PipelineResult;

/**
 * @author Herve Quiroz
 */
public final class TubularPipeline extends Pipeline
{
    private final RunnablePipeline pipeline;
    private final Map<String, Result> outputPortBindings = new HashMap<String, Result>();

    TubularPipeline(final RunnablePipeline pipeline)
    {
        this.pipeline = Preconditions.checkNotNull(pipeline);
    }

    @Override
    public void setParameter(final QName name, final Object value)
    {
        pipeline.withParam(name, value.toString());
    }

    @Override
    public void setOption(final QName name, final Object value)
    {
        pipeline.withOption(name, value.toString());
    }

    @Override
    public void bindInputPort(final String name, final Iterable<Source> sources)
    {
        bindInputPort(name, sources);
    }

    @Override
    public void bindOutputPort(final String name, final Result result)
    {
        outputPortBindings.put(name, result);
    }

    @Override
    public PipelineResult execute()
    {
        final org.trancecode.xproc.PipelineResult result;
        try
        {
            result = pipeline.run();
        }
        catch (final XProcException e)
        {
            throw TubularExceptions.toXProcException(e);
        }

        for (final Entry<String, Result> outputPortBinding : outputPortBindings.entrySet())
        {
            result.readNode(outputPortBinding.getKey(), outputPortBinding.getValue());
        }

        return new TubularPipelineResult(result);
    }
}
