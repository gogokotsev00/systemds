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

package org.apache.sysds.test.functions.iogen;

import org.junit.Test;

public class MatrixGenerateReaderMMTest extends GenerateReaderMatrixTest {

	private final static String TEST_NAME = "FrameGenerateReaderLibSVMTest";

	@Override
	protected String getTestName() {
		return TEST_NAME;
	}

	@Test
	public void test1() {
		String HOME = SCRIPT_DIR + TEST_DIR + "in/MM/";
		String[] fields = {"F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10"};
		Boolean[] parallel = {true, false};
		for(Boolean b : parallel) {
			for(String f : fields) {
				String sampleRaw = HOME + f + "/sample-queen-mm200.raw";
				String sampleFrame = HOME + f + "/sample-queen-mm200.matrix";
				runGenerateReaderTest(sampleRaw, sampleFrame, b);
			}
		}
	}
}
