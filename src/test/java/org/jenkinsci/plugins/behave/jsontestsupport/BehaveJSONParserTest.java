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

import hudson.model.TaskListener;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class BehaveJSONParserTest {


	//@Test
	public void testParsing() throws Exception {
		BehaveJSONParser parser = new BehaveJSONParser();
		
		File f = getResourceAsFile("ScenarioResultTest/behave_examples_java-calculator__behave-report.json");
		
		List<File> files = new ArrayList<>();
		files.add(f);
		
		TaskListener mockListener = Mockito.mock(TaskListener.class);
		Mockito.when(mockListener.getLogger()).thenReturn(System.out);
		
		BehaveTestResult testresult = parser.parse(files, mockListener);

		assertThat("Tests passed", testresult.isPassed(), is(true));
		assertThat("Correct # of passing tests", testresult.getPassCount(), is(8));
		assertThat("Correct # of failing tests", testresult.getFailCount(), is(0));
		assertThat("Correct # of skipped tests", testresult.getSkipCount(), is(0));
		assertThat("Duration is correct", testresult.getDuration(), is(0.13143785F));
		assertThat("Duration string is correct", testresult.getDurationString(), is("0.13 sec"));
		assertThat("Correct # of children", testresult.getChildren(), hasSize(3));
		assertThat("Correct # of features", testresult.getFeatures(), hasSize(3));

		// Get the individual Features and check their scenarios.
	}

	//@Test
	public void testBackgroundFailure() throws Exception {
		BehaveJSONParser parser = new BehaveJSONParser();

		File f = getResourceAsFile("ScenarioResultTest/backgroundFailure.json");

		List<File> files = new ArrayList<>();
		files.add(f);

		TaskListener mockListener = Mockito.mock(TaskListener.class);
		Mockito.when(mockListener.getLogger()).thenReturn(System.out);

		BehaveTestResult testresult = parser.parse(files, mockListener);

		assertThat("Test should failed", testresult.isPassed(), is(false));

		assertThat("Correct # of passing tests", testresult.getPassCount(), is(7));
		assertThat("Correct # of failing tests", testresult.getFailCount(), is(1));
		assertThat("Correct # of skipped tests", testresult.getSkipCount(), is(0));
		assertThat("Duration is correct", testresult.getDuration(), is(0.33427134F));
		assertThat("Duration string is correct", testresult.getDurationString(), is("0.33 sec"));
		assertThat("Correct # of children", testresult.getChildren(), hasSize(3));
		assertThat("Correct # of features", testresult.getFeatures(), hasSize(3));

		// Get the individual Features and check their scenarios.
	}

	//@Test
	public void testPendingStep() throws Exception {
		BehaveJSONParser parser = new BehaveJSONParser();

		File f = getResourceAsFile("ScenarioResultTest/pending.json");

		List<File> files = new ArrayList<>();
		files.add(f);

		TaskListener mockListener = Mockito.mock(TaskListener.class);
		Mockito.when(mockListener.getLogger()).thenReturn(System.out);

		BehaveTestResult testresult = parser.parse(files, mockListener);

		assertThat("result should be pass", testresult.isPassed(), is(true));

		assertThat("Correct # of passing tests", testresult.getPassCount(), is(0));
		assertThat("Correct # of failing tests", testresult.getFailCount(), is(0));
		assertThat("Correct # of skipped tests", testresult.getSkipCount(), is(1));
		assertThat("Duration is correct", testresult.getDuration(), is(0.100813F));
		assertThat("Duration string is correct", testresult.getDurationString(), is("0.1 sec"));
		assertThat("Correct # of children", testresult.getChildren(), hasSize(1));
		assertThat("Correct # of features", testresult.getFeatures(), hasSize(1));

		// Get the individual Features and check their scenarios.
	}

	//@Test
	public void testUndefinedStep() throws Exception {
		BehaveJSONParser parser = new BehaveJSONParser();

		File f = getResourceAsFile("ScenarioResultTest/undefinedStep.json");

		List<File> files = new ArrayList<>();
		files.add(f);

		TaskListener mockListener = Mockito.mock(TaskListener.class);
		Mockito.when(mockListener.getLogger()).thenReturn(System.out);

		BehaveTestResult testresult = parser.parse(files, mockListener);

		assertThat("result should be failure", testresult.isPassed(), is(false));

		assertThat("Correct # of passing tests", testresult.getPassCount(), is(7));
		assertThat("Correct # of failing tests", testresult.getFailCount(), is(1));
		assertThat("Correct # of skipped tests", testresult.getSkipCount(), is(0));
		assertThat("Duration is correct", testresult.getDuration(), is(0.023931958F));
		assertThat("Duration string is correct", testresult.getDurationString(), is("23 ms"));
		assertThat("Correct # of children", testresult.getChildren(), hasSize(3));
		assertThat("Correct # of features", testresult.getFeatures(), hasSize(3));

		// Get the individual Features and check their scenarios.
	}

	//@Test
	public void testEmbededItem() throws Exception {
		BehaveJSONParser parser = new BehaveJSONParser();

		File f = getResourceAsFile("ScenarioResultTest/behave-embedded-item.json");

		List<File> files = new ArrayList<>();
		files.add(f);

		TaskListener mockListener = Mockito.mock(TaskListener.class);
		Mockito.when(mockListener.getLogger()).thenReturn(System.out);

		BehaveTestResult testresult = parser.parse(files, mockListener);
		assertThat("Embedded items found", testresult.getFeatures().iterator().next().getChildren().iterator().next()
		                                   .getEmbeddedItems(), hasSize(1));
	}
    
    @Test
    public void testBehaveSample() throws Exception {
        BehaveJSONParser parser = new BehaveJSONParser();
        
        File f = getResourceAsFile("ScenarioResultTest/behave-sample.json");
        
        List<File> files = new ArrayList<>();
        files.add(f);
        
        TaskListener mockListener = Mockito.mock(TaskListener.class);
        Mockito.when(mockListener.getLogger()).thenReturn(System.out);
        
        BehaveTestResult testresult = parser.parse(files, mockListener);
        assertEquals(testresult.getFeatures().iterator().next().getDisplayName(),
                "Start all SAS services across an environment");
    }

	private static File getResourceAsFile(String resource) throws Exception {
		URL url = BehaveJSONParserTest.class.getResource(resource);
		Assert.assertNotNull("Resource " + resource + " could not be found", url);
		File f = new File(url.toURI());
		return f;
	}
	
}


