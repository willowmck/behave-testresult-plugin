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

import hudson.XmlFile;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.junit.TestResult;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.util.HeapSpaceStringConverter;
import hudson.util.XStream2;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.export.Exported;

import com.thoughtworks.xstream.XStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link Action} that displays the Behave test result.
 *
 * <p>
 * The actual test reports are isolated by {@link WeakReference}
 * so that it doesn't eat up too much memory.
 *
 * @author James Nord
 * @author Kohsuke Kawaguchi (original junit support)
 */
public final class BehaveTestResultAction extends AbstractTestResultAction<BehaveTestResultAction> implements StaplerProxy {

   private static final Logger LOGGER = Logger.getLogger(BehaveTestResultAction.class.getName());

   private static final XStream XSTREAM = new XStream2();

   private transient WeakReference<BehaveTestResult> result;
   
   private final Lock resultLock = new ReentrantLock();
   
   private int totalCount = -1;
	private int failCount = -1;
	private int skipCount = -1;

	static {
     XSTREAM.alias("result",BehaveTestResult.class);
      //XSTREAM.alias("suite",SuiteResult.class);
      //XSTREAM.alias("case",CaseResult.class);
      //XSTREAM.registerConverter(new HeapSpaceStringConverter(),100);
      
       XSTREAM.registerConverter(new HeapSpaceStringConverter(),100);
   }


	
	public BehaveTestResultAction(AbstractBuild owner, BehaveTestResult result, BuildListener listener) {
		super(owner);
		setResult(result, listener);
	}
	
   /**
    * Overwrites the {@link BehaveTestResult} by a new data set.
     * @param result the behave test result
     * @param listener the build listener
    */
   public void setResult(BehaveTestResult result, BuildListener listener) {
       
        resultLock.lock();
        try {
            totalCount = result.getTotalCount();
            failCount = result.getFailCount();
            skipCount = result.getSkipCount();
            // persist the data
            try {
                getDataFile().write(result);
            } catch (IOException ex) {
                ex.printStackTrace(listener.fatalError("Failed to save the Behave test result."));
                LOGGER.log(Level.WARNING, "Failed to save the Behave test result.", ex);
            }
            this.result = new WeakReference<>(result);
        } finally {
               resultLock.unlock();
       }
   }
	
   private XmlFile getDataFile() {
      return new XmlFile(XSTREAM,new File(owner.getRootDir(), "behaveResult.xml"));
  }

   /**
    * Loads a {@link TestResult} from disk.
    */
   private BehaveTestResult load() {
   	BehaveTestResult r;
       try {
           r = (BehaveTestResult)getDataFile().read();
       } catch (IOException e) {
           LOGGER.log(Level.WARNING, "Failed to load " + getDataFile(), e);
           r = new BehaveTestResult(); // return a dummy
       }
       r.tally();
       r.setOwner(this.owner);
       return r;
   }
   
	@Override
   @Exported(visibility = 2)
   public int getFailCount() {
		return failCount;
	}

	@Override
   @Exported(visibility = 2)
   public int getTotalCount() {
		return totalCount;
	}

	@Override
	@Exported(visibility = 2)
   public int getSkipCount() {
		return skipCount;
	}
	

	@Exported(visibility = 5)
    @Override
	public BehaveTestResult getResult() {
        resultLock.lock();
        BehaveTestResult r;
        try {
           if (result == null) {
               r = load();
               result = new WeakReference<>(r);
           }
           else {
               r = result.get();
           }
           
           if (r == null) {
               r = load();
               result = new WeakReference<>(r);
           }
           
           if (totalCount == -1) {
               totalCount = r.getTotalCount();
               failCount = r.getFailCount();
               skipCount = r.getSkipCount();
           }
        } finally {
           resultLock.unlock();
        }
		return r;
	}
	
// Can't do this as AbstractTestResult is not generic!!!
//	@Override
//	public Collection<ScenarioResult> getFailedTests() {
//		return getResult().getFailedTests();
//	};
	
   @Override
	public Object getTarget() {
	   return getResult();
   }


   @Override
    public String getDisplayName() {
       return "Behave Test Result";
   }

   @Override
    public  String getUrlName() {
       return "BehaveTestReport";
   }
}
