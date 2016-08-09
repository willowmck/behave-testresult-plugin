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
import hudson.model.AbstractBuild;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;

/**
 * Represents a Before or After belonging to a Scenario. Although this is a test Object as it is a background
 * it is not intended for individual Display.
 * 
 * @author James Nord
 */
public class BeforeAfterResult extends TestResult {

	private static final long serialVersionUID = 1L;
    
	private final Match match;
	private final Result result;

	private transient AbstractBuild<?, ?> owner;


	public BeforeAfterResult(Match match, Result result) {
		this.match = match;
		this.result = result;
	}


	@Override
	public String getName() {
		return "Behave Background";
	}


	/**
	 * Gets the total number of passed tests.
         * @return total number of passed tests
	 */
	@Override
	public int getPassCount() {
		return BehaveUtils.PASSED_TEST_STRING.equals(result.getStatus()) ? 1 : 0;
	}


	/**
	 * Gets the total number of failed tests.
         * @return total number of failed tests
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
         * @return total number of skipped tests
	 */
	@Override
	public int getSkipCount() {
		if (BehaveUtils.SKIPPED_TEST_STRING.equals(result.getStatus())
		    || BehaveUtils.PENDING_TEST_STRING.equals(result.getStatus())) {
			return 1;
		}
		return 0;
	}


	@Override
	public AbstractBuild<?, ?> getOwner() {
		return owner;
	}


	void setOwner(AbstractBuild<?, ?> owner) {
		this.owner = owner;
	}


	@Override
	public TestObject getParent() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public TestResult findCorrespondingResult(String id) {
		// TODO Auto-generated method stub
		return null;
	}


        @Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public float getDuration() {
		return BehaveUtils.durationFromResult(result);
	}


	Match getMatch() {
		return match;
	}


	Result getResult() {
		return result;
	}

}
