package com.dbfs.jtt.model;

import java.util.Calendar;

public class WeeklyReport {

	private final int today;

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
