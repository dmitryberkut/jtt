package com.dbfs.jtt.model;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;


import com.dbfs.jtt.dialogs.LoginDialog;
import com.dbfs.jtt.util.LogManager;

public class DailyTotalCounter{

	private static String DATE_FORMAT = "yyyy-MM-dd";
	
	private Logger logger = Logger.getLogger(DailyTotalCounter.class);

	private String userName;
	
	public DailyTotalCounter(String userName) {
		this.userName = userName;
		logger.info("init daily counter with username: "+ userName);
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	private Long load(){
		//TODO: merge with LoginDialog and make one data access interface 
		Long res = 0l;
		// get presets
		ISecurePreferences preferences = getActualPresets(this.userName);
		logger.info("load daily counter with username: "+ userName);
		try {
			res = preferences.getLong(LoginDialog.DAYLY_TIME, 0l);
		} catch (StorageException e) {
		    LogManager.logStack(e);
		}
		logger.info("res is: "+res );
		return res;
	}
	
	public String getString(){
		return millisec2Str(load());
	}
	
	public String add(Long value){
		String res = "";
		try {
			Long last = load();
			last+=value;
			logger.debug("last is: "+ last);
			res = String.valueOf(last);
			ISecurePreferences preferences = getActualPresets(this.userName);
			preferences .putLong(LoginDialog.DAYLY_TIME, last, false);
			logger.info("storing daily counter with username: "+ userName);
			preferences.flush();
		} catch (StorageException e) {
		    LogManager.logStack(e);
		} catch (IOException e) {
		    LogManager.logStack(e);
		}
		
		return res;
	}
	
	private String millisec2Str(long value){
		long days = TimeUnit.MILLISECONDS.toDays(value);
		long hours  = TimeUnit.MILLISECONDS.toHours(value);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(value);
		
		minutes = minutes - TimeUnit.HOURS.toMinutes(hours);
		hours = hours- TimeUnit.DAYS.toHours(days);
		
		
		logger.debug("days is: "+days+ " hours is: "+hours+" minutes:"+minutes);
		String res = ""+(days>0?days+"d ":"")+(hours>0?hours+"h ":"")+(minutes>0?minutes+"m":"");
		if(res.equalsIgnoreCase(""))res = "0m";
		logger.debug(res);
		return res;
	}
	
	/**
	 * Return actual presets node with binding to user and date. 
	 * If saved day is not today - remove all not actual.
	 * @return
	 */
	private static ISecurePreferences getActualPresets(String userName){
		ISecurePreferences res = null;
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		preferences = preferences.node(LoginDialog.DAYLY_ROOT);// if not exist - create new (default)
		
		DateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
		try {
			String today = formatter.format(new Date());
			String savedDay = preferences.get(LoginDialog.DAYLY_TODAY, "");// will be not null any case
			
			if (!savedDay.equalsIgnoreCase("") && !today.equalsIgnoreCase(savedDay)){ 
				// if not first run AND saved day is not today
				preferences.node(savedDay).removeNode(); // remove not required
			}
			
			if(!today.equalsIgnoreCase(savedDay)){ // fix to this day (can be first run)
				preferences.put(LoginDialog.DAYLY_TODAY, today, false);
			}
			
			res = preferences.node(today);			
			res = res.node(userName);
			
		} catch (StorageException e) {
		    LogManager.logStack(e);
		} catch (IllegalArgumentException e) {
		    LogManager.logStack(e);
		}
		
		try {
			preferences.flush();
		} catch (IOException e) {
		    LogManager.logStack(e);
			preferences = null;
		}			

		
		return (preferences==null)?null:res;
	}
	
	public void clear() {
		try {
			ISecurePreferences preferences = getActualPresets(this.userName);
			preferences.putLong(LoginDialog.DAYLY_TODAY, 0L, false);
		} catch (StorageException e) {
		    LogManager.logStack(e);
		}
	}

	public List<String[]> getWeekReport() {
		
		ISecurePreferences preferences = getActualPresets(this.userName);
		
		List<String[]> data = new ArrayList<String[]>();
		data.add(new String[]{"Sunday","1h"});
		return data;
	}
	

}
