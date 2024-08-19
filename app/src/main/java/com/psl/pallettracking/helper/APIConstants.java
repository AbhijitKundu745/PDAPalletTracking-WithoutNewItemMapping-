package com.psl.pallettracking.helper;

import java.util.Calendar;
import java.util.Date;

public class APIConstants {
    //http://psltestapi.azurewebsites.net/
    public static final String M_POST_INVENTORY = "/PDA/InsertTransactionDetails";

    public static final String M_UPLOAD_INVENTORY = "/PDA/TransactionMobile";
    public static final String M_ASSET_REGISTRATION = "/PDA/RegisterAssetMobile";
    public static final String M_TRUCK_DETAILS = "/PDA/GetTruckDetailsForPDA";
    public static final String M_DC_DETAILS = "/PDA/GetDCDetailsForPDA";
    public static final String M_GET_ITEM_DETAILS = "/PDA/GetSTOLineItemsByTruckID";
    public static final String M_UPLOAD_ITEM_DETAILS = "/PDA/InsertTransactionForItems";
    public static final String M_GET_PARTIAL_WORK_ORDERS = "/PDA/GetPartialWorkorderList";
    public static final String M_GET_PARTIAL_WORK_ORDERS_DETAILS = "/PDA/GetPartialWorkorderItemDetails";
    public static final String M_UPLOAD_PARTIAL_WORK_ORDERS_DETAILS = "/PDA/InsertPartialWorkorderDetails";
    public static final String M_GET_BIN_DETAILS = "/PDA/GetBinInfo";
 public static final String M_UPLOAD_ITEM_MOVEMENT = "/PDA/ItemMovementTransaction";
 public static final String M_UPLOAD_ITEM_QR_MANUAL = "/PDA/InsertTransactionForQRManual";


    public static final String M_GET_ASSET_MASTER = "/PDA/GetAllAssetsMobile?tenantID=";
    public static final String M_GET_ROOM_MASTER = "/PDA/GetAllRooms?tenantID=";
    public static final String M_GET_LOST_ASSET_MASTER = "/PDA/GetLostAssets?tenantID=";

 public static final String M_GET_WORK_ORDER_DETAILS = "/PDA/GetReaderWorkorderList";
 public static final String M_GET_WORK_ORDER_DETAILS_FOR_PDA = "/PDA/GetWorkorderListForPDA";
 public static final String M_POST_CURRENT_PALLET = "/PDA/UploadCurrentPallet";
 public static final String M_GET_SKU_DETAILS = "/PDA/GetSKUDetailsForPDA";
 public static final String M_GET_ALL_SKU_DATA = "/PDA/GetAllSKUsPDA";
 public static final String M_GET_SO_LINE_ITEMS = "/PDA/GetSOLineItemsLoading";



    public static final String M_USER_LOGIN = "/PDA/MobileLogin";
   // public static final String M_UPLOAD_ASSET_PALLET_MAPPING = "/PDA/RegisterAssetPallet";
   // public static final String M_UPLOAD_CONTAINER_PALLET_MAPPING = "/PDA/RegisterContainerPallet";
    public static final String M_GET_PRODUCT_MASTER = "/PDA/GetAllAssetsMobile?tenantID=";

    public static final int API_TIMEOUT = 60;

    public static final String K_STATUS = "status";
    public static final String K_MESSAGE = "message";
    public static final String K_DATA = "data";
    public static final String K_ASSETS = "Assets";
    public static final String K_PALLETS = "Pallets";

    public static final String K_USER = "UserName";
    public static final String K_PASSWORD = "Password";
    public static final String K_DEVICE_ID = "ClientDeviceID";
    public static final String K_PALLET_ID = "PalletID";
    public static final String K_CONTAINER_ID = "ContainerID";
    public static final String K_USER_ID = "UserID";
    public static final String K_CUSTOMER_ID = "CustomerID";


    public static final String K_ACTIVITY_ID = "ActivityID";
    public static final String K_ACTIVITY_TYPE = "ActivityType";
    public static final String K_COMPANY_ID = "";
    public static final String K_ACTIVITY_DETAILS_ID = "ActivityDetailsID";
    public static final String K_PARENT_TAG_ID = "ParentTagID";
    public static final String K_PARENT_ASSET_TYPE = "ParentAssetType";
    public static final String K_TRUCK_NUMBER = "TruckNumber";
    public static final String K_PROCESS_TYPE = "ProcessType";
    public static final String K_DRN = "DRN";
    public static final String K_TRUCK_TAG_ID = "TruckTagID";
    public static final String K_Qty = "Qty";
    public static final String K_COMPANY_CODE = "CompanyCode";
    public static final String K_TAG_ACCESS_PASSWORD = "TagPassword";
    public static final String K_IS_LOG_REQUIRED = "ISLogRequired";
    public static final String K_CURRENT_ACCESS_PASSWORD = "";

