package com.dbfs.jtt.model;

import java.util.concurrent.TimeUnit;

public class Task {
	private final static int HOURS_PER_DAY = 8;
	private String key;
	private String url;
	private String urlParent;
	private String description = "";
	private long timeEstimated;
	private String logWorkH = "";
	private String projectName = "";
	private String status = "";
	private boolean isRunning;
	private boolean isSuspended;
	private long timeSpent = 0l;
	private long currentTimeSpent;
	private long startedTimer;
	private String parentKey;
	private String parentSum;
	private String comment = "";

	public String getParentKey() {
		return parentKey;
	}

	public void setParentKey(String parentKey) {
		this.parentKey = parentKey;
	}

	public String getParentSum() {
		return parentSum;
	}

	public void setParentSum(String parentSum) {
		this.parentSum = parentSum;
	}

	public Task(String key) {
		setKey(key);
	}

	public Task(String prjName, String key, String url, String desc, long timeEstim, long timeSpent, String status) {
		setStatus(status);
		setProjectName(prjName);
		setKey(key);
		setUrl(url);
		setDescription(desc);
		setTimeEstimated(String.valueOf(timeEstim).length() <= 2 ? TimeUnit.HOURS.toMillis(timeEstim) : TimeUnit.SECONDS.toMillis(timeEstim));
		setTimeSpent(TimeUnit.SECONDS.toMillis(timeSpent));
	}

	public String getFormatedTimeSpent() {
		return millisecondsToDHM(timeSpent);
	}

	public long getCurrentTimeSpent() {
		return currentTimeSpent;
	}

	public void setCurrentTimeSpent(long currentTimeSpent) {
		this.currentTimeSpent = currentTimeSpent;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getParentUrl() {
		return urlParent;
	}

	public void setParentUrl(String urlParent) {
		this.urlParent = urlParent;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if (description != null)
			this.description = description;
	}

	public long getTimeEstimated() {
		return timeEstimated;
	}

	public void setTimeEstimated(long timeEstimated) {
		this.timeEstimated = timeEstimated;
	}

	/**
	 * In milliseconds.
	 * 
	 * @return
	 */
	public long getTimeSpent() {
		return timeSpent;
	}

	public void setTimeSpent(long timeSpent) {
		this.timeSpent = timeSpent;
	}

	public String getLogWorkH() {
		return logWorkH;
	}

	public void setLogWorkH(String logWorkH) {
		this.logWorkH = logWorkH;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
		if (isRunning) {
			isSuspended = false;
		}
	}

	public long getStartedTimer() {
		return startedTimer;
	}

	public void setStartedTimer(long startedTimer) {
		this.startedTimer = startedTimer;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isSuspended() {
		return isSuspended;
	}

	public void setSuspended(boolean isSuspended) {
		this.isSuspended = isSuspended;
	}

	public static String millisecondsToDHM(long millisec) {
		long m = TimeUnit.MILLISECONDS.toMinutes(millisec);
		long h = TimeUnit.MILLISECONDS.toHours(millisec);
		long days = h / HOURS_PER_DAY;
		long hours = h % HOURS_PER_DAY;
		long minutes = m - TimeUnit.HOURS.toMinutes(h);
		String res = minutes + "m";
		if (hours > 0) {
			res = hours + "h " + res;
		}
		if (days > 0) {
			res = days + "d " + res;
		}
		return res;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Task && ((Task) obj).getKey().equals(this.getKey()));
	}
}