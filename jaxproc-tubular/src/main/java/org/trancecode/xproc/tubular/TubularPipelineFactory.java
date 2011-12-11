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

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

import org.trancecode.xproc.PipelineConfiguration;
import org.trancecode.xproc.PipelineProcessor;
import org.trancecode.xproc.Tubular;
import org.trancecode.xproc.api.Pipeline;
import org.trancecode.xproc.api.PipelineFactory;
import org.trancecode.xproc.api.XProcProperties;

/**
 * @author Herve Quiroz
 */
public final class TubularPipelineFactory extends PipelineFactory
{
    @Override
    public Pipeline newPipeline(final Source pipelineSource)
    {
        final PipelineConfiguration configuration = new PipelineConfiguration();
        final URIResolver uriResolver = (URIResolver) getProperties().get(XProcProperties.URI_RESOLVER);
        if (uriResolver != null)
        {
            configuration.setUriResolver(uriResolver);
        }

        final PipelineProcessor processor = new PipelineProcessor(configuration);
        return new TubularPipeline(processor.buildPipeline(pipelineSource).load());
    }

    @Override
    public String getVersion()
    {
        return Tubular.version();
    }

    @Override
    public String getXProcVersion()
    {
        return Tubular.xprocVersion();
    }

    @Override
    public String getXPathVersion()
    {
        return Tubular.xpathVersion();
    }

    @Override
    public String getProductName()
    {
        return Tubular.productName();
    }

    @Override
    public String getVendor()
    {
        return Tubular.vendor();
    }

    @Override
    public String getVendorUri()
    {
        return Tubular.vendorUri();
    }
}
