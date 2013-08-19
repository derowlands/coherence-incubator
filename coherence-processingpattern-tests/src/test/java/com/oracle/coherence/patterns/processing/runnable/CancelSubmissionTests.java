/*
 * File: CancelSubmissionTests.java
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

package com.oracle.coherence.patterns.processing.runnable;

import com.oracle.coherence.common.identifiers.StringBasedIdentifier;

import com.oracle.coherence.patterns.processing.ProcessingSession;
import com.oracle.coherence.patterns.processing.SubmissionOutcome;
import com.oracle.coherence.patterns.processing.SubmissionRetentionPolicy;
import com.oracle.coherence.patterns.processing.SubmissionState;
import com.oracle.coherence.patterns.processing.internal.DefaultProcessingSession;
import com.oracle.coherence.patterns.processing.internal.DefaultSubmissionConfiguration;
import com.oracle.coherence.patterns.processing.misc.AbstractClusterTests;

import com.oracle.tools.runtime.coherence.Cluster;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This is a test that exercises the ability to control
 * the lifecycle of a Submission directly.
 */
public class CancelSubmissionTests extends AbstractClusterTests
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
    public void testCancelSubmission() throws Throwable
    {
        extendClientInit(pofConfig, extendClientCacheConfig);

        ProcessingSession session = null;

        try
        {
            session = new DefaultProcessingSession(StringBasedIdentifier.newInstance("RunnableLifecycleTestSession"));

            System.out.println("Testing CANCELLING SUBMISSIONS");

            String taskName = "RunnableToCancel";
            SubmissionOutcome outcome = session.submit(new DoNothingRunnable(15000),
                                                       new DefaultSubmissionConfiguration(null),
                                                       StringBasedIdentifier.newInstance(taskName),
                                                       SubmissionRetentionPolicy.ExplicitRemove,
                                                       null);

            System.out.println("Wait a second before CANCELLING submission");

            Thread.currentThread();
            Thread.sleep(1000);
            session.cancelSubmission(outcome.getIdentifier());
            outcome.get();
            System.out.println("After get:" + outcome.getSubmissionState());
            assertTrue(outcome.getSubmissionState() == SubmissionState.CANCELLED);
            System.out.println("Waiting 3 seconds to complete cancelling of job");
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
