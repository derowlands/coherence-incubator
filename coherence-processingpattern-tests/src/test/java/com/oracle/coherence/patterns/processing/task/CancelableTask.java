/*
 * File: CancelableTask.java
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

import com.tangosol.io.ExternalizableLite;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.util.ExternalizableHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Taks that is used in Cancellation testing. 
 *
 * @author         David Rowlands
 */
public class CancelableTask implements ResumableTask, ExternalizableLite, PortableObject
{
    private long sleepDuration;


    /**
     * Constructs ...
     *
     */
    public CancelableTask()
    {
    }


    /**
     * Constructs ...
     *
     *
     * @param sleepDuration
     */
    public CancelableTask(int sleepDuration)
    {
        this.sleepDuration = sleepDuration;
    }


    /**
     * Method description
     *
     * @param in
     *
     * @throws IOException
     */
    public void readExternal(DataInput in) throws IOException
    {
        sleepDuration = ExternalizableHelper.readLong(in);
    }


    /**
     * Method description
     *
     * @param out
     *
     * @throws IOException
     */
    public void writeExternal(DataOutput out) throws IOException
    {
        ExternalizableHelper.writeLong(out, sleepDuration);
    }


    /**
     * Method description
     *
     * @param reader
     *
     * @throws IOException
     */
    public void readExternal(PofReader reader) throws IOException
    {
        sleepDuration = reader.readLong(10);
    }


    /**
     * Method description
     *
     * @param writer
     *
     * @throws IOException
     */
    public void writeExternal(PofWriter writer) throws IOException
    {
        writer.writeLong(10, sleepDuration);
    }


    /**
     * Method description
     *
     * @param environment
     *
     * @return
     */
    public Object run(TaskExecutionEnvironment environment)
    {
        try
        {
            Thread.sleep(sleepDuration);
        }
        catch (InterruptedException e)
        {
            System.out.println("Thread was interrupted during sleep - stack trace as follows:");
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Method description
     *
     * @return
     */
    @Override
    public String toString()
    {
        return "CancelableTask [sleepDuration=" + sleepDuration + "]";
    }
}
