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
import java.util.Properties;
import java.util.ServiceLoader;

import javax.xml.namespace.QName;

import org.trancecode.logging.Logger;
import org.trancecode.opts.Command;
import org.trancecode.opts.CommandLineExecutor;
import org.trancecode.opts.Flag;
import org.trancecode.opts.Switch;
import org.trancecode.xproc.api.PipelineFactory;

/**
 * @author Herve Quiroz
 */
@Command("java -jar jaxproc.jar")
public final class JaxprocLauncher implements Runnable
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
    private final Map<String, URI> ports = Maps.newHashMap();
    private URI pipelineUri;

    @Flag(shortOption = "x", longOption = "xpl", description = "XProc pipeline to load and run")
    public void setPipelineUri(final String pipelineUri)
    {
        this.pipelineUri = URI.create(pipelineUri);
    }

    @Flag(shortOption = "l", longOption = "library", description = "XProc pipeline library to load")
    public void setLibraryUri(final String libraryUri)
    {
        this.libraryUri = URI.create(libraryUri);
    }

    @Flag(shortOption = "c", longOption = "classpath", description = "Add some URL to the classpath")
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

    @Flag(shortOption = "o", longOption = "option", description = "Passes an option to the pipeline")
    public void setOption(final String option)
    {
        Preconditions.checkArgument(option.matches(VARIABLE_REGEX), "option does not match <name=value> pattern: %s",
                option);
        final QName name = QName.valueOf(option.replaceAll(VARIABLE_REGEX, "$1"));
        final String value = option.replaceAll(VARIABLE_REGEX, "$2");
        Preconditions.checkState(!options.containsKey(name), "option is set more than once: %s", name);
        options.put(name, value);
    }

    @Flag(shortOption = "p", longOption = "parameter", description = "Passes a parameter to the pipeline")
    public void setParameter(final String parameter)
    {
        Preconditions.checkArgument(parameter.matches(VARIABLE_REGEX),
                "parameter does not match <name=value> pattern: %s", parameter);
        final QName name = QName.valueOf(parameter.replaceAll(VARIABLE_REGEX, "$1"));
        final String value = parameter.replaceAll(VARIABLE_REGEX, "$2");
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

    @Switch(shortOption = "V", longOption = "version", description = "Print version and exit", exit = true)
    public void printVersion()
    {
        System.out.println(getLauncherInformation());
        setupClassLoader();
        System.out.println(PipelineFactory.newInstance());
    }

    @Switch(shortOption = "L", longOption = "list-processors", description = "List available XProc processors and exit", exit = true)
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

        // TODO JaxprocLauncher.run()
        throw new UnsupportedOperationException();
    }

    public static void main(final String[] args) throws Exception
    {
        CommandLineExecutor.execute(JaxprocLauncher.class, args);
    }
}
