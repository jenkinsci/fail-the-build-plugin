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

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;

public class CycleResult extends AbstractResult {

    public static final String NAME = "CYCLE";

    public CycleResult() {
        super(NAME);
    }

    @Override
    public String getDisplayName() {
        return Messages.cycle_displayName();
    }

    @Override
    public Result getResult(final AbstractBuild<?, ?> build, final BuildListener listener) {
        final int resultIndex = (build.getNumber() -1) % (FixResultBuilder.RESULTS.length -1);
        return FixResultBuilder.RESULTS[resultIndex].getResult(build, listener);
    }

}
