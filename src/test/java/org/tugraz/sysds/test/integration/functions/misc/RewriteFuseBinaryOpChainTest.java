/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.tugraz.sysds.test.integration.functions.misc;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.tugraz.sysds.api.DMLScript;
import org.tugraz.sysds.common.Types.ExecMode;
import org.tugraz.sysds.hops.OptimizerUtils;
import org.tugraz.sysds.lops.LopProperties.ExecType;
import org.tugraz.sysds.runtime.matrix.data.MatrixValue.CellIndex;
import org.tugraz.sysds.test.integration.AutomatedTestBase;
import org.tugraz.sysds.test.integration.TestConfiguration;
import org.tugraz.sysds.test.utils.TestUtils;

/**
 * Regression test for function recompile-once issue with literal replacement.
 * 
 */
public class RewriteFuseBinaryOpChainTest extends AutomatedTestBase 
{
	private static final String TEST_NAME1 = "RewriteFuseBinaryOpChainTest1"; //+* (X+s*Y)
	private static final String TEST_NAME2 = "RewriteFuseBinaryOpChainTest2"; //-* (X-s*Y) 
	private static final String TEST_NAME3 = "RewriteFuseBinaryOpChainTest3"; //+* (s*Y+X)
	private static final String TEST_NAME4 = "RewriteFuseBinaryOpChainTest4"; //outer(X, s*Y, "+") not applied
	
	private static final String TEST_DIR = "functions/misc/";
	private static final String TEST_CLASS_DIR = TEST_DIR + RewriteFuseBinaryOpChainTest.class.getSimpleName() + "/";
	
	private static final double eps = Math.pow(10, -10);
	
	@Override
	public void setUp() {
		TestUtils.clearAssertionInformation();
		addTestConfiguration( TEST_NAME1, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME1, new String[] { "R" }) );
		addTestConfiguration( TEST_NAME2, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME2, new String[] { "R" }) );
		addTestConfiguration( TEST_NAME3, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME3, new String[] { "R" }) );
		addTestConfiguration( TEST_NAME4, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME4, new String[] { "R" }) );		
	}
	
	@Test
	public void testFuseBinaryPlusNoRewriteCP() {
		testFuseBinaryChain( TEST_NAME1, false, ExecType.CP );
	}
	
	@Test
	public void testFuseBinaryPlusRewriteCP() {
		testFuseBinaryChain( TEST_NAME1, true, ExecType.CP);
	}
	
	@Test
	public void testFuseBinaryMinusNoRewriteCP() {
		testFuseBinaryChain( TEST_NAME2, false, ExecType.CP );
	}
	
	@Test
	public void testFuseBinaryMinusRewriteCP() {
		testFuseBinaryChain( TEST_NAME2, true, ExecType.CP );
	}
	
	@Test
	public void testFuseBinaryPlus2NoRewriteCP() {
		testFuseBinaryChain( TEST_NAME3, false, ExecType.CP );
	}
	
	@Test
	public void testFuseBinaryPlus2RewriteCP() {
		testFuseBinaryChain( TEST_NAME3, true, ExecType.CP );
	}
	
	@Test
	public void testFuseBinaryPlusNoRewriteSP() {
		testFuseBinaryChain( TEST_NAME1, false, ExecType.SPARK );
	}
	
	@Test
	public void testFuseBinaryPlusRewriteSP() {
		testFuseBinaryChain( TEST_NAME1, true, ExecType.SPARK );
	}
	
	@Test
	public void testFuseBinaryMinusNoRewriteSP() {
		testFuseBinaryChain( TEST_NAME2, false, ExecType.SPARK );
	}
	
	@Test
	public void testFuseBinaryMinusRewriteSP() {
		testFuseBinaryChain( TEST_NAME2, true, ExecType.SPARK );
	}
	
	@Test
	public void testFuseBinaryPlus2NoRewriteSP() {
		testFuseBinaryChain( TEST_NAME3, false, ExecType.SPARK );
	}
	
	@Test
	public void testFuseBinaryPlus2RewriteSP() {
		testFuseBinaryChain( TEST_NAME3, true, ExecType.SPARK );
	}
	
	//negative tests
	
	@Test
	public void testOuterBinaryPlusNoRewriteCP() {
		testFuseBinaryChain( TEST_NAME4, false, ExecType.CP );
	}
	
	@Test
	public void testOuterBinaryPlusRewriteCP() {
		testFuseBinaryChain( TEST_NAME4, true, ExecType.CP);
	}
	
	private void testFuseBinaryChain( String testname, boolean rewrites, ExecType instType )
	{	
		ExecMode platformOld = rtplatform;
		switch( instType ){
			case SPARK: rtplatform = ExecMode.SPARK; break;
			default: rtplatform = ExecMode.HYBRID; break;
		}
		
		boolean sparkConfigOld = DMLScript.USE_LOCAL_SPARK_CONFIG;
		if( rtplatform == ExecMode.SPARK )
			DMLScript.USE_LOCAL_SPARK_CONFIG = true;
		
		boolean rewritesOld = OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION;
		OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION = rewrites;
		
		try
		{	
			TestConfiguration config = getTestConfiguration(testname);
			loadTestConfiguration(config);
			
			String HOME = SCRIPT_DIR + TEST_DIR;
			fullDMLScriptName = HOME + testname + ".dml";
			programArgs = new String[]{"-explain", "-stats","-args", output("S") };
			
			fullRScriptName = HOME + testname + ".R";
			rCmd = getRCmd(inputDir(), expectedDir());			

			runTest(true, false, null, -1);
			runRScript(true); 
			
			//compare matrices 
			HashMap<CellIndex, Double> dmlfile = readDMLMatrixFromHDFS("S");
			HashMap<CellIndex, Double> rfile  = readRMatrixFromFS("S");
			Assert.assertTrue(TestUtils.compareMatrices(dmlfile, rfile, eps, "Stat-DML", "Stat-R"));
		}
		finally
		{
			OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION = rewritesOld;
			rtplatform = platformOld;
			DMLScript.USE_LOCAL_SPARK_CONFIG = sparkConfigOld;
		}
		
	}	
}