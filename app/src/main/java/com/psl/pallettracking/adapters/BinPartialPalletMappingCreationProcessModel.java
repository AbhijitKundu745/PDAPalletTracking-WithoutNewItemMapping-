package com.psl.pallettracking.adapters;

public class BinPartialPalletMappingCreationProcessModel {
    String binNumber,binDescription, batchId, itemID, itemName;
    double pickedQty, originalPickedQty;
    Integer stockBinId;
    boolean isClickedEnable;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }


    public String getBinNumber() {
        return binNumber;
    }

    public void setBinNumber(String binNumber) {
        this.binNumber = binNumber;
    }

    public String getBinDescription() {
        return binDescription;
    }

    public void setBinDescription(String binDescription) {
        this.binDescription = binDescription;
    }

    public double getPickedQty() {
        return pickedQty;
    }

    public void setPickedQty(double pickedQty) {
        this.pickedQty = pickedQty;
    }
    public double getOriginalPickedQty() {
        return originalPickedQty;
    }

    public void setOriginalPickedQty(double originalPickedQty) {
        this.originalPickedQty = originalPickedQty;
    }
    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    public Integer getStockBinId() {
        return stockBinId;
    }

    public void setStockBinId(Integer stockBinId) {
        this.stockBinId = stockBinId;
    }
    public boolean getClickedEnable() {
        return isClickedEnable;
    }
    public void setClickedEnable(boolean isClickedEnable) {
        this.isClickedEnable = isClickedEnable;
    }
}
