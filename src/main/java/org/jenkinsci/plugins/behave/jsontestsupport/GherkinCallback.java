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

import behave.gherkin.formatter.Formatter;
import behave.gherkin.formatter.Reporter;
import behave.gherkin.formatter.model.Background;
import behave.gherkin.formatter.model.Examples;
import behave.gherkin.formatter.model.Feature;
import behave.gherkin.formatter.model.Match;
import behave.gherkin.formatter.model.Result;
import behave.gherkin.formatter.model.Scenario;
import behave.gherkin.formatter.model.ScenarioOutline;
import behave.gherkin.formatter.model.Step;
import behave.gherkin.formatter.model.Tag;
import hudson.model.TaskListener;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The implementation that gets called back by the Gherkin parser.
 * 
 * @author James Nord
 */
class GherkinCallback implements Formatter, Reporter {

	private static final Logger LOG = Logger.getLogger(GherkinCallback.class.getName());
	private boolean ignoreBadSteps = false;
	private TaskListener listener = null;

	private FeatureResult currentFeatureResult = null;
	private ScenarioResult currentScenarioResult = null;
	private BackgroundResult currentBackground = null;

	private Step currentStep = null;
	private Match currentMatch = null;

	private String currentURI = null;

	private BehaveTestResult testResult;


	GherkinCallback(BehaveTestResult testResult) {
		this.testResult = testResult;
	}


	GherkinCallback(BehaveTestResult testResult, TaskListener listener, boolean ignoreBadSteps){
		this(testResult);
		this.listener = listener;
		this.ignoreBadSteps = ignoreBadSteps;
	}

	// Formatter implementation

	// called before a feature to identify the feature
        @Override
	public void uri(String uri) {
		LOG.log(Level.FINE, "URI: {0}", uri);
		if (currentURI != null) {
			LOG.log(Level.SEVERE, "URI received before previous uri handled");
			throw new BehaveModelException("URI received before previous uri handled");
		}
		currentURI = uri;
	}


