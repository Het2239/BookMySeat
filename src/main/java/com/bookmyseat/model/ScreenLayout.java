package com.bookmyseat.model;

import com.bookmyseat.model.enums.SeatType;

public class ScreenLayout {
    private int layoutId;
    private int screenId;
    private char rowLabel;
    private int colNum;
    private SeatType seatType;
    private boolean isAisle;
    private boolean isActive;

    public ScreenLayout() {}

    public ScreenLayout(int layoutId, int screenId, char rowLabel, int colNum,
                        SeatType seatType, boolean isAisle, boolean isActive) {
        this.layoutId = layoutId;
        this.screenId = screenId;
        this.rowLabel = rowLabel;
        this.colNum = colNum;
        this.seatType = seatType;
        this.isAisle = isAisle;
        this.isActive = isActive;
    }

    /** Convenience: e.g. "A3", "B10" */
    public String getSeatCode() {
        return "" + rowLabel + colNum;
    }

    public int getLayoutId()            { return layoutId; }
    public void setLayoutId(int v)      { this.layoutId = v; }
    public int getScreenId()            { return screenId; }
    public void setScreenId(int v)      { this.screenId = v; }
    public char getRowLabel()           { return rowLabel; }
    public void setRowLabel(char v)     { this.rowLabel = v; }
    public int getColNum()              { return colNum; }
    public void setColNum(int v)        { this.colNum = v; }
    public SeatType getSeatType()       { return seatType; }
    public void setSeatType(SeatType v) { this.seatType = v; }
    public boolean isAisle()            { return isAisle; }
    public void setAisle(boolean v)     { this.isAisle = v; }
    public boolean isActive()           { return isActive; }
    public void setActive(boolean v)    { this.isActive = v; }

    @Override
    public String toString() {
        return getSeatCode() + "(" + seatType + ")";
    }
}
