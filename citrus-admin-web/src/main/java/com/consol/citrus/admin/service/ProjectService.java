/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.admin.service;

import com.consol.citrus.Citrus;
import com.consol.citrus.admin.Application;
import com.consol.citrus.admin.connector.WebSocketPushMessageListener;
import com.consol.citrus.admin.exception.ApplicationRuntimeException;
import com.consol.citrus.admin.model.Project;
import com.consol.citrus.admin.model.ProjectSettings;
import com.consol.citrus.admin.model.spring.Property;
import com.consol.citrus.admin.model.spring.SpringBean;
import com.consol.citrus.admin.service.spring.SpringBeanService;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.xml.xpath.XPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;

import javax.annotation.PostConstruct;
import javax.xml.xpath.XPathConstants;
import java.io.*;
import java.util.*;

/**
 * @author Christoph Deppisch
 */
@Service
public class ProjectService {

    @Autowired
    private FileBrowserService fileBrowserService;

    /** Current project actively opened in Citrus admin */
    private Project project;

    /** Holds in memory list of recently opened projects */
    private List<String> recentlyOpened = new ArrayList<>();

    /** Spring bean service*/
    @Autowired
    private SpringBeanService springBeanService;

    @Autowired
    private Environment environment;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ProjectService.class);

    @PostConstruct
    public void loadDefaultProject() throws Exception {
        if (project == null && StringUtils.hasText(System.getProperty(Application.PROJECT_HOME))) {
            load(System.getProperty(Application.PROJECT_HOME));
        }
    }

    /**
     * Loads Citrus project from project home.
     * @param projectHomeDir
     * @return
     */
    public void load(String projectHomeDir) {
        Project project = new Project(projectHomeDir);
        if (project.getProjectInfoFile().exists()) {
            project.loadSettings();
        } else if (!validateProject(project)) {
            throw new ApplicationRuntimeException("Invalid project home - not a proper Citrus project");
        }

        if (project.isMavenProject()) {
            try {
                String pomXml = FileUtils.readToString(new FileSystemResource(project.getMavenPomFile()));
                SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
                nsContext.bindNamespaceUri("mvn", "http://maven.apache.org/POM/4.0.0");

                Document pomDoc = XMLUtils.parseMessagePayload(pomXml);
                project.setName(evaluate(pomDoc, "/mvn:project/mvn:artifactId", nsContext));

                String version = evaluate(pomDoc, "/mvn:project/mvn:version", nsContext);
                String parentVersion = evaluate(pomDoc, "/mvn:project/mvn:parent/mvn:version", nsContext);
                if (StringUtils.hasText(version)) {
                    project.setVersion(version);
                } else if (StringUtils.hasText(parentVersion)) {
                    project.setVersion(parentVersion);
                }

                project.getSettings().setBasePackage(evaluate(pomDoc, "/mvn:project/mvn:groupId", nsContext));

                String citrusVersion = evaluate(pomDoc, "/mvn:project/mvn:properties/mvn:citrus.version", nsContext);
                if (StringUtils.hasText(citrusVersion)) {
                    project.getSettings().setCitrusVersion(citrusVersion);
                }

                project.setDescription(evaluate(pomDoc, "/mvn:project/mvn:description", nsContext));

                project.getSettings().setConnectorActive(StringUtils.hasText(evaluate(pomDoc,
                        "/mvn:project/mvn:dependencies/mvn:dependency/mvn:artifactId[. = 'citrus-admin-connector']", nsContext)));
            } catch (IOException e) {
                throw new ApplicationRuntimeException("Unable to open Maven pom.xml file", e);
            }
        } else if (project.isAntProject()) {
            try {
                String buildXml = FileUtils.readToString(new FileSystemResource(project.getAntBuildFile()));
                SimpleNamespaceContext nsContext = new SimpleNamespaceContext();

                Document buildDoc = XMLUtils.parseMessagePayload(buildXml);
                project.setName(evaluate(buildDoc, "/project/@name", nsContext));

                String citrusVersion = evaluate(buildDoc, "/project/property[@name='citrus.version']/@value", nsContext);
                if (StringUtils.hasText(citrusVersion)) {
                    project.getSettings().setCitrusVersion(citrusVersion);
                }

                project.setDescription(evaluate(buildDoc, "/project/@description", nsContext));
            } catch (IOException e) {
                throw new ApplicationRuntimeException("Unable to open Apache Ant build.xml file", e);
            }
        }

        saveProject(project);

        this.project = project;
        if (!this.recentlyOpened.contains(projectHomeDir)) {
            if (projectHomeDir.endsWith("/")) {
                this.recentlyOpened.add(projectHomeDir.substring(0, projectHomeDir.length() - 1));
            } else {
                this.recentlyOpened.add(projectHomeDir);
            }
        }
        System.setProperty(Application.PROJECT_HOME, projectHomeDir);
    }

    /**
     * Returns the project's Spring application context config file.
     * @return the config file or null if no config file exists within the selected project.
     */
    public File getProjectContextConfigFile() {
        return fileBrowserService.findFileInPath(new File(project.getProjectHome()), getDefaultConfigurationFile(), true);
    }

    /**
     * Save project settings.
     */
    public void update(Project project) {
        this.project.setDescription(project.getDescription());
        this.project.setSettings(project.getSettings());
        saveProject(this.project);
    }

    /**
     * Save project to file system.
     * @param project
     */
    public void saveProject(Project project) {
        try (FileOutputStream fos = new FileOutputStream(project.getProjectInfoFile())) {
            fos.write(Jackson2ObjectMapperBuilder.json().build().writer().withDefaultPrettyPrinter().writeValueAsBytes(project));
            fos.flush();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Unable to open project info file", e);
        }
    }

    /**
     * Reads default Citrus project property file for active project.
     * @return properties loaded or empty properties if nothing is found
     */
    public Properties getProjectProperties() {
        File projectProperties = fileBrowserService.findFileInPath(new File(project.getProjectHome()), "citrus.properties", true);

        try {
            if (projectProperties != null) {
                return PropertiesLoaderUtils.loadProperties(new FileSystemResource(projectProperties));
            }
        } catch (IOException e) {
            log.warn("Unable to read default Citrus project properties from file resource", e);
        }

        return new Properties();
    }

    /**
     * Evaluates Xpath expression on document and returns null safe result value as String representation.
     * @param document
     * @param expression
     * @param nsContext
     * @return
     */
    private String evaluate(Document document, String expression, SimpleNamespaceContext nsContext) {
        Object result = XPathUtils.evaluateExpression(document, expression, nsContext, XPathConstants.STRING);
        return result != null ? result.toString() : "";
    }

    /**
     * Checks if home directory is valid Citrus project.
     *
     * @param project
     */
    public boolean validateProject(Project project) {
        File homeDir = new File(project.getProjectHome());

        try {
            Assert.isTrue(homeDir.exists(), "Invalid project home directory");
            Assert.isTrue(new File(homeDir, project.getSettings().getJavaSrcDirectory()).exists(), "Missing Java source directory");
            Assert.isTrue(new File(homeDir, project.getSettings().getXmlSrcDirectory()).exists(), "Missing resources directory");
            Assert.isTrue(new File(homeDir, project.getSettings().getSpringApplicationContext()).exists(), "Missing Spring application context file");
        } catch (IllegalArgumentException e) {
            log.warn("Project home validation failed: " + e.getLocalizedMessage());
            return false;
        }

        return true;
    }

    /**
     * Gets file name from path pattern property.
     * @return
     */
    private String getDefaultConfigurationFile() {
        String configurationFile = Citrus.DEFAULT_APPLICATION_CONTEXT;

        if (configurationFile.startsWith("classpath*:")) {
            configurationFile = configurationFile.substring("classpath*:".length());
        }

        if (configurationFile.startsWith("classpath:")) {
            configurationFile = configurationFile.substring("classpath:".length());
        }

        return configurationFile;
    }

    /**
     * Gets the currently active project.
     * @return
     */
    public Project getActiveProject() {
        return project;
    }

    /**
     * Sets the project property.
     *
     * @param project
     */
    public void setActiveProject(Project project) {
        this.project = project;
    }

    /**
     * Adds the citrus admin connector dependency to the target project Maven POM.
     */
    public void addConnector() {
        if (project.isMavenProject()) {
            try {
                String pomXml = FileUtils.readToString(new FileSystemResource(project.getMavenPomFile()));

                if (!pomXml.contains("<artifactId>citrus-admin-connector</artifactId>")) {
                    pomXml = pomXml.replaceAll("</dependencies>", "  <dependency>" + System.lineSeparator() +
                            "      <groupId>com.consol.citrus</groupId>" + System.lineSeparator() +
                            "      <artifactId>citrus-admin-connector</artifactId>" + System.lineSeparator() +
                            "      <version>1.0.0-beta-4</version>" + System.lineSeparator() +
                            "    </dependency>" + System.lineSeparator() +
                            "  </dependencies>");

                    FileUtils.writeToFile(pomXml, new FileSystemResource(project.getMavenPomFile()).getFile());
                }

                project.getSettings().setUseConnector(true);
                project.getSettings().setConnectorActive(true);
                saveProject(project);

                if (springBeanService.getBeanDefinition(getProjectContextConfigFile(), getActiveProject(), WebSocketPushMessageListener.class.getSimpleName(), SpringBean.class) == null) {
                    SpringBean pushMessageListener = new SpringBean();
                    pushMessageListener.setId(WebSocketPushMessageListener.class.getSimpleName());
                    pushMessageListener.setClazz(WebSocketPushMessageListener.class.getName());

                    if (!environment.getProperty("local.server.port", "8080").equals("8080")) {
                        Property portProperty = new Property();
                        portProperty.setName("port");
                        portProperty.setValue(environment.getProperty("local.server.port"));
                        pushMessageListener.getProperties().add(portProperty);
                    }
                    springBeanService.addBeanDefinition(getProjectContextConfigFile(), getActiveProject(), pushMessageListener);
                }
            } catch (IOException e) {
                throw new ApplicationRuntimeException("Failed to add admin connector dependency to Maven pom.xml file", e);
            }
        }
    }

    /**
     * Removes the citrus admin connector dependency from the target project Maven POM.
     */
    public void removeConnector() {
        if (project.isMavenProject()) {
            try {
                String pomXml = FileUtils.readToString(new FileSystemResource(project.getMavenPomFile()));

                pomXml = pomXml.replaceAll("\\s*<dependency>[\\s\\n\\r]*<groupId>com\\.consol\\.citrus</groupId>[\\s\\n\\r]*<artifactId>citrus-admin-connector</artifactId>[\\s\\n\\r]*<version>.*</version>[\\s\\n\\r]*</dependency>", "");
                pomXml = pomXml.replaceAll("\\s*<dependency>[\\s\\n\\r]*<groupId>com\\.consol\\.citrus</groupId>[\\s\\n\\r]*<artifactId>citrus-admin-connector</artifactId>[\\s\\n\\r]*</dependency>", "");

                FileUtils.writeToFile(pomXml, new FileSystemResource(project.getMavenPomFile()).getFile());

                project.getSettings().setUseConnector(false);
                project.getSettings().setConnectorActive(false);
                saveProject(project);

                List<String> listenerBeans = springBeanService.getBeanNames(getProjectContextConfigFile(), getActiveProject(), WebSocketPushMessageListener.class.getName());
                for (String listenerBean : listenerBeans) {
                    springBeanService.removeBeanDefinition(getProjectContextConfigFile(), getActiveProject(), listenerBean);
                }
            } catch (IOException e) {
                throw new ApplicationRuntimeException("Failed to add admin connector dependency to Maven pom.xml file", e);
            }
        }
    }

    /**
     * Sets new settings as defaults.
     * @param settings
     */
    public void applySettings(ProjectSettings settings) {
        System.setProperty(Application.JAVA_SRC_DIRECTORY, settings.getJavaSrcDirectory());
        System.setProperty(Application.XML_SRC_DIRECTORY, settings.getXmlSrcDirectory());
        System.setProperty(Application.SPRING_APPLICATION_CONTEXT, settings.getSpringApplicationContext());
    }

    /**
     * Sets the fileBrowserService property.
     *
     * @param fileBrowserService
     */
    public void setFileBrowserService(FileBrowserService fileBrowserService) {
        this.fileBrowserService = fileBrowserService;
    }

    /**
     * Sets the springBeanService property.
     *
     * @param springBeanService
     */
    public void setSpringBeanService(SpringBeanService springBeanService) {
        this.springBeanService = springBeanService;
    }

    /**
     * Sets the environment property.
     *
     * @param environment
     */
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * Gets the recently opened projects.
     * @return
     */
    public String[] getRecentProjects() {
        return recentlyOpened.toArray(new String[recentlyOpened.size()]);
    }
}
