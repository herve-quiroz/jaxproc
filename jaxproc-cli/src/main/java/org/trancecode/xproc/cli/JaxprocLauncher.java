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

import com.google.common.base.Preconditions;

import java.net.URI;
import java.util.Map;

import javax.xml.namespace.QName;

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

    private URI libraryUri;
    private Map<QName, Object> options;
    private Map<QName, Object> parameters;
    private Map<String, URI> ports;
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

    @Switch(shortOption = "V", longOption = "version", description = "Print version and exit", exit = true)
    public void printVersion()
    {
        // TODO spawn new classloader
        System.out.println(PipelineFactory.newInstance());
    }

    @Switch(shortOption = "L", longOption = "list-processors", description = "List available XProc processors and exit", exit = true)
    public void listProcessors()
    {
        // TODO
    }

    @Override
    public void run()
    {
        // TODO JaxprocLauncher.run()
        throw new UnsupportedOperationException();
    }

    public static void main(final String[] args) throws Exception
    {
        CommandLineExecutor.execute(JaxprocLauncher.class, args);
    }
}
