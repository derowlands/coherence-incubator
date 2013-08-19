/*
 * File: DuplicateIdentifierTests.java
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

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test multiple task using the same indentifiers. 
 * 
 * @author Christer Fahlgren
 */
public class DuplicateIdentifierTests extends AbstractClusterTests
{
    /**
     * A test that checks that an exception is thrown for duplicate identifiers.
     */
    @Test
    public void testDuplicateIdentifier() throws Throwable
    {
        extendClientInit(pofConfig, extendClientCacheConfig);

        ProcessingSession session = null;

        try
        {
            session = new DefaultProcessingSession(StringBasedIdentifier.newInstance("DuplicateIdentifierSession"));

            String taskName = "DuplicateIdentifier";

            System.out.println("Submitting first task");

            SubmissionOutcome outcome = session.submit(new TestTask(taskName),
                                                       new DefaultSubmissionConfiguration(null),
                                                       StringBasedIdentifier.newInstance(taskName),
                                                       SubmissionRetentionPolicy.ExplicitRemove,
                                                       null);

            try
            {
                System.out.println("Submitting second task");
                session.submit(new TestTask(taskName),
                               new DefaultSubmissionConfiguration(null),
                               StringBasedIdentifier.newInstance(taskName),
                               SubmissionRetentionPolicy.ExplicitRemove,
                               null);
            }
            catch (IllegalStateException e)
            {
                System.out.println("Expected IllegalStateException thrown during submit of second task:" + e);
            }

            NamedCache cache = CacheFactory.getCache("coherence.patterns.processing.submissions");

            assertEquals(cache.size(), 1);
            System.out.println("Now lets wait for the completion of first outcome.");
            System.out.print(outcome.get());
            System.out.println("\n");
            System.out.println("========================================");
            System.out.println("TEST COMPLETED.");

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
