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

import com.consol.citrus.admin.model.*;
import com.consol.citrus.admin.process.ProcessMonitor;
import com.consol.citrus.admin.service.executor.TestExecutor;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author Christoph Deppisch
 */
@Service
public class TestExecutionService {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TestExecutionService.class);

    @Autowired
    private ProcessMonitor processMonitor;

    @Autowired
    private TestExecutor testExecutor;

    /**
     * Runs a test case and returns result outcome (success or failure).
     * @param project
     * @param test
     * @return
     */
    public TestResult execute(Project project, Test test) {
        TestResult result = new TestResult();
        result.setTest(test);

        try {
            String processId = testExecutor.execute(test, project);

            result.setProcessId(processId);
            result.setSuccess(true);
        } catch (Exception e) {
            log.warn("Failed to execute Citrus test case '" + test.getName() + "'", e);
            setFailureStack(result, e);
        }

        return result;
    }

    /**
     * Stops process with given id.
     * @param processId
     */
    public void stop(String processId) {
        processMonitor.stopProcess(processId);
    }

    /**
     * Adds failure stack information to test result.
     * @param result
     * @param e
     */
    private void setFailureStack(TestResult result, Exception e) {
        result.setSuccess(false);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(os));
        result.setStackTrace("Caused by: " + os.toString());

        if (e instanceof CitrusRuntimeException) {
            result.setFailureStack(((CitrusRuntimeException)e).getFailureStackAsString());
        }
    }
}
