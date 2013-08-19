/*
 * File: GetIdentifiersTests.java
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

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.identifiers.StringBasedIdentifier;

import com.oracle.coherence.patterns.processing.ProcessingSession;
import com.oracle.coherence.patterns.processing.SubmissionOutcome;
import com.oracle.coherence.patterns.processing.SubmissionRetentionPolicy;
import com.oracle.coherence.patterns.processing.internal.DefaultProcessingSession;
import com.oracle.coherence.patterns.processing.internal.DefaultSubmissionConfiguration;
import com.oracle.coherence.patterns.processing.misc.AbstractClusterTests;

import com.oracle.tools.runtime.coherence.Cluster;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Test to ensure test identifiers are created and handled correctly. 
 * 
 * @author Christer Fahlgren
 */
public class GetIdentifiersTests extends AbstractClusterTests
{
    /**
     * {@inheritDoc}
     */
    public Cluster startCluster()
    {
        return startTestCluster(gridCacheConfig, pofConfig, 0, 1, true);
    }


    /**
     * A test that creates tasks with specific identifiers and then tests for their existence.
     */
    @Test
    public void testGetIdentifiers() throws Throwable
    {
        extendClientInit(pofConfig, extendClientCacheConfig);

        ProcessingSession session = null;

        try
        {
            session = new DefaultProcessingSession(StringBasedIdentifier.newInstance("GetIdentifiersSession"));

            String taskName = "firstIdentifier";
            SubmissionOutcome outcome = session.submit(new TestTask(taskName),
                                                       new DefaultSubmissionConfiguration(null),
                                                       StringBasedIdentifier.newInstance(taskName),
                                                       SubmissionRetentionPolicy.ExplicitRemove,
                                                       new SubmissionCallback(taskName));

            String taskName2 = "secondIdentifier";
            SubmissionOutcome secondoutcome = session.submit(new TestTask(taskName2),
                                                             new DefaultSubmissionConfiguration(null),
                                                             StringBasedIdentifier.newInstance(taskName2),
                                                             SubmissionRetentionPolicy.ExplicitRemove,
                                                             new SubmissionCallback(taskName2));

            assertTrue(session.submissionExists(StringBasedIdentifier.newInstance(taskName)));
            assertTrue(session.submissionExists(StringBasedIdentifier.newInstance(taskName2)));
            assertFalse(session.submissionExists(StringBasedIdentifier.newInstance("doesn't exist")));

            Iterator<Identifier>  iter = session.getIdentifierIterator();
            ArrayList<Identifier> ids  = new ArrayList<Identifier>();

            while (iter.hasNext())
            {
                Identifier id = iter.next();

                ids.add(id);
                System.out.println("Id in list:" + id);
            }

            assertTrue(ids.contains(StringBasedIdentifier.newInstance(taskName)));
            assertTrue(ids.contains(StringBasedIdentifier.newInstance(taskName2)));
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
