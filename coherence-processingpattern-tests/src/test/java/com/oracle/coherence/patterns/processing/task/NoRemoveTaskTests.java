/*
 * File: NoRemoveTaskTests.java
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

import com.oracle.coherence.common.identifiers.StringBasedIdentifier;

import com.oracle.coherence.patterns.processing.ProcessingSession;
import com.oracle.coherence.patterns.processing.SubmissionOutcome;
import com.oracle.coherence.patterns.processing.SubmissionRetentionPolicy;
import com.oracle.coherence.patterns.processing.internal.DefaultProcessingSession;
import com.oracle.coherence.patterns.processing.internal.DefaultSubmissionConfiguration;
import com.oracle.coherence.patterns.processing.misc.AbstractClusterTests;

import com.oracle.tools.runtime.coherence.Cluster;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DateFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Verify submission retention policy. 
 * 
 * @author Christer Fahlgren
 */
public class NoRemoveTaskTests extends AbstractClusterTests
{
    /**
     * {@inheritDoc}
     */
    public Cluster startCluster()
    {
        return startTestCluster(gridCacheConfig, pofConfig, 0, 1, true);
    }


    /**
     * A test that creates tasks, and controls the lifecycle directly.
     */
    @Test
    public void testNoRemoveTask() throws Throwable
    {
        extendClientInit(pofConfig, extendClientCacheConfig);

        ProcessingSession session = null;

        try
        {
            session =
                new DefaultProcessingSession(StringBasedIdentifier
                    .newInstance("TaskExecutionSample"
                                 + DateFormat.getDateTimeInstance().format(System.currentTimeMillis())));

            ArrayList<SubmissionOutcome> tasklist = new ArrayList<SubmissionOutcome>();

            for (int i = 0; i < 5; i++)
            {
                String              taskName = "TestTask:" + Integer.toString(i);
                Map<String, String> attrMap  = new HashMap<String, String>();

                attrMap.put("type", "grid");
                System.out.println("Submitting test tasks " + i);

                SubmissionOutcome outcome = session.submit(new TestTask(taskName),
                                                           new DefaultSubmissionConfiguration(attrMap),
                                                           StringBasedIdentifier.newInstance(taskName),
                                                           SubmissionRetentionPolicy.ExplicitRemove,
                                                           new SubmissionCallback(taskName));

                tasklist.add(outcome);
            }

            NamedCache cache = CacheFactory.getCache("coherence.patterns.processing.submissions");

            System.out.println("Cache size: " + cache.size());

            for (int i = 0; i < tasklist.size(); i++)
            {
                System.out.println("Waiting for: " + i);

                System.out.println("Result:" + Integer.toString(i) + ":" + tasklist.get(i).get().toString());
                System.out.println("Took:" + Integer.toString(i) + ":" + tasklist.get(i).getExecutionDuration() + " L:"
                                   + tasklist.get(i).getWaitDuration());
            }

            // Verifying that the submissions didn't get deleted.
            assertEquals(cache.size(), tasklist.size());

            ProcessingSession newsession = new DefaultProcessingSession(StringBasedIdentifier.newInstance("NewOwner"));
            ArrayList<SubmissionOutcome> newtasklist = new ArrayList<SubmissionOutcome>();

            for (int i = 0; i < tasklist.size(); i++)
            {
                System.out.println("Changing owner: " + i);
                newtasklist.add(newsession.acquireSubmission(tasklist.get(i).getIdentifier(),
                                                             SubmissionRetentionPolicy.ExplicitRemove, null));
            }

            System.out.println("After changing owner Cache size: " + cache.size());

            // Now since we changed owner for the Submissions, we shall not be able to remove
            // them using the original session.
            for (int i = 0; i < tasklist.size(); i++)
            {
                assertFalse(session.discardSubmission(tasklist.get(i).getIdentifier()));
            }

            // But we should be able to remove them using the new session.
            for (int i = 0; i < newtasklist.size(); i++)
            {
                assertTrue(newsession.discardSubmission(newtasklist.get(i).getIdentifier()));
            }

            System.out.println("after remove Cache size: " + cache.size());

            System.out.println("Cache size: " + cache.size());
            assertEquals(cache.size(), 0);
            System.out.println("Finished");
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
