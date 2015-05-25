package com.xti.jenkins.plugin.awslambda.service;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.util.OneShotEvent;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WorkSpaceZipperTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testGetZipWithZip() throws Exception {
        final OneShotEvent buildEnded = new OneShotEvent();

        FreeStyleProject p = j.createFreeStyleProject();
        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                                   BuildListener listener) throws InterruptedException, IOException {
                build.getWorkspace().child("echo.zip").copyFrom(new FileInputStream(getResource("echo.zip")));
                buildEnded.signal();
                return true;
            }
        });

        p.scheduleBuild2(0);
        buildEnded.block();

        JenkinsLogger logger = new JenkinsLogger(System.out);
        WorkSpaceZipper workSpaceZipper = new WorkSpaceZipper(p.getSomeWorkspace(), logger);
        File zip = workSpaceZipper.getZip("echo.zip");

        assertTrue(zip.exists());
        assertFalse(zip.getAbsolutePath().contains("aws-lambda"));

        ZipFile zipFile = new ZipFile(zip);
        assertNotNull(zipFile);
        assertNotNull(zipFile.getEntry("index.js"));
    }

    @Test
    public void testGetZipWithFolder() throws Exception {
        final OneShotEvent buildEnded = new OneShotEvent();

        FreeStyleProject p = j.createFreeStyleProject();
        p.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                                   BuildListener listener) throws InterruptedException, IOException {
                build.getWorkspace().child("echo").child("index.js").copyFrom(new FileInputStream(getResource("echo/index.js")));
                buildEnded.signal();
                return true;
            }
        });

        p.scheduleBuild2(0);
        buildEnded.block();

        JenkinsLogger logger = new JenkinsLogger(System.out);
        WorkSpaceZipper workSpaceZipper = new WorkSpaceZipper(p.getSomeWorkspace(), logger);
        File zip = workSpaceZipper.getZip("echo");

        assertTrue(zip.exists());
        assertFalse(zip.getAbsolutePath().contains("aws-lambda"));

        ZipFile zipFile = new ZipFile(zip);
        assertNotNull(zipFile);
        assertNotNull(zipFile.getEntry("index.js"));
    }

    private File getResource(String resourcePath){
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(resourcePath);
        if(resource != null){
            return new File(resource.getFile());
        } else {
            throw new IllegalStateException("Could not load " + resourcePath);
        }
    }
}