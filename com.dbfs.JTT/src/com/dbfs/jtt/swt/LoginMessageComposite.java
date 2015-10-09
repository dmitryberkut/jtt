package com.dbfs.jtt.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

import com.dbfs.jtt.resources.ColorSchemes;

public class LoginMessageComposite extends Composite {
	private final static int LEFT_OFFSET = 2;
	private final static int RIGHT_OFFSET = 2;
	private final static int TOP_OFFSET = 2;
	private final static int BOTTOM_OFFSET = 2;
	private final static int TEXT_INDENT = 5;
	private String typeMsg = "Error: ";
	private String msg = "";

	private GC gc;
	private Image imageMessage;
	private Display display;
	private int widthComposite;
	private int heightComposite;
	private Font typeFont;
	private Font msgFont;
	private int typeTextHeight;
	private int typeTextWidth;
	private int msgTextHeight;
	private int msgTextWidth;

	public void setMsg(String msg) {
		this.msg = msg;
		calculateMsgAttrs();
	}

	public void setTypeMsg(String type) {
		typeMsg = type;
	}

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public LoginMessageComposite(Composite parent, int style) {
		super(parent, style);
		Rectangle rect = new Rectangle(0, 0, 600, 400);
		display = parent.getParent().getDisplay();
		imageMessage = new Image(display, rect);
		gc = new GC(imageMessage);

		typeFont = new Font(display, "Arial", 10, SWT.BOLD);
		msgFont = new Font(display, "Arial", 11, SWT.NORMAL);
		gc.setFont(typeFont);
		typeTextHeight = gc.getFontMetrics().getHeight();
		int x = 0;
		for (int z = 0; z < typeMsg.length(); z++) {
			x += gc.getCharWidth(typeMsg.charAt(z));
		}
		typeTextWidth = x;

		calculateMsgAttrs();

		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.drawImage(imageMessage, e.x, e.y, e.width, e.height, e.x, e.y, e.width, e.height);
			}
		});
		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				if (((widthComposite == getClientArea().width) && (heightComposite == getClientArea().height)) || (getClientArea().width == 0) || (getClientArea().height == 0)) {
					return;
				}
				widthComposite = getClientArea().width;
				heightComposite = getClientArea().height;
				drawMessage();
			}
		});
		drawMessage();
	}

	protected void drawMessage() {
		gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		gc.fillRectangle(0, 0, imageMessage.getImageData().width, imageMessage.getImageData().height);
		gc.setBackground(ColorSchemes.loginMsgBackgroundColor);
		gc.fillRoundRectangle(LEFT_OFFSET, TOP_OFFSET, widthComposite - LEFT_OFFSET - RIGHT_OFFSET, heightComposite - TOP_OFFSET - BOTTOM_OFFSET, 5, 5);
		gc.setForeground(ColorSchemes.loginMsgForgraundColor);
		gc.drawRoundRectangle(LEFT_OFFSET, TOP_OFFSET, widthComposite - LEFT_OFFSET - RIGHT_OFFSET, heightComposite - TOP_OFFSET - BOTTOM_OFFSET, 5, 5);
		gc.setForeground(ColorSchemes.loginMsgTextColor);
		gc.setFont(typeFont);
		int x = (widthComposite - typeTextWidth - TEXT_INDENT - msgTextWidth) / 2;
		gc.drawText(typeMsg, x, (heightComposite - typeTextHeight) / 2);
		gc.setFont(msgFont);
		gc.drawText(msg, x + typeTextWidth + TEXT_INDENT, (heightComposite - msgTextHeight) / 2);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	private void calculateMsgAttrs() {
		gc.setFont(msgFont);
		msgTextHeight = gc.getFontMetrics().getHeight();
		int x = 0;
		for (int z = 0; z < msg.length(); z++) {
			x += gc.getCharWidth(msg.charAt(z));
		}
		msgTextWidth = x;
	}
}