        @Override
	public void feature(Feature feature) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.log(Level.FINE, "Feature: {0}{1}", new Object[]{feature.getKeyword(), feature.getName()});
			List<Tag> tags = feature.getTags();
			for (Tag tag : tags) {
				LOG.log(Level.FINE, "         {0}", tag.getName());
			}
			LOG.log(Level.FINE, "         {0}", feature.getDescription());
		}
		// a new feature being received signals the end of the previous feature
		currentFeatureResult = new FeatureResult(currentURI, feature);
		currentURI = null;
		testResult.addFeatureResult(currentFeatureResult);
	}


	// applies to a scenario
        @Override
	public void background(Background background) {
		LOG.log(Level.FINE, "Background: {0}", background.getName());
		if (currentBackground != null) {
			LOG.log(Level.SEVERE, "Background: '{'{0}'}' received before previous background: '{'{1}'}' handled", new Object[]{background.getName(), currentBackground.getName()});
			throw new BehaveModelException("Background: {" + background.getName() + "} received before previous background: {" + currentBackground.getName()+ "} handled");
		}
		currentBackground = new BackgroundResult(background);
	}


        @Override
	public void scenario(Scenario scenario) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.log(Level.FINE, "Scenario: {0} {1}", new Object[]{scenario.getKeyword(), scenario.getName()});
			List<Tag> tags = scenario.getTags();
			for (Tag tag : tags) {
				LOG.log(Level.FINE, "         {0}", tag.getName());
			}
			LOG.log(Level.FINE, "          {0}", scenario.getDescription());
			LOG.log(Level.FINE, "          {0}", scenario.getComments());
		}
		// a new scenario signifies that the previous scenario has been handled.
		currentScenarioResult = new ScenarioResult(scenario, currentBackground);
		currentBackground = null;
		currentFeatureResult.addScenarioResult(currentScenarioResult);
	}


	// appears to not be called.
        @Override
	public void scenarioOutline(ScenarioOutline scenarioOutline) {
		LOG.log(Level.FINE, "ScenarioOutline: {0}", scenarioOutline.getName());
	}


	// appears to not be called.
        @Override
	public void examples(Examples examples) {
		// not stored in the json - used in the Gherkin only
		LOG.log(Level.FINE, "Examples: {0}", examples.getName());
	}

	// appears to not be called.
        @Override
	 public void startOfScenarioLifeCycle(Scenario scenario) {
		 LOG.log(Level.FINE, "startOfScenarioLifeCycle: {0}", scenario.getName());
	}

	// appears to not be called.
        @Override
	public void endOfScenarioLifeCycle(Scenario scenario) {
		LOG.log(Level.FINE, "endOfScenarioLifeCycle: {0}", scenario.getName());
	}

	// A step has been called - could be in a background or a Scenario
        @Override
	public void step(Step step) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.log(Level.FINE, "Step: {0} {1}", new Object[]{step.getKeyword(), step.getName()});
			LOG.log(Level.FINE, "      {0}", step.getRows());
			// logger.fine("      " + step.getStackTraceElement());
		}
		if (currentStep != null) {
			String error = "Step: {" + step.getKeyword() + "} name: {" + step.getName() +
					"} received before previous step: {" + step.getKeyword() + "} name: {" + step.getName() +
					"} handled! Maybe caused by broken JSON, see #JENKINS-21835";
			listener.error(error);
			LOG.log(Level.SEVERE, error);
			if (!ignoreBadSteps) {
				throw new BehaveModelException(error);
			}
		}
		currentStep = step;
	}

	// marks the end of a feature
        @Override
	public void eof() {
		LOG.log(Level.FINE, "eof");
		currentFeatureResult = null;
		currentScenarioResult = null;
		currentBackground = null;
		currentStep = null;
		currentURI = null;
	}


        @Override
	public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
		LOG.log(Level.SEVERE, "syntaxError: - Failed to parse Gherkin json file.");
		StringBuilder sb = new StringBuilder("Failed to parse Gherkin json file.");
		sb.append("\tline: ").append(line);
		sb.append("\turi: ").append(uri);
		sb.append("\tState: ").append(state);
		sb.append("\tEvent: ").append(event);
		throw new BehaveModelException(sb.toString());
	}

        @Override
	public void done() {
		// appears to not be called?
		LOG.log(Level.FINE, "done");
	}

        @Override
	public void close() {
		// appears to not be called?
		LOG.log(Level.FINE, "close");
	}


	// Reporter implementation.

	// applies to a scenario - any code that is tagged as @Before
        @Override
	public void before(Match match, Result result) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.log(Level.FINE, "rep  before match: {0}", match.getLocation());
			LOG.log(Level.FINE, "rep        result : " + "(passed) {0}", Result.PASSED.equals(result.getStatus()));
			LOG.log(Level.FINE, "rep        result : {0}", result.getDuration());
			LOG.log(Level.FINE, "rep        result : {0}", BehaveUtils.listToString(result.getErrorMessage()));
			LOG.log(Level.FINE, "rep        result : {0}", result.getError());
		}
		currentScenarioResult.addBeforeResult(new BeforeAfterResult(match, result));
	}


	// applies to a step, may be in a scenario or a background
        @Override
	public void result(Result result) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.log(Level.FINE, "rep  result: " + "(passed) {0}", Result.PASSED.equals(result.getStatus()));
			LOG.log(Level.FINE, "rep          {0}", result.getDuration());
			LOG.log(Level.FINE, "rep          {0}", BehaveUtils.listToString(result.getErrorMessage()));
			LOG.log(Level.FINE, "rep          {0}", result.getError());
		}
		StepResult stepResult = new StepResult(currentStep, currentMatch, result);
		if (currentBackground != null) {
			currentBackground.addStepResult(stepResult);
		}
		else {
			currentScenarioResult.addStepResult(stepResult);
		}
		currentStep = null;
		currentMatch = null;
	}


	// applies to a scenario - any code that is tagged as @After
        @Override
	public void after(Match match, Result result) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.log(Level.FINE, "rep  after match  : {0}", match.getLocation());
			LOG.log(Level.FINE, "rep        result : " + "(passed) {0}", Result.PASSED.equals(result.getStatus()));
			LOG.log(Level.FINE, "rep        result : {0}", result.getDuration());
			LOG.log(Level.FINE, "rep        result : {0}", BehaveUtils.listToString(result.getErrorMessage()));
			LOG.log(Level.FINE, "rep        result : {0}", result.getError());
		}
		currentScenarioResult.addAfterResult(new BeforeAfterResult(match, result));
	}


	// applies to a step
        @Override
	public void match(Match match) {
		// applies to a step.
		LOG.log(Level.FINE, "rep  match: {0}", match.getLocation());
		if (currentMatch != null) {
			LOG.log(Level.SEVERE, "Match: {0} received before previous Match: {1}handled", new Object[]{match.getLocation(), currentMatch.getLocation()});
			throw new BehaveModelException("Match: " + match.getLocation() + " received before previous Match: " +
					currentMatch.getLocation()+ "handled");
		}
		currentMatch = match;
	}


        @Override
	public void embedding(String mimeType, byte[] data) {
		LOG.log(Level.FINE, "rep  embedding: {0}", mimeType);
		try {
			File f = BehaveUtils.createEmbedFile(data);
			EmbeddedItem embed = new EmbeddedItem(mimeType, f.getName());
			currentScenarioResult.addEmbeddedItem(embed);
		}
		catch (IOException ex) {
			throw new BehavePluginException("Failed to write embedded data to temporary file", ex);
		}
	}


        @Override
	public void write(String text) {
		LOG.log(Level.FINE, "rep  write: {0}", text);
	}

}
