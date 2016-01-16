package com.dbfs.jtt;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.dbfs.jtt.model.DailyTotalCounter;
import com.dbfs.jtt.model.SOAPSession;
import com.dbfs.jtt.model.Task;
import com.dbfs.jtt.swt.HeadComposite;
import com.dbfs.jtt.swt.TasksComposite;
import com.dbfs.jtt.util.LogManager;
import com.eclipsesource.ui.partblockmonitor.internal.IResponsiveRunner;
import com.eclipsesource.ui.partblockmonitor.internal.NiftyProgress;
import com.eclipsesource.ui.partblockmonitor.internal.TransparentUIBlocker;

public class TasksView extends ViewPart {
	private static final String FEEDBACK_LINK = "https://github.com/dmitryberkut/jtt/issues";
	private final Logger logger = Logger.getLogger(TasksView.class.getName());
	public static final String ID = "com.dbfs.JTT.views.tasks";
	private List<Task> tasks;
	private Map<String, List<Task>> projects;
	private TasksComposite tasksComposite;
	private HeadComposite headComposite;
	private String userName;
	private final static String PRJ_KEY_REGEX = "(\\S+) - ";
	private final Pattern patternPrjKey = Pattern.compile(PRJ_KEY_REGEX);
	private Link link;
	private Label lblDayTime;
	private final DailyTotalCounter dailyTotalCounter;
	private ProgressBar progressBar;
	public static final ExecutorService pool = Executors.newCachedThreadPool();
	private int indx;
	private Queue<Task> taskPool;
	private Slider slider;
	private final int NUM_OF_UPDATE_THREADS = 7;
	private final int SYNC_PAUSE = 60000;
	private final boolean isSync = true;
	private Job syncJob;
	private int alpha;
	TransparentUIBlocker uiBlocker;

	public TasksView() {
		super();
		dailyTotalCounter = new DailyTotalCounter(userName);
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		tasks = SOAPSession.getInstance().getIssues();
		projects = SOAPSession.getInstance().getProjects();
		userName = SOAPSession.getInstance().getConnectionDetails().getUser();
		dailyTotalCounter.setUserName(userName);
	}

	private class SyncJob extends Job {

