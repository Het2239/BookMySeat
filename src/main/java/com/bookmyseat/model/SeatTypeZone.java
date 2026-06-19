package com.bookmyseat.model;

import com.bookmyseat.model.enums.SeatType;
import java.math.BigDecimal;

public class SeatTypeZone {
    private int zoneId;
    private int screenId;
    private SeatType zoneType;
    private char rowStart;
    private char rowEnd;
    private BigDecimal price;

    public SeatTypeZone() {}

    public SeatTypeZone(int zoneId, int screenId, SeatType zoneType,
                        char rowStart, char rowEnd, BigDecimal price) {
        this.zoneId = zoneId;
        this.screenId = screenId;
        this.zoneType = zoneType;
        this.rowStart = rowStart;
        this.rowEnd = rowEnd;
        this.price = price;
    }

    public int getZoneId()              { return zoneId; }
    public void setZoneId(int v)        { this.zoneId = v; }
    public int getScreenId()            { return screenId; }
    public void setScreenId(int v)      { this.screenId = v; }
    public SeatType getZoneType()       { return zoneType; }
    public void setZoneType(SeatType v) { this.zoneType = v; }
    public char getRowStart()           { return rowStart; }
    public void setRowStart(char v)     { this.rowStart = v; }
    public char getRowEnd()             { return rowEnd; }
    public void setRowEnd(char v)       { this.rowEnd = v; }
    public BigDecimal getPrice()        { return price; }
    public void setPrice(BigDecimal v)  { this.price = v; }

    @Override
    public String toString() {
        return zoneType + " (Rows " + rowStart + "-" + rowEnd + ") ₹" + price;
    }
}
