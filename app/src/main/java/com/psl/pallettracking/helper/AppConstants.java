package com.psl.pallettracking.helper;

import com.psl.pallettracking.database.AssetMaster;
import com.psl.pallettracking.database.LocationMaster;

import java.util.ArrayList;
import java.util.List;

public class AppConstants {

    public static final String ASSET_TYPE_SPLIT_DATA = "PSLLAB";
    public static String SEARCH = "30361F4B3802";

    public static String UNKNOWN_ASSET = "UNKNOWN";

    public static final String ASSET_COUNT = "tagCount";
    public static final String ASSET_TYPE_ID = "assetTypeID";
    public static final String ASSET_TYPE_NAME = "assetTypeName";
    public static final String ASSET_NAME = "assetName";
    public static final String ASSET_TAG_ID = "assetTagId";

    public static final String MENU_ID_CARTON_PALLET_MAPPING = "MAP_CARTON_PALLET_ID";
    public static final String MENU_ID_ITEM_PALLET_MAPPING = "MAP_ITEM_PALLET_ID";
    public static final String MENU_ID_PALLET_MOVEMENT = "ORDER_MODULE";
    public static final String MENU_ID_CONTAINER_PALLET_MAPPING = "MAP_PALLET_BIN";
    public static final String MENU_ID_INVENTORY = "INVENTORY_ID";
    public static final String MENU_ID_PARTIAL = "PARTIAL_METHOD";
    public static final String MENU_ID_ITEM_MOVEMENT = "ITEM_MOVEMENT";
    public static final String MENU_ID_SEARCH = "SEARCH_ID";
    public static final String MENU_ID_CHECKIN = "CHECKIN_ID";
    public static final String MENU_ID_CHECKOUT = "CHECKOUT_ID";
    public static final String MENU_ID_ASSETSYNC = "ASSETSYNC_ID";
    public static final String MENU_ID_ROOMCHECKOUT = "ROOMCHECKOUT_ID";
    public static final String MENU_ID_TRACKPOINT = "TRACKPOINT_ID";
    public static final String MENU_ID_SECURITYOUT = "SECURITYOUT_ID";
    public static final String MENU_ID_MAP_PARTIAL_PALLET = "MAP_PARTIAL_PALLET_ITEM";
    public static final String MENU_ID_STOCK_RECORD = "STOCK_RECORD";

    public static List<AssetMaster> getAssetMasterList(){
        List<AssetMaster> list = new ArrayList<>();

        //Company Code = "21";
        //ID - 01 - SHIRT
        //ID - 02 - PANT
        //ID - 03 - JACKET
        //ID - 04 - TOWEL
        //ID - 05 - BEDSHIT
        //ID - 06 - PILLOW COVER
        
        String companycode = "15";//E2,21
        String shirt1 = "01";
        String pant2 = "02";
        String jacket3 = "03";
        String towel4 = "04";
        String bedshit5 = "05";
        String pilowcover6 = "06";

        AssetMaster a1 = new AssetMaster();
        a1.setAssetId("00001");
        a1.setAssetName("SHIRT1");
        a1.setAssetTypeId(shirt1);
        a1.setIsAssetActive("true");
        a1.setIsAssetRegistered("true");
        a1.setAssetSerialNumber("00000001");


        AssetMaster a11 = new AssetMaster();
        a11.setAssetId("0002");
        a11.setAssetName("SHIRT2");
        a11.setAssetTypeId(shirt1);
        a11.setIsAssetActive("true");
        a11.setIsAssetRegistered("true");
        a11.setAssetSerialNumber("00000002");

        AssetMaster a2 = new AssetMaster();
        a2.setAssetId("0003");
        a2.setAssetTypeId(pant2);
        a2.setAssetName("PANT1");
        a2.setIsAssetActive("true");
        a2.setIsAssetRegistered("true");
        a2.setAssetSerialNumber("00000002");

        AssetMaster a3 = new AssetMaster();
        a3.setAssetId("0004");
        a3.setAssetTypeId(jacket3);
        a3.setAssetName("JACKET1");
        a3.setIsAssetActive("true");
        a3.setIsAssetRegistered("true");
        a3.setAssetSerialNumber("00000003");

        AssetMaster a4 = new AssetMaster();
        a4.setAssetId("0005");
        a4.setAssetTypeId(towel4);
        a4.setAssetName("TOWEL1");
        a4.setIsAssetActive("true");
        a4.setIsAssetRegistered("true");
        a4.setAssetSerialNumber("00000004");

        AssetMaster a5 = new AssetMaster();
        a5.setAssetId("0006");
        a5.setAssetTypeId(bedshit5);
        a5.setAssetName("BEDSHIT1");
        a5.setIsAssetActive("true");
        a5.setIsAssetRegistered("true");
        a5.setAssetSerialNumber("00000005");

        AssetMaster a6 = new AssetMaster();
        a6.setAssetId("0007");
        a6.setAssetTypeId(pilowcover6);
        a6.setAssetName("PILLOW COVER1");
        a6.setIsAssetActive("true");
        a6.setIsAssetRegistered("true");
        a6.setAssetSerialNumber("00000006");

        AssetMaster a7 = new AssetMaster();
        a7.setAssetId("0008");
        a7.setAssetTypeId(pilowcover6);
        a7.setAssetName("PILLOW COVER1");
        a7.setIsAssetActive("true");
        a7.setIsAssetRegistered("false");
        a7.setAssetSerialNumber("");
        
        list.add(a1);
        list.add(a11);
        list.add(a2);
        list.add(a3);
        list.add(a4);
        list.add(a5);
        list.add(a6);
        list.add(a7);
        return list;
    }


