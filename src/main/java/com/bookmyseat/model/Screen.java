package com.bookmyseat.model;

public class Screen {
    private int screenId;
    private int theatreId;
    private String screenName;
    private int totalRows;
    private int totalCols;

    public Screen() {}

    public Screen(int screenId, int theatreId, String screenName, int totalRows, int totalCols) {
        this.screenId = screenId;
        this.theatreId = theatreId;
        this.screenName = screenName;
        this.totalRows = totalRows;
        this.totalCols = totalCols;
    }

    public int getScreenId()            { return screenId; }
    public void setScreenId(int v)      { this.screenId = v; }
    public int getTheatreId()           { return theatreId; }
    public void setTheatreId(int v)     { this.theatreId = v; }
    public String getScreenName()       { return screenName; }
    public void setScreenName(String v) { this.screenName = v; }
    public int getTotalRows()           { return totalRows; }
    public void setTotalRows(int v)     { this.totalRows = v; }
    public int getTotalCols()           { return totalCols; }
    public void setTotalCols(int v)     { this.totalCols = v; }

    @Override
    public String toString() {
        return "[" + screenId + "] " + screenName + " (" + totalRows + " rows × " + totalCols + " cols)";
    }
}
