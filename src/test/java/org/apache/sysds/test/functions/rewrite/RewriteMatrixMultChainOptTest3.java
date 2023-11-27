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

package org.apache.sysds.test.functions.rewrite;

import org.apache.sysds.common.Types.ExecMode;
import org.apache.sysds.runtime.matrix.data.MatrixValue.CellIndex;
import org.apache.sysds.test.AutomatedTestBase;
import org.apache.sysds.test.TestConfiguration;
import org.apache.sysds.test.TestUtils;
import org.junit.Test;

import java.util.HashMap;

public class RewriteMatrixMultChainOptTest3 extends AutomatedTestBase
{
	private static final String TEST_NAME1 = "RewriteMMChainTest2";
	private static final String TEST_DIR = "functions/rewrite/";
	private static final String TEST_CLASS_DIR = TEST_DIR + RewriteMatrixMultChainOptTest3.class.getSimpleName() + "/";

	private static final int rowsA = 5;
	private static final int colsA = 3;
	private static final int colsB = 7;
	private static final int rowsX = 9;
	private static final int colsX = 4;
	private static final int colsY = 5;
	private static final double eps = Math.pow(10, -10);

	@Override
	public void setUp() {
		TestUtils.clearAssertionInformation();
		addTestConfiguration( TEST_NAME1, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME1, new String[] {"R"}));
	}

	@Test
	public void testMMChain1Singlenode() {
		testMMChainWithTransposeOperator(TEST_NAME1, ExecMode.SINGLE_NODE);
	}
	
	@Test
	public void testMMChain1Hybrid() {
		testMMChainWithTransposeOperator(TEST_NAME1, ExecMode.HYBRID);
	}
	
	@Test
	public void testMMChain1Spark() {
		testMMChainWithTransposeOperator(TEST_NAME1, ExecMode.HYBRID);
	}

	private void testMMChainWithTransposeOperator(String testname, ExecMode et)
	{
		ExecMode etOld = setExecMode(et);
		
		try
		{
			TestConfiguration config = getTestConfiguration(testname);
			loadTestConfiguration(config);

			String HOME = SCRIPT_DIR + TEST_DIR;
			fullDMLScriptName = HOME + testname + ".dml";
			programArgs = new String[]{ "-explain", "hops", "-stats",
				"-args", input("A"), input("B"), input("X"), input("Y"), output("R") };
			fullRScriptName = HOME + testname + ".R";
			rCmd = getRCmd(inputDir(), expectedDir());

			double[][] A = getRandomMatrix(rowsA, colsA, -1, 1, 0.97d, 7);
			double[][] B = getRandomMatrix(colsA, colsB, -1, 1, 0.9d, 3);
			double[][] X = getRandomMatrix(rowsX, colsX, -1, 1, 0.97d, 7);
			double[][] Y = getRandomMatrix(colsX, colsY, -1, 1, 0.9d, 3);
			writeInputMatrixWithMTD("A", A, true);
			writeInputMatrixWithMTD("B", B, true);
			writeInputMatrixWithMTD("X", X, true);
			writeInputMatrixWithMTD("Y", Y, true);

			//execute tests
			runTest(true, false, null, -1);
			runRScript(true);

			//compare matrices
			HashMap<CellIndex, Double> dmlfile = readDMLMatrixFromOutputDir("R");
			HashMap<CellIndex, Double> rfile  = readRMatrixFromExpectedDir("R");
			TestUtils.compareMatrices(dmlfile, rfile, eps, "Stat-DML", "Stat-R");
		}
		finally {
			resetExecMode(etOld);
		}
	}
}
