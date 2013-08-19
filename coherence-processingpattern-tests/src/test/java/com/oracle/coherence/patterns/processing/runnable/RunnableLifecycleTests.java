/*
 * File: RunnableLifecycleTests.java
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

import com.tangosol.io.pof.PortableException;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This is a test that exercises the ability to control
 * the lifecycle of a Submission directly.
 *
 * @author David Rowlands
 */
public class RunnableLifecycleTests extends AbstractClusterTests
{
    /**
     * {@inheritDoc}
     */
    public Cluster startCluster()
    {
        return startTestCluster(gridCacheConfig, pofConfig, 0, 1, true);
    }


    /**
     * A test that checks that runnables with no result are removed with RemoveOnFinalState policy.
     */
    @Test
    public void testRunnableNoResultLifecycle() throws Throwable
    {
        extendClientInit(pofConfig, extendClientCacheConfig);

        ProcessingSession session = null;

        try
        {
            session = new DefaultProcessingSession(StringBasedIdentifier.newInstance("RunnableLifecycleTestSession"));

            String taskName = "RunnableLifecycle";
            SubmissionOutcome outcome = session.submit(new DoNothingRunnable(2000),
                                                       new DefaultSubmissionConfiguration(null),
                                                       StringBasedIdentifier.newInstance(taskName),
                                                       SubmissionRetentionPolicy.RemoveOnFinalState,
                                                       null);

            assertTrue(session.submissionExists(outcome.getIdentifier()));
            Thread.currentThread();
            Thread.sleep(5000);

            // After having waited for one second, the runnable should have run and have been removed.
            // Now we submit a second identically named runnable
            session.submit(new DoNothingRunnable(),
                           new DefaultSubmissionConfiguration(null),
                           StringBasedIdentifier.newInstance(taskName),
                           SubmissionRetentionPolicy.RemoveOnFinalState,
                           null);

            Thread.currentThread();
            Thread.sleep(1000);

            // And after having waited another second, the second one should have run and have been deleted

            // We verify that the submission is gone
            assertFalse(session.submissionExists(StringBasedIdentifier.newInstance(taskName)));

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


    /**
     * A test that checks that runnables with no result are not removed with ExplicitRemove policy.
     */
    @Test
    public void testRunnableNoResultLifecycleExplicitRemove() throws Throwable
    {
        extendClientInit(pofConfig, extendClientCacheConfig);

        ProcessingSession session = null;

        try
        {
            session = new DefaultProcessingSession(StringBasedIdentifier.newInstance("RunnableLifecycleTestSession"));

            System.out.println("Cluster started - now running testRunnableNoResultLifecycleExplicitRemove");

            String taskName = "RunnableLifecycle";
            SubmissionOutcome outcome = session.submit(new DoNothingRunnable(),
                                                       new DefaultSubmissionConfiguration(null),
                                                       StringBasedIdentifier.newInstance(taskName),
                                                       SubmissionRetentionPolicy.ExplicitRemove,
                                                       null);

            Thread.currentThread();
            Thread.sleep(1000);

            assertTrue(outcome.getSubmissionState() == SubmissionState.DONE);

            try
            {
                // After having waited for one second, the runnable should have run and still be there
                // Now we submit a second identically named runnable
                session.submit(new DoNothingRunnable(),
                               new DefaultSubmissionConfiguration(null),
                               StringBasedIdentifier.newInstance(taskName),
                               SubmissionRetentionPolicy.RemoveOnFinalState,
                               null);
            }
            catch (PortableException e)
            {
                if (e.getName().equals("Portable(java.lang.IllegalStateException)"))
                {
                    // The above should throw an exception because the submission still exists
                    System.out.println("An expected IllegalStateException testRunnableNoResultLifecycleExplicitRemove");
                }
                else
                {
                    throw e;
                }
            }

        }
        catch (Exception t)
        {
            System.out.println("Unexpected Exception in testRunnableNoResultLifecycleExplicitRemove");
            System.out.println("caught exception:" + t);
        }
        finally
        {
            if (session != null)
            {
                session.shutdown();
            }
        }
    }
}
