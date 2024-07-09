package com.psl.pallettracking.adapters;

public class BinPartialPalletMappingCreationProcessModel {
    String binNumber,binDescription, batchId, itemID, itemName;
    int pickedQty, originalPickedQty;

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

    public int getPickedQty() {
        return pickedQty;
    }

    public void setPickedQty(int pickedQty) {
        this.pickedQty = pickedQty;
    }
    public int getOriginalPickedQty() {
        return originalPickedQty;
    }

    public void setOriginalPickedQty(int originalPickedQty) {
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
}
