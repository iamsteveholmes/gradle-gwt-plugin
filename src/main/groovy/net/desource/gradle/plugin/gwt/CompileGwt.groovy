/*
   Copyright 2011 the original author or authors.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.desource.gradle.plugin.gwt

import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.TaskAction

/**
 *
 * @author Markus Kobler
 */
class CompileGwt extends AbstractGwtTask {
    
    static final String COMPILER_CLASSNAME = 'com.google.gwt.dev.Compiler'

    File buildDir
    boolean debug = true
    String style = 'OBF'
    boolean disableClassMetadata
    boolean disableCastChecking
    boolean validateOnly
    boolean draftCompile
    boolean compileReport
    int localWorkers


    def CompileGwt() {
        localWorkers = Runtime.getRuntime().availableProcessors();
    }

    @InputFiles
    Iterable<File> getClasspath() {
        super.getClasspath()
    }

    @OutputDirectory
    File getBuildDir() {
        buildDir
    }

    @TaskAction
    def compileGwt() {

        if( modules == null || modules.size == 0 ) throw new StopActionException("No modules specified");

        configureAntClasspath(getProject().ant, getClasspath())

        Map otherArgs = [
            classpathref: GWT_CLASSPATH_ID,
            classname: COMPILER_CLASSNAME
        ]
      
        ant.java(otherArgs + options.optionMap()) {
            
            options.forkOptions.jvmArgs.each { jvmarg(value: it) }
            options.forkOptions.environment.each {String key, value -> env(key: key, value: value) }
            options.systemProperties.each {String key, value -> sysproperty(key: key, value: value) }

            if (debug) {
                arg(line: '-ea')
            }

            if (validateOnly) arg(line: '-validateOnly')
            if (draftCompile) arg(line: '-draftCompile')
            if (compileReport) arg(line: '-compileReport')
            if (localWorkers > 1) arg(line: "-localWorkers ${localWorkers}")

            if (disableClassMetadata) arg(line: "-disableClassMetadata")
            if (disableCastChecking) arg(line: "-XdisableCastChecking")

            arg(line: "-logLevel ${logLevel}")
            arg(line: "-style ${style}")

            if (genDir) {
                genDir.mkdirs()
                arg(value: "-gen")
                arg(value: "${genDir}")
            }

            if (workDir) {
                workDir.mkdirs()
                arg(value: "-workDir")
                arg(value: "${workDir}")
            }

            if (extraDir) {
                extraDir.mkdirs()
                arg(value: "-extra")
                arg(value: "${extraDir}")
            }

            buildDir.mkdirs()
            arg(value: "-war")            
            arg(value: "${buildDir}")

            modules.each {
                logger.info("Compiling GWT Module {}", it)
                arg(value: it)
            }
        }

    }

}
