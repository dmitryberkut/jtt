package com.dbfs.jtt.swt;

import org.eclipse.swt.graphics.Rectangle;

public class TaskItem {
    private Rectangle btnTimer;
    private Rectangle btnLogwork;
    private Rectangle txtLogwork;
    private Rectangle brdLogwork;
    private Rectangle link;
    private int itemY;
    private int btnTimerY;
    private int btnLogworkY;
    private int txtLogworkY;
    private int brdLogworkY;
    private int linkY;
    private int linkParentY;
    private int linkWidth;
    private int linkParentWidth;
    
    public TaskItem (Rectangle btnT, Rectangle btnL, Rectangle txtL, Rectangle brdL, Rectangle link) {
        setBtnTimer(btnT);
        setBtnLogwork(btnL);
        setTxtLogwork(txtL);
        setBrdLogwork(brdL);
        setLink(link);
    }
    public TaskItem() {
        
    }
    public Rectangle getBtnTimer() {
        return btnTimer;
    }
    public void setBtnTimer(Rectangle btnTimer) {
        this.btnTimer = btnTimer;
    }
    public Rectangle getBtnLogwork() {
        return btnLogwork;
    }
    public void setBtnLogwork(Rectangle btnLogwork) {
        this.btnLogwork = btnLogwork;
    }
    public Rectangle getTxtLogwork() {
        return txtLogwork;
    }
    public void setTxtLogwork(Rectangle txtLogwork) {
        this.txtLogwork = txtLogwork;
    }
    public Rectangle getBrdLogwork() {
        return brdLogwork;
    }
    public void setBrdLogwork(Rectangle brdLogwork) {
        this.brdLogwork = brdLogwork;
    }
    public Rectangle getLink() {
        return link;
    }
    public void setLink(Rectangle link) {
        this.link = link;
    }
    public int getItemY() {
        return itemY;
    }
    public void setItemY(int itemY) {
        this.itemY = itemY;
    }
    public int getBtnTimerY() {
        return btnTimerY;
    }
    public void setBtnTimerY(int btnTimerY) {
        this.btnTimerY = btnTimerY;
    }
    public int getBtnLogworkY() {
        return btnLogworkY;
    }
    public void setBtnLogworkY(int btnLogworkY) {
        this.btnLogworkY = btnLogworkY;
    }
    public int getTxtLogworkY() {
        return txtLogworkY;
    }
    public void setTxtLogworkY(int txtLogworkY) {
        this.txtLogworkY = txtLogworkY;
    }
    public int getBrdLogworkY() {
        return brdLogworkY;
    }
    public void setBrdLogworkY(int brdLogworkY) {
        this.brdLogworkY = brdLogworkY;
    }
    public int getLinkY() {
        return linkY;
    }
    public void setLinkY(int linkY) {
        this.linkY = linkY;
    }
    public int getParentLinkY() {
        return linkParentY;
    }
    public void setParentLinkY(int linkParentY) {
        this.linkParentY = linkParentY;
    }
    public int getLinkWidth() {
        return linkWidth;
    }
    public void setLinkWidth(int linkWidth) {
        this.linkWidth = linkWidth;
    }
    public int getLinkParentWidth() {
        return linkParentWidth;
    }
    public void setLinkParentWidth(int linkParentWidth) {
        this.linkParentWidth = linkParentWidth;
    }
}
