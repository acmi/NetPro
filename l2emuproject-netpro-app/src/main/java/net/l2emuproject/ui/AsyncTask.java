/*
 * Copyright (c) 2012-2015, RaveN Network INC. and/or its affiliates. All rights reserved.
 * RAVEN NETWORK INC. PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * Licensed Materials - Property of RaveN Network INC.
 * Restricted Rights - Use, duplication or disclosure restricted.
 */
package net.l2emuproject.ui;

import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import net.l2emuproject.util.logging.L2Logger;

/**
 * <p>
 * AsyncTask enables proper and easy use of the UI thread. This class allows to perform background operations and publish results on the UI thread without having to manipulate threads and/or handlers.
 * </p>
 * <p>
 * AsyncTask is designed to be a helper class around {@link SwingWorker} and does not constitute a generic threading framework. AsyncTasks should ideally be used for short operations (a few seconds at
 * the most.) If you need to keep threads running for long periods of time, it is highly recommended you use the various APIs provided by the <code>java.util.concurrent</code> package such as
 * {@link Executor}, {@link ThreadPoolExecutor} and {@link FutureTask} .
 * </p>
 * <p>
 * An asynchronous task is defined by a computation that runs on a background thread and whose result is published on the UI thread. An asynchronous task is defined by 3 generic types, called
 * <code>Params</code>, <code>Progress</code> and <code>Result</code>, and 4 steps, called <code>onPreExecute</code>, <code>doInBackground</code>, <code>onProgressUpdate</code> and
 * <code>onPostExecute</code>.
 * </p>
 * <h2>Usage</h2>
 * <p>
 * AsyncTask must be subclassed to be used. The subclass will override at least one method ( {@link #doInBackground}), and most often will override a second one ({@link #onPostExecute}.)
 * </p>
 * <p>
 * Here is an example of subclassing:
 * </p>
 * 
 * <pre class="prettyprint">
 * private class DownloadFilesTask extends AsyncTask&lt;URL, Integer, Long&gt;
 * {
 * 	protected Long doInBackground(URL... urls)
 * 	{
 * 		int count = urls.length;
 * 		long totalSize = 0;
 * 		for (int i = 0; i &lt; count; i++)
 * 		{
 * 			totalSize += Downloader.downloadFile(urls[i]);
 * 			publishProgress((int)((i / (float)count) * 100));
 * 			// Escape early if cancel() is called
 * 			if (isCancelled())
 * 				break;
 * 		}
 * 		return totalSize;
 * 	}
 * 	
 * 	protected void onProgressUpdate(Integer... progress)
 * 	{
 * 		setProgressPercent(progress[0]);
 * 	}
 * 	
 * 	protected void onPostExecute(Long result)
 * 	{
 * 		showDialog(&quot;Downloaded &quot; + result + &quot; bytes&quot;);
 * 	}
 * }
 * </pre>
 * <p>
 * Once created, a task is executed very simply:
 * </p>
 * 
 * <pre class="prettyprint">
 * new DownloadFilesTask().execute(url1, url2, url3);
 * </pre>
 * 
 * <h2>AsyncTask's generic types</h2>
 * <p>
 * The three types used by an asynchronous task are the following:
 * </p>
 * <ol>
 * <li><code>Params</code>, the type of the parameters sent to the task upon execution.</li>
 * <li><code>Progress</code>, the type of the progress units published during the background computation.</li>
 * <li><code>Result</code>, the type of the result of the background computation.</li>
 * </ol>
 * <p>
 * Not all types are always used by an asynchronous task. To mark a type as unused, simply use the type {@link Void}:
 * </p>
 * 
 * <pre>
 * private class MyTask extends AsyncTask&lt;Void, Void, Void&gt; { ... }
 * </pre>
 * 
 * <h2>The 4 steps</h2>
 * <p>
 * When an asynchronous task is executed, the task goes through 4 steps:
 * </p>
 * <ol>
 * <li>{@link #onPreExecute()}, invoked on the UI thread before the task is executed. This step is normally used to setup the task, for instance by showing a progress bar in the user interface.</li>
 * <li>{@link #doInBackground}, invoked on the background thread immediately after {@link #onPreExecute()} finishes executing. This step is used to perform background computation that can take a long
 * time. The parameters of the asynchronous task are passed to this step. The result of the computation must be returned by this step and will be passed back to the last step. This step can also use
 * {@link #publish} to publish one or more units of progress. These values are published on the UI thread, in the {@link #process} step.</li>
 * <li>{@link #process}, invoked on the UI thread after a call to {@link #publish}. The timing of the execution is undefined. This method is used to display any form of progress in the user interface
 * while the background computation is still executing. For instance, it can be used to animate a progress bar or show logs in a text field.</li>
 * <li>{@link #onPostExecute}, invoked on the UI thread after the background computation finishes. The result of the background computation is passed to this step as a parameter.</li>
 * </ol>
 * <h2>Cancelling a task</h2>
 * <p>
 * A task can be cancelled at any time by invoking {@link #cancel(boolean)}. Invoking this method will cause subsequent calls to {@link #isCancelled()} to return true. After invoking this method,
 * {@link #onCancelled(Object)}, instead of {@link #onPostExecute(Object)} will be invoked after {@link #doInBackground(Object[])} returns. To ensure that a task is cancelled as quickly as possible,
 * you should always check the return value of {@link #isCancelled()} periodically from {@link #doInBackground(Object[])}, if possible (inside a loop for instance.)
 * </p>
 * <h2>Threading rules</h2>
 * <p>
 * There are a few threading rules that must be followed for this class to work properly:
 * </p>
 * <ul>
 * <li>An instance <b>may</b> be created and it's execute method <b>may</b> be called outside of the UI thread.</li>
 * <li>Do not call {@link #onPreExecute()}, {@link #onPostExecute}, {@link #doInBackground}, {@link #process} manually.</li>
 * <li>The task can be executed only once (an exception will be thrown if a second execution is attempted.)</li>
 * </ul>
 * <h2>Memory observability</h2>
 * <p>
 * AsyncTask guarantees that all callback calls are synchronized in such a way that the following operations are safe without explicit synchronizations.
 * </p>
 * <ul>
 * <li>Set member fields in the constructor or {@link #onPreExecute}, and refer to them in {@link #doInBackground}.
 * <li>Set member fields in {@link #doInBackground}, and refer to them in {@link #process} and {@link #onPostExecute}.
 * </ul>
 * <h2>Order of execution</h2>
 * <p>
 * Each asynchronous task runs in a separate Swing worker thread.
 * </p>
 * 
 * @author savormix (adaptation)
 * @param <Params> type of a single input element
 * @param <Progress> type of the progress update reporting element
 * @param <Result> type of the task result
 */
