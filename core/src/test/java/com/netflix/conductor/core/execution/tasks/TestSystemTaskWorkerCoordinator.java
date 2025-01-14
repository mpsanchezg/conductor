/*
 *  Copyright 2021 Netflix, Inc.
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations under the License.
 */
package com.netflix.conductor.core.execution.tasks;

import com.netflix.conductor.core.config.ConductorProperties;
import com.netflix.conductor.core.execution.AsyncSystemTaskExecutor;
import com.netflix.conductor.dao.QueueDAO;
import com.netflix.conductor.service.ExecutionService;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestSystemTaskWorkerCoordinator {

    private static final String TEST_QUEUE = "test";
    private static final String EXECUTION_NAMESPACE_CONSTANT = "@exeNS";
    private static final String ISOLATION_CONSTANT = "-iso";

    private QueueDAO queueDAO;
    private AsyncSystemTaskExecutor asyncSystemTaskExecutor;
    private ExecutionService executionService;
    private ConductorProperties properties;

    @Before
    public void setUp() {
        queueDAO = mock(QueueDAO.class);
        asyncSystemTaskExecutor = mock(AsyncSystemTaskExecutor.class);
        executionService = mock(ExecutionService.class);
        properties = mock(ConductorProperties.class);
        when(properties.getSystemTaskWorkerPollInterval()).thenReturn(Duration.ofMillis(50));
        when(properties.getSystemTaskWorkerExecutionNamespace()).thenReturn("");
    }

    @Test
    public void isSystemTask() {
        createTaskMapping();
        SystemTaskWorkerCoordinator systemTaskWorkerCoordinator = new SystemTaskWorkerCoordinator(queueDAO,
                asyncSystemTaskExecutor, properties, executionService, Collections.emptySet());
        assertTrue(systemTaskWorkerCoordinator.isAsyncSystemTask(TEST_QUEUE + ISOLATION_CONSTANT));
    }

    @Test
    public void isSystemTaskNotPresent() {
        createTaskMapping();
        SystemTaskWorkerCoordinator systemTaskWorkerCoordinator = new SystemTaskWorkerCoordinator(queueDAO,
                asyncSystemTaskExecutor, properties, executionService, Collections.emptySet());
        assertFalse(systemTaskWorkerCoordinator.isAsyncSystemTask(null));
    }

    @Test
    public void testIsFromCoordinatorExecutionNameSpace() {
        doReturn("exeNS").when(properties).getSystemTaskWorkerExecutionNamespace();
        SystemTaskWorkerCoordinator systemTaskWorkerCoordinator = new SystemTaskWorkerCoordinator(queueDAO,
                asyncSystemTaskExecutor, properties, executionService, Collections.emptySet());
        assertTrue(
            systemTaskWorkerCoordinator.isFromCoordinatorExecutionNameSpace(TEST_QUEUE + EXECUTION_NAMESPACE_CONSTANT));
    }

    private void createTaskMapping() {
        WorkflowSystemTask mockWorkflowTask = mock(WorkflowSystemTask.class);
        when(mockWorkflowTask.getTaskType()).thenReturn(TEST_QUEUE);
        when(mockWorkflowTask.isAsync()).thenReturn(true);
        SystemTaskWorkerCoordinator.taskNameWorkflowTaskMapping.put(TEST_QUEUE, mockWorkflowTask);
    }
}
