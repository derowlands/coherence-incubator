/*
 * File: CancelTaskTests.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.oracle.coherence.patterns.processing.task;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.oracle.coherence.common.identifiers.StringBasedIdentifier;
import com.oracle.coherence.patterns.processing.ProcessingSession;
import com.oracle.coherence.patterns.processing.SubmissionOutcome;
import com.oracle.coherence.patterns.processing.SubmissionRetentionPolicy;
import com.oracle.coherence.patterns.processing.SubmissionState;
import com.oracle.coherence.patterns.processing.internal.DefaultProcessingSession;
import com.oracle.coherence.patterns.processing.internal.DefaultSubmissionConfiguration;
import com.oracle.coherence.patterns.processing.misc.AbstractClusterTests;
import com.oracle.tools.runtime.coherence.Cluster;

/**
 * This is a test that exercises canceling of a task
 *
 * @author Christer Fahlgren
 */
public class CancelTaskTests extends AbstractClusterTests
{
    /**
     * {@inheritDoc}
     */
    public Cluster startCluster()
    {
        return startTestCluster(gridCacheConfig, pofConfig, 0, 1, true);
    }


    /**
     * A test that checks that a Runnable can be canceled after submission.
     */
    @Test
    public void testCancelTask() throws Throwable
    {
        extendClientInit(pofConfig, extendClientCacheConfig);

        ProcessingSession session = null;

        try
        {
            session =
                new DefaultProcessingSession(StringBasedIdentifier.newInstance("RunnableLifecycleTestSession"));

            System.out.println("Testing CANCELLING TASK");
            String taskName = "TaskToCancel";
            SubmissionOutcome outcome = session.submit(new CancelableTask(6000),
                                                       new DefaultSubmissionConfiguration(null),
                                                       StringBasedIdentifier.newInstance(taskName),
                                                       SubmissionRetentionPolicy.ExplicitRemove,
                                                       null);

            System.out.println("Wait a second before CANCELLING Task");
            assertTrue(session.submissionExists(outcome.getIdentifier()));
            Thread.currentThread();
            Thread.sleep(500);
            session.cancelSubmission(outcome.getIdentifier());
            outcome.get();
            System.out.println("After get:" + outcome.getSubmissionState());
            assertTrue(outcome.getSubmissionState() == SubmissionState.CANCELLED);
            System.out.println("Waiting 3 seconds to complete cancelling of task");
            Thread.currentThread();
            Thread.sleep(3000);

        }
        catch (Throwable t)
        {
            System.out.println("Unexpected Exception in " + getClass().getName());
            t.printStackTrace(System.out);
            fail();
        }
        finally
        {
            if (session != null)
            {
                System.out.println("Finally - shutting down cluster");
                session.shutdown();
            }
        }
    }
}
