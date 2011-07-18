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
import hudson.util.ListBoxModel;

public abstract class AbstractResult {
    
    private final String name;

    public AbstractResult(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String getDisplayName();

    public abstract Result getResult(AbstractBuild<?, ?> build, BuildListener listener);

    public ListBoxModel.Option getOption() {
        return new ListBoxModel.Option(getDisplayName(), getName());
    }

    public boolean perform(AbstractBuild<?, ?> build, BuildListener listener) {
        final Result result = getResult(build, listener);
        if (build.getResult() == null) {
            listener.getLogger().println(Messages.console_settingResult(result.color.getDescription()));
            build.setResult(result);
        } else if (build.getResult().isBetterThan(result)) {
            listener.getLogger().println(Messages.console_settingResult(result.color.getDescription()));
            build.setResult(result.combine(build.getResult()));
        } else if (build.getResult().isWorseThan(result)) {
            listener.getLogger().println(Messages.console_resultWorse(
                    build.getResult().color.getDescription(), result.color.getDescription()));
        } else {
            listener.getLogger().println(Messages.console_resultEqual(result.color.getDescription()));
        }
        return result.isBetterOrEqualTo(Result.UNSTABLE);
    }

}