    public static final String K_ROOM_MASTER = "Room";
    public static final String K_VENDOR_MASTER = "Vendor";
    public static final String K_ASSETTYPE_MASTER = "AssetType";

    public static final String K_VENDOR_ID = "VendorID";
    public static final String K_ROOM_ID = "RoomID";
    public static final String K_ROOM_RFID = "TagId";
    public static final String K_UID = "UID";
    public static final String K_VENDOR_NAME = "Name";
    public static final String K_ROOM_NAME = "RoomName";
    public static final String K_ASSET_TYPE_ID = "ATypeID";
    public static final String K_ASSET_TYPE_NAME = "AssetName";
    public static final String K_ITEM_DESCRIPTION = "ItemDescription";
    public static final String K_TOUCH_POINT_ID = "TouchpointID";

    public static final String K_ASSET_ID = "AssetID";

    public static final String K_PRODUCT_ID = "tagid";
    public static final String K_PRODUCT_NAME = "name";
    public static final String K_PRODUCT_TYPE = "type";
    public static final String K_ASSET_NAME = "AName";
    public static final String K_ASSET_SERIAL_NUMBER = "ASerialNo";

    public static final String K_TAG_TID = "ATagTid";
    public static final String K_CURRENT_TAG_ID = "ATagId";
    public static final String K_PREVIOUS_TAG_ID = "OldATagId";

    public static final String K_IS_REGISTERED = "IsRegistered";
    public static final String K_INVENTORY_TYPE = "ActivityType";
    public static final String K_INVENTORY_COUNT = "Count";
    public static final String K_INVENTORY_TIME = "TimeTaken";
    public static final String K_INVENTORY_START_DATE_TIME = "StartDate";
    public static final String K_INVENTORY_END_DATE_TIME = "EndDate";
    public static final String K_TRANSACTION_DATE_TIME = "TransactionDateTime";


    public static final String K_DASHBOARD_ARRAY = "Dashboard";
    public static final String K_DASHBOARD_MENU_ID = "Menu_ID";
    public static final String K_DASHBOARD_MENU_NAME = "Menu_Name";
    public static final String K_DASHBOARD_MENU_ACTIVITY_NAME = "Menu_Activity_Name";
    public static final String K_DASHBOARD_MENU_IMAGE = "Menu_Image";
    public static final String K_DASHBOARD_MENU_ACTIVE = "Menu_Is_Active";
    public static final String K_DASHBOARD_MENU_SEQUENCE = "Menu_Sequence";


    public static final String K_ACTION_SYNC = "SYNC";
    public static final String K_ACTION_INVENTORY = "INV";
    public static final String K_ACTION_MAPPING = "MAP";
    public static final String K_ACTION_CHECKIN = "IN";
    public static final String K_ACTION_CHECKOUT = "OUT";
    public static final String K_ACTION_ROOM_CHECKOUT = "ROOMOUT";
    public static final String K_ACTION_TRACKPOINT = "TRACKPOINT";
    public static final String K_ACTION_SECURITY_OUT = "SECURITYOUT";


 public static final String DEVICE_ID = "ClientDeviceID";
 public static final String READER_STATUS = "ReaderStatus";
 public static final String ANTENA_ID = "AntennaID";
 public static final String RSSI = "RSSI";
 public static final String TRANSACTION_DATE_TIME = "TransDatetime";
 public static final String TOUCH_POINT_TYPE = "TouchPointType";
 public static final String COUNT = "Count";
 public static final String PALLET_TAG_ID = "PalletTagID";

 public static final String SUB_TAG_DETAILS = "SubTagDetails";
 public static final String SUB_TAG_ID = "TagID";
 public static final String SUB_TAG_CATEGORY_ID = "CategoryID";
 public static final String SUB_TAG_TYPE = "TagType";

 public static final String TRANS_ID= "TransID";
 public static final String SUB_TRANS_ID= "TransID";


