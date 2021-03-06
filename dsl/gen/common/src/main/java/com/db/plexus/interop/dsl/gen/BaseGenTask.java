/**
 * Copyright 2017 Plexus Interop Deutsche Bank AG
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.db.plexus.interop.dsl.gen;

import com.db.plexus.interop.dsl.gen.errors.CodeGenerationException;
import com.db.plexus.interop.dsl.gen.util.FileUtils;
import com.google.inject.Inject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class BaseGenTask implements GenTask {

    @Inject
    private XtextResourceSet resourceSet;

    @Inject
    protected IResourceValidator validator;

    protected Logger logger = Logger.getLogger("PlexusCodeGenerator");

    private URI baseUri;
    private URI outDirUri;

    protected String inputFilesGlob(PlexusGenConfig config) {
        return config.getInput();
    }

    public void doGen(PlexusGenConfig config) throws IOException {
        this.baseUri = getBaseDirUri(config);
        this.outDirUri = getOutDirUri(config);
        loadResources(config);
        validateResources();
        doGenWithResources(config, this.resourceSet.getResources());
    }

    protected abstract void doGenWithResources(PlexusGenConfig config, EList<Resource> resources) throws IOException;

    protected URI getBaseDirUri() {
        return this.baseUri;
    }

    protected URI getOutDirUri() {
        return outDirUri;
    }

    protected String getAbsolutePath(String relativePath) {
        return Paths.get(".").resolve(relativePath).toAbsolutePath().toString();
    }

    protected void validateResources() {

        final List<Issue> allIssues = new ArrayList<>();

        for (Resource resource : resourceSet.getResources()) {
            this.logger.info("Loaded resource: " + resource.getURI());
            final List<Issue> issues = validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
            allIssues.addAll(issues);
        }

        final List<Issue> errors = allIssues.stream().filter(issue -> issue.getSeverity() == Severity.ERROR).collect(Collectors.toList());

        if (!errors.isEmpty()) {
            this.logger.severe(validationErrorsMessage(errors));
        }
        for (Issue error : errors) {
            this.logger.severe(error.toString());
        }

        final List<Issue> otherIssues = allIssues.stream().filter(issue -> issue.getSeverity() != Severity.ERROR).collect(Collectors.toList());

        if (!otherIssues.isEmpty()) {
            this.logger.warning(String.format("%d validation warnings found:", otherIssues.size()));
        }
        for (Issue warning : otherIssues) {
            this.logger.warning(warning.toString());
        }
        if (!errors.isEmpty()) {
            throw new CodeGenerationException(validationErrorsMessage(errors));
        }

    }

    private String validationErrorsMessage(List<Issue> errors) {
        return String.format("%d validation errors found in loaded resources", errors.size());
    }

    protected void loadResources(PlexusGenConfig config) throws IOException {
        FileUtils.processFiles(
                config.getBaseDir(),
                this.inputFilesGlob(config),
                (path) -> loadResource(path));
        EcoreUtil2.resolveAll(resourceSet);
    }
    
    private void loadResource(Path path) {
    	resourceSet.getResource(URI.createFileURI(path.toString()), true);
    }

    private URI getBaseDirUri(PlexusGenConfig config) {
        final java.net.URI workingDirUri = Paths.get(".").toAbsolutePath().toUri();
        return URI.createFileURI(config.getBaseDir()).resolve(
                URI.createURI(workingDirUri.toString()));
    }

    private URI getOutDirUri(PlexusGenConfig config) {
        final java.net.URI workingDirUri = Paths.get(".").toAbsolutePath().toUri();
        return URI.createFileURI(config.getOutDir()).resolve(
                URI.createURI(workingDirUri.toString()));
    }

    protected void writeToFile(String outPath, String content) throws IOException {
        this.logger.info("Saving to: " + outPath);
        FileUtils.writeStringToFile(new File(outPath), content);
    }

    protected int generateMessages(List<String> args) throws IOException {
        this.logger.info(String.format("Running compiler with args [%s]", String.join(" ", args)));
        try {
            final int resultCode = new ProcessBuilder(args)
                    .inheritIO()
                    .start()
                    .waitFor();
            if (resultCode != 0) {
                this.logger.log(Level.WARNING, "Compiler has returned non-zero result code " + resultCode);
            }
            return resultCode;
        } catch (Exception e) {
            this.logger.log(Level.WARNING, "Could not generate messages definitions");
            e.printStackTrace();
            return 1;
        }
    }

}