    public static List<AssetMaster> getAssetTypeMasterList(){
        List<AssetMaster> list = new ArrayList<>();

        //Company Code = "21";
        //ID - 01 - SHIRT
        //ID - 02 - PANT
        //ID - 03 - JACKET
        //ID - 04 - TOWEL
        //ID - 05 - BEDSHIT
        //ID - 06 - PILLOW COVER

        String companycode = "15";//E2,21
        String shirt1 = "01";
        String pant2 = "02";
        String jacket3 = "03";
        String towel4 = "04";
        String bedshit5 = "05";
        String pilowcover6 = "06";

        AssetMaster a1 = new AssetMaster();
        a1.setAssetTypeName("SHIRT");
        a1.setAssetTypeId(shirt1);

        AssetMaster a2 = new AssetMaster();
        a2.setAssetTypeId(pant2);
        a2.setAssetTypeName("PANT");


        AssetMaster a3 = new AssetMaster();
        a3.setAssetTypeId(jacket3);
        a3.setAssetTypeName("JACKET");

        AssetMaster a4 = new AssetMaster();
        a4.setAssetTypeId(towel4);
        a4.setAssetTypeName("TOWEL");


        AssetMaster a5 = new AssetMaster();
        a5.setAssetTypeId(bedshit5);
        a5.setAssetTypeName("BEDSHIT");

        AssetMaster a6 = new AssetMaster();
        a6.setAssetTypeId(pilowcover6);
        a6.setAssetTypeName("PILLOW COVER");

        list.add(a1);
        list.add(a2);
        list.add(a3);
        list.add(a4);
        list.add(a5);
        list.add(a6);
        return list;
    }



    public static List<LocationMaster> getLocationMasterList(){
        List<LocationMaster> list = new ArrayList<>();

        //Company Code = "21";
        //ID - 01 - SHIRT
        //ID - 02 - PANT
        //ID - 03 - JACKET
        //ID - 04 - TOWEL
        //ID - 05 - BEDSHIT
        //ID - 06 - PILLOW COVER

        String companycode = "15";//E2,21
        String shirt1 = "01";
        String pant2 = "02";
        String jacket3 = "03";
        String towel4 = "04";
        String bedshit5 = "05";
        String pilowcover6 = "06";

        LocationMaster a1 = new LocationMaster();
        a1.setLocationId("01");
        a1.setLocationName("Location 1");

        LocationMaster a2 = new LocationMaster();
        a2.setLocationId("02");
        a2.setLocationName("Location 2");

        LocationMaster a3 = new LocationMaster();
        a3.setLocationId("03");
        a3.setLocationName("Location 3");

        LocationMaster a4 = new LocationMaster();
        a4.setLocationId("04");
        a4.setLocationName("Location 4");

        LocationMaster a5 = new LocationMaster();
        a5.setLocationId("05");
        a5.setLocationName("Location 5");

        LocationMaster a6 = new LocationMaster();
        a6.setLocationId("06");
        a6.setLocationName("Location 6");

        list.add(a1);
        list.add(a2);
        list.add(a3);
        list.add(a4);
        list.add(a5);
        list.add(a6);
        return list;
    }

    public static final String DASHBOARD_MENU_ASSET_PALLET_MAPPING_IN = "Asset Mapping Inward";
    public static final String DASHBOARD_MENU_CONTAINER_PALLET_MAPPING = "Pallet Mapping";
    public static final String DASHBOARD_MENU_ASSET_PALLET_MAPPING_OUT = "Asset Mapping Outward";
    public static final String DASHBOARD_MENU_PALLET_MOVEMENT = "Pallet Movement";
    public static final String DASHBOARD_MENU_PARTIAL_LOADING = "Dispatch Pallet Creation";
    public static final String DASHBOARD_MENU_ITEM_MOVEMENT = "Item Movement";
    public static final String DASHBOARD_MENU_STOCK_RECORD = "Stock Record";
    public static final String DASHBOARD_MENU_INVENTORY = "Inventory";
    public static final String DASHBOARD_MENU_SEARCH = "Search";
    public static final String DASHBOARD_MENU_CHECKIN = "Check In";
    public static final String DASHBOARD_MENU_CHECKOUT = "Check Out";
    public static final String DASHBOARD_MENU_ROOMCHECKOUT = "Room Check Out";
    public static final String DASHBOARD_MENU_SECURITYOUT = "Security Out";
    public static final String DASHBOARD_MENU_TRACK_POINT = "Track Point";
    public static final String DASHBOARD_MENU_SYNC = "Sync";

}
