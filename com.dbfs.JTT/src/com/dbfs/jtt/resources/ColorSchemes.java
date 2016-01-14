package com.dbfs.jtt.resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ColorSchemes {
	public static Color taskColor = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
	public static Color taskNormalPriorityBorderColor = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BORDER);
	public static Color taskNormalPriorityItemBackgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	public static Color taskHighPriorityBorderColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);// high
	public static Color taskHighPriorityItemBackgroundColor = new Color(Display.getCurrent(), 255, 207, 207);// high
	public static Color taskLowPriorityBorderColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);// low
	public static Color taskLowPriorityItemBackgroundColor = new Color(Display.getCurrent(), 160, 217, 145);// low
	public static Color subTaskItemBackgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
	public static Color taskGradientColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
	public static Color taskStartColor = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
	public static Color taskStartGradientColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);
	public static Color taskStartTextColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	public static Color taskWorkLogTextBackgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	public static Color taskStopColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	public static Color taskStopTextColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	public static Color taskStopGradientColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);
	public static Color taskMiniBtnTColor = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
	public static Color taskMiniBtnTGradientColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);
	public static Color taskMiniBtnPauseColor = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
	public static Color taskMiniBtnPauseGradientColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW);
	public static Color taskMiniBtnBorderColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	public static Color taskSelectedColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
	public static Color timeOverTextColor = new Color(Display.getCurrent(), 237, 80, 7);
	public static Color time2OverTextColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	public static Color blankTextboxBackgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
	public static Color normalTextboxBackgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	public static Color loginMsgForegraundColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	public static Color loginMsgBackgroundColor = new Color(Display.getCurrent(), 255, 207, 207);
	public static Color loginMsgTextColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	public static Color newTaskColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	public static Color taskPriorityBtnBackgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	public static Color taskPriorityBtnGradientColor = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
	public static Color taskPriorityBtnOverHighColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	public static Color taskPriorityBtnOverNormalColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE);
	public static Color taskPriorityBtnOverLowColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);
	public static Color EstimatedTaskColor = new Color(Display.getCurrent(), 138, 176, 212);
	public static Color RemainingTaskColor = new Color(Display.getCurrent(), 235, 141, 0);
	public static Color LoggedTaskColor = new Color(Display.getCurrent(), 83, 166, 38);
}
