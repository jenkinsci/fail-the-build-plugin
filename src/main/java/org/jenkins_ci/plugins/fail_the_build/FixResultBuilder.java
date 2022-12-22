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

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixResultBuilder extends Builder {
    
    public static final AbstractResult SUCCESS = new BallResult(Result.SUCCESS);
    public static final AbstractResult UNSTABLE = new BallResult(Result.UNSTABLE);
    public static final AbstractResult FAILURE = new BallResult(Result.FAILURE);
    public static final AbstractResult ABORTED = new BallResult(Result.ABORTED);
    public static final AbstractResult CYCLE = new CycleResult();
    
    public static final AbstractResult[] RESULTS = {SUCCESS, UNSTABLE, FAILURE, ABORTED, CYCLE};

    private transient Map<Integer, AbstractResult> resultsByBuildNumber = null;
    private final String defaultResultName;
    private final String success;
    private final String unstable;
    private final String failure;
    private final String aborted;

    @DataBoundConstructor
    public FixResultBuilder(final String defaultResultName, final String success, final String unstable, final String failure,
                            final String aborted) {
        this.defaultResultName = defaultResultName;
        this.success = success;
        this.unstable = unstable;
        this.failure = failure;
        this.aborted = aborted;
    }

    public String getDefaultResultName() {
        return defaultResultName;
    }

    private AbstractResult getDefaultResult() {
        for (AbstractResult result : RESULTS) {
            if (result.getName().equals(defaultResultName)) return result;
        }
        throw new RuntimeException(Messages.exception_invalidResultName(defaultResultName));
    }

    public String getAborted() {
        return aborted;
    }

    public String getFailure() {
        return failure;
    }

    public String getSuccess() {
        return success;
    }

    public String getUnstable() {
        return unstable;
    }

    private static List<Integer> convertToNumbers(final String listString) {
        final List<Integer> numbers = new ArrayList<Integer>();
        if (Util.fixEmptyAndTrim(listString) == null) return numbers;
        for (String numberString : listString.trim().split("[,\\s]+")) {
            try {
                numbers.add(Integer.parseInt(numberString));
            } catch (NumberFormatException nfe) {
                throw new RuntimeException(Messages.exception_nfe(numberString, listString, nfe.getLocalizedMessage()));
            }
        }
        return numbers;
    }

    @Override
    public FixResultBuilderDescriptor getDescriptor() {
        return Jenkins.get().getDescriptorByType(FixResultBuilderDescriptor.class);
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) {
        if (build == null) return false;
        initResultsByBuildNumber();
        AbstractResult result = resultsByBuildNumber.get(build.getNumber());
        if (result == null)
            result = getDefaultResult();
        return result.perform(build, listener);
    }

    private synchronized void initResultsByBuildNumber() {
        if (resultsByBuildNumber != null) return;
        resultsByBuildNumber = new HashMap<Integer, AbstractResult>();
        parseAndStore(SUCCESS, success);
        parseAndStore(UNSTABLE, unstable);
        parseAndStore(FAILURE, failure);
        parseAndStore(ABORTED, aborted);
    }

    private void parseAndStore(final AbstractResult result, final String buildNumberList) {
        for (int buildNumber : convertToNumbers(buildNumberList)) {
            resultsByBuildNumber.put(buildNumber, result);
        }
    }

    @Extension
    public static class FixResultBuilderDescriptor extends BuildStepDescriptor<Builder> {

        public ListBoxModel doFillDefaultResultNameItems() {
            ListBoxModel items = new ListBoxModel();
            for (AbstractResult result : RESULTS) {
                items.add(result.getOption());
            }
            return items;
        }

        @Override
        public String getDisplayName() {
            return Messages.builder_displayName();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getSuccessTitle() {
            return SUCCESS.getDisplayName();
        }

        public String getUnstableTitle() {
            return UNSTABLE.getDisplayName();
        }

        public String getFailureTitle() {
            return FAILURE.getDisplayName();
        }

        public String getAbortedTitle() {
            return ABORTED.getDisplayName();
        }

    }

}
