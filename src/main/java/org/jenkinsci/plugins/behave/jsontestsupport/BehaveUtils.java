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

import behave.gherkin.formatter.model.Result;
import behave.gherkin.formatter.model.TagStatement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BehaveUtils {

	private static final Logger LOG = Logger.getLogger(BehaveUtils.class.getName());

	public static final String FAILED_TEST_STRING = "failed";
	public static final String PASSED_TEST_STRING = "passed";
	public static final String UNDEFINED_TEST_STRING = "undefined";
	public static final String PENDING_TEST_STRING = "pending";
	public static final String SKIPPED_TEST_STRING = "skipped";


	/** Get the duration (in seconds) that the result took. */
	static float durationFromResult(Result result) {
		// internally this is in nanosecodes
		Long l = result.getDuration();
		if (l == null) {
			return 0.0f;
		}
		return l.floatValue() / 1000000000.0f;
	}
    
    public static String listToString(List<String> stringList) {
        StringBuilder sb = new StringBuilder();
        for (String s : stringList) 
            sb.append(s);
        return sb.toString();
    }


	/**
	 * Get the ID from the TagStatement. For some reason the authors of behave thought the ID should be
	 * private with no getter... TODO - create a patch for behave so we do not need this hack.
	 * 
	 * @param stmt the {@link TagStatement} with the ID
	 * @return the ID of the {@link TagStatement} - possibly <code>null</code>
	 */
	public static String getId(TagStatement stmt) {
		try {
			Field f = TagStatement.class.getField("id");
			f.setAccessible(true);
			return (String) f.get(stmt);
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			LOG.log(Level.WARNING, "Could not get ID from statement: " + stmt.getName(), e);
		}
		return null;
	}


	/**
	 * Create a temporary file on the slave to store the embedded content
	 * 
         * @param data the data
         * @return a file
	 * @throws IOException if we couldn't create a temporary file
	 */
	public static File createEmbedFile(byte[] data) throws IOException {
		File f = File.createTempFile("cuke_", ".embed");
		{
			try (FileOutputStream fos = new FileOutputStream(f)) {
				fos.write(data);
				fos.flush();
			}
		}
		return f;
	}
}
