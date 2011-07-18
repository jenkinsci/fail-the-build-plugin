/*
 * The MIT License
 *
 * Copyright (C) 2010-2011 by Anthony Robinson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkins_ci.plugins.fail_the_build;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import org.jvnet.hudson.test.HudsonTestCase;

public class FixResultBuilderTest extends HudsonTestCase {

    public void testSuccess() throws Exception {
        assertDefaultResult(Result.SUCCESS);
    }

    public void testUnstable() throws Exception {
        assertDefaultResult(Result.UNSTABLE);        
    }

    public void testFailed() throws Exception {
        assertDefaultResult(Result.FAILURE);        
    }

    public void testAborted() throws Exception {
        assertDefaultResult(Result.ABORTED);        
    }

    public void testCycle() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(createFixResultBuilder(CycleResult.NAME));
        buildAndAssertResult(Result.SUCCESS, project);
        buildAndAssertResult(Result.UNSTABLE, project);
        buildAndAssertResult(Result.FAILURE, project);
        buildAndAssertResult(Result.ABORTED, project);
        buildAndAssertResult(Result.SUCCESS, project);
        buildAndAssertResult(Result.UNSTABLE, project);
        buildAndAssertResult(Result.FAILURE, project);
        buildAndAssertResult(Result.ABORTED, project);
    }

    public void testBuildNumberResult() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(createFixResultBuilder(Result.FAILURE.toString(), "1", null, null, null));
        buildAndAssertResult(Result.SUCCESS, project);
    }

    public void testBuildNumberResultWithWhiteSpace() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(createFixResultBuilder(Result.FAILURE.toString(), " 1 ", null, null, null));
        buildAndAssertResult(Result.SUCCESS, project);
    }

    public void testSuccessNumbers() throws Exception {
        final String successes = "1,2, 4, 6 7 ";
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(createFixResultBuilder(Result.FAILURE.toString(), successes, null, null, null));
        buildAndAssertResult(Result.SUCCESS, project); // 1
        buildAndAssertResult(Result.SUCCESS, project); // 2
        buildAndAssertResult(Result.FAILURE, project);
        buildAndAssertResult(Result.SUCCESS, project); // 4
        buildAndAssertResult(Result.FAILURE, project);
        buildAndAssertResult(Result.SUCCESS, project); // 6
        buildAndAssertResult(Result.SUCCESS, project); // 7
        buildAndAssertResult(Result.FAILURE, project);
    }

    public void testAllResultsByBuildNumber() throws Exception {
        final String successes = "6";
        final String unstable = "1,3,4,10";
        final String failure = "2, 7, 8";
        final String aborted = " 5 ";
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(createFixResultBuilder(Result.FAILURE.toString(), successes, unstable, failure, aborted));
        buildAndAssertResult(Result.UNSTABLE, project); // 1
        buildAndAssertResult(Result.FAILURE, project); // 2
        buildAndAssertResult(Result.UNSTABLE, project); // 3
        buildAndAssertResult(Result.UNSTABLE, project); // 4
        buildAndAssertResult(Result.ABORTED, project); // 5
        buildAndAssertResult(Result.SUCCESS, project); // 6
        buildAndAssertResult(Result.FAILURE, project); // 7
        buildAndAssertResult(Result.FAILURE, project); // 8
        buildAndAssertResult(Result.FAILURE, project); // 9
        buildAndAssertResult(Result.UNSTABLE, project); // 10
    }

    private void assertDefaultResult(final Result result) throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(createFixResultBuilder(result.toString()));
        buildAndAssertResult(result, project);
    }

    private void buildAndAssertResult(final Result expectedResult, final FreeStyleProject project) throws Exception {
        assertEquals(expectedResult, project.scheduleBuild2(0).get().getResult());
    }

    private FixResultBuilder createFixResultBuilder(final String defaultConfigName) {
        return createFixResultBuilder(defaultConfigName, null, null, null, null);
    }

    private FixResultBuilder createFixResultBuilder(final String defaultConfigName, final String success, final String unstable,
                                                    final String failure, final String aborted) {
        return new FixResultBuilder(defaultConfigName, success, unstable, failure, aborted);
    }

}