 public static final String CURRENT_PALLET_NAME = "PalletName";
 public static final String CURRENT_SCANNED_PALLET_NAME = "CurrentScannedPalletName";
 public static final String CURRENT_SCANNED_PALLET_TAG_ID = "CurrentScannedPalletTagID";
 public static final String CURRENT_PALLET_TAG_ID = "PalletTagID";
 public static final String LAST_UPDATED_DATE_TIME = "LastUpdatedDateTime";
 public static final String CURRENT_TEMP_STORAGE_NAME = "TemporaryStorageName";
 public static final String CURRENT_TEMP_STORAGE_TAG_ID = "TemporaryStorageTagID";
 public static final String CURRENT_LOADING_AREA_NAME = "LoadingAreaName";
 public static final String CURRENT_LOADING_AREA_TAG_ID = "LoadingAreaTagID";
 public static final String CURRENT_BIN_LOCATION_NAME = "BinLocation";
 public static final String DESTINATION_LOCATION_NAME = "LocationName";
 public static final String CURRENT_BIN_LOCATION_TAG_ID = "BinLocationTagID";
 public static final String CURRENT_WORK_ORDER_LIST_ITEM_STATUS = "ListItemStatus";

 public static final String K_SOURCE_PALLET_TAG_ID = "SourcePalletTagID";
 public static final String K_DESTINATION_PALLET_TAG_ID = "DestinationPalletTagID";
 public static final String K_SOURCE_BIN_TAG_ID = "SourceBinTagID";
 public static final String K_DESTINATION_BIN_TAG_ID = "DestinationBinTagID";
 public static final String DATA = "data";
 public static final String STATUS = "status";
 public static final String MESSAGE = "message";
 public static final String READER_CONFIGURATION = "configuration";
 public static final String READER_CONFIGURATION_RSSI = "RSSI";
 public static final String READER_CONFIGURATION_POWER = "Power";
 public static final String READER_CONFIGURATION_POLLING_TIMER = "PollingTimer";
 public static final String START_WORK_ORDER = "StartWorkOrder";
 public static final String CURRENT_WORK_ORDER_NUMBER = "WorkorderNumber";
 public static final String CURRENT_WORK_ORDER_TYPE = "WorkorderType";
 public static final String CURRENT_WORK_ORDER_STATUS = "WorkorderStatus";
 public static final String K_DC_TAG_ID = "DCTagID";
 public static final String K_DC_NO = "DCNo";

 public static String getSystemDateTimeForBatchId() {
  try {
   int year, monthformat, dateformat, sec;
   String da, mont, hor, min, yr, systemDate, secs;
   Calendar calendar = Calendar.getInstance();
   calendar.setTime(new Date());
   year = calendar.get(Calendar.YEAR);
   monthformat = calendar.get(Calendar.MONTH) + 1;
   dateformat = calendar.get(Calendar.DATE);
   int hours = calendar.get(Calendar.HOUR_OF_DAY);
   int minutes = calendar.get(Calendar.MINUTE);
   sec = calendar.get(Calendar.SECOND);
   da = Integer.toString(dateformat);
   mont = Integer.toString(monthformat);
   hor = Integer.toString(hours);
   min = Integer.toString(minutes);
   secs = Integer.toString(sec);
   if (da.trim().length() == 1) {
    da = "0" + da;
   }
   if(mont.trim().equals("1")){
    mont = "01";
   }
   if(mont.trim().equals("2")){
    mont = "02";
   }
   if(mont.trim().equals("3")){
    mont = "03";
   }
   if(mont.trim().equals("4")){
    mont = "04";
   }
   if(mont.trim().equals("5")){
    mont = "05";
   }
   if(mont.trim().equals("6")){
    mont = "06";
   }
   if(mont.trim().equals("7")){
    mont = "07";
   }
   if(mont.trim().equals("8")){
    mont = "08";
   }
   if(mont.trim().equals("9")){
    mont = "09";
   }
   if(mont.trim().equals("10")){
    mont = "10";
   }
   if(mont.trim().equals("11")){
    mont = "11";
   }
   if(mont.trim().equals("12")){
    mont = "12";
   }

   if (hor.trim().length() == 1) {
    hor = "0" + hor;
   }
   if (min.trim().length() == 1) {
    min = "0" + min;
   }
   if (secs.trim().length() == 1) {
    secs = "0" + secs;
   }
   yr = Integer.toString(year);
   // systemDate = (da + mont + yr + hor + min + secs);
   systemDate = (yr + "-" + mont + "-" + da + "-" + hor + ":" + min + ":" + secs);
   return systemDate;
  } catch (Exception e) {
   // return "01011970000000";
   // return "1970-01-01 00:00:00";
   return "1970-01-01-00:00:00";
  }
 }

}
