package com.dbfs.jtt.model;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.swift.common.soap.jira.JiraSoapService;
import org.swift.common.soap.jira.JiraSoapServiceService;
import org.swift.common.soap.jira.JiraSoapServiceServiceLocator;
import org.swift.common.soap.jira.RemoteAuthenticationException;
import org.swift.common.soap.jira.RemoteIssue;
import org.swift.common.soap.jira.RemotePermissionException;
import org.swift.common.soap.jira.RemoteValidationException;
import org.swift.common.soap.jira.RemoteWorklog;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.ProgressMonitor;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.domain.TimeTracking;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.json.BasicIssueJsonParser;
import com.dbfs.jtt.Application;
import com.dbfs.jtt.util.LogManager;

/**
 * This represents a SOAP session with JIRA including that state of being logged in or not
 */
public class SOAPSession implements IAdaptable {
	Logger logger = Logger.getLogger(SOAPSession.class);
	private static String JQL_QUERY_TEMPLATE = "assignee = '%s' AND status != Closed";
	// private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
	private ConnectionDetails connectionDetails;
	private final JiraSoapServiceServiceLocator jiraSoapServiceLocator = new JiraSoapServiceServiceLocator();
	private JiraSoapService jiraSoapService;
	private String token;
	private static SOAPSession INSTANCE;
	private List<Task> issues;
	private List<Task> closedIssues;
	private String jqlQuery = "";
	private Map<String, List<Task>> projects;
	private String connectionFaultMsg;

	/*
	 * private Map<String, String> subtaskIDs = new HashMap<String, String>(); private Map<String, Integer> taskKeyIndx = new HashMap<String, Integer>();
	 */