		public SyncJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				for (int i = 0; i < SYNC_PAUSE; i++) {
					if (!isSync) {
						break;
					}
					Thread.sleep(1);
				}
			} catch (Exception e) {
				logger.debug(e.getMessage());
				LogManager.logStack(e);
			}
			while (isSync) {
				try {
					LogManager.log(Level.INFO, "SyncJob", "Start sync issues");
					logger.debug("Start sync issues");
					SearchResult searchResult = SOAPSession.getInstance().getSearchResult();
					LogManager.log(Level.INFO, "SyncJob", "Issues in Jira/Client app: " + searchResult.getTotal() + "/" + tasks.size());
					logger.debug("Issues in Jira/Client app: " + searchResult.getTotal() + "/" + tasks.size());
					for (BasicIssue bIssue : searchResult.getIssues()) {
						if (!isSync) {
							break;
						}
						boolean isFound = false;
						for (Task task : tasks) {
							if (!task.getKey().isEmpty() && task.getKey().equalsIgnoreCase(bIssue.getKey())) {
								isFound = true;
								break;
							}
						}
						if (!isFound) {
							final Task task = new Task(bIssue.getKey());
							SOAPSession.getInstance().updateTaskFromJira(task);
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									tasksComposite.addTask(task);
								}
							});
						}
					}
				} catch (Exception e) {
					logger.debug(e.getMessage());
					LogManager.logStack(e);
				}
				try {
					for (int i = 0; i < SYNC_PAUSE; i++) {
						if (!isSync) {
							break;
						}
						Thread.sleep(1);
					}
				} catch (Exception e) {
					logger.debug(e.getMessage());
					LogManager.logStack(e);
				}
			}
			// TODO return Status.OK_STATUS;
		}

	}

	private void updateTask(final IProgressMonitor monitor) {
		final Task task = taskPool.poll();
		if (task != null) {
			SOAPSession.getInstance().updateTaskFromJira(task);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (progressBar.isDisposed() || tasksComposite.isDisposed()) {
						return;
					}
					monitor.setTaskName(task.getKey());
					progressBar.setSelection(indx++);
					progressBar.setToolTipText(indx + " / " + tasks.size());
					int taskIndx = tasksComposite.getTaskIndx(task);
					tasksComposite.resizeTaskItem(taskIndx);
					tasksComposite.drawItem(taskIndx);
					monitor.worked(1);

					if (indx >= tasks.size()) {
						startStop(false);
						tasksComposite.update(tasks);
						tasksComposite.redraw();
						uiBlocker.releaseUIComponents();
					}
				}
			});
			updateTask(monitor);
		}
	}

	private String getStatusesToFilter() {
		String res = null;
		int index = headComposite.getComboStatusFilter().getSelectionIndex();
		if (index != 0) {
			res = headComposite.getComboStatusFilter().getText();
		}
		return res;
	}

	private List<Task> getTasksToFilter() {
		List<Task> tasksToUpdate = tasks;// default behavior
		if (headComposite.getComboFilterPrj().getSelectionIndex() != 0) {
			String prjKey = headComposite.getComboFilterPrj().getText();
			Matcher m = patternPrjKey.matcher(prjKey);
			if (m.find()) {
				prjKey = m.group(1);
			}
			tasksToUpdate = projects.get(prjKey);
		}

		// do status filter
		List<Task> res = tasksToUpdate;
		String status = getStatusesToFilter();
		if (status != null) {
			res = new ArrayList<Task>();
			for (Task task : tasksToUpdate) {
				if (task.getStatus().equalsIgnoreCase(status)) {
					res.add(task);
				}
			}
		}
		return res;
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new FormLayout());
		headComposite = new HeadComposite(parent, SWT.NONE);
		FormData fd_headComposite = new FormData();
		fd_headComposite.bottom = new FormAttachment(0, 45);
		fd_headComposite.right = new FormAttachment(100);
		fd_headComposite.top = new FormAttachment(0, 10);
		fd_headComposite.left = new FormAttachment(0);
		headComposite.setLayoutData(fd_headComposite);
		headComposite.getLblUserName().setText(userName);
		fillPrjFilterCompo();
		headComposite.getComboFilterPrj().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// headComposite.getComboFilterPrj().setEnabled(false);
				tasksComposite.update(getTasksToFilter());
			}
		});

		fillStatusesFilterCompo();

		headComposite.getComboStatusFilter().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tasksComposite.update(getTasksToFilter());
			}
		});

		slider = new Slider(parent, SWT.VERTICAL);
		FormData fd_slider = new FormData();
		fd_slider.top = new FormAttachment(headComposite, 1);
		fd_slider.bottom = new FormAttachment(100, -248);
		fd_slider.right = new FormAttachment(100, -1);
		// fd_slider.left = new FormAttachment(tasksComposite);
		slider.setLayoutData(fd_slider);

		tasksComposite = new TasksComposite(parent, SWT.NONE, tasks, slider, this);
		FormData fd_tasksComposite = new FormData();
		fd_tasksComposite.top = new FormAttachment(headComposite, 1);
		fd_tasksComposite.bottom = new FormAttachment(100, -148);
		fd_tasksComposite.left = new FormAttachment(0, 10);
		fd_tasksComposite.right = new FormAttachment(slider);
		tasksComposite.setLayoutData(fd_tasksComposite);

		slider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tasksComposite.onScroll();
			}
		});
		headComposite.getBtnLogInOut().setText(SOAPSession.getInstance().getAuthenticationToken() != null ? "Log Out" : "Log In");

		link = new Link(parent, SWT.NONE);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pool.execute(new Runnable() {
					@Override
					public void run() {
						org.eclipse.swt.program.Program.launch(FEEDBACK_LINK);
					}
				});
			}
		});

		FormData fd_link = new FormData();
		fd_link.right = new FormAttachment(100, -22);
		fd_link.bottom = new FormAttachment(100, -10);
		link.setLayoutData(fd_link);
		link.setText("<a>Leave a feedback</a>");

		Label lblVersion = new Label(parent, SWT.NONE);
		FormData fd_lblVersion = new FormData();
		fd_lblVersion.right = new FormAttachment(tasksComposite, 174);
		fd_lblVersion.bottom = new FormAttachment(link, 4, SWT.BOTTOM);
		fd_lblVersion.left = new FormAttachment(tasksComposite, 0, SWT.LEFT);
		lblVersion.setLayoutData(fd_lblVersion);
		lblVersion.setText("Version: " + JTTVersion.getText());
		lblDayTime = new Label(parent, SWT.NONE);
		/*
		 * lblDayTime.addMouseListener(new MouseAdapter() {
		 * @Override public void mouseDown(MouseEvent e) { dailyTotalCounter.setUserName(userName); setDeilyTime(dailyTotalCounter.getString()); WeekReportDialog weekReport = new WeekReportDialog(null,TasksView.this.userName); weekReport.open(); } });
		 */

		FormData fd_lblNewLabel = new FormData();
		// fd_lblNewLabel.top = new FormAttachment(tasksComposite, 99);
		fd_lblNewLabel.bottom = new FormAttachment(lblVersion, 3, SWT.TOP);
		fd_lblNewLabel.left = new FormAttachment(0, 10);
		fd_lblNewLabel.right = new FormAttachment(100, -262);
		lblDayTime.setLayoutData(fd_lblNewLabel);
		// TODO will be enabled when feature finished
		lblDayTime.setVisible(false);
		dailyTotalCounter.setUserName(userName);
		setDeilyTime(dailyTotalCounter.getString());

		progressBar = new ProgressBar(parent, SWT.SMOOTH);
		FormData fd_progressBar = new FormData();
		fd_progressBar.bottom = new FormAttachment(100, -10);
		fd_progressBar.left = new FormAttachment(lblDayTime, 5);
		fd_progressBar.right = new FormAttachment(link, -5);
		progressBar.setLayoutData(fd_progressBar);
		// progressBar.setVisible(false);

		headComposite.getBtnLogInOut().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (SOAPSession.getInstance().getAuthenticationToken() != null) {
					if (!close()) {
						return;
					}
					try {
						SOAPSession.getInstance().logOut();
						headComposite.getBtnLogInOut().setText("Log In");
						headComposite.getLblUserName().setText("");
						userName = "";
						tasksComposite.update(null);
						disablePrjFilterCombo();
						disableStatusesFilterCombo();
						headComposite.getBtnRefresh().setEnabled(false);
						Application.setFirstTry(false);
						login();
					} catch (RemoteException e1) {
						logger.debug(e1.getMessage());
						LogManager.logStack(e1);
					}
				} else {
					login();
				}
			}
		});

		headComposite.getBtnRefresh().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!close()) {
					return;
				}
				disablePrjFilterCombo();
				disableStatusesFilterCombo();
				Application.refresh();
				updateUI();
			}
		});
		/*final Shell shell = parent.getShell();
		// alpha = shell.getAlpha();
		shell.addShellListener(new ShellListener() {

			@Override
			public void shellActivated(ShellEvent event) {
				LogManager.log(Level.INFO, "TasksView", "activate with alpha: " + alpha);
				// shell.setAlpha(alpha);
			}

			@Override
			public void shellClosed(ShellEvent arg0) {
				LogManager.log(Level.INFO, "TasksView", "close");
				isSync = false;
				if (syncJob != null) {
					syncJob.cancel();
				}
			}

			@Override
			public void shellDeactivated(ShellEvent arg0) {
				LogManager.log(Level.INFO, "TasksView", "Deactivate with alpha: " + alpha);
				// shell.setAlpha(alpha - 150);
			}

			@Override
			public void shellDeiconified(ShellEvent arg0) {
				LogManager.log(Level.INFO, "TasksView", "Deiconified");
			}

			@Override
			public void shellIconified(ShellEvent arg0) {
				LogManager.log(Level.INFO, "TasksView", "Iconified");
			}
		});*/

		taskPool = new ConcurrentLinkedQueue<Task>(tasks);
		startStop(true);
		uiBlocker = new TransparentUIBlocker(tasksComposite, false);
		for (int i = 0; i < NUM_OF_UPDATE_THREADS; i++) {
			/*Job job = new UpdateTasksJob("Update issues " + i);
			job.schedule();*/
			NiftyProgress updateTaskThread = new NiftyProgress(rr, uiBlocker);
			updateTaskThread.run();
		}
		syncJob = new SyncJob("SyncJob");
		syncJob.schedule();
	}

	IResponsiveRunner rr = new IResponsiveRunner() {
		@Override
		public void runOutsideUIThread(final IProgressMonitor monitor) {
			monitor.beginTask("Update issues", tasks.size());
			updateTask(monitor);
			/*if (indx >= tasks.size()) {
				// monitor.done();
			}*/
		}

		@Override
		public void handleException(Exception caught) {
		}

		@Override
		public void uiFeedbackAfterRun() {
		}
	};

	/*private class UpdateTasksJob extends Job {
		public UpdateTasksJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			updateTask(monitor);
			return Status.OK_STATUS;
		}
	}*/

	private void startStop(boolean isStart) {
		progressBar.setVisible(isStart);
		slider.setEnabled(!isStart);
		slider.setTouchEnabled(!isStart);
		if (isStart) {
			progressBar.setMinimum(0);
			progressBar.setMaximum(tasks.size());
		} else {
			indx = 0;
			fillPrjFilterCompo();
			fillStatusesFilterCompo();
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	/*** PJTT-25 remove/disable add new task UI ***/
	/*
	 * public void addTask() { tasksComposite.addTask(new Task(String.valueOf(System.currentTimeMillis()).substring(10, 13), "https://jira.dbfs.net/browse/PJTT-1", newTaskComposite.getTextDescription().getText(), Long.parseLong(newTaskComposite.getTextEstimated().getText()), 0)); newTaskComposite.getBtnAdd().setEnabled(false); newTaskComposite.getTextEstimated().setText("");
	 * newTaskComposite.getTextDescription().setText(""); }
	 */

	public void login() {
		if (Application.login(SOAPSession.getInstance())) {
			headComposite.getBtnLogInOut().setText(SOAPSession.getInstance().getAuthenticationToken() != null ? "Log Out" : "Log In");
			String username = SOAPSession.getInstance().getAuthenticationToken() != null ? SOAPSession.getInstance().getConnectionDetails().getUser() : "";
			headComposite.getLblUserName().setText(username);
			userName = username;
			if (!username.equalsIgnoreCase("")) {
				dailyTotalCounter.setUserName(username);
			}
			updateUI();
		}
	}

	private void fillPrjFilterCompo() {
		headComposite.getComboFilterPrj().setEnabled(true);
		for (String key : projects.keySet()) {
			headComposite.getComboFilterPrj().add(key + " - " + projects.get(key).get(0).getProjectName());
		}
	}

	private void fillStatusesFilterCompo() {
		headComposite.getComboStatusFilter().setEnabled(true);
		Set<String> statuses = parseStatuses(tasks);
		for (String e : statuses) {
			headComposite.getComboStatusFilter().add(e);
		}
	}

	private Set<String> parseStatuses(List<Task> tasks2) {
		Set<String> res = new HashSet<String>();
		for (Task task : tasks2) {
			res.add(task.getStatus());
		}
		logger.debug("parsed statuses: " + res);
		return res;
	}

	private void disablePrjFilterCombo() {
		headComposite.getComboFilterPrj().setItems(new String[] { "All projects" });
		headComposite.getComboFilterPrj().select(0);
		headComposite.getComboFilterPrj().setEnabled(false);
	}

	private void disableStatusesFilterCombo() {
		headComposite.getComboStatusFilter().setItems(new String[] { "All statuses" });
		headComposite.getComboStatusFilter().select(0);
		headComposite.getComboStatusFilter().setEnabled(false);
	}

	public void setDeilyTime(String value) {
		lblDayTime.setText("Today total: " + value);
	}

	private boolean close() {
		if (Application.isStartedTask()) {
			MessageBox messageBox = new MessageBox(headComposite.getShell(), SWT.APPLICATION_MODAL | SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WORKING);
			messageBox.setText("Jira Time Tracker");
			messageBox.setMessage("Do you want to update worklog for started task?");
			int res = messageBox.open();
			if (res == SWT.CANCEL) {
				return false;
			}
			if (res == SWT.YES) {
				TasksComposite.updateWorklog();
				return false;
			}
			if (res == SWT.NO) {
				TasksComposite.cancelTimer();
			}
		}
		return true;
	}

	private void updateUI() {
		tasks = SOAPSession.getInstance().getIssues();
		projects = SOAPSession.getInstance().getProjects();

		tasksComposite.update(tasks);
		fillPrjFilterCompo();
		fillStatusesFilterCompo();
		if ((tasks != null) && (tasks.size() > 0)) {
			headComposite.getBtnRefresh().setEnabled(true);
		}
		drawDailyTime();
	}

	public String getUserName() {
		return userName;
	}

	private void drawDailyTime() {
		setDeilyTime(dailyTotalCounter.getString());
	}
}