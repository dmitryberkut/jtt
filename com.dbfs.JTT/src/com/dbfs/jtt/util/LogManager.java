package com.dbfs.jtt.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.Preferences;

import com.dbfs.jtt.Application;
import com.dbfs.jtt.JTTVersion;
import com.dbfs.jtt.preferences.LoggingPreferencePage;

public class LogManager extends Thread {
	private boolean running;
	private static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	private final String filePath = "log" + File.separatorChar;
	private String fileName = "";
	private FileWriter fileWriter;
	private BufferedWriter bufferedWriter;
	private static Date date = new Date();
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS");
	private static DateFormat dateFormatFileName = new SimpleDateFormat("yyyyMMddHHmmss");
	static private long maxSize;

	@Override
	public void run() {
		int curSize = 0;
		while (running) {
			String data = "";
			try {
				data = queue.take();
			} catch (InterruptedException e3) {
				System.out.println(e3);
			}
			curSize += data.getBytes().length;
			if ((maxSize > 0) && (curSize > maxSize)) {
				setFileName();
				try {
					bufferedWriter.write(dateFormat.format(date) + " [" + Thread.currentThread().getName() + "][" + Thread.currentThread().getId() + "][INFO] Log limit has been achieved (bytes " + (curSize - data.getBytes().length) + "). New file will be created (" + fileName + ")");
					bufferedWriter.close();
					fileWriter.close();
				} catch (IOException e) {
					System.out.println(e);
				}
				openFile();
				curSize = 0;
			}
			try {
				bufferedWriter.write(data + SWT.LF);
				if (queue.isEmpty()) {
					bufferedWriter.flush();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
		try {
			bufferedWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public static void addLog(String msg) {
		queue.add(msg);
	}

	public LogManager() {
		super("LoggingManager");
		running = true;
		setFileName();
		openFile();
		start();
	}

	@SuppressWarnings("deprecation")
	public static void logStack(StackTraceElement[] stackElements, String exName) {
		ThreadGroup currentThreadGroup = Thread.currentThread().getThreadGroup();
		Thread[] threads = new Thread[currentThreadGroup.activeCount()];
		currentThreadGroup.enumerate(threads, false);
		for (int i = 0; i < threads.length; i++) {
			if (!threads[i].getName().equals("LoggingManager") && !threads[i].getName().equals(Thread.currentThread().getName())) {
				threads[i].suspend();
			}
		}
		String currentStack = "ExceptionType: " + exName + SWT.LF + "ExceptionStack:" + SWT.LF;
		for (StackTraceElement stackElement : stackElements) {
			currentStack += SWT.TAB + stackElement.toString() + SWT.LF;
		}
		log(Thread.currentThread().getName(), Thread.currentThread().getId(), currentStack, "EXCEPTION");

		for (int i = 0; i < threads.length; i++) {
			if (!threads[i].getName().equals("LoggingManager") && !threads[i].getName().equals(Thread.currentThread().getName())) {
				StackTraceElement stackElements1[] = threads[i].getStackTrace();
				currentStack = "Stack for '" + threads[i].getName() + "' thread:" + SWT.LF;
				for (int z = 0; z < (stackElements1.length - 1); z++) {
					currentStack += SWT.TAB + stackElements1[z].toString() + SWT.LF;
				}
				currentStack += SWT.TAB + stackElements1[stackElements1.length - 1].toString();
				log(threads[i].getName(), threads[i].getId(), currentStack, "DEBUG");
				threads[i].resume();
			}
		}

		if (isDialogs()) {
			String currentStack1 = "ExceptionType: " + exName + SWT.LF + "ExceptionStack:" + SWT.LF;
			for (StackTraceElement stackElement : stackElements) {
				currentStack1 += SWT.TAB + stackElement.toString() + SWT.LF;
			}
			final String curStack = currentStack1;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageBox messageBox = new MessageBox(new Shell(Display.getDefault(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL), SWT.APPLICATION_MODAL | SWT.ICON_ERROR | SWT.OK);
					messageBox.setText("EXCEPTION");
					messageBox.setMessage(curStack);
					messageBox.open();
				}
			});
		}

	}

	public static void logStack(Exception e) {
		logStack(e.getStackTrace(), e.toString());
	}

	public static void logStack(Throwable e) {
		logStack(e.getStackTrace(), e.toString());
	}

	/***
	 * private static boolean isLogging() {
	 * 
	 * @SuppressWarnings("deprecation") Preferences preferences = new ConfigurationScope().getNode(Application.PLUGIN_ID); return preferences.getBoolean(LoggingPreferencePage.IS_LOGGING, false); }
	 */

	/*** temporary logging */
	private static boolean isLogging() {
		return true;
	}

	private static boolean isDialogs() {
		@SuppressWarnings("deprecation")
		Preferences preferences = new ConfigurationScope().getNode(Application.PLUGIN_ID);
		return preferences.getBoolean(LoggingPreferencePage.IS_DIALOGS, false);
	}

	public static void log(Level level, String fromLogger, String msg) {
		if (isLogging()) {
			date = new Date();
			addLog(dateFormat.format(date) + " [" + Thread.currentThread().getName() + "][" + Thread.currentThread().getId() + "][" + level.getName() + "] " + msg);
		}
	}

	public static void log(String threadName, long threadId, String msg, String level) {
		date = new Date();
		addLog(dateFormat.format(date) + " [" + threadName + "][" + threadId + "][" + level + "] " + msg);
	}

	public static void info(String msg) {
		if (isLogging()) {
			date = new Date();
			addLog(dateFormat.format(date) + " [" + Thread.currentThread().getName() + "][" + Thread.currentThread().getId() + "][INFO] " + msg);
		}
	}

	public static void setMaxSize(int maxsize) {
		maxSize = maxsize * 1024 * 1024;
	}

	private void openFile() {
		File file = new File(filePath + fileName);
		try {
			fileWriter = new FileWriter(file, true);
		} catch (IOException e) {
		}
		bufferedWriter = new BufferedWriter(fileWriter);
	}

	private void setFileName() {
		date = new Date();
		String processName = ManagementFactory.getRuntimeMXBean().getName();
		fileName = "JiraTimeTracker_" + dateFormatFileName.format(date) + processName.split("@")[0] + ".log";
	}

	@Override
	public void destroy() {
		running = false;
		queue.add(new String(""));
		try {
			join();
		} catch (InterruptedException e) {
			LogManager.logStack(e);
		}
	}

	public static String getConfigurationInfo() {
		String info = "";
		info += "JRE Version: " + System.getProperty("java.version") + ";" + "\nApp Version: " + JTTVersion.getText() + "\nWorking Dir: " + System.getProperty("user.dir");
		info += ".";
		return info;
	}
}