public abstract class AsyncTask<Params, Progress, Result> extends SwingWorker<Result, Progress>
{
	private static final L2Logger LOG = L2Logger.getLogger(AsyncTask.class);
	
	private Params[] _params;
	private Result _result;
	
	/**
	 * Creates a new asynchronous task. This constructor does not need to be invoked on the UI
	 * thread.
	 */
	public AsyncTask()
	{
	}
	
	/**
	 * Executes the task with the specified parameters. The task returns itself (this) so that the
	 * caller can keep a reference to it.
	 * <p>
	 * Each asynchronous task runs in a separate Swing worker thread.
	 * </p>
	 * <p>
	 * This method may be invoked outside the UI thread.
	 * </p>
	 * 
	 * @param params The parameters of the task.
	 * @return This instance of AsyncTask.
	 * @throws IllegalStateException If {@link #getState()} returns either {@link javax.swing.SwingWorker.StateValue#STARTED} or {@link javax.swing.SwingWorker.StateValue#DONE} .
	 */
	@SafeVarargs
	public final AsyncTask<Params, Progress, Result> execute(Params... params) throws IllegalStateException
	{
		if (getState() != StateValue.PENDING)
			throw new IllegalStateException();
		
		_params = params;
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				onPreExecute();
				execute();
			}
		});
		return this;
	}
	
	@Override
	protected final void done()
	{
		try
		{
			if (isCancelled())
				onCancelled(_result);
			else
				onPostExecute(_result);
		}
		catch (Throwable t)
		{
			LOG.fatal(getClass().getSimpleName(), t);
			throw t;
		}
	}
	
	@Override
	protected final Result doInBackground()
	{
		try
		{
			{
				//final long start = System.nanoTime();
				{
					_result = doInBackground(_params);
				}
				//final long end = System.nanoTime();
				//RunnableStatsManager.handleStats(getClass(), "doInBackground()", end - start);
			}
			return _result;
		}
		catch (Throwable t)
		{
			LOG.fatal(getClass().getSimpleName(), t);
			throw t;
		}
	}
	
	/**
	 * Runs on the UI thread before {@link #doInBackground}.
	 * 
	 * @see #onPostExecute
	 * @see #doInBackground
	 */
	protected void onPreExecute()
	{
		// overridden when necessary
	}
	
	/**
	 * Override this method to perform a computation on a background thread. The specified
	 * parameters are the parameters passed to {@link #execute} by the caller of this task. This
	 * method can call {@link #publish} to publish updates on the UI thread.
	 * 
	 * @param params The parameters of the task.
	 * @return A result, defined by the subclass of this task.
	 * @see #onPreExecute()
	 * @see #onPostExecute
	 * @see #publish(Object...)
	 */
	@SuppressWarnings("unchecked")
	protected abstract Result doInBackground(Params... params);
	
	/**
	 * <p>
	 * Runs on the UI thread after {@link #doInBackground}. The specified result is the value returned by {@link #doInBackground}.
	 * </p>
	 * <p>
	 * This method won't be invoked if the task was cancelled.
	 * </p>
	 * 
	 * @param result The result of the operation computed by {@link #doInBackground}.
	 * @see #onPreExecute
	 * @see #doInBackground
	 * @see #onCancelled(Object)
	 */
	protected void onPostExecute(Result result)
	{
		// overridden when necessary
	}
	
	/**
	 * <p>
	 * Runs on the UI thread after {@link #cancel(boolean)} is invoked and {@link #doInBackground(Object[])} has finished.
	 * </p>
	 * <p>
	 * The default implementation simply invokes {@link #onCancelled()} and ignores the result. If you write your own implementation, do not call <code>super.onCancelled(result)</code>.
	 * </p>
	 * 
	 * @param result The result, if any, computed in {@link #doInBackground(Object[])}, can be null
	 * @see #cancel(boolean)
	 * @see #isCancelled()
	 */
	protected void onCancelled(Result result)
	{
		onCancelled();
	}
	
	/**
	 * <p>
	 * Applications should preferably override {@link #onCancelled(Object)}. This method is invoked by the default implementation of {@link #onCancelled(Object)}.
	 * </p>
	 * <p>
	 * Runs on the UI thread after {@link #cancel(boolean)} is invoked and {@link #doInBackground(Object[])} has finished.
	 * </p>
	 * 
	 * @see #onCancelled(Object)
	 * @see #cancel(boolean)
	 * @see #isCancelled()
	 */
	protected void onCancelled()
	{
		// overridden when necessary
	}
}
