package com.dbfs.jtt.resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ColorSchemes {
    public static Color taskColor = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
    public static Color taskBorderColor = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BORDER);
    public static Color taskItemBackgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
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
    public static Color loginMsgForgraundColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
    public static Color loginMsgBackgroundColor = new Color(Display.getCurrent(), 255, 207, 207);
    public static Color loginMsgTextColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	public static Color newTaskColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
}
