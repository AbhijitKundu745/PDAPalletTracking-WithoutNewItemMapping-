package com.psl.pallettracking.viewHolder;

import com.psl.pallettracking.helper.DefaultConstants;

public class ItemDetailsList {
    private String serialNo, ItemDesc, SkuCode, PickedQty, ScannedQty, RemainingQty;

    public String getSerialNo() {
        return (serialNo != null) ? serialNo : "defaultSerialNo";
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }
    public String getItemDesc() {
        return (ItemDesc != null) ? ItemDesc : DefaultConstants.DEFAULT_SKU_CODE;
    }

    public void setItemDesc(String ItemDesc) {
        this.ItemDesc = ItemDesc;
    }
    public String getSkuCode() {
        return (SkuCode != null) ? SkuCode : DefaultConstants.DEFAULT_SKU_CODE;
    }

    public void setSkuCode(String SkuCode) {
        this.SkuCode = SkuCode;
    }

    public String getPickedQty() {
        return (PickedQty != null) ? PickedQty : "0";
    }
    public void setPickedQty(String PickedQty) {
        this.PickedQty = PickedQty;
    }
    public String getScannedQty() {
        return (ScannedQty != null) ? ScannedQty : "0";
    }
    public void setScannedQty(String ScannedQty) {
        this.ScannedQty = ScannedQty;
    }
    public String getRemainingQty() {
        return (RemainingQty != null) ? RemainingQty : PickedQty;
    }
    public void setRemainingQty(String RemainingQty) {
        this.RemainingQty = RemainingQty;
    }
}