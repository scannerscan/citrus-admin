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

package com.consol.citrus.admin.service.report;

import com.consol.citrus.admin.model.Project;
import com.consol.citrus.admin.model.TestReport;

/**
 * @author Christoph Deppisch
 */
public interface TestReportService {

    /**
     * Loads latest test results and creates report.
     * @return
     */
    TestReport getLatest(Project activeProject);

    /**
     * Checks whether test results are available in active project.
     * @param activeProject
     * @return
     */
    boolean hasTestResults(Project activeProject);

}