	public static SOAPSession getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SOAPSession();
		}
		return INSTANCE;
	}

	private SOAPSession() {
	}

	public void connectAndLogin(IProgressMonitor monitor) throws ServiceException, RemoteException, RemoteAuthenticationException {
		try {
			monitor.beginTask("Connecting...", IProgressMonitor.UNKNOWN);
			// monitor.subTask("Contacting " + connectionDetails.getServer() + "...");
			jiraSoapServiceLocator.setJirasoapserviceV2EndpointAddress(connectionDetails.getServer() + ConnectionDetails.getEndpoint());
			jiraSoapServiceLocator.setMaintainSession(true);
			jiraSoapService = jiraSoapServiceLocator.getJirasoapserviceV2();
			token = jiraSoapService.login(connectionDetails.getUser(), connectionDetails.getPassword());
			getTasksFromJqlSearch(monitor);
			setConnectionFaultMsg(null);
		} finally {
			monitor.done();
		}
	}

	public void refresh(IProgressMonitor monitor) throws org.swift.common.soap.jira.RemoteException, RemoteException, RestClientException {
		try {
			monitor.beginTask("Refresh...", 100);
			monitor.subTask("Getting a list of issues...");
			getTasksFromJqlSearch(monitor);
		} finally {
			monitor.done();
		}
	}

	public void logOut() throws RemoteException {
		jiraSoapService.logout(token);
		token = null;
		setIssues(null);
		setProjects(null);
		setConnectionFaultMsg(null);
	}

	public void updateWorklog(final Task task) throws RemotePermissionException, RemoteValidationException, org.swift.common.soap.jira.RemoteException, RemoteException {
		final String methodName = "updateWorklog";
		if (token == null) {
			return;
		}

		logger.info("updateWorklog start here");
		LogManager.log(Level.INFO, "SOAPSession", "updateWorklog start here");
		RemoteWorklog[] worklogs = tryDoThis(new Callable<RemoteWorklog[]>() {
			@Override
			public RemoteWorklog[] call() throws Exception {
				RemoteWorklog[] rwarr = null;
				try {
					rwarr = jiraSoapService.getWorklogs(token, task.getKey());
				} catch (Exception e) {
					LogManager.logStack(e);
					LogManager.log(Level.WARNING, methodName, "Start new session and try to login...");
					token = jiraSoapService.login(connectionDetails.getUser(), connectionDetails.getPassword());
					rwarr = jiraSoapService.getWorklogs(token, task.getKey());
					LogManager.log(Level.INFO, methodName, "Connection is established. Token: " + token);
				}
				return rwarr;
			}
		}, 1);

		if (worklogs == null) {
			logger.info("failed to receive worklogs. exit function");
			LogManager.log(Level.INFO, "SOAPSession", "failed to receive worklogs. exit function");
			return;
		}
		logger.info("worklogs.length:" + worklogs.length);
		LogManager.log(Level.INFO, "SOAPSession", "worklogs.length:" + worklogs.length);
		Calendar calendar = Calendar.getInstance();
		logger.info("today is YEAR:" + calendar.get(Calendar.YEAR) + " DAY_OF_YEAR:" + calendar.get(Calendar.DAY_OF_YEAR));
		LogManager.log(Level.INFO, "SOAPSession", "today is YEAR:" + calendar.get(Calendar.YEAR) + " DAY_OF_YEAR:" + calendar.get(Calendar.DAY_OF_YEAR));

		// get worklog with today date
		RemoteWorklog oldWorklog = null;
		boolean differentComments = false;
		for (RemoteWorklog worklog : worklogs) {
			Calendar tmpCalendar = worklog.getStartDate();
			String author = worklog.getAuthor();
			logger.info("worklog YEAR:" + tmpCalendar.get(Calendar.YEAR) + " DAY_OF_YEAR:" + tmpCalendar.get(Calendar.DAY_OF_YEAR) + ", author: " + author);
			LogManager.log(Level.INFO, "SOAPSession", "worklog YEAR:" + tmpCalendar.get(Calendar.YEAR) + " DAY_OF_YEAR:" + tmpCalendar.get(Calendar.DAY_OF_YEAR) + ", author: " + author);

			if (connectionDetails.getUser().equalsIgnoreCase(author) && (tmpCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)) && (tmpCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR))) {
				// day
				oldWorklog = worklog;
				logger.info("Find worklog ok");
				LogManager.log(Level.INFO, "SOAPSession", "Find worklog ok");
				break;
			}
		}

		final RemoteWorklog worklog = new RemoteWorklog();
		worklog.setUpdateAuthor(connectionDetails.getUser());

		long millisis = task.getCurrentTimeSpent();
		// select entry for work with
		if (oldWorklog != null) { // copy from old worklog entry
			differentComments = (oldWorklog.getComment() != null) && !oldWorklog.getComment().isEmpty() && (task.getComment() != null) && !task.getComment().isEmpty() && !oldWorklog.getComment().equalsIgnoreCase(task.getComment());
			if (differentComments) {
				worklog.setStartDate(calendar);
			} else {
				worklog.setId(oldWorklog.getId());
				worklog.setCreated(oldWorklog.getCreated());
				worklog.setStartDate(oldWorklog.getStartDate() == null ? calendar : oldWorklog.getStartDate());
				millisis = TimeUnit.SECONDS.toMillis(oldWorklog.getTimeSpentInSeconds()) + millisis;
				// worklog.setComment(worklogCommentUpdate(oldWorklog.getComment(),calendar));
				worklog.setComment(oldWorklog.getComment());
			}
		} else { // populate new worklog entry
			worklog.setStartDate(calendar);
			/*** worklog.setComment(genComment(calendar)); */
		}
		if ((task.getComment() != null) && !task.getComment().isEmpty()) {
			worklog.setComment(task.getComment());
		}
		String asString = Task.millisecondsToDHM(millisis);
		worklog.setTimeSpent(asString);

		// select correct 'save' strategy
		if ((oldWorklog != null) && !differentComments) { // work with old worklog entry
			tryDoThis(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					LogManager.log(Level.INFO, "SOAPSession", "jiraSoapService.updateWorklogAndAutoAdjustRemainingEstimate(" + token + ", " + worklog + ");");
					jiraSoapService.updateWorklogAndAutoAdjustRemainingEstimate(token, worklog);
					return null;
				}
			}, 1);
		} else { // create new worklog entry
			tryDoThis(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					LogManager.log(Level.INFO, "SOAPSession", "jiraSoapService.addWorklogAndAutoAdjustRemainingEstimate(" + token + ", " + task.getKey() + ", " + worklog + ");");
					jiraSoapService.addWorklogAndAutoAdjustRemainingEstimate(token, task.getKey(), worklog);
					return null;
				}
			}, 1);
		}
		task.setTimeSpent(getAlreadyTracked(task));
		task.setCurrentTimeSpent(0);
		task.setComment("");
		return;
	}

	/**
	 * @param task
	 * @return in millisec.
	 */
	private long getAlreadyTracked(final Task task) {
		ProgressMonitor pm = new NullProgressMonitor();
		Issue issue = getRestClient().getIssueClient().getIssue(task.getKey(), pm);
		TimeTracking tt = issue.getTimeTracking();
		long alreadyMinutes = tt.getTimeSpentMinutes() == null ? 0l : tt.getTimeSpentMinutes();
		long alreadyMilliseconds = TimeUnit.MINUTES.toMillis(alreadyMinutes);
		return alreadyMilliseconds;
	}

	/***
	 * private String genComment(Calendar calendar) { return "Jira Time Tracker " + JTTVersion.getText() + " [" + dateFormatter.format(calendar.getTime()) + "]"; }
	 */

	/*
	 * public SOAPSession(URL webServicePort) { jiraSoapServiceLocator = new JiraSoapServiceServiceLocator(); fJiraSoapServiceGetter.setJirasoapserviceV2EndpointAddress(server + endPoint); fJiraSoapServiceGetter.setMaintainSession(true); fJiraSoapService = fJiraSoapServiceGetter.getJirasoapserviceV2(); try { if (webServicePort == null) { jiraSoapService =
	 * jiraSoapServiceLocator.getJirasoapserviceV2(); } else { jiraSoapService = jiraSoapServiceLocator.getJirasoapserviceV2(webServicePort); System.out.println("SOAP Session service endpoint at " + webServicePort.toExternalForm()); } } catch (ServiceException e) { throw new RuntimeException("ServiceException during SOAPClient contruction", e); } }
	 */

	/***
	 * private String worklogCommentUpdate(String comment, Calendar calendar) { // "Jira Time Tracker " + JTTVersion.getText() + " [" + dateFormatter.format(calendar.getTime()) + "]" String regex = "Jira\\s+Time\\s+Tracker\\s+[\\d\\.]+\\s+\\[[\\d\\.\\ :]+\\]"; Matcher matcher = Pattern.compile(regex).matcher(comment); String foundStr = null; while(matcher.find()){// get last match foundStr =
	 * matcher.group(); } if(foundStr!=null){// match exist - remove it comment = comment.substring(0,comment.lastIndexOf(foundStr)); if(comment.length()!=0){ if(comment.charAt(comment.length()-1) == '\n'){ comment = comment.substring(0,comment.length()-1); }; } } comment+="\n"+genComment(calendar); // and anyway write again return comment; }
	 */

	public ConnectionDetails getConnectionDetails() {
		return connectionDetails;
	}

	public void setConnectionDetails(ConnectionDetails connectionDetails) {
		this.connectionDetails = connectionDetails;
		if (connectionDetails == null) {
			return;
		}
		jqlQuery = String.format(JQL_QUERY_TEMPLATE, connectionDetails.getUser());
	}

	public String getAuthenticationToken() {
		return token;
	}

	public JiraSoapService getJiraSoapService() {
		return jiraSoapService;
	}

	public JiraSoapServiceService getJiraSoapServiceLocator() {
		return jiraSoapServiceLocator;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

	public List<Task> getIssues() {
		return issues;
	}

	public void setIssues(List<Task> issues) {
		if ((this.issues != null) && (issues != null) && (this.issues.size() > issues.size())) {
			List<Task> closed = new ArrayList<Task>();
			for (int i = 0; i < this.issues.size(); i++) {
				String searchKey = this.issues.get(i).getKey();
				for (int z = 0; z < issues.size(); z++) {
					if (searchKey.equals(issues.get(z).getKey())) {
						break;
					} else if (z == (issues.size() - 1)) {
						closed.add(this.issues.get(i));
					}
				}
			}
			closedIssues = closed;
		} else {
			closedIssues = null;
		}
		this.issues = issues;
	}

	public List<Task> getClosedIssues() {
		return closedIssues;
	}

	public void setClosedIssues(List<Task> closedIssues) {
		this.closedIssues = closedIssues;
	}

	public Map<String, List<Task>> getProjects() {
		return projects;
	}

	public void setProjects(Map<String, List<Task>> projects) {
		this.projects = projects;
	}

	public void updateTaskFromJira(Task task) {
		JiraRestClient restClient = getRestClient();
		NullProgressMonitor pm = new NullProgressMonitor();
		Issue issue = restClient.getIssueClient().getIssue(task.getKey(), pm);
		TimeTracking tt = issue.getTimeTracking();
		long oe = tt.getOriginalEstimateMinutes() != null ? TimeUnit.MINUTES.toSeconds(tt.getOriginalEstimateMinutes()) : 0;
		long ts = tt.getTimeSpentMinutes() != null ? TimeUnit.MINUTES.toSeconds(tt.getTimeSpentMinutes()) : 0;
		String status = issue.getStatus().getName();
		task.setProjectName(issue.getProject().getName());
		task.setUrl(connectionDetails.getServer() + "/browse/" + task.getKey());
		task.setDescription(issue.getSummary());
		task.setTimeEstimated(String.valueOf(oe).length() <= 2 ? TimeUnit.HOURS.toMillis(oe) : TimeUnit.SECONDS.toMillis(oe));
		task.setTimeSpent(TimeUnit.SECONDS.toMillis(ts));
		task.setStatus(status);
		LogManager.log(Level.INFO, "", task.getKey() + " | " + issue.getProject().getName() + " | " + issue.getSummary());

		Field ob = issue.getField("parent");
		if (ob != null) {
			JSONObject jsonParent = (JSONObject) ob.getValue();
			BasicIssue bi = null;
			try {
				bi = new BasicIssueJsonParser().parse(jsonParent);
			} catch (JSONException e1) {
				LogManager.logStack(e1);
			}
			task.setParentKey(bi.getKey());
			task.setParentUrl(connectionDetails.getServer() + "/browse/" + bi.getKey());
			try {
				Issue iss2 = restClient.getIssueClient().getIssue(bi.getKey(), pm);
				task.setParentSum(iss2.getSummary());
			} catch (Exception e) {
				LogManager.logStack(e);
				try {
					token = jiraSoapService.login(connectionDetails.getUser(), connectionDetails.getPassword());
					RemoteIssue remoteIssue = jiraSoapService.getIssue(token, bi.getKey());
					if (remoteIssue != null) {
						task.setParentSum(remoteIssue.getSummary());
					} else {
						task.setParentSum("Parent task name");
					}
				} catch (Exception e1) {
					LogManager.logStack(e);
				}
			}
		}
		String keyPrj = issue.getProject().getKey();
		if (!projects.containsKey(keyPrj)) {
			projects.put(keyPrj, new ArrayList<Task>());
		}
		projects.get(keyPrj).add(task);
	}

	public SearchResult getSearchResult() {
		JiraRestClient restClient = getRestClient();
		NullProgressMonitor pm = new NullProgressMonitor();
		LogManager.log(Level.INFO, "", "jqlQuery = '" + jqlQuery + "'");
		SearchRestClient searchClient = restClient.getSearchClient();
		LogManager.log(Level.INFO, "", "try to get searchResult...");
		return searchClient.searchJql(jqlQuery, 1000, 0, pm);
	}

	public void getTasksFromJqlSearch(IProgressMonitor monitor) throws RestClientException {
		LogManager.log(Level.INFO, "", "start getTasksFromJqlSearch() function");
		List<Task> res = new ArrayList<Task>();
		// let's now print all issues matching a JQL string (here: all assigned issues)
		try {
			SearchResult searchResult = getSearchResult();
			// float k = (searchResult.getTotal() > 0) ? 100 / searchResult.getTotal() : 100;
			int i = 0;
			projects = new HashMap<String, List<Task>>();
			LogManager.log(Level.INFO, "", "getting issues...");
			SubMonitor subMonitor = SubMonitor.convert(monitor, searchResult.getTotal());
			for (BasicIssue bIssue : searchResult.getIssues()) {
				if (monitor.isCanceled()) {
					break;
				}
				LogManager.log(Level.INFO, "", "getting issues: " + (i + 1) + "/" + searchResult.getTotal());
				/*
				 * Issue issue = restClient.getIssueClient().getIssue(bIssue.getKey(), pm); subMonitor.setTaskName(bIssue.getKey() + " | " + issue.getSummary()); if (issue.getIssueType().isSubtask()) { } TimeTracking tt = issue.getTimeTracking(); long oe = tt.getOriginalEstimateMinutes() != null ? TimeUnit.MINUTES.toSeconds(tt.getOriginalEstimateMinutes()) : 0; long ts = tt.getTimeSpentMinutes() != null ?
				 * TimeUnit.MINUTES.toSeconds(tt.getTimeSpentMinutes()) : 0; String status = issue.getStatus().getName(); Task task = new Task(issue.getProject().getName(), bIssue.getKey(), connectionDetails.getServer() + "/browse/" + bIssue.getKey(), issue.getSummary(), oe, ts, status); LogManager.log(Level.INFO, "", bIssue.getKey() + " | " + issue.getProject().getName() + " | " + issue.getSummary());
				 * Field ob = issue.getField("parent"); if (ob != null) { JSONObject jsonParent = (JSONObject) ob.getValue(); BasicIssue bi = null; try { bi = new BasicIssueJsonParser().parse(jsonParent); } catch (JSONException e1) { LogManager.logStack(e1); } task.setParentKey(bi.getKey()); task.setParentUrl(connectionDetails.getServer() + "/browse/" + bi.getKey()); try { Issue iss2 =
				 * restClient.getIssueClient().getIssue(bi.getKey(), pm); task.setParentSum(iss2.getSummary()); } catch (Exception e) { LogManager.logStack(e); try { RemoteIssue remoteIssue = jiraSoapService.getIssue(token, bi.getKey()); if (remoteIssue != null) { task.setParentSum(remoteIssue.getSummary()); } else { task.setParentSum("Parent task name"); } } catch (Exception e1) { LogManager.logStack(e);
				 * } } } res.add(task);
				 */
				res.add(new Task(bIssue.getKey()));

				/*
				 * String keyPrj = issue.getProject().getKey(); if (!projects.containsKey(keyPrj)) { projects.put(keyPrj, new ArrayList<Task>()); } projects.get(keyPrj).add(task);
				 */

				subMonitor.newChild(1);
				if (monitor != null) {
					// monitor.worked((int) (k * i++));
					monitor.subTask("Loading JIRA's tickets... [" + (++i) + "/" + searchResult.getTotal() + "]");
				}

			}

			if (monitor != null) {
				// monitor.worked(100);
				monitor.done();
			}
			setIssues(res);
		} catch (RestClientException e) {
			LogManager.logStack(e);
			Display disp = Display.getCurrent() == null ? Display.getDefault() : Display.getCurrent();
			disp.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (openQuestion()) {
						getTasksFromJqlSearch(null);
					}
					if (projects == null) {
						projects = new HashMap<String, List<Task>>();
					}
				}
			});
			// throw e;
		}

		/*********************/
		/*** Jira SOAP API ***/
		/*********************/
		/******
		 * Start******RemoteIssue[] issuesFromTextSearch = jiraSoapService.getIssuesFromJqlSearch(token, JQL_QUERY_TEMPLATE, 10000); for (RemoteIssue remoteIssue : issuesFromTextSearch) { RemoteWorklog[] worklogs = jiraSoapService.getWorklogs(token, remoteIssue.getKey()); long lastSpentTime = 0; for (RemoteWorklog remoteWorklog : worklogs) { lastSpentTime = remoteWorklog.getTimeSpentInSeconds(); }
		 * //res.add(new Task(remoteIssue.getKey(), connectionDetails.getServer() + "/browse/" + remoteIssue.getKey(), remoteIssue.getSummary(), lastSpentTime, lastSpentTime)); }****End
		 *****/
		LogManager.log(Level.INFO, "", "end getTasksFromJqlSearch() function: tasks.size() = " + res.size());
	}

	private JiraRestClient getRestClient() {
		/*********************/
		/*** Jira REST API ***/
		/*********************/

		final JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
		URI jiraServerUri = null;
		try {
			jiraServerUri = new URI(connectionDetails.getServer());
		} catch (URISyntaxException e) {
			logger.error(e);
			LogManager.logStack(e);
		}
		return factory.createWithBasicHttpAuthentication(jiraServerUri, connectionDetails.getUser(), connectionDetails.getPassword());
	}

	public String getConnectionFaultMsg() {
		return connectionFaultMsg;
	}

	public void setConnectionFaultMsg(String connectionFaultMsg) {
		this.connectionFaultMsg = connectionFaultMsg;
	}

	private void reconnect() throws InterruptedException, InvocationTargetException {
		try {
			ProgressMonitorDialog progress = new ProgressMonitorDialog(null);
			progress.setCancelable(true);
			progress.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						connectAndLogin(monitor);
					} catch (ServiceException e) {
						throw new InvocationTargetException(e, Application.handleConnectionError(e.getMessage()));
					} catch (RemoteAuthenticationException e) {
						throw new InvocationTargetException(e, Application.handleConnectionError(e.getFaultString()));
					} catch (RemoteException e) {
						throw new InvocationTargetException(e, Application.handleConnectionError(e.getMessage()));
					}
				}
			});
		} catch (InvocationTargetException e1) {
			logger.debug(e1.getMessage());
			SOAPSession.getInstance().setConnectionFaultMsg(e1.getMessage());
			LogManager.logStack(e1);
			logger.warn(e1);
			throw e1;
		} catch (InterruptedException e1) {
			logger.debug(e1.getMessage());
			SOAPSession.getInstance().setConnectionFaultMsg(e1.getMessage());
			LogManager.logStack(e1);
			logger.warn(e1);
			throw e1;
		}
	}

	/**
	 * Optimized for jira soap services.
	 * 
	 * @param funct
	 * @return
	 */
	<T> T tryDoThis(Callable<T> funct, int attempts) {
		logger.info("start tryDoThis with attempts:" + attempts);
		attempts--;
		if (attempts < 0) {
			logger.info("tryDoThis MUST ASK BEFORE");
			if (!openQuestion()) {
				return null;
			}
		}

		try {
			return funct.call();
		} catch (RemoteException e) {
			LogManager.logStack(e);
			logger.warn(e);
		} catch (Exception e) {
			LogManager.logStack(e);
			logger.warn(e);
		}
		try {
			reconnect();
		} catch (InterruptedException e1) {
			LogManager.logStack(e1);
			logger.warn(e1);
		} catch (InvocationTargetException e1) {
			LogManager.logStack(e1);
			logger.warn(e1);
		}
		logger.info("END tryDoThis with attempts:" + attempts);
		return tryDoThis(funct, attempts);
	}

	private boolean openQuestion() {
		Display disp = Display.getCurrent() == null ? Display.getDefault() : Display.getCurrent();
		Shell shell = new Shell(disp);
		MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		messageBox.setMessage("During connection to server an error occurs. Try again?");
		messageBox.setText("Connection is lost");
		return messageBox.open() == SWT.YES;
	}
}
