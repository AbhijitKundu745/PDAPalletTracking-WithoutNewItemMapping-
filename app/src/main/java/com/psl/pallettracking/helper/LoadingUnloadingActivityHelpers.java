package com.psl.pallettracking.helper;


import static com.psl.pallettracking.ext.DataExt.getTagType;
import static com.psl.pallettracking.ext.DataExt.typeTemporaryStorage;

import android.util.Log;

import com.psl.pallettracking.bean.WorkOrderListItem;

import java.util.ArrayList;
import java.util.List;

public class LoadingUnloadingActivityHelpers {

    public static boolean isEpcPresentInWorkOrder(String epc, List<WorkOrderListItem> workOrderListItemList) {
        boolean isPresent = false;
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                if (
                        (epc.equals(item.getPalletName()) ||
                        epc.equals(item.getPalletTagId()) ||
                        epc.equals(item.getTempStorageTagId()) ||
                        epc.equals(item.getLoadingAreaTagId()) ||
                        epc.equals(item.getBinLocationTagId())) &&
                        item.getListItemStatus().equalsIgnoreCase("Pending")
                ) {
                    // If the string is found in any field, set isPresent to true and break the loop
                    isPresent = true;
                    break;
                }
            }
        }
        return isPresent;
    }


    public static boolean isWorkOrderItemStatusIsPending(String palletTagId, List<WorkOrderListItem> workOrderListItemList) {
        boolean isPending = true;
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                if (palletTagId.equals(item.getPalletTagId())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    if (!item.getListItemStatus().equalsIgnoreCase("Pending")) {
                        isPending = false;
                        break;
                    }
                }
            }
        }
        return isPending;
    }
    public static boolean isPalletTagIdPresentInWorkOrder(String palletTagId, List<WorkOrderListItem> workOrderListItemList) {
        boolean isPresent = false;
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                if (palletTagId.equals(item.getPalletTagId())) {
                    // If found, retrieve the loadingAreaTagId and break the loop

                    isPresent = true;
                        break;

                }
            }
        }
        return isPresent;
    }


    public static boolean isWorkOrderCompleted(List<WorkOrderListItem> workOrderListItemList) {
        boolean isWorkOrderCompleted = true;
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // If found, retrieve the loadingAreaTagId and break the loop
                if (item.getListItemStatus().equalsIgnoreCase("Pending")) {
                    isWorkOrderCompleted = false;
                    break;
                }
            }
        }
        return isWorkOrderCompleted;
    }


    public static String getPalletNameByPalletTagId(String palletTagId, List<WorkOrderListItem> workOrderListItemList) {
        String palletName = "-";
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                // Check if the current item's palletTagId matches the one we're looking for
                if (palletTagId.equals(item.getPalletTagId())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    palletName = item.getPalletName();
                    break;
                }
            }
        }
        return palletName;
    }
    public static String getPalletTagByPalletTagId(String palletTagId, List<WorkOrderListItem> workOrderListItemList) {
        String palletID = "-";
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                // Check if the current item's palletTagId matches the one we're looking for
                if (palletTagId.equals(item.getPalletTagId())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    palletID = item.getPalletTagId();
                    break;
                }
            }
        }
        return palletID;
    }

    public static String getU0LoadingAreaTagIdForPallet(String palletTagId, List<WorkOrderListItem> workOrderListItemList) {
        String loadingAreaTagId = "-";
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                // Check if the current item's palletTagId matches the one we're looking for
                if (palletTagId.equals(item.getPalletTagId())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    loadingAreaTagId = item.getLoadingAreaTagId();
                    break;
                }
            }
        }
        return loadingAreaTagId;
    }

    public static String getU1BinTagIdForPallet(String palletTagId, List<WorkOrderListItem> workOrderListItemList) {
        String binLocationTagId = "-";
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                // Check if the current item's palletTagId matches the one we're looking for
                if (palletTagId.equals(item.getPalletTagId())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    binLocationTagId = item.getBinLocationTagId();
                    break;
                }
            }
        }
        return binLocationTagId;
    }


    public static String getTagNameByOrderType(String palletTagId,String type, List<WorkOrderListItem> workOrderListItemList) {
        String binLocationTagId = "-";
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                // Check if the current item's palletTagId matches the one we're looking for
                if (palletTagId.equals(item.getPalletTagId())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    if(type.equalsIgnoreCase("U0")){
                        binLocationTagId = item.getLoadingAreaName();
                    }
                    if(type.equalsIgnoreCase("U1")){
                        binLocationTagId = item.getBinLocationName();
                    }
                    if(type.equalsIgnoreCase("L0")){
                        binLocationTagId = item.getTempStorageName();
                    }
                    if(type.equalsIgnoreCase("L1")){
                        binLocationTagId = item.getLoadingAreaName();
                    }
                    if(type.equalsIgnoreCase("I0")){
                        binLocationTagId = item.getBinLocationName();
                    }

                    break;
                }
            }
        }
        return binLocationTagId;
    }

    public static String getWorkOrderNumberByPalletTagId(String palletTagId, List<WorkOrderListItem> workOrderListItemList) {
        String binLocationTagId = "-";
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                // Check if the current item's palletTagId matches the one we're looking for
                if (palletTagId.equals(item.getPalletTagId())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    binLocationTagId = item.getWorkorderNumber();
                    break;
                }
            }
        }
        return binLocationTagId;
    }
    public static String getWorkOrderTypeByPalletTagId(String palletTagId, List<WorkOrderListItem> workOrderListItemList) {
        String binLocationTagId = "-";
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                // Check if the current item's palletTagId matches the one we're looking for
                if (palletTagId.equals(item.getPalletTagId())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    binLocationTagId = item.getWorkorderType();
                    break;
                }
            }
        }
        return binLocationTagId;
    }

    public static String getWorkOrderStatusByPalletTagId(String palletTagId, List<WorkOrderListItem> workOrderListItemList) {
        String binLocationTagId = "-";
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                // Check if the current item's palletTagId matches the one we're looking for
                if (palletTagId.equals(item.getPalletTagId())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    binLocationTagId = item.getWorkorderStatus();
                    break;
                }
            }
        }
        return binLocationTagId;
    }


    public static String getL0TempStorageTagIdForPallet(String palletTagId, List<WorkOrderListItem> workOrderListItemList) {
        String tempStorageTagId = "-";
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                // Check if the current item's palletTagId matches the one we're looking for
                if (palletTagId.equals(item.getPalletTagId())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    tempStorageTagId = item.getTempStorageTagId();
                    break;
                }
            }
        }
        return tempStorageTagId;
    }

    public static String getL1LoadingAreaTagIdForPallet(String palletTagId, List<WorkOrderListItem> workOrderListItemList) {
        String loadingAreaTagId = "-";
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                // Check if the current item's palletTagId matches the one we're looking for
                if (palletTagId.equals(item.getPalletTagId())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    loadingAreaTagId = item.getLoadingAreaTagId();
                    break;
                }
            }
        }
        return loadingAreaTagId;
    }
    public static String getI0BinTagIdForPallet(String palletTagId, List<WorkOrderListItem> workOrderListItemList) {
        String binTagId = "-";
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                // Check if the current item's palletTagId matches the one we're looking for
                if (palletTagId.equals(item.getPalletTagId())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    binTagId = item.getBinLocationTagId();
                    break;
                }
            }
        }
        return binTagId;
    }

    public static List<WorkOrderListItem> getUpdatedWorkOrderList(List<WorkOrderListItem> itemList, String palletTagIdToFind, String newStatus) {
        List<WorkOrderListItem> updatedList = new ArrayList<>();

        for (WorkOrderListItem item : itemList) {
            // Check if the current item's palletTagId matches the one we're looking for
            if (palletTagIdToFind.equals(item.getPalletTagId())) {
                // If found, update the listItemStatus
                item.setListItemStatus(newStatus);
            }
            updatedList.add(item);
        }

        return updatedList;
    }
    public static List<WorkOrderListItem> getUpdatedTotalOrders(List<WorkOrderListItem> itemList, String palletTagIdToFind, String newStatus) {
       int cnt = 0;
       List<WorkOrderListItem> updatedList = new ArrayList<>();
        for (WorkOrderListItem item : itemList) {
            // Check if the current item's palletTagId matches the one we're looking for
            if (palletTagIdToFind.equals(item.getPalletTagId())) {
                // If found, update the listItemStatus
                //itemList.remove(cnt);
            }else{
                updatedList.add(item);
            }
            cnt++;
        }
        return updatedList;
    }

    public static List<WorkOrderListItem> getUpdatedRecOrders(List<WorkOrderListItem> itemList, String palletTagIdToFind, String newStatus) {
        int cnt = 0;
        List<WorkOrderListItem> updatedList = new ArrayList<>();
        for (WorkOrderListItem item : itemList) {
            // Check if the current item's palletTagId matches the one we're looking for
            if (palletTagIdToFind.equals(item.getPalletTagId())) {
                // If found, update the listItemStatus
                //itemList.remove(cnt);

            }else{
                updatedList.add(item);
            }
            cnt++;
        }
        return updatedList;
    }
    public static List<WorkOrderListItem> getUpdatedDisOrders(List<WorkOrderListItem> itemList, String palletTagIdToFind, String newStatus) {
        int cnt = 0;
        List<WorkOrderListItem> updatedList = new ArrayList<>();
        for (WorkOrderListItem item : itemList) {
            // Check if the current item's palletTagId matches the one we're looking for
            if (palletTagIdToFind.equals(item.getPalletTagId())) {
                // If found, update the listItemStatus
               // itemList.remove(cnt);

            }else{
                updatedList.add(item);
            }
            cnt++;
        }
        return updatedList;
    }
    public static WorkOrderListItem getWorkorderListItem(String palletTagID, List<WorkOrderListItem> workOrderListItemList) {
        WorkOrderListItem workorderItem = null;
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                // Check if the current item's palletTagId matches the one we're looking for
                if (palletTagID.equals(item.getPalletTagId())) {
                    workorderItem = item;
                    break;
                }
                }
            }
        return workorderItem;
    }
    public static String getWorkorderType(String palletTagID, List<WorkOrderListItem> workOrderListItemList) {
        String workorderType = "";
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                // Check if the current item's palletTagId matches the one we're looking for
                if (palletTagID.equals(item.getPalletTagId())) {
                    workorderType = item.getWorkorderType();
                    break;
                }
            }
        }
        return workorderType;
    }
    public static String getWorkorderStatus(String palletTagID, List<WorkOrderListItem> workOrderListItemList) {
        String workorderStatus = "";
        if (workOrderListItemList != null) {
            for (WorkOrderListItem item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                // Check if the current item's palletTagId matches the one we're looking for
                if (palletTagID.equals(item.getPalletTagId())) {
                    workorderStatus = item.getWorkorderStatus();
                    break;
                }
            }
        }
        return workorderStatus;
    }
}
