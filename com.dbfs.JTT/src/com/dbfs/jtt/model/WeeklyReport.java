package com.dbfs.jtt.model;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WeeklyReport {

	private int today;
	
	public WeeklyReport() {
		Calendar rightNow = Calendar.getInstance();
		today = rightNow.get(Calendar.DAY_OF_WEEK);
		// TODO Auto-generated constructor stub
	}
	
	
	 // Sunday Monday Tuesday Wednesday Thursday Friday Saturday
	
	public static void main(String[] args) {
		Calendar now = Calendar.getInstance();
		
		System.out.println(now.getFirstDayOfWeek());
		
	}
}
