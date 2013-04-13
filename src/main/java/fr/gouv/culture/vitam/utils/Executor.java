/**
 * This file is part of Vitam Project.
 * 
 * Copyright 2010, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Vitam Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Vitam is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Vitam. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.gouv.culture.vitam.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;


/**
 * Executor of external commands
 * 
 * @author Frederic Bregier
 *
 */
public class Executor {


	private static class CheckEndOfExecute extends TimerTask
	{
		final AtomicBoolean finished;
		final ExecuteWatchdog watchdog;
		final String realProcess;
		
		public CheckEndOfExecute(AtomicBoolean finished, ExecuteWatchdog watchdog, 
				String realProcess) {
			this.finished = finished;
			this.watchdog = watchdog;
			this.realProcess = realProcess;
		}
		@Override
		public void run() {
			if (this.finished.get()) {
				return;
			}
			System.err.println("Process too long...");
			if (this.realProcess != null) {
				killProcess(realProcess);
			}
			this.watchdog.destroyProcess();
		}
		
	}

	/**
	 * Execute an external command
	 * @param cmd
	 * @param tempDelay
	 * @param correctValues
	 * @param showOutput
	 * @param realCommand
	 * @return correctValues if ok, < 0 if an execution error occurs, or other error values
	 */
	public static int exec(List<String> cmd, long tempDelay, int [] correctValues, 
			boolean showOutput, String realCommand) {
		// Create command with parameters
	    CommandLine commandLine = new CommandLine(cmd.get(0));
	    for (int i = 1; i < cmd.size(); i ++) {
	        commandLine.addArgument(cmd.get(i));
	    }
	    DefaultExecutor defaultExecutor = new DefaultExecutor();
	    ByteArrayOutputStream outputStream;
	    outputStream = new ByteArrayOutputStream();
	    PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream);
	    defaultExecutor.setStreamHandler(pumpStreamHandler);
	    
	    defaultExecutor.setExitValues(correctValues);
	    AtomicBoolean isFinished = new AtomicBoolean(false);
	    ExecuteWatchdog watchdog = null;
	    Timer timer = null;
	    if (tempDelay > 0) {
	        // If delay (max time), then setup Watchdog
	    	timer = new Timer(true);
	        watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
	        defaultExecutor.setWatchdog(watchdog);
	        CheckEndOfExecute endOfExecute = new CheckEndOfExecute(isFinished, watchdog, 
	        		realCommand);
	    	timer.schedule(endOfExecute, tempDelay);
	    }
	    int status = -1;
	    try {
	        // Execute the command
	        status = defaultExecutor.execute(commandLine);
	    } catch (ExecuteException e) {
	        if (e.getExitValue() == -559038737) {
	            // Cannot run immediately so retry once
	            try {
	                Thread.sleep(100);
	            } catch (InterruptedException e1) {
	            }
	            try {
	                status = defaultExecutor.execute(commandLine);
	            } catch (ExecuteException e1) {
	                pumpStreamHandler.stop();
	            	System.err.println(StaticValues.LBL.error_error.get() +
	                		"Exception: " + e.getMessage() +
	                        " Exec in error with " + commandLine.toString() 
	                        + "\n\t" + outputStream.toString());
	                status = -2;
	                try {
	                    outputStream.close();
	                } catch (IOException e2) {
	                }
	                return status;
	            } catch (IOException e1) {
	                pumpStreamHandler.stop();
	            	System.err.println(StaticValues.LBL.error_error.get() +
	                		"Exception: " + e.getMessage() +
	                        " Exec in error with " + commandLine.toString()
	                        + "\n\t" + outputStream.toString());
	                status = -2;
	                try {
	                    outputStream.close();
	                } catch (IOException e2) {
	                }
	                return status;
	            }
	        } else {
	            pumpStreamHandler.stop();
	        	System.err.println(StaticValues.LBL.error_error.get() +
	            		"Exception: " + e.getMessage() +
	                    " Exec in error with " + commandLine.toString()
	                    + "\n\t" + outputStream.toString());
	            status = -2;
	            try {
	                outputStream.close();
	            } catch (IOException e2) {
	            }
	            return status;
	        }
	    } catch (IOException e) {
	        pumpStreamHandler.stop();
	    	System.err.println(StaticValues.LBL.error_error.get() +
	        		"Exception: " + e.getMessage() +
	                " Exec in error with " + commandLine.toString()
	                + "\n\t" + outputStream.toString());
	        status = -2;
	        try {
	            outputStream.close();
	        } catch (IOException e2) {
	        }
	        return status;
	    } finally {
	        isFinished.set(true);
	    	if (timer != null) {
	    		timer.cancel();
	    	}
	    	try {
	            Thread.sleep(200);
	        } catch (InterruptedException e1) {
	        }
	    }
	    pumpStreamHandler.stop();
	    if (defaultExecutor.isFailure(status) && watchdog != null) {
	    	if (watchdog.killedProcess()) {
	            // kill by the watchdoc (time out)
	            if (showOutput) {
	            	System.err.println(StaticValues.LBL.error_error.get() +
	            		"Exec is in Time Out");
	            }
	    	}
	        status = -3;
	        try {
	            outputStream.close();
	        } catch (IOException e2) {
	        }
	    } else {
	        if (showOutput) {
	        	System.out.println("Exec: " + outputStream.toString());
	        }
	        try {
	            outputStream.close();
	        } catch (IOException e2) {
	        }
	    }
	    return status;
	}

	public static void killProcess(String command) {
		if (command != null) {
			// force kill in case
			ArrayList<String> kill = new ArrayList<String>();
			String osName = SystemPropertyUtil.get("os.name").toLowerCase();
			if (osName.indexOf("win") >= 0) {
				kill.add("taskkill");
				kill.add("/F");
				kill.add("/IM");
				kill.add(command);
			} else {
				kill.add("killall");
				kill.add(command);
			}
			exec(kill, 2000, new int[] { 0, 128 }, false, null);
		}
	
	}
}
