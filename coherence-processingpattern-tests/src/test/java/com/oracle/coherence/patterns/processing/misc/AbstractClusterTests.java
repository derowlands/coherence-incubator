/*
 * File: AbstractClusterTests.java
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

package com.oracle.coherence.patterns.processing.misc;

import static com.oracle.tools.deferred.DeferredHelper.eventually;
import static com.oracle.tools.deferred.DeferredHelper.invoking;
import static org.hamcrest.CoreMatchers.is;

import com.oracle.tools.deferred.DeferredAssert;
import com.oracle.tools.runtime.coherence.Cluster;
import com.oracle.tools.runtime.coherence.ClusterBuilder;
import com.oracle.tools.runtime.coherence.ClusterMember;
import com.oracle.tools.runtime.coherence.ClusterMemberSchema;
import com.oracle.tools.runtime.console.SystemApplicationConsole;
import com.oracle.tools.runtime.java.JavaApplicationBuilder;
import com.oracle.tools.runtime.java.NativeJavaApplicationBuilder;
import com.oracle.tools.runtime.java.container.Container;
import com.oracle.tools.runtime.network.AvailablePortIterator;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.Base;

import org.junit.After;
import org.junit.Before;

import java.util.Iterator;

/**
 * An abstract class used for process pattern tests. 
 *
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author David Rowlands
 */
public abstract class AbstractClusterTests
{
    /** 
     * single server processing pattern cache configuration file. 
     */
    public final static String singleCacheConfig = "coherence-processingpattern-test-single-server-cache-config.xml";

    /** 
     * grid processing pattern cache configuration file. 
     */
    public final static String gridCacheConfig = "coherence-processingpattern-test-grid-server-cache-config.xml";

    /** 
     * extend client cache configuration file.
     */
    public final static String extendClientCacheConfig =
        "coherence-processingpattern-test-extendclient-cache-config.xml";
    
    /** 
     * extend client single task processing cache configuration file.
     */
    public final static String extendClientTaskCacheConfig =
        "coherence-processingpattern-test-extend-taskprocessing-cache-config.xml";

    /** 
     * extend client single task processing cache configuration file.
     */
    public final static String proxyCacheConfig =
        "coherence-processingpattern-test-proxy-cache-config.xml";

    /** 
     * pof configuration file.
     */
    public final static String pofConfig = "coherence-processingpattern-test-pof-config.xml";

    /**
     * Cluster used by tests.
     */
    private Cluster            m_cluster;

    /**
     * The AvailablePortIterator must be a singleton since it allocates ports
     * across tests.
     */
    private static AvailablePortIterator s_availablePortIterator;

    
    
    /**
     * Initialize the test and start a cache server(s).
     */
    @Before
    public void startup()
    {
        m_cluster     = startCluster();
    }


    /**
     * Shutdown the cluster.
     */
    @After
    public void shutdown()
    {
        shutdownCluster(m_cluster);
    }


    /**
     * Default starting of Cluster. 
     * Can be extended by tests as needed. 
     * 
     * @return Cluster the started cluster
     */
    public Cluster startCluster()
    {
        return startTestCluster(singleCacheConfig, pofConfig, 0, 1, true);
    }


    /**
     * Default shutdown of Cluster
     * Can be extended by tests as needed.
     */
    public void shutdownCluster(Cluster cluster)
    {
    	if (cluster != null)
    	{
    		shutdownTestCluster(cluster);
    	}
    }


    /**
     * Common logic to initialize extend client configuration. 
     *
     * @param pofConfig - pof configuration file used extend client
     * @param cacheConfig - cache configuration file
     */
    public void extendClientInit(String pofConfig,
                                 String cacheConfig)
    {
        System.setProperty("tangosol.pof.config", pofConfig);
        System.setProperty("tangosol.coherence.cacheconfig", extendClientCacheConfig);

        // turn off local clustering so we don't connect with the process just started
        System.setProperty("tangosol.coherence.tcmp.enabled", "false");

        Iterator<ClusterMember> members = m_cluster.iterator();

        if (members.hasNext())
        {
            String remPort = members.next().getSystemProperty("proxy.port");

            System.setProperty("remote.port", remPort);
        }

    }


    /**
     * Shutdown a server within cluster. 
     */
    public void shutdownServer()
    {
        Iterator<ClusterMember> membersIter = m_cluster.iterator();
        ClusterMember           server      = null;

        while (membersIter.hasNext())
        {
            server = (ClusterMember) membersIter.next();
        }

        if (server != null)
        {
            server.destroy();
        }

    }
    

    
    /**
     * Create the cluster and start it.
     *
     * @return this object
     */
   public Cluster startTestCluster(String  sCachConfig,
                         String  sPofConfig,
                         int     unicastPort,
                         int     memberCount,
                         boolean fStorageEnabled)
    {
    	
    	Cluster cluster = null; 
    	
        // ensure this process does not start or join a cluster
        // System.setProperty("tangosol.coherence.tcmp.enabled", "false");
    	int clusterPort = unicastPort == 0 ?  Container.getAvailablePorts().next() : unicastPort;

        try
        {
            JavaApplicationBuilder<ClusterMember, ClusterMemberSchema> bldrApp =
                new NativeJavaApplicationBuilder<ClusterMember, ClusterMemberSchema>();

            ClusterBuilder clusterBuilder = new ClusterBuilder();

            for (int i = 1; i <= memberCount; i++)
            {
                ClusterMemberSchema schema =
                    new ClusterMemberSchema().setEnvironmentInherited(true).setPreferIPv4(true)
                        .setClusterPort(clusterPort)
                        .setSystemProperty("test.unicast.port",
                                           String.valueOf(clusterPort)).setSystemProperty("proxy.port",
                                        		   									Container.getAvailablePorts().next())
                                                                                                  .setCacheConfigURI(sCachConfig)
                                                                                                  .setSingleServerMode()
                                                                                                  .setStorageEnabled(fStorageEnabled)
                                                                                                  .setJMXPort(Container.getAvailablePorts())
                                                                                                  .setRemoteJMXManagement(true)
                                                                                                  .setJMXManagementMode(ClusterMemberSchema
                                                                                                      .JMXManagementMode
                                                                                                      .ALL).setJMXSupport(true)
                                                                                                          .addOption("-ea");

                if (sPofConfig != null)
                {
                    schema.setPofConfigURI(sPofConfig);
                    schema.setPofEnabled(true);
                }

                System.out.println("Adding builder " + i);
                clusterBuilder.addBuilder(bldrApp, schema, "processing-pattern-" + i, 1);
            }

            cluster = clusterBuilder.realize(new SystemApplicationConsole());

            while (cluster.getClusterSize() < memberCount)
            {
                System.out.println("Cluster size is " + cluster.getClusterSize());
                Base.sleep(4000);
            }

            DeferredAssert.assertThat(eventually(invoking(cluster).getClusterSize()), is(memberCount));

            return cluster;

        }
        catch (Exception e)
        {
            throw Base.ensureRuntimeException(e);
        }
    }

   
   /**
    * Shutdown the cluster.
    */
   public void shutdownTestCluster(Cluster cluster)
   {
       CacheFactory.shutdown();

       if (cluster != null)
       {
           cluster.destroy();
       }

       // wait until cluster has been shut down
       DeferredAssert.assertThat(eventually(invoking(cluster).getClusterSize()), is(0));
   }
   
}
