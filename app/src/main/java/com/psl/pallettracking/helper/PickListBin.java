package com.psl.pallettracking.helper;

import java.util.List;

public class PickListBin {
    int stockBinId;
    String label;
    String binName;
    String batchMonth;
    String batchDateTime;
    String bayName;
    String shelf;
    String binNumber;

    public int getStockBinId() {
        return stockBinId;
    }

    public void setStockBinId(int stockBinId) {
        this.stockBinId = stockBinId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getBinName() {
        return binName;
    }

    public void setBinName(String binName) {
        this.binName = binName;
    }

    public String getBatchMonth() {
        return batchMonth;
    }

    public void setBatchMonth(String batchMonth) {
        this.batchMonth = batchMonth;
    }

    public String getBatchDateTime() {
        return batchDateTime;
    }

    public void setBatchDateTime(String batchDateTime) {
        this.batchDateTime = batchDateTime;
    }

    public String getBayName() {
        return bayName;
    }

    public void setBayName(String bayName) {
        this.bayName = bayName;
    }

    public String getShelf() {
        return shelf;
    }

    public void setShelf(String shelf) {
        this.shelf = shelf;
    }

    public String getBinNumber() {
        return binNumber;
    }

    public void setBinNumber(String binNumber) {
        this.binNumber = binNumber;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    double qty;
}
