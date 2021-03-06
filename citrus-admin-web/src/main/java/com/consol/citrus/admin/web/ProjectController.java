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

package com.consol.citrus.admin.web;

import com.consol.citrus.admin.model.Project;
import com.consol.citrus.admin.model.ProjectSettings;
import com.consol.citrus.admin.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Christoph Deppisch
 */
@Controller
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Project getProject() {
        return projectService.getActiveProject();
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    @ResponseBody
    public String getProjectHome() {
        return projectService.getActiveProject() != null ? projectService.getActiveProject().getProjectHome() : "";
    }

    @RequestMapping(value = "/settings", method = RequestMethod.GET)
    @ResponseBody
    public ProjectSettings getProjectSettings() {
        if (projectService.getActiveProject() != null) {
            return projectService.getActiveProject().getSettings();
        }

        return getDefaultProjectSettings();
    }

    @RequestMapping(value = "/settings/default", method = RequestMethod.GET)
    @ResponseBody
    public ProjectSettings getDefaultProjectSettings() {
        return new Project().getSettings();
    }

    @RequestMapping(value = "/recent", method = RequestMethod.GET)
    @ResponseBody
    public String[] getRecentProjects() {
        return projectService.getRecentProjects();
    }

    @RequestMapping(value = "/settings/default", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity saveProjectSettings(@RequestBody ProjectSettings settings) {
        projectService.applySettings(settings);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity open(@RequestBody String projectHome) {
        projectService.load(projectHome);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity update(@RequestBody Project project) {
        projectService.update(project);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/connector/add", method = RequestMethod.GET)
    public ResponseEntity addConnector() {
        projectService.addConnector();
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/connector/remove", method = RequestMethod.GET)
    public ResponseEntity removeConnector() {
        projectService.removeConnector();
        return ResponseEntity.ok().build();
    }
}
