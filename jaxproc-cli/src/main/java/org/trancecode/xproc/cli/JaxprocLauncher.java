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
package org.trancecode.xproc.cli;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.trancecode.logging.Logger;
import org.trancecode.opts.AbstractLog4jLauncher;
import org.trancecode.opts.Argument;
import org.trancecode.opts.Command;
import org.trancecode.opts.Option;
import org.trancecode.opts.Options;
import org.trancecode.xproc.api.Pipeline;
import org.trancecode.xproc.api.PipelineFactory;
import org.trancecode.xproc.api.PipelineResult;

/**
 * @author Herve Quiroz
 */
@Command("java -jar jaxproc.jar")
public final class JaxprocLauncher extends AbstractLog4jLauncher implements Runnable
{
    private static final String VARIABLE_REGEX = "([^=]+)=([^=]+)";
    private static final Logger LOG = Logger.getLogger(JaxprocLauncher.class);
    private static final BiMap<String, String> PIPELINE_FACTORY_CLASS_ALIASES;

    static
    {
        final Builder<String, String> aliases = ImmutableBiMap.builder();
        aliases.put("tubular", "org.trancecode.xproc.tubular.TubularPipelineFactory");
        PIPELINE_FACTORY_CLASS_ALIASES = aliases.build();
    }

    private final List<URL> classpath = Lists.newArrayList();
    private URI libraryUri;
    private final Map<QName, Object> options = Maps.newHashMap();
    private final Map<QName, Object> parameters = Maps.newHashMap();
    private final Map<String, URI> inputPorts = Maps.newHashMap();
    private final Map<String, URI> outputPorts = Maps.newHashMap();
    private URI pipelineUri;

    @Option(shortName = "i", longName = "input-port", description = "Bind an input port to a ressource")
    @Argument(label = "NAME=URI", pattern = "^([^=]+)=([^=]+)$")
    public void bindInputPort(final String port, final URI resource)
    {
        Preconditions.checkArgument(!inputPorts.containsKey(port), "duplicate input port binding: %s = %s", port,
                resource);
        inputPorts.put(port, resource);
    }

    @Option(shortName = "o", longName = "output-port", description = "Bind an input port to a ressource")
    @Argument(label = "NAME=URI", pattern = "^([^=]+)=([^=]+)$")
    public void bindOutputPort(final String port, final URI resource)
    {
        Preconditions.checkArgument(!outputPorts.containsKey(port), "duplicate output port binding: %s = %s", port,
                resource);
        outputPorts.put(port, resource);
    }

    @Option(shortName = "x", longName = "xpl", description = "XProc pipeline to load and run")
    @Argument(label = "URI")
    public void setPipelineUri(final String pipelineUri)
    {
        this.pipelineUri = URI.create(pipelineUri);
    }

    @Option(shortName = "l", longName = "library", description = "XProc pipeline library to load")
    @Argument(label = "URI")
    public void setLibraryUri(final String libraryUri)
    {
        this.libraryUri = URI.create(libraryUri);
    }

    @Option(shortName = "c", longName = "classpath", description = "Add some URL to the classpath", multiple = true)
    @Argument(label = "URL")
    public void addClasspathUrl(final String url)
    {
        try
        {
            classpath.add(new URL(url));
        }
        catch (final MalformedURLException e)
        {
            throw new IllegalArgumentException(url, e);
        }
    }

    @Option(shortName = "O", longName = "option", description = "Passes an option to the pipeline", multiple = true)
    @Argument(label = "KEY=VALUE", pattern = "^([^=]+)=([^=]+)$")
    public void setOption(final QName name, final String value)
    {
        Preconditions.checkState(!options.containsKey(name), "option is set more than once: %s", name);
        options.put(name, value);
    }

    @Option(shortName = "P", longName = "parameter", description = "Passes a parameter to the pipeline", multiple = true)
    @Argument(label = "KEY=VALUE", pattern = "^([^=]+)=([^=]+)$")
    public void setParameter(final QName name, final String value)
    {
        Preconditions.checkState(!parameters.containsKey(name), "parameter is set more than once: %s", name);
        parameters.put(name, value);
    }

    private String getLauncherInformation()
    {
        final URL mavenPropertiesUrl = Resources
                .getResource("META-INF/maven/org.trancecode.xproc.jaxproc/jaxproc-cli/pom.properties");
        final Properties mavenProperties = new Properties();
        try
        {
            mavenProperties.load(mavenPropertiesUrl.openStream());
        }
        catch (final IOException e)
        {
            throw new IllegalStateException(e);
        }
        return String.format("%s version %s", mavenProperties.get("artifactId"), mavenProperties.get("version"));
    }

    @Option(shortName = "V", longName = "version", description = "Print version and exit", exit = true)
    public void printVersion()
    {
        System.out.println(getLauncherInformation());
        setupClassLoader();
        System.out.println(PipelineFactory.newInstance());
    }

    @Option(shortName = "L", longName = "list-processors", description = "List available XProc processors and exit", exit = true)
    public void listProcessors()
    {
        setupClassLoader();

        final Iterable<StringBuilder> factories = Iterables.transform(
                ServiceLoader.load(PipelineFactory.class, getClassLoader()),
                new Function<PipelineFactory, StringBuilder>()
                {
                    @Override
                    public StringBuilder apply(final PipelineFactory factory)
                    {
                        final StringBuilder result = new StringBuilder();
                        result.append(" ** ");
                        final String alias = PIPELINE_FACTORY_CLASS_ALIASES.inverse().get(factory.getClass().getName());
                        if (alias != null)
                        {
                            result.append(alias).append(" = ");
                        }
                        result.append(factory.getClass().getName()).append("\n");
                        result.append(factory);
                        return result;
                    }
                });

        System.out.println(Joiner.on("--------------------").join(factories));
    }

    private static ClassLoader getClassLoader()
    {
        return Thread.currentThread().getContextClassLoader();
    }

    private void setupClassLoader()
    {
        if (classpath.isEmpty())
        {
            return;
        }

        LOG.debug("adding URLs to the classpath:\n  {}", Joiner.on("\n  ").join(classpath));
        final ClassLoader mainClassLoader = Thread.currentThread().getContextClassLoader();
        final URL[] urls = classpath.toArray(new URL[0]);
        final ClassLoader combinedClassLoader = new URLClassLoader(urls, mainClassLoader);
        Thread.currentThread().setContextClassLoader(combinedClassLoader);
    }

    @Override
    public void run()
    {
        setupClassLoader();

        final PipelineFactory factory = PipelineFactory.newInstance();
        final Pipeline pipeline = factory.newPipeline(new StreamSource(pipelineUri.toString()));
        pipeline.setOptions(options);
        pipeline.setParameters(parameters);
        for (final Entry<String, URI> port : inputPorts.entrySet())
        {
            pipeline.bindInputPortToResource(port.getKey(), port.getValue());
        }

        final PipelineResult result = pipeline.execute();

        for (final Entry<String, URI> port : outputPorts.entrySet())
        {
            result.readDocument(port.getKey(), port.getValue());
        }
    }

    public static void main(final String[] args) throws Exception
    {
        Options.execute(JaxprocLauncher.class, args);
    }
}
