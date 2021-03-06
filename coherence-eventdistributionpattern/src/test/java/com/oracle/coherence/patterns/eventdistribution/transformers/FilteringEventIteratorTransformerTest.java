/*
 * File: FilteringEventIteratorTransformerTest.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
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

package com.oracle.coherence.patterns.eventdistribution.transformers;

import com.oracle.coherence.common.events.EntryInsertedEvent;
import com.oracle.coherence.common.events.EntryRemovedEvent;
import com.oracle.coherence.common.events.EntryUpdatedEvent;
import com.oracle.coherence.common.events.Event;
import com.oracle.coherence.common.util.SimpleBinaryEntry;
import com.oracle.coherence.patterns.eventdistribution.EventIteratorTransformer;
import com.oracle.coherence.patterns.eventdistribution.events.DistributableEntry;
import com.oracle.coherence.patterns.eventdistribution.events.DistributableEntryEvent;
import com.oracle.coherence.patterns.eventdistribution.events.DistributableEntryInsertedEvent;
import com.oracle.coherence.patterns.eventdistribution.events.DistributableEntryRemovedEvent;
import com.oracle.coherence.patterns.eventdistribution.events.DistributableEntryUpdatedEvent;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.filter.LikeFilter;
import com.tangosol.util.filter.NeverFilter;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * The {@link FilteringEventIteratorTransformerTest} is designed to exercise {@link FilteringEventIteratorTransformer}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class FilteringEventIteratorTransformerTest
{
    /**
     * An {@link ArrayList} of {@link Event}s to transform for the test.
     */
    private ArrayList<Event> events;


    /**
     * Setup the test
     */
    @Before
    public void setup()
    {
        // create some random DistributableEntryEvents
        events = new ArrayList<Event>();

        Random random = new Random();

        for (int i = 0; i < 10; i++)
        {
            BinaryEntry             entry = new SimpleBinaryEntry("Item " + i, "Value " + i, "Original Value " + i);

            DistributableEntryEvent event = null;

            switch (random.nextInt(3))
            {
            case 0 :
                event = new DistributableEntryInsertedEvent("my-cache", new DistributableEntry(entry));
                break;

            case 1 :
                event = new DistributableEntryUpdatedEvent("my-cache", new DistributableEntry(entry));
                break;

            case 2 :
                event = new DistributableEntryRemovedEvent("my-cache", new DistributableEntry(entry));
                break;
            }

            if (event != null)
            {
                events.add(event);
            }
        }
    }


    /**
     * Ensure that we can filter insert events
     */
    @Test
    public void testFilteringInsertedEvents()
    {
        EventIteratorTransformer transformer = new FilteringEventIteratorTransformer(new LikeFilter("getClass.getName",
                                                                                                    "%Inserted%",
                                                                                                    true));

        Iterator<Event> iterator = transformer.transform(events.iterator());

        while (iterator.hasNext())
        {
            Assert.assertTrue(iterator.next() instanceof EntryInsertedEvent);
        }
    }


    /**
     * Ensure that we can filter update events
     */
    @Test
    public void testFilteringUpdatedEvents()
    {
        EventIteratorTransformer transformer = new FilteringEventIteratorTransformer(new LikeFilter("getClass.getName",
                                                                                                    "%Updated%",
                                                                                                    true));

        Iterator<Event> iterator = transformer.transform(events.iterator());

        while (iterator.hasNext())
        {
            Assert.assertTrue(iterator.next() instanceof EntryUpdatedEvent);
        }
    }


    /**
     * Ensure that we can filter remove events
     */
    @Test
    public void testFilteringRemovedEvents()
    {
        EventIteratorTransformer transformer = new FilteringEventIteratorTransformer(new LikeFilter("getClass.getName",
                                                                                                    "%Removed%",
                                                                                                    true));

        Iterator<Event> iterator = transformer.transform(events.iterator());

        while (iterator.hasNext())
        {
            Assert.assertTrue(iterator.next() instanceof EntryRemovedEvent);
        }
    }


    /**
     * Ensure that we can completely eliminate events
     */
    @Test
    public void testEliminatingEvents()
    {
        EventIteratorTransformer transformer = new FilteringEventIteratorTransformer(NeverFilter.INSTANCE);

        Iterator<Event>          iterator    = transformer.transform(events.iterator());

        Assert.assertFalse(iterator.hasNext());
    }
}
