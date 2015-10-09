package com.dbfs.jtt.swt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.dbfs.jtt.Activator;
import com.dbfs.jtt.Application;
import com.dbfs.jtt.TasksView;
import com.dbfs.jtt.dialogs.ClosedIssueDialog;
import com.dbfs.jtt.model.DailyTotalCounter;
import com.dbfs.jtt.model.SOAPSession;
import com.dbfs.jtt.model.Task;
import com.dbfs.jtt.resources.ColorSchemes;
import com.dbfs.jtt.resources.IImageKeys;
import com.dbfs.jtt.util.LogManager;

public class TasksComposite extends Composite {
	private final Logger logger = Logger.getLogger(TasksComposite.class.getName());
	private final static String DEBUG_INFO_INDX_UNDER_MOUSE_CURSOR = "indx under mouse cursor: ";
	private final static String DEBUG_INFO_INDX_MOUSE_STATUS = "mouse status: ";

	private final static int MAX_COUNT_TASK = 3;
	private final static int HEIGHT_COMPOSITE_OFFSET = 10;
	private final static int MINIMUM_SPENT_TIME_MINUTES = 5;

	private final static int MOUSE_OVER_ITEM = 1;
	private final static int MOUSE_DOWN_ITEM = 2;
	private final static int MOUSE_OVER_TIMER_BTN = 4;
	private final static int MOUSE_DOWN_TIMER_BTN = 8;
	private final static int MOUSE_OVER_LOG_WORK_BTN = 16;
	private final static int MOUSE_DOWN_LOG_WORK_BTN = 32;
	private final static int MOUSE_OVER_LOG_WORK_TXT = 64;
	private final static int MOUSE_DOWN_LOG_WORK_TXT = 128;
	private final static int MOUSE_UP_LOG_WORK_TXT = 256;
	private final static int MOUSE_OUT_ITEM = 512;
	private final static int MOUSE_OVER_LINK = 1024;
	private final static int MOUSE_DOWN_LINK = 2048;
	private final static int MOUSE_UP_LINK = 4096;
	private final static int MOUSE_OVER_PARENT_LINK = 8192;
	private final static int MOUSE_DOWN_PARENT_LINK = 16384;
	private final static int MOUSE_UP_PARENT_LINK = 32768;
	private final static int MOUSE_OVER_MINI_BTN = 65536;
	private final static int MOUSE_DOWN_MINI_BTN = 131072;

	private GC tCompGC;
	private Image imageTasks;
	private Image sizeAllImage;
	private Text text;
	private final Cursor CURSOR_SIZEALL;
	private final Cursor CURSOR_ARROW;
	private final Cursor CURSOR_I;
	private final Cursor CURSOR_LINK;
	private final int LEFT_OFFSET = 0;
	private final int RIGHT_OFFSET = 5;
	private final int GRADIENT_OFFSET = 2;
	private final int WIDTH_ARC = 7;
	private final int SUB_ITEM_HEIGHT = 13;
	private final int SUB_ITEM_OFFSET_X = 10;
	private final int ITEM_HEIGHT = 70;
	private final int ITEM_OFFSET = 0;
	private final int LINK_OFFSET = 5;
	private final int LOG_WORK_OFFSET = 5;
	private final int LOG_WORK_WIDTH = 55;
	private final int LOG_WORK_PADDING = 4;
	private final int LOG_WORK_TEXT_BOX_WIDTH = 30;

	private final int BTN_TIMER_OFFSET = 10;
	private final int BTN_TIMER_WIDTH = 50;
	private final int BTN_TIMER_HEIGHT = ITEM_HEIGHT - (BTN_TIMER_OFFSET * 2);
	private final int BTN_LOG_WORK_WIDTH = LOG_WORK_WIDTH - (LOG_WORK_PADDING * 2);
	private final int BTN_LOG_WORK_HEIGHT = 20;

	private final String START = "START";
	private final String STOP = "STOP";
	private final String RESUME = "RESUME";
	private final String COMMENT_MINI_BTN_TEXT = "T";
	private final String PAUSE_MINI_BTN_TEXT = "II";
	private final String TIME_FORMAT = "0:00:00";

	private int btnTimerX;
	private int btnLogWorkX;
	private int txtLogWorkX;
	private int linkX;
	private int linkHeight;
	private boolean isLogWorkChanged;
	private int widthComposite;
	private int heightComposite;
	private int standardTextHeight;
	private int linkTextHeight;
	private int linkParentTextHeight;
	private int mouseStatus;
	private int startTextOffsetX;
	private int stopTextOffsetX;
	private int timeTextOffsetX;
	private Font timeFont;
	private Font stopFont;
	private Font resumeFont;
	private Font startFont;
	private Font loggedTimeFont;
	private Font linkFont;
	private Font linkParentFont;
	private Font descFont;
	private Font logWorkFont;
	private Display display;
	private final DateFormat formatter = new SimpleDateFormat("mm:ss", Locale.getDefault());
	private int dragItemY = ITEM_OFFSET;
	private int[] cells;
	private List<Task> tasks;
	private List<TaskItem> taskItems;
	private int indxUnderCursor;
	private int indxSelectedItem;
	private int indxRunningItem = -1;
	private Slider slider;
	private int scroll;
	Point[] offset = new Point[1];
	private static Task activeTask;
	private static boolean stopped;
	private TasksView taskView;
	private DailyTotalCounter dailyTotalCounter;

