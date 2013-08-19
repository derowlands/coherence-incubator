/*
 * File: SimpleCallableTests.java
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

package com.oracle.coherence.patterns.processing.callable;

import com.oracle.coherence.common.identifiers.StringBasedIdentifier;

import com.oracle.coherence.patterns.processing.ProcessingSession;
import com.oracle.coherence.patterns.processing.SubmissionOutcome;
import com.oracle.coherence.patterns.processing.internal.DefaultProcessingSession;
import com.oracle.coherence.patterns.processing.internal.DefaultSubmissionConfiguration;
import com.oracle.coherence.patterns.processing.misc.AbstractClusterTests;

import com.oracle.tools.runtime.coherence.Cluster;

import org.junit.Test;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;

import java.util.concurrent.ExecutionException;

/**
 * A sample demonstrating a Callable using the processing pattern and extend client with 
 * task processor defined on client.
 *
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 * 
 * @author David Rowlands
 */
public class SimpleCallableExtendTaskTests extends AbstractClusterTests
{
    /**
     * {@inheritDoc}
     */
    public Cluster startCluster()
    {
        return startTestCluster(proxyCacheConfig, pofConfig, 0, 2, true);
    }


    /**
     * The simpleCallable method initializes the pattern and submits the Callable for execution, then waits for the result.
     *
     * @throws InterruptedException if execution was interrupted
     * @throws ExecutionException if execution failed
     */
    @Test
    public void simpleCallable() throws InterruptedException, ExecutionException, Throwable
    {
        extendClientInit(pofConfig, extendClientTaskCacheConfig);

        ProcessingSession session = null;

        try
        {
            session = new DefaultProcessingSession(StringBasedIdentifier.newInstance("SimpleCallableSample"
                                                                                     + System.currentTimeMillis()));

            final ArrayList<SubmissionOutcome> submissionlist = new ArrayList<SubmissionOutcome>();

            for (int i = 0; i < 30; i++)
            {
                final SubmissionOutcome outcome = session.submit(new SimpleCallable(),
                                                                 new DefaultSubmissionConfiguration(),
                                                                 new SubmissionCallback("Submission:" + i));

                // And add it to a list to keep track of it
                submissionlist.add(outcome);
            }

            
            for (int i = 0; i < submissionlist.size(); i++)
            {
            	SubmissionOutcome outcome = submissionlist.get(i);
                System.out.println("Result:" + Integer.toString(i) + ":" + outcome.get().toString());

                final Date submittime = new Date(outcome.getSubmissionTime());

                System.out.println("Submitted at:" + submittime.toString() + " Latency:"
                                   + Long.toString(outcome.getWaitDuration()) + " Execution took "
                                   + Long.toString(outcome.getExecutionDuration()) + " msec.");
            }

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
