/*
 * The MIT License
 *
 * Copyright (c) 2013, Cisco Systems, Inc., a California corporation
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
package org.jenkinsci.plugins.behave.jsontestsupport;

import behave.gherkin.formatter.model.Match;
import behave.gherkin.formatter.model.Result;
import behave.gherkin.formatter.model.Step;
import hudson.model.AbstractBuild;
import hudson.tasks.test.TestResult;

/**
 * Represents a Step belonging to a Scenario from Cucumber.
 * 
 * @author James Nord
 */
public class StepResult extends TestResult {
	private static final long serialVersionUID = 1L;
	private final Step step;
	private final Match match;
	private final Result result;

	private ScenarioResult parent;
	private transient AbstractBuild<?, ?> owner;


	StepResult(Step step, Match match, Result result) {
		this.step = step;
		this.match = match;
		this.result = result;
	}


    @Override
	public String getDisplayName() {
		return "Cucumber Step result";
	}


	@Override
	public AbstractBuild<?, ?> getOwner() {
		return owner;
	}


	void setOwner(AbstractBuild<?, ?> owner) {
		this.owner = owner;
	}


	@Override
	public ScenarioResult getParent() {
		return parent;
	}


	protected void setParent(ScenarioResult parent) {
		this.parent = parent;
	}


	@Override
	public TestResult findCorrespondingResult(String id) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public float getDuration() {
		return BehaveUtils.durationFromResult(result);
	}


	/**
	 * Gets the total number of passed tests.
     * @return pass count
	 */
    @Override
	public int getPassCount() {
		return BehaveUtils.PASSED_TEST_STRING.equals(result.getStatus()) ? 1 : 0;
	}


	/**
	 * Gets the total number of failed tests.
     * @return fail count
	 */
    @Override
	public int getFailCount() {
		if (BehaveUtils.FAILED_TEST_STRING.equals(result.getStatus())
			    || BehaveUtils.UNDEFINED_TEST_STRING.equals(result.getStatus())) {
			return 1;
		}
		return 0;
	}


	/**
	 * Gets the total number of skipped tests.
     * @return skip count
	 */
    @Override
	public int getSkipCount() {
		if (BehaveUtils.SKIPPED_TEST_STRING.equals(result.getStatus())
		    || BehaveUtils.PENDING_TEST_STRING.equals(result.getStatus())) {
			return 1;
		}
		return 0;
	}


	Step getStep() {
		return step;
	}


	Match getMatch() {
		return match;
	}


	Result getResult() {
		return result;
	}
}