	private final static String TOOL_TIP_TEXT_TIMER_BTN_RUNNING = "Minimum spent time: " + MINIMUM_SPENT_TIME_MINUTES + " min\nWorkLog Comment: ";
	private final static String TOOL_TIP_TEXT_MINI_BTN_RUNNING = "Suspend work on task ";
	private final static String TOOL_TIP_TEXT_MINI_BTN = "Start work log with Comment";

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public TasksComposite(Composite parent, int style, final List<Task> ptasks, final Slider slider, TasksView tasksView) {
		super(parent, SWT.NO_BACKGROUND);
		taskView = tasksView;
		dailyTotalCounter = new DailyTotalCounter(tasksView.getUserName());
		sizeAllImage = Activator.getImageDescriptor(IImageKeys.SIZE_ALL_CURSOR).createImage();
		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseScrolled(MouseEvent e) {
				if (!slider.getEnabled() || (MAX_COUNT_TASK >= tasks.size())) {
					return;
				}
				logger.debug(e.count);
				int shift = -1;
				if (e.count < 0) {
					if ((slider.getSelection() + heightComposite) == slider.getMaximum()) {
						return;
					}
					shift = 1;
				} else if (slider.getSelection() == 0) {
					return;
				}
				slider.setSelection(slider.getSelection() + (ITEM_HEIGHT * shift));
				onScroll();
			}
		});
		Rectangle rect = new Rectangle(0, 0, 1600, 1200);
		display = parent.getParent().getDisplay();
		imageTasks = new Image(display, rect);
		tCompGC = new GC(imageTasks);
		tCompGC.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		tCompGC.fillRectangle(0, 0, 1600, 1200);
		tCompGC.setFont(display.getSystemFont());
		this.slider = slider;
		text = new Text(this, SWT.NONE);

		update(ptasks);

		text.addListener(SWT.MouseMove, new Listener() {
			@Override
			public void handleEvent(Event e) {
				setCursor(CURSOR_I);
			}
		});
		text.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event e) {
				if (text.getText().length() == 0) {
					updateLogWorkFromTextbox();
					text.setVisible(true);
					text.setFocus();
				} else {
					String str = text.getText();
					for (int i = 0; i < str.length(); i++) {
						char ch = str.charAt(i);
						if ((!Character.isDigit(ch) && (ch != 'm') && (ch != 'h') && (ch != '.')) || (str.indexOf("h") == 0) || (str.indexOf("m") == 0) || (str.indexOf(".") == 0)) {
							text.setText("");
							return;
						}
					}
					isLogWorkChanged = true;
					drawBtnLogWork(indxSelectedItem);
					/***
					 * if (Integer.parseInt(text.getText()) > 24) { text.setText("24"); }
					 */
				}
			}
		});
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.keyCode == SWT.CR) || (e.keyCode == SWT.KEYPAD_CR)) {
					updateLogWorkFromTextbox();
				}
			}
		});

		text.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(final VerifyEvent event) {
				switch (event.keyCode) {
				case 0: // clearing of text
				case SWT.BS: // Backspace
				case SWT.DEL: // Delete
				case SWT.HOME: // Home
				case SWT.END: // End
				case SWT.ARROW_LEFT: // Left arrow
				case SWT.ARROW_RIGHT: // Right arrow
				case SWT.CR:
					return;
				}

				if ((!Character.isDigit(event.character) && (event.character != '.') && (event.character != 'm') && (event.character != 'h')) || ((event.character == '.') && (text.getText().indexOf('.') != -1)) || ((event.character == '.') && (text.getText().length() == 0)) || (text.getText().indexOf('m') != -1) || ((event.character == 'm') && (text.getText().length() == 0)) || (text.getText().indexOf('h') != -1)
						|| ((event.character == 'h') && (text.getText().length() == 0)) || (text.getText().length() == 5)) {
					event.doit = false; // disallow the action
					return;
				}
			}
		});
		Font prevFont = tCompGC.getFont();
		final Composite shell = parent;
		int x = 0;
		for (int i = 0; i < START.length(); i++) {
			x += tCompGC.getCharWidth(START.charAt(i));
		}
		startTextOffsetX = (BTN_TIMER_WIDTH - x) / 2;
		x = 0;
		for (int i = 0; i < STOP.length(); i++) {
			x += tCompGC.getCharWidth(STOP.charAt(i));
		}
		stopTextOffsetX = (BTN_TIMER_WIDTH - x) / 2;
		timeFont = new Font(display, "Arial", 8, SWT.BOLD);
		stopFont = new Font(display, "Arial", 6, SWT.BOLD);
		resumeFont = new Font(display, "Arial", 5, SWT.BOLD);
		startFont = new Font(display, "Arial", 7, SWT.BOLD);
		loggedTimeFont = new Font(display, "Arial", 7, SWT.NORMAL);
		logWorkFont = new Font(display, "Arial", 6, SWT.NORMAL);
		tCompGC.setFont(timeFont);
		x = 0;
		for (int i = 0; i < TIME_FORMAT.length(); i++) {
			x += tCompGC.getCharWidth(TIME_FORMAT.charAt(i));
		}
		timeTextOffsetX = (BTN_TIMER_WIDTH - x) / 2;
		standardTextHeight = tCompGC.getFontMetrics().getHeight();
		tCompGC.setFont(prevFont);
		CURSOR_ARROW = new Cursor(display, SWT.CURSOR_ARROW);
		CURSOR_I = new Cursor(display, SWT.CURSOR_IBEAM);
		CURSOR_LINK = new Cursor(display, SWT.CURSOR_HAND);

		PaletteData paletteData = new PaletteData(new RGB[] { new RGB(0, 0, 0), new RGB(255, 255, 255) });
		ImageData sourceData = new ImageData(32, 32, 1, paletteData);
		ImageData maskData = new ImageData(32, 32, 1, paletteData);

		/*** Because SWT provides another SIZE_ALL cursor for MacOS X ***/
		int[] cursorSource = new int[] {
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,0,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,0,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,
				1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,
				1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,0,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,0,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1 };

		int[] cursorMask = new int[] {
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,1,1,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,1,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,1,1,1,1,0,0,0,0,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,
				0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,
				0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,
				0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,
				0,0,0,0,0,0,1,1,1,1,0,0,0,0,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,1,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,1,1,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
				0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0, };

		sourceData.setPixels(0, 0, 1024, cursorSource, 0);
		maskData.setPixels(0, 0, 1024, cursorMask, 0);
		/*** CURSOR_SIZEALL = new Cursor(display, sourceData, maskData, 16, 16); */
		CURSOR_SIZEALL = new Cursor(display, sizeAllImage.getImageData(), 10, 10);
		final MessageBox messageBox = new MessageBox(parent.getShell(), SWT.APPLICATION_MODAL | SWT.YES | SWT.NO | SWT.ICON_QUESTION);

		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.drawImage(imageTasks, e.x, e.y, e.width, e.height, e.x, e.y, e.width, e.height);
			}
		});

		addMouseListener(new MouseListener() {
			@Override
			public void mouseDown(MouseEvent e) {
				if ((indxUnderCursor >= tasks.size()) || (indxSelectedItem >= tasks.size())) {
					return;
				}
				int startY = 0;
				if (tasks.get(indxUnderCursor).getParentKey() != null) {
					startY = SUB_ITEM_HEIGHT;
				}
				if (isLogWorkChanged && ((e.x < btnLogWorkX) || (e.x > (btnLogWorkX + BTN_LOG_WORK_WIDTH)) || ((e.y + scroll) < (taskItems.get(indxUnderCursor).getBtnLogworkY() + startY)) || ((e.y + scroll) > (taskItems.get(indxUnderCursor).getBtnLogworkY() + startY + BTN_LOG_WORK_HEIGHT)))
						&& ((e.x < txtLogWorkX) || (e.x > (txtLogWorkX + LOG_WORK_TEXT_BOX_WIDTH)) || ((e.y + scroll) < (taskItems.get(indxUnderCursor).getTxtLogworkY() + startY)) || ((e.y + scroll) > (taskItems.get(indxUnderCursor).getTxtLogworkY() + startY + standardTextHeight)))) {
					if (!text.isVisible() || (text.getText().length() == 0) || text.getText().equals("0") || !isLogWorkChanged) {
						return;
					}
					messageBox.setText("Warning");
					messageBox.setMessage("Do you want to save changes to Log Work?");
					int res = messageBox.open();
					if (res == SWT.YES) {
						updateLogWorkFromTextbox();
					} else {
						tasks.get(indxSelectedItem).setLogWorkH("");
						text.setVisible(false);
						isLogWorkChanged = false;
						drawTxtLogWork(indxUnderCursor);
						drawBtnLogWork(indxUnderCursor);
						if (indxSelectedItem != indxUnderCursor) {
							drawTxtLogWork(indxSelectedItem);
							drawBtnLogWork(indxSelectedItem);
						}
					}
					return;
				}
				if (!isMouseStatus(MOUSE_DOWN_LOG_WORK_TXT) && (indxSelectedItem != -1) && ((e.x < btnLogWorkX) || (e.x > (btnLogWorkX + BTN_LOG_WORK_WIDTH)) || ((e.y + scroll) < (taskItems.get(indxUnderCursor).getBtnLogworkY() + startY)) || ((e.y + scroll) > (taskItems.get(indxUnderCursor).getBtnLogworkY() + BTN_LOG_WORK_HEIGHT)))) {
					text.setVisible(false);
					drawTxtLogWork(indxSelectedItem);
				}
				if ((startY > 0) && (e.x > linkX) && (e.x < (linkX + taskItems.get(indxUnderCursor).getLinkParentWidth())) && ((e.y + scroll) > taskItems.get(indxUnderCursor).getParentLinkY()) && ((e.y + scroll) < (taskItems.get(indxUnderCursor).getParentLinkY() + linkParentTextHeight))) {
					addMouseStatus(MOUSE_DOWN_PARENT_LINK);
					indxSelectedItem = indxUnderCursor;
				} else if ((e.x > linkX) && (e.x < (linkX + taskItems.get(indxUnderCursor).getLinkWidth())) && ((e.y + scroll) > (taskItems.get(indxUnderCursor).getLinkY() + startY)) && ((e.y + scroll) < (taskItems.get(indxUnderCursor).getLinkY() + startY + linkHeight))) {
					addMouseStatus(MOUSE_DOWN_LINK);
					indxSelectedItem = indxUnderCursor;
				} else if ((e.x > btnTimerX) && (e.x < (btnTimerX + BTN_TIMER_WIDTH)) && ((e.y + scroll) > taskItems.get(indxUnderCursor).getBtnTimerY()) && ((e.y + scroll) < (taskItems.get(indxUnderCursor).getBtnTimerY() + BTN_TIMER_HEIGHT))) {
					int widthMiniBtn = (BTN_TIMER_WIDTH * 10) / 25;
					int heightMiniBtn = (BTN_TIMER_HEIGHT * 10) / 25;
					int xMiniBtn = BTN_TIMER_WIDTH - widthMiniBtn;
					int yMiniBtn = BTN_TIMER_HEIGHT - heightMiniBtn;
					if ((e.x > (btnTimerX + xMiniBtn)) && (e.x < (btnTimerX + xMiniBtn + widthMiniBtn)) && ((e.y + scroll) > (taskItems.get(indxUnderCursor).getBtnTimerY() + yMiniBtn)) && ((e.y + scroll) < (taskItems.get(indxUnderCursor).getBtnTimerY() + yMiniBtn + heightMiniBtn))) {
						addMouseStatus(MOUSE_DOWN_MINI_BTN);
					} else {
						addMouseStatus(MOUSE_DOWN_TIMER_BTN);
					}
					indxSelectedItem = indxUnderCursor;
					drawBtnStart(indxUnderCursor);
				} else if (!tasks.get(indxUnderCursor).isRunning() && (e.x > btnLogWorkX) && (e.x < (btnLogWorkX + BTN_LOG_WORK_WIDTH)) && ((e.y + scroll) > taskItems.get(indxUnderCursor).getBtnLogworkY()) && ((e.y + scroll) < (taskItems.get(indxUnderCursor).getBtnLogworkY() + BTN_LOG_WORK_HEIGHT))) {
					if (isLogWorkChanged && (indxSelectedItem != indxUnderCursor)) {
						return;
					}
					addMouseStatus(MOUSE_DOWN_LOG_WORK_BTN);
					indxSelectedItem = indxUnderCursor;
					drawBtnLogWork(indxUnderCursor);
				} else if (!tasks.get(indxUnderCursor).isRunning() && (e.x > txtLogWorkX) && (e.x < (txtLogWorkX + LOG_WORK_TEXT_BOX_WIDTH)) && ((e.y + scroll) > taskItems.get(indxUnderCursor).getTxtLogworkY()) && ((e.y + scroll) < (taskItems.get(indxUnderCursor).getTxtLogworkY() + standardTextHeight))) {
					addMouseStatus(MOUSE_DOWN_LOG_WORK_TXT);
					indxSelectedItem = indxUnderCursor;
				} else if ((e.x > LEFT_OFFSET) && (e.x < (widthComposite - RIGHT_OFFSET)) && ((e.y + scroll) > taskItems.get(indxUnderCursor).getItemY()) && ((e.y + scroll) < (taskItems.get(indxUnderCursor).getItemY() + ITEM_HEIGHT))) {
					addMouseStatus(MOUSE_DOWN_ITEM);
					indxSelectedItem = indxUnderCursor;
					Point pt1 = toDisplay(0, 0);
					Point pt2 = shell.toDisplay(e.x, e.y + scroll);
					offset[0] = new Point(pt2.x - pt1.x, (e.y + scroll) - taskItems.get(indxUnderCursor).getItemY());
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				setFocus();
				if ((indxUnderCursor >= tasks.size()) || (indxSelectedItem >= tasks.size())) {
					return;
				}
				int startY = 0;
				if (tasks.get(indxUnderCursor).getParentKey() != null) {
					startY = SUB_ITEM_HEIGHT;
				}
				if (offset[0] != null) {
					offset[0] = null;
					eraseItem(indxSelectedItem);
					if ((indxSelectedItem - 1) >= 0) {
						drawItem(indxSelectedItem - 1);
					}
					if ((indxSelectedItem + 1) < tasks.size()) {
						drawItem(indxSelectedItem + 1);
					}
					moveTaskItem(indxSelectedItem, cells[indxSelectedItem]);
					drawItem(indxSelectedItem);
				}
				if ((startY > 0) && (e.x > linkX) && (e.x < (linkX + taskItems.get(indxUnderCursor).getLinkParentWidth())) && ((e.y + scroll) > taskItems.get(indxUnderCursor).getParentLinkY()) && ((e.y + scroll) < (taskItems.get(indxUnderCursor).getParentLinkY() + linkParentTextHeight))) {
					if (isMouseStatus(MOUSE_DOWN_PARENT_LINK)) {
						org.eclipse.swt.program.Program.launch(tasks.get(indxUnderCursor).getParentUrl());
					}
				} else if ((e.x > linkX) && (e.x < (linkX + taskItems.get(indxUnderCursor).getLinkWidth())) && ((e.y + scroll) > (taskItems.get(indxUnderCursor).getLinkY() + startY)) && ((e.y + scroll) < (taskItems.get(indxUnderCursor).getLinkY() + startY + linkHeight))) {
					if (isMouseStatus(MOUSE_DOWN_LINK)) {
						org.eclipse.swt.program.Program.launch(tasks.get(indxUnderCursor).getUrl());
					}
				} else if ((e.x > btnTimerX) && (e.x < (btnTimerX + BTN_TIMER_WIDTH)) && ((e.y + scroll) > taskItems.get(indxUnderCursor).getBtnTimerY()) && ((e.y + scroll) < (taskItems.get(indxUnderCursor).getBtnTimerY() + BTN_TIMER_HEIGHT))) {
					int widthMiniBtn = (BTN_TIMER_WIDTH * 10) / 25;
					int heightMiniBtn = (BTN_TIMER_HEIGHT * 10) / 25;
					int xMiniBtn = BTN_TIMER_WIDTH - widthMiniBtn;
					int yMiniBtn = BTN_TIMER_HEIGHT - heightMiniBtn;
					if ((e.x > (btnTimerX + xMiniBtn)) && (e.x < (btnTimerX + xMiniBtn + widthMiniBtn)) && ((e.y + scroll) > (taskItems.get(indxUnderCursor).getBtnTimerY() + yMiniBtn)) && ((e.y + scroll) < (taskItems.get(indxUnderCursor).getBtnTimerY() + yMiniBtn + heightMiniBtn))) {
						if (!isMouseStatus(MOUSE_DOWN_MINI_BTN)) {
							return;
						}
						addMouseStatus(MOUSE_OVER_MINI_BTN);
						if (tasks.get(indxUnderCursor).isRunning()) {
							tasks.get(indxUnderCursor).setSuspended(true);
							toggleTimer(indxUnderCursor);
						} else {
							InputDialog dialog = new InputDialog(getShell(), "WorkLog Comment", "Please write work log comment:", tasks.get(indxUnderCursor).getComment(), null);
							if (dialog.open() == Window.OK) {
								tasks.get(indxUnderCursor).setComment(dialog.getValue());
								if ((getActiveTask() != null) && !tasks.contains(getActiveTask())) {
									showInfoMessage("Info", "You already have started task: " + getActiveTask().getKey() + ", but it is hidden by filters.");
								} else if (isOtherTaskStarted()) {
									getActiveTask().setRunning(false);
									drawItem(getActiveTask());
								}
								toggleTimer(indxUnderCursor);
							}
						}
					} else {
						if (!isMouseStatus(MOUSE_DOWN_TIMER_BTN)) {
							return;
						}
						addMouseStatus(MOUSE_OVER_TIMER_BTN);
						if ((getActiveTask() != null) && !tasks.contains(getActiveTask())) {
							showInfoMessage("Info", "You already have started task: " + getActiveTask().getKey() + ", but it is hidden by filters.");
						} else if (isOtherTaskStarted()) {
							getActiveTask().setRunning(false);
							drawItem(getActiveTask());
						}
						toggleTimer(indxUnderCursor);
					}
					drawItem(indxUnderCursor);
				} else if ((e.x > btnLogWorkX) && (e.x < (btnLogWorkX + BTN_LOG_WORK_WIDTH)) && ((e.y + scroll) > taskItems.get(indxUnderCursor).getBtnLogworkY()) && ((e.y + scroll) < (taskItems.get(indxUnderCursor).getBtnLogworkY() + BTN_LOG_WORK_HEIGHT))) {
					if (!isMouseStatus(MOUSE_DOWN_LOG_WORK_BTN) || !isLogWorkChanged) {
						return;
					}
					updateLogWorkFromTextbox();
				} else if ((e.x > txtLogWorkX) && (e.x < (txtLogWorkX + LOG_WORK_TEXT_BOX_WIDTH)) && ((e.y + scroll) > taskItems.get(indxUnderCursor).getTxtLogworkY()) && ((e.y + scroll) < (taskItems.get(indxUnderCursor).getTxtLogworkY() + standardTextHeight))) {
					if (!isMouseStatus(MOUSE_DOWN_LOG_WORK_TXT)) {
						return;
					}
					indxSelectedItem = indxUnderCursor;
					addMouseStatus(MOUSE_UP_LOG_WORK_TXT);
					text.setLocation(txtLogWorkX + 1, (taskItems.get(indxUnderCursor).getTxtLogworkY() + 1) - scroll);
					text.setSize(LOG_WORK_TEXT_BOX_WIDTH - 1, standardTextHeight - 1);
					text.setVisible(true);
					text.setFocus();
					text.setText("");
				}
			}

			private void toggleTimer(int index) {
				Task task = tasks.get(index);
				stopped = task.isRunning();
				task.setRunning(!task.isRunning());
				if (task.isRunning()) {
					indxRunningItem = index;
					task.setStartedTimer(System.currentTimeMillis() - task.getCurrentTimeSpent());
					setActiveTask(task);
					startWork(task);
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent arg) {
				if (((arg.y + scroll) > (cells[cells.length - 1] + ITEM_HEIGHT)) || ((arg.y + scroll) < 0)) {
					return;
				}
				indxUnderCursor = getIndx(arg.y + scroll);
				if ((indxUnderCursor >= tasks.size()) || (indxSelectedItem >= tasks.size())) {
					indxUnderCursor = tasks.size() - 1;
					setCursor(CURSOR_ARROW);
					return;
				}
				int startY = 0;
				int startX = 0;
				if (tasks.get(indxUnderCursor).getParentKey() != null) {
					startY = SUB_ITEM_HEIGHT;
					startX = SUB_ITEM_OFFSET_X;
				}
				if ((startY > 0) && (arg.x > linkX) && (arg.x < (linkX + taskItems.get(indxUnderCursor).getLinkParentWidth())) && ((arg.y + scroll) > taskItems.get(indxUnderCursor).getParentLinkY()) && ((arg.y + scroll) < (taskItems.get(indxUnderCursor).getParentLinkY() + linkParentTextHeight))) {
					if (isMouseStatus(MOUSE_OVER_PARENT_LINK)) {
						return;
					}
					setCursor(CURSOR_LINK);
					addMouseStatus(MOUSE_OVER_PARENT_LINK);
				} else if ((arg.x > (linkX + startX)) && (arg.x < (linkX + startX + taskItems.get(indxUnderCursor).getLinkWidth())) && ((arg.y + scroll) > (taskItems.get(indxUnderCursor).getLinkY() + startY)) && ((arg.y + scroll) < (taskItems.get(indxUnderCursor).getLinkY() + startY + linkHeight))) {
					if (isMouseStatus(MOUSE_OVER_LINK)) {
						return;
					}
					setCursor(CURSOR_LINK);
					addMouseStatus(MOUSE_OVER_LINK);
				} else if ((arg.x > btnTimerX) && (arg.x < (btnTimerX + BTN_TIMER_WIDTH)) && ((arg.y + scroll) > taskItems.get(indxUnderCursor).getBtnTimerY()) && ((arg.y + scroll) < (taskItems.get(indxUnderCursor).getBtnTimerY() + BTN_TIMER_HEIGHT))) {
					setCursor(CURSOR_ARROW);
					int widthMiniBtn = (BTN_TIMER_WIDTH * 10) / 25;
					int heightMiniBtn = (BTN_TIMER_HEIGHT * 10) / 25;
					int xMiniBtn = BTN_TIMER_WIDTH - widthMiniBtn;
					int yMiniBtn = BTN_TIMER_HEIGHT - heightMiniBtn;
					if ((arg.x > (btnTimerX + xMiniBtn)) && (arg.x < (btnTimerX + xMiniBtn + widthMiniBtn)) && ((arg.y + scroll) > (taskItems.get(indxUnderCursor).getBtnTimerY() + yMiniBtn)) && ((arg.y + scroll) < (taskItems.get(indxUnderCursor).getBtnTimerY() + yMiniBtn + heightMiniBtn))) {
						if (isMouseStatus(MOUSE_DOWN_MINI_BTN) || isMouseStatus(MOUSE_OVER_MINI_BTN)) {
							return;
						}
						addMouseStatus(MOUSE_OVER_MINI_BTN);
					} else {
						if (isMouseStatus(MOUSE_DOWN_TIMER_BTN) || isMouseStatus(MOUSE_OVER_TIMER_BTN)) {
							return;
						}
						addMouseStatus(MOUSE_OVER_TIMER_BTN);
					}
					drawBtnStart(indxUnderCursor);

				} else if (!tasks.get(indxUnderCursor).isRunning() && (arg.x > btnLogWorkX) && (arg.x < (btnLogWorkX + BTN_LOG_WORK_WIDTH)) && ((arg.y + scroll) > taskItems.get(indxUnderCursor).getBtnLogworkY()) && ((arg.y + scroll) < (taskItems.get(indxUnderCursor).getBtnLogworkY() + BTN_LOG_WORK_HEIGHT))) {
					if (isMouseStatus(MOUSE_DOWN_LOG_WORK_BTN) || isMouseStatus(MOUSE_OVER_LOG_WORK_BTN)) {
						return;
					}
					setCursor(CURSOR_ARROW);
					addMouseStatus(MOUSE_OVER_LOG_WORK_BTN);
					drawBtnLogWork(indxUnderCursor);
				} else if (!tasks.get(indxUnderCursor).isRunning() && (arg.x > txtLogWorkX) && (arg.x < (txtLogWorkX + LOG_WORK_TEXT_BOX_WIDTH)) && ((arg.y + scroll) > taskItems.get(indxUnderCursor).getTxtLogworkY()) && ((arg.y + scroll) < (taskItems.get(indxUnderCursor).getTxtLogworkY() + standardTextHeight))) {
					if (isMouseStatus(MOUSE_OVER_LOG_WORK_TXT)) {
						return;
					}
					setCursor(CURSOR_I);
					addMouseStatus(MOUSE_OVER_LOG_WORK_TXT);
				} else {
					if (isMouseStatus(MOUSE_OVER_TIMER_BTN) || isMouseStatus(MOUSE_DOWN_TIMER_BTN) || isMouseStatus(MOUSE_OVER_LOG_WORK_BTN) || isMouseStatus(MOUSE_DOWN_LOG_WORK_BTN) || isMouseStatus(MOUSE_OVER_MINI_BTN) || isMouseStatus(MOUSE_DOWN_MINI_BTN)) {
						addMouseStatus(MOUSE_OVER_ITEM);
						drawBtnStart(indxUnderCursor);
						drawBtnLogWork(indxUnderCursor);
					}
					if ((arg.x > LEFT_OFFSET) && (arg.x < (widthComposite - RIGHT_OFFSET)) && ((arg.y + scroll) > taskItems.get(indxUnderCursor).getItemY()) && ((arg.y + scroll) < (taskItems.get(indxUnderCursor).getItemY() + ITEM_HEIGHT))) {
						setCursor(CURSOR_SIZEALL);
						addMouseStatus(MOUSE_OVER_ITEM);
					} else {
						setCursor(CURSOR_ARROW);
						addMouseStatus(MOUSE_OUT_ITEM);
					}
					if (offset[0] != null) {
						Point pt = offset[0];
						eraseItem(indxSelectedItem);
						dragItemY = (arg.y + scroll) - pt.y;
						if ((indxSelectedItem - 1) >= 0) {
							if ((taskItems.get(indxSelectedItem).getItemY() - taskItems.get(indxSelectedItem - 1).getItemY()) < (ITEM_HEIGHT / 2)) {
								eraseItem(indxSelectedItem - 1);
								moveTaskItem(indxSelectedItem - 1, taskItems.get(indxSelectedItem - 1).getItemY() + ITEM_HEIGHT);
								drawItem(indxSelectedItem - 1);
								shiftTask(indxSelectedItem, false);
								if (tasks.contains(getActiveTask()) && (indxRunningItem != -1)) {
									if (indxRunningItem == indxSelectedItem) {
										indxRunningItem--;
									} else if (indxRunningItem == (indxSelectedItem - 1)) {
										indxRunningItem = indxSelectedItem;
									}
								}
								indxSelectedItem--;
								if ((dragItemY - scroll) < 0) {
									slider.setSelection(slider.getSelection() - ITEM_HEIGHT);
									onScroll();
								}
							} else {
								drawItem(indxSelectedItem - 1);
							}
						}
						if ((indxSelectedItem + 1) < tasks.size()) {
							if ((taskItems.get(indxSelectedItem + 1).getItemY() - taskItems.get(indxSelectedItem).getItemY()) < (ITEM_HEIGHT / 2)) {
								eraseItem(indxSelectedItem + 1);
								moveTaskItem(indxSelectedItem + 1, taskItems.get(indxSelectedItem + 1).getItemY() - ITEM_HEIGHT);
								drawItem(indxSelectedItem + 1);
								shiftTask(indxSelectedItem, true);
								if (tasks.contains(getActiveTask()) && (indxRunningItem != -1)) {
									if (indxRunningItem == indxSelectedItem) {
										indxRunningItem++;
									} else if (indxRunningItem == (indxSelectedItem + 1)) {
										indxRunningItem = indxSelectedItem;
									}
								}
								indxSelectedItem++;
								if ((dragItemY + ITEM_HEIGHT) > (heightComposite + scroll)) {
									slider.setSelection(slider.getSelection() + ITEM_HEIGHT);
									onScroll();
								}
							} else {
								drawItem(indxSelectedItem + 1);
							}
						}
						moveTaskItem(indxSelectedItem, dragItemY);
						drawItem(indxSelectedItem);
					}
				}
			}
		});

		addMouseStatus(MOUSE_OUT_ITEM);
		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				if ((widthComposite == getClientArea().width) && (heightComposite == getClientArea().height)) {
					return;
				}
				for (int i = 0; i < tasks.size(); i++) {
					eraseItem(i);
				}
				widthComposite = getClientArea().width;
				heightComposite = (ITEM_HEIGHT * 3) + ITEM_OFFSET + HEIGHT_COMPOSITE_OFFSET;
				setSize(widthComposite, heightComposite);
				slider.setSize(slider.getSize().x, heightComposite);
				slider.setThumb(heightComposite);
				slider.setMaximum((ITEM_HEIGHT * tasks.size()) + ITEM_OFFSET);
				if (tasks.size() > MAX_COUNT_TASK) {
					slider.setEnabled(true);
				} else {
					slider.setEnabled(false);
				}
				for (int i = 0; i < tasks.size(); i++) {
					resizeTaskItem(i);
					drawItem(i);
				}
			}
		});
		Shell shell1 = parent.getShell();
		shell1.addShellListener(new ShellListener() {
			Job job = new Job("sync of status issues") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					return Status.OK_STATUS;
				}
			};

			@Override
			public void shellActivated(ShellEvent event) {
				System.out.println("activate");
				job.schedule();
			}

			@Override
			public void shellClosed(ShellEvent arg0) {
				System.out.println("close");
				job.cancel();
			}

			@Override
			public void shellDeactivated(ShellEvent arg0) {
				System.out.println("Deactivate");
			}

			@Override
			public void shellDeiconified(ShellEvent arg0) {
				System.out.println("Deiconified");
			}

			@Override
			public void shellIconified(ShellEvent arg0) {
				System.out.println("Iconified");
			}
		});

	}

	private void updateLogWorkFromTextbox() {
		final Task localTask = tasks.get(indxSelectedItem);
		localTask.setLogWorkH("");
		isLogWorkChanged = false;
		drawTxtLogWork(indxSelectedItem);
		drawBtnLogWork(indxSelectedItem);
		if (text.getText().length() == 0) {
			return;
		}
		String str = text.getText();
		float value = 0f;
		int addSpentH = 0;
		long millis = 0;
		if ((str.indexOf('h') == -1) && (str.indexOf('m') == -1)) {
			if (str.indexOf('.') == -1) {
				addSpentH = Integer.parseInt(str);
				millis = TimeUnit.HOURS.toMillis(addSpentH);
			} else {
				value = Float.parseFloat(str);
				addSpentH = (int) (value * 60);
				millis = TimeUnit.MINUTES.toMillis(addSpentH);
			}
		} else if (str.indexOf('h') != -1) {
			str = str.substring(0, str.indexOf('h'));
			if (str.indexOf('.') == -1) {
				addSpentH = Integer.parseInt(str);
				millis = TimeUnit.HOURS.toMillis(addSpentH);
			} else {
				value = Float.parseFloat(str);
				addSpentH = (int) (value * 60);
				millis = TimeUnit.MINUTES.toMillis(addSpentH);
			}
		} else {
			str = str.substring(0, str.indexOf('m'));
			if (str.indexOf('.') != -1) {
				showErrorMessage("Error", "The logged time can not be less than " + MINIMUM_SPENT_TIME_MINUTES + " minutes!");
				return;
			} else {
				addSpentH = Integer.parseInt(str);
				if (addSpentH < MINIMUM_SPENT_TIME_MINUTES) {
					showErrorMessage("Error", "The logged time can not be less than " + MINIMUM_SPENT_TIME_MINUTES + " minutes!");
					return;
				}
				millis = TimeUnit.MINUTES.toMillis(addSpentH);
			}
		}
		// localTask.setTimeSpent(localTask.getTimeSpent() + millis);
		text.setVisible(false);
		// drawTimeEstimation(indxSelectedItem);
		text.setText("");
		if (millis > 0) {
			localTask.setCurrentTimeSpent(localTask.getCurrentTimeSpent() + millis);
		}
		Job job = new Job("Update worklog from txt") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				updateWorklog(localTask);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		// drawItem(localTask);
		dailyTotalCounter.setUserName(taskView.getUserName());
		dailyTotalCounter.add(millis);
		drawDailyTime();
	}

	private boolean isMouseStatus(int check) {
		return ((mouseStatus & check) == check);
	}

	private void addMouseStatus(int status) {
		if (mouseStatus == status) {
			return;
		}
		mouseStatus = status;
		String stat = "";
		setToolTipText("");
		if (isMouseStatus(MOUSE_OVER_ITEM)) {
			stat = "MOUSE_OVER_ITEM";
		} else if (isMouseStatus(MOUSE_DOWN_ITEM)) {
			stat = "MOUSE_DOWN_ITEM";
		} else if (isMouseStatus(MOUSE_OVER_TIMER_BTN)) {
			stat = "MOUSE_OVER_TIMER_BTN";
			if (tasks.get(indxUnderCursor).isRunning()) {
				setToolTipText(TOOL_TIP_TEXT_TIMER_BTN_RUNNING + tasks.get(indxUnderCursor).getComment());
			} else {
			}
		} else if (isMouseStatus(MOUSE_DOWN_TIMER_BTN)) {
			stat = "MOUSE_DOWN_TIMER_BTN";
		} else if (isMouseStatus(MOUSE_OVER_LOG_WORK_BTN)) {
			stat = "MOUSE_OVER_WORK_LOG_BTN";
		} else if (isMouseStatus(MOUSE_DOWN_LOG_WORK_BTN)) {
			stat = "MOUSE_DOWN_WORK_LOG_BTN";
		} else if (isMouseStatus(MOUSE_OVER_LOG_WORK_TXT)) {
			stat = "MOUSE_OVER_WORK_LOG_TXT";
		} else if (isMouseStatus(MOUSE_OUT_ITEM)) {
			stat = "MOUSE_OUT_ITEM";
		} else if (isMouseStatus(MOUSE_DOWN_LOG_WORK_TXT)) {
			stat = "MOUSE_DOWN_LOG_WORK_TXT";
		} else if (isMouseStatus(MOUSE_UP_LOG_WORK_TXT)) {
			stat = "MOUSE_UP_LOG_WORK_TXT";
		} else if (isMouseStatus(MOUSE_OVER_LINK)) {
			stat = "MOUSE_OVER_LINK";
		} else if (isMouseStatus(MOUSE_DOWN_LINK)) {
			stat = "MOUSE_DOWN_LINK";
		} else if (isMouseStatus(MOUSE_UP_LINK)) {
			stat = "MOUSE_UP_LINK";
		} else if (isMouseStatus(MOUSE_OVER_PARENT_LINK)) {
			stat = "MOUSE_OVER_PARENT_LINK";
		} else if (isMouseStatus(MOUSE_DOWN_PARENT_LINK)) {
			stat = "MOUSE_DOWN_PARENT_LINK";
		} else if (isMouseStatus(MOUSE_UP_PARENT_LINK)) {
			stat = "MOUSE_UP_PARENT_LINK";
		} else if (isMouseStatus(MOUSE_OVER_MINI_BTN)) {
			stat = "MOUSE_OVER_MINI_BTN";
			if (tasks.get(indxUnderCursor).isRunning()) {
				setToolTipText(TOOL_TIP_TEXT_MINI_BTN_RUNNING + tasks.get(indxUnderCursor).getKey());
			} else {
				setToolTipText(TOOL_TIP_TEXT_MINI_BTN);
			}
		} else if (isMouseStatus(MOUSE_DOWN_MINI_BTN)) {
			stat = "MOUSE_DOWN_MINI_BTN";
		} else {
		}
		logger.debug(DEBUG_INFO_INDX_MOUSE_STATUS + stat);
	}

	protected void eraseItem(int indx) {
		tCompGC.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		tCompGC.fillRectangle(LEFT_OFFSET - 2, taskItems.get(indx).getItemY() - scroll, widthComposite, ITEM_HEIGHT + 1);
		redraw(LEFT_OFFSET - 2, taskItems.get(indx).getItemY() - 2 - scroll, widthComposite, ITEM_HEIGHT + 4, false);
	}

	public void resizeTaskItem(int indx) {
		int width = widthComposite - LEFT_OFFSET - RIGHT_OFFSET;
		btnTimerX = width - BTN_TIMER_OFFSET - BTN_TIMER_WIDTH;
		taskItems.get(indx).setItemY((indx * ITEM_HEIGHT) + ITEM_OFFSET);
		taskItems.get(indx).setBtnTimerY(taskItems.get(indx).getItemY() + BTN_TIMER_OFFSET);
		btnLogWorkX = (btnTimerX - LOG_WORK_OFFSET - LOG_WORK_WIDTH) + LOG_WORK_PADDING;
		taskItems.get(indx).setBtnLogworkY(taskItems.get(indx).getBtnTimerY() + (LOG_WORK_PADDING * 2) + standardTextHeight);
		txtLogWorkX = btnLogWorkX;
		int startY = 0;
		if (tasks.get(indx).getParentKey() != null) {
			startY = SUB_ITEM_HEIGHT;
			/*** Start block of setting width for parent link ***/
			Font prevFont = tCompGC.getFont();
			int x = 0;
			tCompGC.setFont(linkParentFont);
			for (int z = 0; z < tasks.get(indx).getParentKey().length(); z++) {
				x += tCompGC.getCharWidth(tasks.get(indx).getParentKey().charAt(z));
			}
			taskItems.get(indx).setLinkParentWidth(x);
			tCompGC.setFont(prevFont);
			/*** End block of setting width for parent link ***/
		}
		if (text.isVisible()) {
			text.setLocation(txtLogWorkX + 1, text.getLocation().y + startY);
		}
		taskItems.get(indx).setTxtLogworkY(taskItems.get(indx).getBtnTimerY() + LOG_WORK_PADDING);
		linkX = LEFT_OFFSET + LINK_OFFSET;
		taskItems.get(indx).setLinkY(taskItems.get(indx).getItemY() + LINK_OFFSET);
		taskItems.get(indx).setParentLinkY(taskItems.get(indx).getItemY());
		linkHeight = linkTextHeight;
	}

	private void moveTaskItem(int indx, int toY) {
		taskItems.get(indx).setItemY(toY);
		taskItems.get(indx).setBtnTimerY(taskItems.get(indx).getItemY() + BTN_TIMER_OFFSET);
		taskItems.get(indx).setBtnLogworkY(taskItems.get(indx).getBtnTimerY() + (LOG_WORK_PADDING * 2) + standardTextHeight);
		taskItems.get(indx).setTxtLogworkY(taskItems.get(indx).getBtnTimerY() + LOG_WORK_PADDING);
		taskItems.get(indx).setLinkY(taskItems.get(indx).getItemY() + LINK_OFFSET);
		taskItems.get(indx).setParentLinkY(taskItems.get(indx).getItemY());
	}

	/**
	 * Draw exist only in list task.
	 * 
	 * @param task
	 */
	public void drawItem(Task task) {
		int indx = getTaskIndx(task);
		if (indx != -1) {
			drawItem(indx);
		}
	}

	public int getTaskIndx(Task task) {
		if (!tasks.contains(task)) {
			return -1;
		}
		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).equals(task)) {
				return i;
			}
		}
		return -1;
	}

	public void drawItem(int indx) {
		if ((tasks.size() == 0) || (taskItems.size() == 0) || ((taskItems.get(indx).getItemY() + ITEM_HEIGHT) <= scroll) || (taskItems.get(indx).getItemY() >= (scroll + heightComposite))) {
			return;
		}
		int startY = 0;
		int startX = 0;
		if (tasks.get(indx).getParentKey() != null) {
			startX = SUB_ITEM_OFFSET_X;
		}
		// logger.debug("HashCode for tasks List: " + tasks.hashCode());
		int width = widthComposite - LEFT_OFFSET - RIGHT_OFFSET;
		tCompGC.setForeground(ColorSchemes.taskBorderColor);
		tCompGC.setBackground(ColorSchemes.taskItemBackgroundColor);
		tCompGC.fillRoundRectangle(LEFT_OFFSET, taskItems.get(indx).getItemY() - scroll, width, ITEM_HEIGHT, WIDTH_ARC, WIDTH_ARC);

		/*** Painting Gradient for task item ***/
		/*
		 * tCompGC.setBackground(ColorSchemes.taskGradientColor); tCompGC.setForeground(ColorSchemes.taskColor); tCompGC.fillGradientRectangle(LEFT_OFFSET + GRADIENT_OFFSET, taskItems.get(indx).getItemY() + GRADIENT_OFFSET - scroll, width - GRADIENT_OFFSET, ITEM_HEIGHT / 2 - GRADIENT_OFFSET, true); tCompGC.setBackground(ColorSchemes.taskColor); tCompGC.setForeground(ColorSchemes.taskGradientColor);
		 * tCompGC.fillGradientRectangle(LEFT_OFFSET + GRADIENT_OFFSET, taskItems.get(indx).getItemY() + ITEM_HEIGHT / 2 - scroll, width - GRADIENT_OFFSET, ITEM_HEIGHT / 2 - GRADIENT_OFFSET, true);
		 */
		tCompGC.drawRoundRectangle(LEFT_OFFSET, taskItems.get(indx).getItemY() - scroll, width, ITEM_HEIGHT, WIDTH_ARC, WIDTH_ARC);
		tCompGC.drawRoundRectangle(LEFT_OFFSET + 1, (taskItems.get(indx).getItemY() + 1) - scroll, width - 2, ITEM_HEIGHT - 2, WIDTH_ARC, WIDTH_ARC);
		/*
		 * if (tasks.get(indx).getParentKey() != null) { tCompGC.setForeground(ColorSchemes.taskBorderColor); tCompGC.drawRectangle(LEFT_OFFSET, taskItems.get(indx).getItemY() + startY - scroll, width, (ITEM_HEIGHT - startY) / 2); tCompGC.drawRectangle(LEFT_OFFSET + 1, taskItems.get(indx).getItemY() + startY + 1 - scroll, width - 2, (ITEM_HEIGHT - startY) / 2);
		 * tCompGC.setBackground(ColorSchemes.taskItemBackgroundColor); tCompGC.fillRectangle(LEFT_OFFSET + 2, taskItems.get(indx).getItemY() + startY + 2 - scroll, width - 3, (ITEM_HEIGHT - startY) / 2); }
		 */
		Font prevFont = tCompGC.getFont();
		if (tasks.get(indx).getParentKey() != null) {
			startY = SUB_ITEM_HEIGHT;
			tCompGC.setBackground(ColorSchemes.subTaskItemBackgroundColor);
			tCompGC.fillRectangle(LEFT_OFFSET + 3, (taskItems.get(indx).getItemY() + 2) - scroll, btnTimerX - (LOG_WORK_OFFSET * 2) - LOG_WORK_WIDTH, startY + 5);
			/*
			 * tCompGC.drawRoundRectangle(LEFT_OFFSET, taskItems.get(indx).getItemY() - scroll, width, startY + 5, WIDTH_ARC, WIDTH_ARC); tCompGC.drawRoundRectangle(LEFT_OFFSET + 1, taskItems.get(indx).getItemY() + 1 - scroll, width - 2, startY - 2 + 5, WIDTH_ARC, WIDTH_ARC);
			 */
			Rectangle rect = tCompGC.getClipping();
			int prevClippingWidth = rect.width;
			tCompGC.setClipping(LEFT_OFFSET + 3, (taskItems.get(indx).getItemY() + 2) - scroll, btnTimerX - (LOG_WORK_OFFSET * 2) - LOG_WORK_WIDTH, startY + 5);
			tCompGC.setForeground(ColorSchemes.taskStartTextColor);
			tCompGC.setFont(linkParentFont);
			tCompGC.drawText(tasks.get(indx).getParentKey(), linkX, taskItems.get(indx).getItemY() - scroll, true);
			tCompGC.drawText(tasks.get(indx).getParentSum(), linkX + taskItems.get(indx).getLinkWidth() + 10, taskItems.get(indx).getItemY() - scroll, true);
			tCompGC.setForeground(ColorSchemes.taskBorderColor);
			tCompGC.setClipping(rect.x, rect.y, prevClippingWidth, rect.height);
			tCompGC.drawLine(linkX + 4, (taskItems.get(indx).getLinkY() + startY + 5) - scroll, linkX + 4, (taskItems.get(indx).getLinkY() + startY + ITEM_HEIGHT) - SUB_ITEM_HEIGHT - 8 - scroll);
			// tCompGC.drawLine(linkX + 6, taskItems.get(indx).getLinkY() + startY + 5 - scroll, linkX + 6, taskItems.get(indx).getLinkY() + startY + ITEM_HEIGHT - SUB_ITEM_HEIGHT - 8 - scroll);
			tCompGC.setForeground(ColorSchemes.taskStartTextColor);
			tCompGC.drawLine(linkX, (taskItems.get(indx).getItemY() + linkParentTextHeight) - scroll, linkX + taskItems.get(indx).getLinkParentWidth(), (taskItems.get(indx).getItemY() + linkParentTextHeight) - scroll);
		}

		drawLink(indx);
		drawTimeEstimation(indx);
		Rectangle rect = tCompGC.getClipping();
		int prevClippingWidth = rect.width;
		tCompGC.setFont(descFont);
		tCompGC.setForeground(ColorSchemes.taskStartTextColor);
		tCompGC.setClipping(linkX + startX, rect.y, btnTimerX - (LOG_WORK_OFFSET * 2) - startX, rect.height);
		tCompGC.drawText(tasks.get(indx).getDescription(), linkX + startX, (taskItems.get(indx).getLinkY() + startY + linkHeight + 10) - scroll, true);// ticket title
		tCompGC.setClipping(rect.x, rect.y, prevClippingWidth, rect.height);
		tCompGC.setFont(prevFont);
		drawTxtLogWork(indx);
		drawBtnLogWork(indx);
		drawBtnStart(indx);

		if (tasks.get(indx).getAdded() > 0) {
			tCompGC.setForeground(ColorSchemes.newTaskColor);
			tCompGC.setFont(descFont);
			tCompGC.drawText("New", 10, (taskItems.get(indx).getLinkY() + startY + linkHeight) - 4 - scroll, true);
		}

		/*** test grid ***/
		/*
		 * for (int i = 0; i < cells.length; i++) { tCompGC.drawRectangle(LEFT_OFFSET, cells[i], width, ITEM_HEIGHT); tCompGC.setForeground(ColorSchemes.taskStopColor); tCompGC.drawLine(0, cells[i] + ITEM_HEIGHT / 2, width, cells[i] + ITEM_HEIGHT / 2); tCompGC.setForeground(ColorSchemes.taskStartTextColor); }
		 */

		redraw(LEFT_OFFSET, taskItems.get(indx).getItemY() - scroll, widthComposite, ITEM_HEIGHT + 1, false);
		logger.debug("redraw(LEFT_OFFSET[" + LEFT_OFFSET + "], (taskItems.get(" + indx + ").getItemY())[" + taskItems.get(indx).getItemY() + "] - scroll[" + scroll + "], widthComposite[" + widthComposite + "], ITEM_HEIGHT[" + ITEM_HEIGHT + "] + 1, false);");
	}

	private void drawTimeEstimation(int indx) {
		tCompGC.setFont(loggedTimeFont);
		int startY = 0;
		int startX = 0;
		if (tasks.get(indx).getParentKey() != null) {
			startY = SUB_ITEM_HEIGHT;
			startX = SUB_ITEM_OFFSET_X;
		}
		String estTime = "Estimated: " + Task.millisecondsToDHM(tasks.get(indx).getTimeEstimated());
		String spTime = "Actual: " + (tasks.get(indx).getFormatedTimeSpent() != null ? tasks.get(indx).getFormatedTimeSpent() : Task.millisecondsToDHM(tasks.get(indx).getTimeSpent()));
		int estWidth = 0;
		int spWidth = 0;
		int x = 0;
		for (int i = 0; i < estTime.length(); i++) {
			x += tCompGC.getCharWidth(estTime.charAt(i));
		}
		estWidth = x + 9;
		x = 0;
		for (int i = 0; i < spTime.length(); i++) {
			x += tCompGC.getCharWidth(spTime.charAt(i));
		}
		spWidth = x + 9;

		tCompGC.setBackground(ColorSchemes.taskItemBackgroundColor);
		tCompGC.fillRectangle(linkX + startX + taskItems.get(indx).getLinkWidth() + 10, (taskItems.get(indx).getLinkY() + startY + 2) - scroll, estWidth, linkHeight);
		tCompGC.fillRectangle(linkX + startX + taskItems.get(indx).getLinkWidth() + 20 + estWidth, (taskItems.get(indx).getLinkY() + startY + 2) - scroll, spWidth + 20, linkHeight);
		tCompGC.fillRectangle(linkX + startX + taskItems.get(indx).getLinkWidth() + 10, (taskItems.get(indx).getLinkY() + startY + standardTextHeight) - scroll, spWidth + 20, linkHeight);
		tCompGC.setForeground(ColorSchemes.taskStartTextColor);
		tCompGC.drawText(estTime, linkX + startX + taskItems.get(indx).getLinkWidth() + 10, (taskItems.get(indx).getLinkY() + startY) - scroll, true);
		tCompGC.drawText("Current Spent: " + TimeUnit.MILLISECONDS.toMinutes(tasks.get(indx).getCurrentTimeSpent()) + "m", linkX + startX + taskItems.get(indx).getLinkWidth() + 20 + estWidth, (taskItems.get(indx).getLinkY() + startY) - scroll, true);
		if ((tasks.get(indx).getTimeSpent() - tasks.get(indx).getTimeEstimated()) > 0) {
			if ((tasks.get(indx).getTimeSpent() - tasks.get(indx).getTimeEstimated()) > tasks.get(indx).getTimeEstimated()) {
				tCompGC.setForeground(ColorSchemes.time2OverTextColor);
			} else {
				tCompGC.setForeground(ColorSchemes.timeOverTextColor);
			}
		}
		tCompGC.drawText(spTime, linkX + startX + taskItems.get(indx).getLinkWidth() + 10, (taskItems.get(indx).getLinkY() + startY + standardTextHeight) - 3 - scroll, true);
		redraw(linkX + taskItems.get(indx).getLinkWidth() + 10, taskItems.get(indx).getLinkY() - scroll, btnTimerX - LOG_WORK_OFFSET - LOG_WORK_WIDTH - taskItems.get(indx).getLinkWidth(), linkHeight + standardTextHeight, false);
	}

	protected void drawLink(int indx) {
		int startY = 0;
		int startX = 0;
		if (tasks.get(indx).getParentKey() != null) {
			startY = SUB_ITEM_HEIGHT;
			startX = SUB_ITEM_OFFSET_X;
		}
		tCompGC.setForeground(ColorSchemes.taskStartTextColor);
		Font prevFont = tCompGC.getFont();
		tCompGC.setFont(linkFont);
		tCompGC.drawText(tasks.get(indx).getKey(), linkX + startX, (taskItems.get(indx).getLinkY() + startY) - scroll, true);
		tCompGC.setFont(prevFont);
		tCompGC.drawLine(linkX + startX, (taskItems.get(indx).getLinkY() + startY + linkHeight) - scroll, linkX + startX + taskItems.get(indx).getLinkWidth(), (taskItems.get(indx).getLinkY() + startY + linkHeight) - scroll);
		redraw(linkX, taskItems.get(indx).getLinkY() - scroll, taskItems.get(indx).getLinkWidth(), linkHeight, false);
	}

	protected void drawTxtLogWork(int indx) {
		if (tasks.get(indx).isRunning()) {
			return;
		}
		tCompGC.setBackground(ColorSchemes.taskItemBackgroundColor);
		tCompGC.fillRectangle(btnTimerX - LOG_WORK_OFFSET - LOG_WORK_WIDTH, taskItems.get(indx).getBtnTimerY() - scroll, LOG_WORK_WIDTH, BTN_TIMER_HEIGHT);
		tCompGC.setForeground(ColorSchemes.taskBorderColor);
		tCompGC.drawRectangle(btnTimerX - LOG_WORK_OFFSET - LOG_WORK_WIDTH, taskItems.get(indx).getBtnTimerY() - scroll, LOG_WORK_WIDTH, BTN_TIMER_HEIGHT);
		tCompGC.setForeground(ColorSchemes.taskGradientColor);
		tCompGC.setBackground(ColorSchemes.taskWorkLogTextBackgroundColor);
		tCompGC.fillRectangle(txtLogWorkX, taskItems.get(indx).getTxtLogworkY() - scroll, LOG_WORK_TEXT_BOX_WIDTH, standardTextHeight);
		tCompGC.drawRectangle(txtLogWorkX, taskItems.get(indx).getTxtLogworkY() - scroll, LOG_WORK_TEXT_BOX_WIDTH, standardTextHeight);
		tCompGC.setForeground(ColorSchemes.taskStartTextColor);
		tCompGC.drawText(tasks.get(indx).getLogWorkH(), txtLogWorkX + LOG_WORK_PADDING, taskItems.get(indx).getTxtLogworkY() - scroll, true);
		tCompGC.drawText("h", txtLogWorkX + LOG_WORK_PADDING + LOG_WORK_TEXT_BOX_WIDTH, taskItems.get(indx).getTxtLogworkY() - scroll, true);
		redraw(txtLogWorkX, taskItems.get(indx).getTxtLogworkY() - scroll, LOG_WORK_TEXT_BOX_WIDTH, standardTextHeight, false);
	}

	protected void drawBtnLogWork(int indx) {
		if (tasks.get(indx).isRunning()) {
			return;
		}
		Color mainColor = ColorSchemes.taskColor;
		Color gradColor = ColorSchemes.taskGradientColor;
		if (isLogWorkChanged && (indx == indxSelectedItem)) {
			mainColor = ColorSchemes.taskStartColor;
			gradColor = ColorSchemes.taskStartGradientColor;
		}
		tCompGC.setBackground(mainColor);
		tCompGC.fillRoundRectangle(btnLogWorkX, taskItems.get(indx).getBtnLogworkY() - scroll, BTN_LOG_WORK_WIDTH, BTN_LOG_WORK_HEIGHT, WIDTH_ARC, WIDTH_ARC);
		if (isLogWorkChanged && (indx == indxSelectedItem)) {
			tCompGC.setForeground(gradColor);
			if (!isMouseStatus(MOUSE_OVER_LOG_WORK_BTN) && !isMouseStatus(MOUSE_DOWN_LOG_WORK_BTN)) {
				tCompGC.fillGradientRectangle(btnLogWorkX + GRADIENT_OFFSET, (taskItems.get(indx).getBtnLogworkY() + (BTN_LOG_WORK_HEIGHT / 2)) - scroll, BTN_LOG_WORK_WIDTH - GRADIENT_OFFSET, (BTN_LOG_WORK_HEIGHT / 2) - GRADIENT_OFFSET, true);
				tCompGC.setBackground(gradColor);
				tCompGC.setForeground(mainColor);
				tCompGC.fillGradientRectangle(btnLogWorkX + GRADIENT_OFFSET, (taskItems.get(indx).getBtnLogworkY() + GRADIENT_OFFSET) - scroll, BTN_LOG_WORK_WIDTH - GRADIENT_OFFSET, (BTN_LOG_WORK_HEIGHT / 2) - GRADIENT_OFFSET, true);
			} else {
				if (isMouseStatus(MOUSE_DOWN_LOG_WORK_BTN)) {
					tCompGC.setBackground(gradColor);
					tCompGC.setForeground(mainColor);
				}
				tCompGC.fillGradientRectangle(btnLogWorkX + GRADIENT_OFFSET, (taskItems.get(indx).getBtnLogworkY() + GRADIENT_OFFSET) - scroll, BTN_LOG_WORK_WIDTH - GRADIENT_OFFSET, BTN_LOG_WORK_HEIGHT - GRADIENT_OFFSET, true);
			}
		}
		tCompGC.setForeground(gradColor);
		tCompGC.drawRoundRectangle(btnLogWorkX, taskItems.get(indx).getBtnLogworkY() - scroll, BTN_LOG_WORK_WIDTH, BTN_LOG_WORK_HEIGHT, WIDTH_ARC, WIDTH_ARC);
		tCompGC.drawRoundRectangle(btnLogWorkX + 1, (taskItems.get(indx).getBtnLogworkY() + 1) - scroll, BTN_LOG_WORK_WIDTH - 2, BTN_LOG_WORK_HEIGHT - 2, WIDTH_ARC, WIDTH_ARC);
		Font prevFont = tCompGC.getFont();
		tCompGC.setFont(logWorkFont);
		if (isLogWorkChanged && (indx == indxSelectedItem)) {
			tCompGC.setForeground(ColorSchemes.taskStartTextColor);
		}
		tCompGC.drawText("LogWork", btnLogWorkX + 3, (taskItems.get(indx).getBtnLogworkY() + 5) - scroll, true);
		tCompGC.setFont(prevFont);
		redraw(btnLogWorkX, taskItems.get(indx).getBtnLogworkY() - scroll, BTN_LOG_WORK_WIDTH, BTN_LOG_WORK_HEIGHT, false);
	}

	protected void drawBtnStart(int indx) {
		Color mainColor = ColorSchemes.taskStartColor;
		Color gradColor = ColorSchemes.taskStartGradientColor;
		if (tasks.get(indx).isRunning()) {
			mainColor = ColorSchemes.taskStopColor;
			gradColor = ColorSchemes.taskStopGradientColor;
		}
		tCompGC.setBackground(mainColor);
		tCompGC.setForeground(gradColor);
		tCompGC.fillRoundRectangle(btnTimerX, taskItems.get(indx).getBtnTimerY() - scroll, BTN_TIMER_WIDTH, BTN_TIMER_HEIGHT, WIDTH_ARC, WIDTH_ARC);

		if ((!isMouseStatus(MOUSE_OVER_TIMER_BTN) && !isMouseStatus(MOUSE_DOWN_TIMER_BTN)) || (indx != indxUnderCursor/* && !isMouseBtnOver && !isMouseBtnPressed */)) {
			tCompGC.fillGradientRectangle(btnTimerX + GRADIENT_OFFSET, (taskItems.get(indx).getBtnTimerY() + (BTN_TIMER_HEIGHT / 2)) - scroll, BTN_TIMER_WIDTH - GRADIENT_OFFSET, (BTN_TIMER_HEIGHT / 2) - GRADIENT_OFFSET, true);
			tCompGC.setBackground(gradColor);
			tCompGC.setForeground(mainColor);
			tCompGC.fillGradientRectangle(btnTimerX + GRADIENT_OFFSET, (taskItems.get(indx).getBtnTimerY() + GRADIENT_OFFSET) - scroll, BTN_TIMER_WIDTH - GRADIENT_OFFSET, (BTN_TIMER_HEIGHT / 2) - GRADIENT_OFFSET, true);
		} else {
			if (isMouseStatus(MOUSE_DOWN_TIMER_BTN)) {
				tCompGC.setBackground(gradColor);
				tCompGC.setForeground(mainColor);
			}
			tCompGC.fillGradientRectangle(btnTimerX + GRADIENT_OFFSET, (taskItems.get(indx).getBtnTimerY() + GRADIENT_OFFSET) - scroll, BTN_TIMER_WIDTH - GRADIENT_OFFSET, BTN_TIMER_HEIGHT - GRADIENT_OFFSET, true);
		}
		tCompGC.setForeground(gradColor);
		tCompGC.drawRoundRectangle(btnTimerX, taskItems.get(indx).getBtnTimerY() - scroll, BTN_TIMER_WIDTH, BTN_TIMER_HEIGHT, WIDTH_ARC, WIDTH_ARC);
		tCompGC.drawRoundRectangle(btnTimerX + 1, (taskItems.get(indx).getBtnTimerY() + 1) - scroll, BTN_TIMER_WIDTH - 2, BTN_TIMER_HEIGHT - 2, WIDTH_ARC, WIDTH_ARC);
		tCompGC.setForeground(ColorSchemes.taskStartTextColor);
		Font prevFont = tCompGC.getFont();
		if (tasks.get(indx).isRunning()) {
			tCompGC.setForeground(ColorSchemes.taskStopTextColor);
			tCompGC.setFont(stopFont);
			tCompGC.drawText(STOP, (btnTimerX + stopTextOffsetX) - 2, (taskItems.get(indx).getBtnTimerY() + ((BTN_TIMER_HEIGHT) / 2)) - scroll - 4, true);
			tCompGC.setFont(timeFont);
			tCompGC.drawText(/* TIME_FORMAT */TimeUnit.MILLISECONDS.toHours(tasks.get(indx).getCurrentTimeSpent()) + ":" + formatter.format(new Date(tasks.get(indx).getCurrentTimeSpent())), btnTimerX + timeTextOffsetX, (taskItems.get(indx).getBtnTimerY() + (GRADIENT_OFFSET * 2)) - scroll, true);
		} else if (tasks.get(indx).getCurrentTimeSpent() == 0) {
			tCompGC.setFont(startFont);
			tCompGC.drawText(START, btnTimerX + startTextOffsetX, (taskItems.get(indx).getBtnTimerY() + ((BTN_TIMER_HEIGHT - standardTextHeight) / 2)) - scroll, true);
			// subButton here
		} else {
			tCompGC.setFont(resumeFont);
			tCompGC.drawText(RESUME, (btnTimerX + stopTextOffsetX) - 2, (taskItems.get(indx).getBtnTimerY() + ((BTN_TIMER_HEIGHT) / 2)) - scroll - 4, true);
			tCompGC.setFont(timeFont);
			tCompGC.drawText(/* TIME_FORMAT */TimeUnit.MILLISECONDS.toHours(tasks.get(indx).getCurrentTimeSpent()) + ":" + formatter.format(new Date(tasks.get(indx).getCurrentTimeSpent())), btnTimerX + timeTextOffsetX, (taskItems.get(indx).getBtnTimerY() + (GRADIENT_OFFSET * 2)) - scroll, true);
		}
		tCompGC.setFont(prevFont);
		/*** Start Draw miniButton ***/
		String textMiniBtn = COMMENT_MINI_BTN_TEXT;
		if (tasks.get(indx).isRunning()) {
			textMiniBtn = PAUSE_MINI_BTN_TEXT;
			mainColor = ColorSchemes.taskMiniBtnPauseColor;
			gradColor = ColorSchemes.taskMiniBtnPauseGradientColor;
		} else {
			mainColor = ColorSchemes.taskMiniBtnTColor;
			gradColor = ColorSchemes.taskMiniBtnTGradientColor;
		}
		int widthMiniBtn = (BTN_TIMER_WIDTH * 10) / 25;
		int heightMiniBtn = (BTN_TIMER_HEIGHT * 10) / 25;
		int xMiniBtn = BTN_TIMER_WIDTH - widthMiniBtn;
		int yMiniBtn = BTN_TIMER_HEIGHT - heightMiniBtn;
		boolean vertical = true;
		if ((!isMouseStatus(MOUSE_OVER_MINI_BTN) && !isMouseStatus(MOUSE_DOWN_MINI_BTN)) || (indx != indxUnderCursor)) {
			tCompGC.setBackground(gradColor);
			tCompGC.setForeground(mainColor);
		} else {
			tCompGC.setBackground(mainColor);
			tCompGC.setForeground(gradColor);
			if (isMouseStatus(MOUSE_OVER_MINI_BTN)) {
				vertical = false;
			}
		}
		tCompGC.fillGradientRectangle(btnTimerX + xMiniBtn + 1, (taskItems.get(indx).getBtnTimerY() + yMiniBtn + 1) - scroll, widthMiniBtn - 3, heightMiniBtn - 3, vertical);
		// tCompGC.fillRoundRectangle(btnTimerX + xMiniBtn, taskItems.get(indx).getBtnTimerY() + yMiniBtn - scroll, widthMiniBtn - 2, heightMiniBtn - 2, WIDTH_ARC, WIDTH_ARC);
		tCompGC.setForeground(ColorSchemes.taskMiniBtnBorderColor);
		tCompGC.drawRoundRectangle(btnTimerX + xMiniBtn, (taskItems.get(indx).getBtnTimerY() + yMiniBtn) - scroll, widthMiniBtn - 2, heightMiniBtn - 2, WIDTH_ARC, WIDTH_ARC);
		if (tasks.get(indx).isRunning()) {
			tCompGC.setForeground(ColorSchemes.taskStopTextColor);
		}
		tCompGC.setFont(timeFont);
		tCompGC.drawText(textMiniBtn, btnTimerX + xMiniBtn + (widthMiniBtn / 3), (taskItems.get(indx).getBtnTimerY() + yMiniBtn + 2) - scroll, true);
		/*** End Draw miniButton ***/
		redraw(btnTimerX, taskItems.get(indx).getBtnTimerY() - scroll, btnTimerX + BTN_TIMER_WIDTH, taskItems.get(indx).getBtnTimerY() + BTN_TIMER_HEIGHT, false);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void dispose() {
		CURSOR_SIZEALL.dispose();
		CURSOR_ARROW.dispose();
		super.dispose();
	}

	private int getIndx(int coordY) {
		int res = -1;
		for (int i = 0; i < cells.length; i++) {
			if ((coordY - cells[i]) <= ITEM_HEIGHT) {
				res = i;
				if (i != indxUnderCursor) {
					logger.debug(DEBUG_INFO_INDX_UNDER_MOUSE_CURSOR + i);
				}
				break;
			}
		}
		return res;
	}

	private void shiftTask(int indx, boolean isDown) {
		Task t = tasks.get(indx);
		TaskItem ti = taskItems.get(indx);
		if (isDown && ((indx + 1) < tasks.size())) {
			tasks.set(indx, tasks.get(indx + 1));
			tasks.set(indx + 1, t);
			taskItems.set(indx, taskItems.get(indx + 1));
			taskItems.set(indx + 1, ti);
		} else if ((indx - 1) >= 0) {
			tasks.set(indx, tasks.get(indx - 1));
			tasks.set(indx - 1, t);
			taskItems.set(indx, taskItems.get(indx - 1));
			taskItems.set(indx - 1, ti);
		}
	}

	public void addTask(Task task) {
		if (indxRunningItem > -1) {
			indxRunningItem++;
		}
		task.setAdded(System.currentTimeMillis());
		tasks.add(0, task);
		TaskItem ti = new TaskItem();
		Font prevFont = tCompGC.getFont();
		tCompGC.setFont(linkFont);
		if (tasks.get(0) != null) {
			int x = 0;
			for (int z = 0; z < tasks.get(0).getKey().length(); z++) {
				x += tCompGC.getCharWidth(tasks.get(0).getKey().charAt(z));
			}
			ti.setLinkWidth(x);
		}
		tCompGC.setFont(prevFont);
		taskItems.add(0, ti);
		for (int i = 0; i < tasks.size(); i++) {
			resizeTaskItem(i);
			drawItem(i);
		}
		slider.setMaximum((ITEM_HEIGHT * tasks.size()) + ITEM_OFFSET);

		int gridCound = MAX_COUNT_TASK >= tasks.size() ? MAX_COUNT_TASK : tasks.size();
		cells = new int[gridCound];
		for (int i = 0; i < gridCound; i++) {
			cells[i] = (ITEM_HEIGHT * i) + ITEM_OFFSET;
		}
		if ((tasks.size() > MAX_COUNT_TASK) && !slider.isEnabled()) {
			slider.setEnabled(true);
		}
	}

	public void onScroll() {
		scroll = slider.getSelection();
		logger.debug("---- Start Painting Block -----");
		for (int i = 0; i < tasks.size(); i++) {
			eraseItem(i);
			drawItem(i);
		}
		logger.debug("---- End Painting Block -----");
		if (text.isVisible()) {
			text.setLocation(text.getLocation().x, (taskItems.get(indxSelectedItem).getTxtLogworkY() + 1) - scroll);
		}
	}

	public void update(List<Task> pTasks) {
		text.setVisible(false);
		indxSelectedItem = -1;
		tCompGC.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		tCompGC.fillRectangle(0, 0, widthComposite, heightComposite + scroll);
		redraw(0, 0, widthComposite, heightComposite + scroll, false);
		logger.debug("redrawTasksComposite");
		tasks = pTasks;
		if (tasks == null) {
			tasks = new ArrayList<Task>();
		}
		setVisible(tasks.size() > 0);
		taskItems = new ArrayList<TaskItem>();
		Font prevFont = tCompGC.getFont();
		descFont = new Font(display, "Arial", 11, SWT.ITALIC);
		linkFont = new Font(display, "Arial", 10, SWT.BOLD);
		linkParentFont = new Font(display, "Arial", 8, SWT.BOLD);
		tCompGC.setFont(linkParentFont);
		linkParentTextHeight = tCompGC.getFontMetrics().getHeight();
		tCompGC.setFont(linkFont);
		linkTextHeight = tCompGC.getFontMetrics().getHeight();
		int x = 0;
		for (int i = 0; i < tasks.size(); i++) {
			TaskItem ti = new TaskItem();
			if (tasks.get(i) != null) {
				x = 0;
				for (int z = 0; z < tasks.get(i).getKey().length(); z++) {
					x += tCompGC.getCharWidth(tasks.get(i).getKey().charAt(z));
				}
				ti.setLinkWidth(x);
			}
			taskItems.add(ti);
		}
		tCompGC.setFont(prevFont);
		int gridCound = MAX_COUNT_TASK >= tasks.size() ? MAX_COUNT_TASK : tasks.size();
		cells = new int[gridCound];
		for (int i = 0; i < gridCound; i++) {
			cells[i] = (ITEM_HEIGHT * i) + ITEM_OFFSET;
		}
		slider.setEnabled(tasks.size() > MAX_COUNT_TASK);
		// slider.setThumb(MAX_COUNT_TASK * ITEM_HEIGHT + ITEM_OFFSET);
		slider.setMaximum((tasks.size() * ITEM_HEIGHT) + ITEM_OFFSET);
		slider.setThumb(heightComposite);
		slider.setPageIncrement(ITEM_HEIGHT);
		logger.info("slider.getThumb() = " + slider.getThumb() + ", slider.getMaximum() = " + slider.getMaximum() + ", heightComposite = " + heightComposite);
		scroll = 0;
		slider.setSelection(scroll);

		logger.debug("---- Start Painting Block -----");
		for (int i = 0; i < tasks.size(); i++) {
			resizeTaskItem(i);
			drawItem(i);
		}
		logger.debug("---- End Painting Block -----");

		if (SOAPSession.getInstance().getClosedIssues() != null) {
			for (int i = 0; i < SOAPSession.getInstance().getClosedIssues().size(); i++) {
				ClosedIssueDialog dialog = new ClosedIssueDialog(getShell(), SOAPSession.getInstance().getClosedIssues().get(i));
				dialog.open();
			}
			SOAPSession.getInstance().setClosedIssues(null);
		}
	}

	protected void updateWorklog(final Task task) {
		try {
			logger.debug("Time is logged: " + Task.millisecondsToDHM(task.getCurrentTimeSpent()));
			SOAPSession.getInstance().updateWorklog(task);
			// drawTimeEstimation(tasks.indexOf(task));
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					drawItem(task);
				}
			});
		} catch (Exception e) {
			logger.error(e.getMessage());
			LogManager.logStack(e);
		}
	}

	public static void updateWorklog() {
		if (getActiveTask() == null) {
			return;
		}
		getActiveTask().setRunning(false);
	}

	public static void cancelTimer() {
		stopped = true;
	}

	public static Task getActiveTask() {
		return activeTask;
	}

	private void setActiveTask(Task activeTask) {
		TasksComposite.activeTask = activeTask;
		Application.setStartedTask(activeTask == null ? false : true);
	}

	/*** New method doesn't block UI ***/
	private void startWork(final Task task) {
		// final String key = tasks.get(indxUnderCursor).getKey();
		Job job = new Job("Logging Work for " + task.getKey()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				while (!stopped && task.getKey().equalsIgnoreCase(getActiveTask().getKey())) {
					// TODO Need to avoid search object in collection every second!!!
					if (!tasks.contains(getActiveTask())) {
						break;
					}
					long nowTime = System.currentTimeMillis();
					task.setCurrentTimeSpent(nowTime - task.getStartedTimer());
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							drawBtnStart(indxRunningItem);
						}
					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						LogManager.logStack(e);
					}
					// TODO wtf?
					/*
					 * if (offset[0] != null && indxRunningItem != indxSelectedItem) { Display.getDefault().asyncExec(new Runnable() { public void run() { drawItem(indxSelectedItem); } }); }
					 */
				}
				// TODO WTF? setUserName every second?
				dailyTotalCounter.setUserName(taskView.getUserName());
				prepareAndLogTime(task);
				if (task.getKey().equalsIgnoreCase(getActiveTask().getKey())) {
					setActiveTask(null);
				}
				return Status.OK_STATUS;
			}

		};
		// job.setUser(true);
		job.schedule();
	}

	private void prepareAndLogTime(final Task task) {
		if (!task.isRunning()) {
			dailyTotalCounter.add(task.getCurrentTimeSpent());
			drawDailyTime();
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					drawItem(tasks.indexOf(task));
				}
			});
			if (!task.isSuspended() && (TimeUnit.MILLISECONDS.toMinutes(task.getCurrentTimeSpent()) >= MINIMUM_SPENT_TIME_MINUTES)) {
				updateWorklog(task);
			}
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					// drawItem(task);
					if ((getActiveTask() == null) || task.getKey().equalsIgnoreCase(getActiveTask().getKey())) {
						indxRunningItem = -1;
					}
				}
			});
		}
	}

	private void drawDailyTime() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				taskView.setDeilyTime(dailyTotalCounter.getString());
			}
		});
	}

	private void showErrorMessage(String title, String msg) {
		showMessage(title, msg, SWT.APPLICATION_MODAL | SWT.OK | SWT.ICON_ERROR);
	}

	private void showInfoMessage(String title, String msg) {
		showMessage(title, msg, SWT.APPLICATION_MODAL | SWT.OK | SWT.ICON_WARNING);
	}

	private void showMessage(String title, String msg, int style) {
		MessageBox messageBox = new MessageBox(display.getActiveShell(), style);
		messageBox.setText(title);
		messageBox.setMessage(msg);
		messageBox.open();
	}

	private boolean isOtherTaskStarted() {
		return (getActiveTask() != null) && !getActiveTask().getKey().equalsIgnoreCase(tasks.get(indxUnderCursor).getKey());
	}

}