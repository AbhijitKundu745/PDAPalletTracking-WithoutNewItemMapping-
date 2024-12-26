package com.psl.pallettracking.database;


import static com.psl.pallettracking.ext.DataExt.typePallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.psl.pallettracking.adapters.DashboardModel;
import com.psl.pallettracking.adapters.MyObject;
import com.psl.pallettracking.bean.TagWithDestination;
import com.psl.pallettracking.helper.AppConstants;
import com.psl.pallettracking.bean.TagBean;
import com.psl.pallettracking.bean.WorkOrderUploadTagBean;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 19;
    private static final String DATABASE_NAME = "PSL_PALLET_TRACKING-DB";

    private static final String TABLE_ASSET_MASTER = "Asset_Master_Table";
    private static final String TABLE_PRODUCT_MASTER = "Product_Master_Table";
    private static final String TABLE_ASSET_TYPE_MASTER = "Asset_Type_Master_Table";
    private static final String TABLE_LOCATION_MASTER = "Location_Master_Table";
    private static final String TABLE_ROOM_MASTER = "Room_Master_Table";
    private static final String TABLE_LOST_ASSET_MASTER = "Lost_Asset_Master_Table";
    private static final String TABLE_DASHBOARD_MENU = "Dashboard_Menu_Table";
    private static final String TABLE_TAG_MASTER = "Tag_Master_Table";
    private static final String TABLE_OFFLINE_TAG_MASTER = "Offline_Tag_Master_Table";
    private static final String TABLE_OFFLINE_TAG_WITH_DESTINATION = "Offline_Tag_Table_With_Destination";
    private static final String TABLE_PARTIAL_DISPATCH = "dispatch_pallet_data";
    private static final String TABLE_SKU_MASTER = "sku_master";


    private static final String KEY_ID = "id";
    private static final String KEY_JSON_DATA = "KEY_JSON_DATA";
    private static final String K_DASHBOARD_MENU_ID = "K_DASHBOARD_MENU_ID";
    private static final String K_DASHBOARD_MENU_NAME = "K_DASHBOARD_MENU_NAME";
    private static final String K_DASHBOARD_MENU_ACTIVITY_NAME = "K_DASHBOARD_MENU_ACTIVITY_NAME";
    private static final String K_DASHBOARD_MENU_IMAGE = "K_DASHBOARD_MENU_IMAGE";
    private static final String K_DASHBOARD_MENU_ACTIVE = "K_DASHBOARD_MENU_ACTIVE";
    private static final String K_DASHBOARD_MENU_SEQUENCE = "K_DASHBOARD_MENU_SEQUENCE";

    private static final String K_ASSET_ID = "K_ASSET_ID";
    private static final String K_PRODUCT_TAG_ID = "K_PRODUCT_TAG_ID";
    private static final String K_PRODUCT_NAME = "K_PRODUCT_NAME";
    private static final String K_PRODUCT_TYPE = "K_PRODUCT_TYPE";
    private static final String K_ASSET_SERIAL_NUMBER = "K_ASSET_SERIAL_NUMBER";
    private static final String K_ASSET_TYPE_ID = "K_ASSET_TYPE_ID";
    private static final String K_LOCATION_ID = "K_LOCATION_ID";
    private static final String K_ROOM_RFID = "K_ROOM_RFID";
    private static final String K_ROOM_ID = "K_ROOM_ID";

    private static final String K_LOST_ASSET_ID = "K_LOST_ASSET_ID";
    private static final String K_LOST_ASSET_TAG_ID = "K_LOST_ASSET_TAG_ID";

    private static final String K_ASSET_NAME = "K_ASSET_NAME";
    private static final String K_ASSET_TYPE_NAME = "K_ASSET_TYPE_NAME";
    private static final String K_IS_ASSET_REGISTERED = "K_IS_ASSET_REGISTERED";
    private static final String K_LOCATION_NAME = "K_LOCATION_NAME";
    private static final String K_ROOM_NAME = "K_ROOM_NAME";

    private static final String K_EPC = "K_EPC";
    private static final String K_WORK_ORDER_NUMBER = "K_WORK_ORDER_NUMBER";
    private static final String K_WORK_ORDER_TYPE = "K_WORK_ORDER_TYPE";
    private static final String K_BATCH_ID = "K_BATCH_ID";
    private static final String K_RSSI = "K_RSSI";
    private static final String K_TIMES = "K_TIMES";
    private static final String K_ANTEANA = "K_ANTEANA";
    private static final String K_ADDITIONAL_DATA = "K_ADDITIONAL_DATA";
    private static final String K_DESTINATION_EPC = "K_DESTINATION_EPC";

    private static final String K_TAG_TYPE = "K_TAG_TYPE";
    private static final String K_DATE_TIME = "K_DATE_TIME";
    private static final String K_ITEM_NAME = "K_ITEM_NAME";
    private static final String K_ITEM_CODE = "K_ITEM_CODE";



    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TAG_MASTER_TABLE = "CREATE TABLE "
                + TABLE_TAG_MASTER
                + "("
                + K_EPC + " TEXT UNIQUE,"//0
                + K_RSSI + " INTEGER,"//1
                + K_TIMES + " INTEGER,"//1
                + K_ANTEANA + " TEXT,"//1
                + K_ADDITIONAL_DATA + " TEXT,"//1
                + K_TAG_TYPE + " TEXT,"//1
                + K_DATE_TIME + " TEXT"//1
                + ")";

        String CREATE_OFFLINE_TAG_MASTER_TABLE = "CREATE TABLE "
                + TABLE_OFFLINE_TAG_MASTER
                + "("
                + K_BATCH_ID + " TEXT,"//0
                + K_WORK_ORDER_NUMBER + " TEXT,"//0
                + K_WORK_ORDER_TYPE + " TEXT,"//0
                + K_EPC + " TEXT,"//0
                + K_RSSI + " INTEGER,"//1
                + K_TIMES + " INTEGER,"//1
                + K_ANTEANA + " TEXT,"//1
                + K_ADDITIONAL_DATA + " TEXT,"//1
                + K_TAG_TYPE + " TEXT,"//1
                + K_DATE_TIME + " TEXT"//1
                + ")";

        String CREATE_ASSET_MASTER_TABLE = "CREATE TABLE "
                + TABLE_ASSET_MASTER
                + "("
                + K_ASSET_ID + " TEXT UNIQUE,"//0
                + K_ASSET_NAME + " TEXT,"//1
                + K_ASSET_TYPE_ID + " TEXT,"//1
                + K_ASSET_SERIAL_NUMBER + " TEXT,"//1
                + K_IS_ASSET_REGISTERED + " TEXT"//1
                + ")";
        String CREATE_PRODUCT_MASTER_TABLE = "CREATE TABLE "
                + TABLE_PRODUCT_MASTER
                + "("
                + K_PRODUCT_TAG_ID + " TEXT UNIQUE,"//0
                + K_PRODUCT_NAME + " TEXT,"//1
                + K_PRODUCT_TYPE + " TEXT"//1
                + ")";

        String CREATE_DASHBOARD_MENU_TABLE = "CREATE TABLE "
                + TABLE_DASHBOARD_MENU
                + "("
                + K_DASHBOARD_MENU_ID + " TEXT UNIQUE,"//0
                + K_DASHBOARD_MENU_NAME + " TEXT,"//1
                + K_DASHBOARD_MENU_ACTIVITY_NAME + " TEXT,"//1
                + K_DASHBOARD_MENU_IMAGE + " TEXT,"//1
                + K_DASHBOARD_MENU_ACTIVE + " TEXT,"//1
                + K_DASHBOARD_MENU_SEQUENCE + " TEXT"//1
                + ")";

        String CREATE_ASSET_TYPE_MASTER_TABLE = "CREATE TABLE "
                + TABLE_ASSET_TYPE_MASTER
                + "("
                + K_ASSET_TYPE_ID + " TEXT UNIQUE,"//0
                + K_ASSET_TYPE_NAME + " TEXT"//1
                + ")";

        String CREATE_ASSET_LOST_MASTER_TABLE = "CREATE TABLE "
                + TABLE_LOST_ASSET_MASTER
                + "("
                + K_LOST_ASSET_ID + " TEXT UNIQUE,"//0
                + K_LOST_ASSET_TAG_ID + " TEXT"//1
                + ")";

        String CREATE_LOCATION_MASTER_TABLE = "CREATE TABLE "
                + TABLE_LOCATION_MASTER
                + "("
                + K_LOCATION_ID + " TEXT UNIQUE,"//0
                + K_LOCATION_NAME + " TEXT"//1

                + ")";

        String CREATE_ROOM_MASTER_TABLE = "CREATE TABLE "
                + TABLE_ROOM_MASTER
                + "("
                + K_ROOM_ID + " TEXT UNIQUE,"//0
                + K_ROOM_NAME + " TEXT,"//1
                + K_ROOM_RFID + " TEXT"//1
                + ")";

        String CREATE_DISPATCH_PALLET_TABLE = "CREATE TABLE " + TABLE_PARTIAL_DISPATCH + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_JSON_DATA + " TEXT)";

        String CREATE_OFFLINE_TAG_WITH_DESTINATION_TABLE = "CREATE TABLE "
                + TABLE_OFFLINE_TAG_WITH_DESTINATION
                + "("
                + K_BATCH_ID + " TEXT,"//0
                + K_WORK_ORDER_NUMBER + " TEXT,"//0
                + K_EPC + " TEXT,"//0
                + K_DESTINATION_EPC + " TEXT,"//1
                + K_WORK_ORDER_TYPE + " TEXT,"//1
                + K_DATE_TIME + " TEXT"//1
                + ")";
        String CREATE_SKU_MASTER_TABLE = "CREATE TABLE "
                + TABLE_SKU_MASTER
                + "("
                + K_ITEM_NAME + " TEXT,"//0
                + K_ITEM_CODE + " TEXT UNIQUE"//1
                + ")";


        db.execSQL(CREATE_ASSET_MASTER_TABLE);
        db.execSQL(CREATE_ASSET_TYPE_MASTER_TABLE);
        db.execSQL(CREATE_LOCATION_MASTER_TABLE);
        db.execSQL(CREATE_ROOM_MASTER_TABLE);
        db.execSQL(CREATE_ASSET_LOST_MASTER_TABLE);
        db.execSQL(CREATE_DASHBOARD_MENU_TABLE);
        db.execSQL(CREATE_PRODUCT_MASTER_TABLE);

        db.execSQL(CREATE_TAG_MASTER_TABLE);
        db.execSQL(CREATE_OFFLINE_TAG_MASTER_TABLE);
        db.execSQL(CREATE_DISPATCH_PALLET_TABLE);
        db.execSQL(CREATE_OFFLINE_TAG_WITH_DESTINATION_TABLE);
        db.execSQL(CREATE_SKU_MASTER_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSET_MASTER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSET_TYPE_MASTER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION_MASTER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM_MASTER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOST_ASSET_MASTER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DASHBOARD_MENU);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT_MASTER);


        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAG_MASTER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OFFLINE_TAG_MASTER);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARTIAL_DISPATCH);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OFFLINE_TAG_WITH_DESTINATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SKU_MASTER);
        // Create tables again
        onCreate(db);
    }

    // Read records related to the search term
    public MyObject[] read(String searchTerm,String assettypeid) {

        // select query
        String sql = "";
        sql += "SELECT * FROM " + TABLE_ASSET_MASTER;
        sql += " WHERE " + K_ASSET_NAME + " LIKE '%" + searchTerm + "%' AND K_ASSET_TYPE_ID = '"+ assettypeid+"'";
       // sql += " ORDER BY " + fieldObjectId + " DESC";
        sql += " LIMIT 0,5";

        SQLiteDatabase db = this.getWritableDatabase();

        // execute the query
        Cursor cursor = db.rawQuery(sql, null);

        int recCount = cursor.getCount();

        MyObject[] ObjectItemData = new MyObject[recCount];
        int x = 0;

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                String objectName = cursor.getString(cursor.getColumnIndexOrThrow(K_ASSET_NAME));
                Log.e("DBNAME", "objectName: " + objectName);

                MyObject myObject = new MyObject(objectName);

                ObjectItemData[x] = myObject;

                x++;

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return ObjectItemData;
    }

    public boolean isSerailNumberAlreadyRegisteredToThisTypeId(String serailnumber,String assettypeid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ASSET_MASTER, new String[]{K_ASSET_SERIAL_NUMBER,K_ASSET_TYPE_ID}, K_ASSET_SERIAL_NUMBER + "=? AND "+K_ASSET_TYPE_ID + "=?",
                new String[]{serailnumber,assettypeid}, null, null, null);
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Found
                    return true;
                } else {
                    //PID Not Found
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        } finally {
            cursor.close();
        }
    }

    public boolean isSerailNumberAlreadyRegistered(String serailnumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ASSET_MASTER, new String[]{K_ASSET_SERIAL_NUMBER}, K_ASSET_SERIAL_NUMBER + "=?",
                new String[]{serailnumber}, null, null, null);
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Found
                    return true;
                } else {
                    //PID Not Found
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        } finally {
            cursor.close();
        }
    }

    //UPDATE SINGLE ROW
    public void updateAssetAsRegistered(String assetid,String serialnumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(K_ASSET_SERIAL_NUMBER, serialnumber);
        cv.put(K_IS_ASSET_REGISTERED, "true");
        db.update(TABLE_ASSET_MASTER, cv, K_ASSET_ID + " = ?", new String[]{assetid});
        db.close();
    }

    public boolean isValidAssetNameForAssetTypeId(String assetname,String assettypeid){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ASSET_MASTER, new String[]{K_ASSET_NAME,K_ASSET_TYPE_ID}, K_ASSET_NAME + "=? AND " + K_ASSET_TYPE_ID + "=?",
                new String[]{assetname, assettypeid}, null, null, null);
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Found
                    return true;
                } else {
                    //PID Not Found
                    return false;
                }
            }
        } catch (Exception e) {

            return false;
        } finally {
            cursor.close();
        }
    }

    public void deleteAssetMaster() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ASSET_MASTER, null, null);
        db.close();
    }
    public void deleteOfflineTagWithDestinationTableForBatch(String batchID) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Specify the WHERE clause and arguments based on batchId
        String whereClause = "K_BATCH_ID = ?";
        String[] whereArgs = {batchID};

        // Delete rows with the specified condition
        db.delete(TABLE_OFFLINE_TAG_WITH_DESTINATION, whereClause, whereArgs);

        // Close the database
        db.close();
    }

    public void storeAssetMaster(List<AssetMaster> lst) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO Asset_Master_Table (K_ASSET_ID,K_ASSET_NAME,K_ASSET_TYPE_ID,K_ASSET_SERIAL_NUMBER,K_IS_ASSET_REGISTERED) VALUES (? ,?, ? ,? ,?)";
        //db.beginTransaction();
        db.beginTransactionNonExclusive();
        SQLiteStatement stmt = db.compileStatement(sql);
        try {
            for (int i = 0; i < lst.size(); i++) {
                stmt.bindString(1, lst.get(i).getAssetId());
                stmt.bindString(2, lst.get(i).getAssetName());
                stmt.bindString(3, lst.get(i).getAssetTypeId());
                stmt.bindString(4, lst.get(i).getAssetSerialNumber());
                stmt.bindString(5, lst.get(i).getIsAssetRegistered());
                //stmt.bindString(5, lst.get(i).getIsAssetActive());
                stmt.execute();
                stmt.clearBindings();
            }
            db.setTransactionSuccessful();
            // db.endTransaction();
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    public int getAssetMasterCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT  " + K_ASSET_ID + "," + " count(*) " + " FROM " + TABLE_ASSET_MASTER;
        Cursor cursor = db.rawQuery(selectQuery, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                count = count + cursor.getInt(1);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return count;
    }

    public String getAssetNameByAssetId(String assetid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ASSET_MASTER, new String[]{K_ASSET_NAME}, K_ASSET_ID + "='" + assetid + "'", null, null, null, null);
        System.out.println("Cursor" + cursor.getCount());
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return AppConstants.UNKNOWN_ASSET;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Note
                    cursor.moveToFirst();
                    return cursor.getString(cursor.getColumnIndexOrThrow(K_ASSET_NAME));
                } else {
                    //PID Not Found
                    return AppConstants.UNKNOWN_ASSET;
                }
            }
        } catch (Exception e) {
            return AppConstants.UNKNOWN_ASSET;
        } finally {
            cursor.close();
        }
    }

    public String getAssetNameByAssetSerialNumber(String serial) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ASSET_MASTER, new String[]{K_ASSET_NAME}, K_ASSET_SERIAL_NUMBER + "='" + serial + "'", null, null, null, null);
        System.out.println("Cursor" + cursor.getCount());
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return AppConstants.UNKNOWN_ASSET;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Note
                    cursor.moveToFirst();
                    return cursor.getString(cursor.getColumnIndexOrThrow(K_ASSET_NAME));
                } else {
                    //PID Not Found
                    return AppConstants.UNKNOWN_ASSET;
                }
            }
        } catch (Exception e) {
            return AppConstants.UNKNOWN_ASSET;
        } finally {
            cursor.close();
        }
    }

    public String getAssetIDByAssetName(String assetName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ASSET_MASTER, new String[]{K_ASSET_ID}, K_ASSET_NAME + "='" + assetName + "'", null, null, null, null);
        System.out.println("Cursor" + cursor.getCount());
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return AppConstants.UNKNOWN_ASSET;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Note
                    cursor.moveToFirst();
                    return cursor.getString(cursor.getColumnIndexOrThrow(K_ASSET_ID));
                } else {
                    //PID Not Found
                    return AppConstants.UNKNOWN_ASSET;
                }
            }
        } catch (Exception e) {
            return AppConstants.UNKNOWN_ASSET;
        } finally {
            cursor.close();
        }
    }


    public String getAssetSerialNumberByAssetName(String assetName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ASSET_MASTER, new String[]{K_ASSET_SERIAL_NUMBER}, K_ASSET_NAME + "='" + assetName + "'", null, null, null, null);
        System.out.println("Cursor" + cursor.getCount());
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return "";
            } else {
                if (cursor.getCount() > 0) {
                    //PID Note
                    cursor.moveToFirst();
                    return cursor.getString(cursor.getColumnIndexOrThrow(K_ASSET_SERIAL_NUMBER));
                } else {
                    //PID Not Found
                    return "";
                }
            }
        } catch (Exception e) {
            return "";
        } finally {
            cursor.close();
        }
    }
    public void deleteProductmaster() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PRODUCT_MASTER, null, null);
        db.close();
    }

    public void storeProductMaster(List<ProductMaster> lst) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO Product_Master_Table (K_PRODUCT_TAG_ID,K_PRODUCT_NAME,K_PRODUCT_TYPE) VALUES (? ,?, ?)";
        //db.beginTransaction();
        db.beginTransactionNonExclusive();
        SQLiteStatement stmt = db.compileStatement(sql);
        try {
            for (int i = 0; i < lst.size(); i++) {
                stmt.bindString(1, ""+lst.get(i).getProductTagId());
                stmt.bindString(2, lst.get(i).getProductName());
                stmt.bindString(3, lst.get(i).getProductType());
                //stmt.bindString(5, lst.get(i).getIsAssetActive());
                stmt.execute();
                stmt.clearBindings();
            }
            db.setTransactionSuccessful();
            // db.endTransaction();
        } catch (Exception e) {
            Log.e("PRODUCTMASTEREXC", e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    public int getProductMasterCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT  " + K_PRODUCT_TAG_ID + "," + " count(*) " + " FROM " + TABLE_PRODUCT_MASTER;
        Cursor cursor = db.rawQuery(selectQuery, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                count = count + cursor.getInt(1);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return count;
    }
    public String getProductNameByProductTagId(String tagId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCT_MASTER, new String[]{K_PRODUCT_NAME}, K_PRODUCT_TAG_ID + "='" + tagId + "'", null, null, null, null);
        System.out.println("Cursor" + cursor.getCount());
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return AppConstants.UNKNOWN_ASSET;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Note
                    cursor.moveToFirst();
                    return cursor.getString(cursor.getColumnIndexOrThrow(K_PRODUCT_NAME));
                } else {
                    //PID Not Found
                    return AppConstants.UNKNOWN_ASSET;
                }
            }
        } catch (Exception e) {
            return AppConstants.UNKNOWN_ASSET;
        } finally {
            cursor.close();
        }
    }
    public ArrayList<String> getAllAssetNamesForSearchSpinner() {
//        ArrayList<String> searchogs = new ArrayList<String>();
//        SQLiteDatabase db = this.getWritableDatabase();
//        String selectQuery = "SELECT  * FROM " + TABLE_ASSET_MASTER;
//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        if (cursor.moveToFirst()) {
//            do {
//                //AppConstants.ASSET_TYPE_SPLIT_DATA
//                String assetname = cursor.getString(cursor.getColumnIndexOrThrow(K_ASSET_NAME));
//                String assetid = cursor.getString(cursor.getColumnIndexOrThrow(K_ASSET_ID));
//                searchogs.add(assetname+AppConstants.ASSET_TYPE_SPLIT_DATA+assetid);
//            } while (cursor.moveToNext());
//        }
//        return searchogs;
        String selectQuery = "SELECT " + K_ASSET_NAME + "|| '"
                + AppConstants.ASSET_TYPE_SPLIT_DATA + "' || " + K_ASSET_ID +
                " AS name FROM " + TABLE_ASSET_MASTER;
        return  getStringList(selectQuery, "name");
    }

    public ArrayList<String> getAllNonRegisteredAssetList() {
        ArrayList<String> registeredEquipmentList = new ArrayList<String>();
        SQLiteDatabase db = this.getWritableDatabase();
        //String selectQuery = "SELECT  * FROM " + TABLE_ASSET_MASTER + " WHERE K_IS_ASSET_REGISTERED = ?;";
        String selectQuery = "SELECT  * FROM " + TABLE_ASSET_MASTER + " WHERE K_IS_ASSET_REGISTERED = 'false' ORDER BY K_ASSET_NAME ASC;";
        //Cursor cursor = db.rawQuery(selectQuery, new String[] {"false"});
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                String assetid = cursor.getString(cursor.getColumnIndexOrThrow(K_ASSET_ID));
                String assetname = cursor.getString(cursor.getColumnIndexOrThrow(K_ASSET_NAME));
                String assettypeid = cursor.getString(cursor.getColumnIndexOrThrow(K_ASSET_TYPE_ID));
                registeredEquipmentList.add(assetname+ AppConstants.ASSET_TYPE_SPLIT_DATA+assetid+AppConstants.ASSET_TYPE_SPLIT_DATA+assettypeid);
            } while (cursor.moveToNext());
        }
        return registeredEquipmentList;
    }


    ///ASSET TYPE
    public void deleteAssetTypeMaster() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ASSET_TYPE_MASTER, null, null);
        db.close();
    }

    public void storeAssetTypeMaster(List<AssetMaster> lst) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO Asset_Type_Master_Table (K_ASSET_TYPE_ID,K_ASSET_TYPE_NAME) VALUES (? ,?)";
        //db.beginTransaction();
        db.beginTransactionNonExclusive();
        SQLiteStatement stmt = db.compileStatement(sql);
        try {
            for (int i = 0; i < lst.size(); i++) {
                stmt.bindString(1, lst.get(i).getAssetTypeId());
                stmt.bindString(2, lst.get(i).getAssetTypeName());
                //stmt.bindString(5, lst.get(i).getIsAssetActive());
                stmt.execute();
                stmt.clearBindings();
            }
            db.setTransactionSuccessful();
            // db.endTransaction();
        } catch (Exception e) {
            Log.e("ASSETTYPEMASTEREXC", e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    public int getAssetTypeMasterCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT  " + K_ASSET_TYPE_ID + "," + " count(*) " + " FROM " + TABLE_ASSET_TYPE_MASTER;
        Cursor cursor = db.rawQuery(selectQuery, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                count = count + cursor.getInt(1);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return count;
    }

    public String getAssetTypeNameByAssetTypeId(String assetid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ASSET_TYPE_MASTER, new String[]{K_ASSET_TYPE_NAME}, K_ASSET_TYPE_ID + "='" + assetid + "'", null, null, null, null);
        System.out.println("Cursor" + cursor.getCount());
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return AppConstants.UNKNOWN_ASSET;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Note
                    cursor.moveToFirst();
                    return cursor.getString(cursor.getColumnIndexOrThrow(K_ASSET_TYPE_NAME));
                } else {
                    //PID Not Found
                    return AppConstants.UNKNOWN_ASSET;
                }
            }
        } catch (Exception e) {
            return AppConstants.UNKNOWN_ASSET;
        } finally {
            cursor.close();
        }
    }

    public String getAssetTypeIDByAssetTypeName(String assetName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ASSET_TYPE_MASTER, new String[]{K_ASSET_TYPE_ID}, K_ASSET_TYPE_NAME + "='" + assetName + "'", null, null, null, null);
        System.out.println("Cursor" + cursor.getCount());
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return AppConstants.UNKNOWN_ASSET;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Note
                    cursor.moveToFirst();
                    return cursor.getString(cursor.getColumnIndexOrThrow(K_ASSET_TYPE_ID));
                } else {
                    //PID Not Found
                    return AppConstants.UNKNOWN_ASSET;
                }
            }
        } catch (Exception e) {
            return AppConstants.UNKNOWN_ASSET;
        } finally {
            cursor.close();
        }
    }

    public ArrayList<String> getAllAssetTypeNamesForSearchSpinner() {
        ArrayList<String> searchogs = new ArrayList<String>();
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_ASSET_TYPE_MASTER;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                //AppConstants.ASSET_TYPE_SPLIT_DATA
                String assetname = cursor.getString(cursor.getColumnIndexOrThrow(K_ASSET_TYPE_NAME));
                String assetid = cursor.getString(cursor.getColumnIndexOrThrow(K_ASSET_TYPE_ID));
                searchogs.add(assetname+AppConstants.ASSET_TYPE_SPLIT_DATA+assetid);
            } while (cursor.moveToNext());
        }
        return searchogs;
    }





    public void deleteLocationMaster() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATION_MASTER, null, null);
        db.close();
    }

    public void storeLocationMaster(List<LocationMaster> lst) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO Location_Master_Table (K_LOCATION_ID,K_LOCATION_NAME) VALUES (? ,?)";
        //db.beginTransaction();
        db.beginTransactionNonExclusive();
        SQLiteStatement stmt = db.compileStatement(sql);
        try {
            for (int i = 0; i < lst.size(); i++) {
                stmt.bindString(1, lst.get(i).getLocationId());
                stmt.bindString(2, lst.get(i).getLocationName());
               // stmt.bindString(3, lst.get(i).getLocationRfid());
                //stmt.bindString(5, lst.get(i).getIsAssetActive());
                stmt.execute();
                stmt.clearBindings();
            }
            db.setTransactionSuccessful();
            // db.endTransaction();
        } catch (Exception e) {
            Log.e("LOCATIONMASTEREXC", e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    public int getLocationMasterCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT  " + K_LOCATION_ID + "," + " count(*) " + " FROM " + TABLE_LOCATION_MASTER;
        Cursor cursor = db.rawQuery(selectQuery, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                count = count + cursor.getInt(1);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return count;
    }

    public String getLocationNameByLocationId(String assetid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LOCATION_MASTER, new String[]{K_LOCATION_NAME}, K_LOCATION_ID + "='" + assetid + "'", null, null, null, null);
        System.out.println("Cursor" + cursor.getCount());
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return AppConstants.UNKNOWN_ASSET;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Note
                    cursor.moveToFirst();
                    return cursor.getString(cursor.getColumnIndexOrThrow(K_LOCATION_NAME));
                } else {
                    //PID Not Found
                    return AppConstants.UNKNOWN_ASSET;
                }
            }
        } catch (Exception e) {
            return AppConstants.UNKNOWN_ASSET;
        } finally {
            cursor.close();
        }
    }

    public String getLocationIdByLocationName(String assetName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LOCATION_MASTER, new String[]{K_LOCATION_ID}, K_LOCATION_NAME + "='" + assetName + "'", null, null, null, null);
        System.out.println("Cursor" + cursor.getCount());
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return AppConstants.UNKNOWN_ASSET;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Note
                    cursor.moveToFirst();
                    return cursor.getString(cursor.getColumnIndexOrThrow(K_LOCATION_ID));
                } else {
                    //PID Not Found
                    return AppConstants.UNKNOWN_ASSET;
                }
            }
        } catch (Exception e) {
            return AppConstants.UNKNOWN_ASSET;
        } finally {
            cursor.close();
        }
    }

    public ArrayList<String> getAllLocationsForSearchSpinner() {
        ArrayList<String> searchogs = new ArrayList<String>();
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATION_MASTER;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                //AppConstants.ASSET_TYPE_SPLIT_DATA
                String assetname = cursor.getString(cursor.getColumnIndexOrThrow(K_LOCATION_NAME));
                String assetid = cursor.getString(cursor.getColumnIndexOrThrow(K_LOCATION_ID));
                searchogs.add(assetname+AppConstants.ASSET_TYPE_SPLIT_DATA+assetid);
            } while (cursor.moveToNext());
        }
        return searchogs;
    }




    public void deleteRoomMaster() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ROOM_MASTER, null, null);
        db.close();
    }

    public void storeRoomMaster(List<RoomMaster> lst) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO Room_Master_Table (K_ROOM_ID,K_ROOM_NAME, K_ROOM_RFID) VALUES (? ,? ,?)";
        //db.beginTransaction();
        db.beginTransactionNonExclusive();
        SQLiteStatement stmt = db.compileStatement(sql);
        try {
            for (int i = 0; i < lst.size(); i++) {
                stmt.bindString(1, lst.get(i).getRoomId());
                stmt.bindString(2, lst.get(i).getRoomName());
                stmt.bindString(3, lst.get(i).getRoomRfid());
                //stmt.bindString(5, lst.get(i).getIsAssetActive());
                stmt.execute();
                stmt.clearBindings();
            }
            db.setTransactionSuccessful();
            // db.endTransaction();
        } catch (Exception e) {
            Log.e("ROOMMASTEREXC", e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    public int getRoomMasterCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT  " + K_ROOM_ID + "," + " count(*) " + " FROM " + TABLE_ROOM_MASTER;
        Cursor cursor = db.rawQuery(selectQuery, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                count = count + cursor.getInt(1);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return count;
    }


    public String getRoomDetailsByRfid(String rfid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ROOM_MASTER, new String[]{K_ROOM_ID,K_ROOM_NAME}, K_ROOM_RFID + "='" + rfid + "'", null, null, null, null);
        System.out.println("Cursor" + cursor.getCount());
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return AppConstants.UNKNOWN_ASSET;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Note
                    cursor.moveToFirst();
                    String roomid = cursor.getString(cursor.getColumnIndexOrThrow(K_ROOM_ID));
                    String roomname = cursor.getString(cursor.getColumnIndexOrThrow(K_ROOM_NAME));
                    String locationdetails =roomname+AppConstants.ASSET_TYPE_SPLIT_DATA+roomid;
                    return locationdetails;
                } else {
                    //PID Not Found
                    return AppConstants.UNKNOWN_ASSET;
                }
            }
        } catch (Exception e) {
            return AppConstants.UNKNOWN_ASSET;
        } finally {
            cursor.close();
        }
    }
    public ArrayList<String> getAllRoomsForSearchSpinner() {
        ArrayList<String> searchogs = new ArrayList<String>();
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_ROOM_MASTER;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                //AppConstants.ASSET_TYPE_SPLIT_DATA
                String assetname = cursor.getString(cursor.getColumnIndexOrThrow(K_ROOM_NAME));
                String assetid = cursor.getString(cursor.getColumnIndexOrThrow(K_ROOM_ID));
                String assetrfid = cursor.getString(cursor.getColumnIndexOrThrow(K_ROOM_RFID));
                searchogs.add(assetname+AppConstants.ASSET_TYPE_SPLIT_DATA+assetid+AppConstants.ASSET_TYPE_SPLIT_DATA+assetrfid);
            } while (cursor.moveToNext());
        }
        return searchogs;
    }

    public void deleteLostAssetMaster() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOST_ASSET_MASTER, null, null);
        db.close();
    }


    /*    private static final String K_LOST_ASSET_ID = "K_LOST_ASSET_ID";
    private static final String K_LOST_ASSET_TAG_ID = "K_LOST_ASSET_TAG_ID";*/
    public void storeLostAssetMaster(List<LostAssetMaster> lst) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO Lost_Asset_Master_Table (K_LOST_ASSET_ID,K_LOST_ASSET_TAG_ID) VALUES (? ,?)";
        //db.beginTransaction();
        db.beginTransactionNonExclusive();
        SQLiteStatement stmt = db.compileStatement(sql);
        try {
            for (int i = 0; i < lst.size(); i++) {
                stmt.bindString(1, lst.get(i).getLostAssetId());
                stmt.bindString(2, lst.get(i).getLostAssetRfid());
                stmt.execute();
                stmt.clearBindings();
            }
            db.setTransactionSuccessful();
            // db.endTransaction();
        } catch (Exception e) {
            Log.e("LOSTASSETMASTEREXC", e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    public int getLostAssetMasterCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT  " + K_LOST_ASSET_ID + "," + " count(*) " + " FROM " + TABLE_LOST_ASSET_MASTER;
        Cursor cursor = db.rawQuery(selectQuery, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                count = count + cursor.getInt(1);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return count;
    }

    public boolean isRfidPresentInLostAssetMaster(String rfid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LOST_ASSET_MASTER, new String[]{K_LOST_ASSET_TAG_ID}, K_LOST_ASSET_TAG_ID + "=?",
                new String[]{rfid}, null, null, null);
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Found
                    return true;
                } else {
                    //PID Not Found
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        } finally {
            cursor.close();
        }
    }

    public void deleteDashboardMenuMaster() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DASHBOARD_MENU, null, null);
        db.close();
    }


    /*private static final String K_DASHBOARD_MENU_ID = "K_DASHBOARD_MENU_ID";
    private static final String K_DASHBOARD_MENU_NAME = "K_DASHBOARD_MENU_NAME";
    private static final String K_DASHBOARD_MENU_IMAGE = "K_DASHBOARD_MENU_IMAGE";
    private static final String K_DASHBOARD_MENU_ACTIVE = "K_DASHBOARD_MENU_ACTIVE";*/
    public void storeDashboardMenuMaster(List<DashboardModel> lst) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO Dashboard_Menu_Table (K_DASHBOARD_MENU_ID,K_DASHBOARD_MENU_NAME,K_DASHBOARD_MENU_ACTIVITY_NAME,K_DASHBOARD_MENU_IMAGE,K_DASHBOARD_MENU_ACTIVE,K_DASHBOARD_MENU_SEQUENCE) VALUES (?,? ,?,?,?,?)";
        //db.beginTransaction();
        db.beginTransactionNonExclusive();
        SQLiteStatement stmt = db.compileStatement(sql);
        try {
            for (int i = 0; i < lst.size(); i++) {
                stmt.bindString(1, lst.get(i).getMenuId());
                stmt.bindString(2, lst.get(i).getMenuName());
                stmt.bindString(3, lst.get(i).getMenuActivityName());
                stmt.bindString(4, lst.get(i).getMenuimageName());
                stmt.bindString(5, lst.get(i).getIsMenuActive());
                stmt.bindString(6, lst.get(i).getMenuSequence());
                stmt.execute();
                stmt.clearBindings();
            }
            db.setTransactionSuccessful();
            // db.endTransaction();
        } catch (Exception e) {
            Log.e("DASHBOARDMENUMASTEREXC", e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    public String getMenuNameByMenuID(String menu_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DASHBOARD_MENU, new String[]{K_DASHBOARD_MENU_NAME}, K_DASHBOARD_MENU_ID + "='" + menu_id + "'", null, null, null, null);
        System.out.println("Cursor" + cursor.getCount());
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return AppConstants.UNKNOWN_ASSET;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Note
                    cursor.moveToFirst();
                    String menuname = cursor.getString(cursor.getColumnIndexOrThrow(K_DASHBOARD_MENU_NAME));
                    return menuname;
                } else {
                    //PID Not Found
                    return AppConstants.UNKNOWN_ASSET;
                }
            }
        } catch (Exception e) {
            return AppConstants.UNKNOWN_ASSET;
        } finally {
            cursor.close();
        }
    }
    public String getMenuActivityNameByMenuID(String menu_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DASHBOARD_MENU, new String[]{K_DASHBOARD_MENU_ACTIVITY_NAME}, K_DASHBOARD_MENU_ID + "='" + menu_id + "'", null, null, null, null);
        System.out.println("Cursor" + cursor.getCount());
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return AppConstants.UNKNOWN_ASSET;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Note
                    cursor.moveToFirst();
                    String menuname = cursor.getString(cursor.getColumnIndexOrThrow(K_DASHBOARD_MENU_ACTIVITY_NAME));
                    return menuname;
                } else {
                    //PID Not Found
                    return AppConstants.UNKNOWN_ASSET;
                }
            }
        } catch (Exception e) {
            return AppConstants.UNKNOWN_ASSET;
        } finally {
            cursor.close();
        }
    }
    public int getDashboardMenuCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT  " + K_DASHBOARD_MENU_ID + "," + " count(*) " + " FROM " + TABLE_DASHBOARD_MENU;
        Cursor cursor = db.rawQuery(selectQuery, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                count = count + cursor.getInt(1);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return count;
    }


    public List<DashboardModel> getDashboardMenuList() {
        List<DashboardModel> registeredEquipmentList = new ArrayList<DashboardModel>();
        SQLiteDatabase db = this.getWritableDatabase();
        //String selectQuery1 = "SELECT  * FROM " + TABLE_DASHBOARD_MENU + " WHERE K_DASHBOARD_MENU_ACTIVE = 'true' ORDER BY K_DASHBOARD_MENU_SEQUENCE ASC;";
        String selectQuery = "SELECT  * FROM " + TABLE_DASHBOARD_MENU + " ORDER BY K_DASHBOARD_MENU_SEQUENCE ASC;";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                DashboardModel dashboardModel = new DashboardModel();
                String id = cursor.getString(cursor.getColumnIndexOrThrow(K_DASHBOARD_MENU_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(K_DASHBOARD_MENU_NAME));
                String activityname = cursor.getString(cursor.getColumnIndexOrThrow(K_DASHBOARD_MENU_ACTIVITY_NAME));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(K_DASHBOARD_MENU_IMAGE));
                String active = cursor.getString(cursor.getColumnIndexOrThrow(K_DASHBOARD_MENU_ACTIVE));
                String sequence = cursor.getString(cursor.getColumnIndexOrThrow(K_DASHBOARD_MENU_SEQUENCE));
                dashboardModel.setMenuId(id);
                dashboardModel.setMenuName(name);
                dashboardModel.setMenuActivityName(activityname);
                dashboardModel.setMenuimageName(image);
                dashboardModel.setIsMenuActive(active);
                dashboardModel.setMenuSequence(sequence);
                registeredEquipmentList.add(dashboardModel);
                  } while (cursor.moveToNext());
        }
        return registeredEquipmentList;
    }

    public void deleteTagMaster() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TAG_MASTER, null, null);
        db.close();
    }
    public void deleteDispatchPalletData(String data) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null && db.isOpen()) {
            String whereClause = "KEY_JSON_DATA = ?";
            String[] whereArgs = {data};
            db.delete(TABLE_PARTIAL_DISPATCH, whereClause, whereArgs);
            db.close(); // Close the database connection after use
        }
    }
    public void deleteOfflineTagMaster() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_OFFLINE_TAG_MASTER, null, null);
        db.close();
    }
    public void deleteOfflineTagWithDestination() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_OFFLINE_TAG_WITH_DESTINATION, null, null);
        db.close();
    }

    public void deleteOfflineTagMasterForBatch(String batchId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Specify the WHERE clause and arguments based on batchId
        String whereClause = "K_BATCH_ID = ?";
        String[] whereArgs = {batchId};

        // Delete rows with the specified condition
        db.delete(TABLE_OFFLINE_TAG_MASTER, whereClause, whereArgs);

        // Close the database
        db.close();
    }

    public void deletePalletTag(String palletEpc) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Specify the WHERE clause and arguments based on batchId
        String whereClause = "K_EPC = ?";
        String[] whereArgs = {palletEpc};

        // Delete rows with the specified condition
        db.delete(TABLE_TAG_MASTER, whereClause, whereArgs);

        // Close the database
        db.close();
    }

    public void storeTagMaster(List<TagBean> lst) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransactionNonExclusive();

        try {
            for (TagBean tag : lst) {
                ContentValues values = new ContentValues();
                values.put(K_EPC, tag.getEpcId());
                values.put(K_RSSI, tag.getRssi());
                values.put(K_TIMES, tag.getTimes());
                values.put(K_ANTEANA, tag.getAntenna());
                values.put(K_ADDITIONAL_DATA, tag.getAdditionalData());
                values.put(K_TAG_TYPE, tag.getTagType());
                values.put(K_DATE_TIME, tag.getAddedDateTime());

                db.insertWithOnConflict(TABLE_TAG_MASTER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }
    public void saveDispatchPalletData(String jsonData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_JSON_DATA, jsonData);
        db.insert(TABLE_PARTIAL_DISPATCH, null, values);
        db.close();
    }
    public List<String> getAllDispatchPalletData() {
        List<String> dispatchPalletDataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()) {
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PARTIAL_DISPATCH, null);
            if (cursor.moveToFirst()) {
                do {
                    String jsonData = cursor.getString(cursor.getColumnIndex(KEY_JSON_DATA));
                    dispatchPalletDataList.add(jsonData);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return dispatchPalletDataList;
    }
    public void storeOfflineTagMaster(List<WorkOrderUploadTagBean> lst) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransactionNonExclusive();

        try {
            for (WorkOrderUploadTagBean tag : lst) {
                ContentValues values = new ContentValues();
                values.put(K_BATCH_ID, tag.getBatchId());
                values.put(K_EPC, tag.getEpcId());
                values.put(K_WORK_ORDER_NUMBER, tag.getWorkOrderNumber());
                values.put(K_WORK_ORDER_TYPE, tag.getWorkOrderType());
                values.put(K_RSSI, tag.getRssi());
                values.put(K_TIMES, tag.getTimes());
                values.put(K_ANTEANA, tag.getAntenna());
                values.put(K_ADDITIONAL_DATA, tag.getAdditionalData());
                values.put(K_TAG_TYPE, tag.getTagType());
                values.put(K_DATE_TIME, tag.getAddedDateTime());

                db.insertWithOnConflict(TABLE_OFFLINE_TAG_MASTER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }
    public void storeOfflineTagWithDestination(TagWithDestination tagWithDestination) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransactionNonExclusive();

        try {
                ContentValues values = new ContentValues();
                values.put(K_BATCH_ID, tagWithDestination.getBatchID());
                values.put(K_WORK_ORDER_NUMBER, tagWithDestination.getWorkOrderNo());
                values.put(K_EPC, tagWithDestination.getPalletTag());
                values.put(K_DESTINATION_EPC, tagWithDestination.getDestinationTag());
                values.put(K_WORK_ORDER_TYPE, tagWithDestination.getWorkOrderType());
                values.put(K_DATE_TIME, tagWithDestination.getDateTime());

                db.insertWithOnConflict(TABLE_OFFLINE_TAG_WITH_DESTINATION, null, values, SQLiteDatabase.CONFLICT_REPLACE);


            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }
    public long getTagMasterCount() {
        SQLiteDatabase db = this.getReadableDatabase(); // Use getReadableDatabase() instead of getWritableDatabase() since you are performing a read operation
        try {
            return DatabaseUtils.queryNumEntries(db, TABLE_TAG_MASTER);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean isPalletTagPresent() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Define the condition
            String condition = K_TAG_TYPE + " = ?";
            String[] selectionArgs = {typePallet};

            // Query the database
            cursor = db.query(
                    TABLE_TAG_MASTER,
                    null, // Columns (null means all columns)
                    condition,
                    selectionArgs,
                    null, // groupBy
                    null, // having
                    null // orderBy
            );

            // Check if there are any rows
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
            return false;
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }
    public TagBean getPalletTag() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        TagBean tag=null;
        try {
            // Query the database to get all rows, ordering by K_TAG_TYPE in descending order
            cursor = db.query(
                    TABLE_TAG_MASTER,
                    null, // Columns (null means all columns)
                    K_TAG_TYPE + "=?", // condition (use "?" as a placeholder)
                    new String[]{typePallet}, // selectionArgs (replace with the actual tag type value)
                    null, // groupBy
                    null, // having
                    K_TAG_TYPE + " DESC" // orderBy
            );

            // Check if there are any rows
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Use the constructor of TagBean to create an object
                    tag = new TagBean(
                            cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(K_RSSI)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(K_TIMES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_ANTEANA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_ADDITIONAL_DATA)),
                            //cursor.getString(cursor.getColumnIndexOrThrow(K_CATEGORY_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_TAG_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_DATE_TIME))
                    );

                    // Add the TagBean object to the list

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return tag;
    }
    public WorkOrderUploadTagBean getPalletTagForBatchId(String batchId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        WorkOrderUploadTagBean tag = null;

        try {
            // Construct the query using rawQuery for better control
            String query = "SELECT * FROM " + TABLE_OFFLINE_TAG_MASTER +
                    " WHERE " + K_TAG_TYPE + "=? AND " + K_BATCH_ID + "=? " +
                    " ORDER BY " + K_TAG_TYPE + " DESC";

            cursor = db.rawQuery(query, new String[]{typePallet, batchId});

            // Move cursor to the first row
            if (cursor != null && cursor.moveToFirst()) {
                // Extract data from the cursor and instantiate the TagBean object
                tag = new WorkOrderUploadTagBean(
                        cursor.getString(cursor.getColumnIndexOrThrow(K_BATCH_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(K_WORK_ORDER_NUMBER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(K_WORK_ORDER_TYPE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(K_RSSI)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(K_TIMES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(K_ANTEANA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(K_ADDITIONAL_DATA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(K_TAG_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(K_DATE_TIME))
                );
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return tag;
    }
    public TagBean getPalletTagForBatchId1(String batchId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        TagBean tag = null;
        try {
            // Query the database to get all rows, ordering by K_TAG_TYPE in descending order
            cursor = db.query(
                    TABLE_OFFLINE_TAG_MASTER,
                    null, // Columns (null means all columns)
                    K_TAG_TYPE + "=? AND " + K_BATCH_ID + "=?", // condition (use "?" as a placeholder)
                    new String[]{typePallet, batchId}, // selectionArgs (replace with the actual tag type and batchId values)
                    null, // groupBy
                    null, // having
                    K_TAG_TYPE + " DESC" // orderBy
            );

            // Rest of your code remains unchanged
            // ...

        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return tag;
    }



    public boolean isEpcPresent(String epc) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Define the condition
            String condition = K_EPC + " = ?";
            String[] selectionArgs = {epc};

            // Query the database
            cursor = db.query(
                    TABLE_TAG_MASTER,
                    null, // Columns (null means all columns)
                    condition,
                    selectionArgs,
                    null, // groupBy
                    null, // having
                    null // orderBy
            );

            // Check if there are any rows
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
            return false;
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public String getAddedDateTimeForEPC(String epc) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Define the condition
            String condition = K_EPC + " = ?";
            String[] selectionArgs = {epc};

            // Query the database
            cursor = db.query(
                    TABLE_TAG_MASTER,
                    new String[]{K_DATE_TIME},
                    condition,
                    selectionArgs,
                    null, // groupBy
                    null, // having
                    null // orderBy
            );

            // Check if there is a result
            if (cursor != null && cursor.moveToFirst()) {
                // Get the addedDateTime value from the cursor
                int columnIndex = cursor.getColumnIndexOrThrow(K_DATE_TIME);
                return cursor.getString(columnIndex);
            } else {
                // Return a default value or handle the case where no matching record is found
                return "";
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
            return "";
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }


    public String getAddedDateTimeForPalletEPC() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Define the condition
            String condition = K_TAG_TYPE + " = ?";
            String[] selectionArgs = {typePallet};

            // Query the database
            cursor = db.query(
                    TABLE_TAG_MASTER,
                    new String[]{K_DATE_TIME},
                    condition,
                    selectionArgs,
                    null, // groupBy
                    null, // having
                    null // orderBy
            );

            // Check if there is a result
            if (cursor != null && cursor.moveToFirst()) {
                // Get the addedDateTime value from the cursor
                int columnIndex = cursor.getColumnIndexOrThrow(K_DATE_TIME);
                return cursor.getString(columnIndex);
            } else {
                // Return a default value or handle the case where no matching record is found
                return "";
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
            return "";
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }
    public String getRSSIPalletEPC() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Define the condition
            String condition = K_TAG_TYPE + " = ?";
            String[] selectionArgs = {typePallet};

            // Query the database
            cursor = db.query(
                    TABLE_TAG_MASTER,
                    new String[]{K_RSSI},
                    condition,
                    selectionArgs,
                    null, // groupBy
                    null, // having
                    null // orderBy
            );

            // Check if there is a result
            if (cursor != null && cursor.moveToFirst()) {
                // Get the addedDateTime value from the cursor
                int columnIndex = cursor.getColumnIndexOrThrow(K_RSSI);
                return cursor.getString(columnIndex);
            } else {
                // Return a default value or handle the case where no matching record is found
                return "-100";
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
            return "-100";
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public List<TagBean> getAllTagData() {
        List<TagBean> tagList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query the database to get all rows, ordering by K_TAG_TYPE in descending order
            cursor = db.query(
                    TABLE_TAG_MASTER,
                    null, // Columns (null means all columns)
                    null, // condition (null means no condition)
                    null, // selectionArgs (null means no arguments)
                    null, // groupBy
                    null, // having
                    K_TAG_TYPE + " DESC" // orderBy
            );

            // Check if there are any rows
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Use the constructor of TagBean to create an object
                    TagBean tag = new TagBean(
                            cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(K_RSSI)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(K_TIMES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_ANTEANA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_ADDITIONAL_DATA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_TAG_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_DATE_TIME))
                    );

                    // Add the TagBean object to the list
                    tagList.add(tag);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return tagList;
    }

    public List<WorkOrderUploadTagBean> getAllTagDataForBatch(String batchId) {
        List<WorkOrderUploadTagBean> tagList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Specify the condition to retrieve data for a specific batchId
            String selection = K_BATCH_ID + "=?";
            String[] selectionArgs = {batchId};

            // Query the database with the specified condition, ordering by K_TAG_TYPE in descending order
            cursor = db.query(
                    TABLE_OFFLINE_TAG_MASTER,
                    null, // Columns (null means all columns)
                    selection, // condition
                    selectionArgs, // selectionArgs
                    null, // groupBy
                    null, // having
                    K_TAG_TYPE + " DESC" // orderBy
            );

            // Check if there are any rows
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Use the constructor of TagBean to create an object
                    WorkOrderUploadTagBean tag = new WorkOrderUploadTagBean(
                            cursor.getString(cursor.getColumnIndexOrThrow(K_BATCH_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_WORK_ORDER_NUMBER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_WORK_ORDER_TYPE)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(K_RSSI)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(K_TIMES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_ANTEANA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_ADDITIONAL_DATA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_TAG_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_DATE_TIME))
                    );

                    // Add the TagBean object to the list
                    tagList.add(tag);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return tagList;
    }


    public String getTopBatchId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String topBatchId = null;

        try {
            // Query the database to get the top row, ordering by K_TAG_TYPE in descending order
            cursor = db.query(
                    TABLE_OFFLINE_TAG_MASTER,
                    new String[]{K_BATCH_ID}, // Columns to retrieve (only batch ID)
                    null, // condition (null means no condition)
                    null, // selectionArgs (null means no arguments)
                    null, // groupBy
                    null, // having
                    K_TAG_TYPE + " DESC", // orderBy
                    "1" // limit to 1 row
            );

            // Check if there is a row
            if (cursor != null && cursor.moveToFirst()) {
                // Retrieve the batch ID
                topBatchId = cursor.getString(cursor.getColumnIndexOrThrow(K_BATCH_ID));
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return topBatchId;
    }
    public long getOfflineTagMasterCount() {
        SQLiteDatabase db = this.getReadableDatabase(); // Use getReadableDatabase() instead of getWritableDatabase() since you are performing a read operation
        try {
            return DatabaseUtils.queryNumEntries(db, TABLE_OFFLINE_TAG_MASTER);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
    public List<WorkOrderUploadTagBean> getAllPalletTags() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        List<WorkOrderUploadTagBean> tags = new ArrayList<>();

        try {
            // Construct the query to select all rows from the table
            String query = "SELECT * FROM " + TABLE_OFFLINE_TAG_MASTER;
            cursor = db.rawQuery(query, null);

            // Iterate through the cursor to get each row
            if (cursor != null && cursor.moveToNext()) {
                do {
                    WorkOrderUploadTagBean tag = new WorkOrderUploadTagBean(
                            cursor.getString(cursor.getColumnIndexOrThrow(K_BATCH_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_WORK_ORDER_NUMBER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_WORK_ORDER_TYPE)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(K_RSSI)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(K_TIMES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_ANTEANA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_ADDITIONAL_DATA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_TAG_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_DATE_TIME))
                    );
                    tags.add(tag);
                } while (cursor.moveToNext());
            }
            } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return tags;
    }
    public boolean isEpcPresentInOffline(String epc) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Define the condition
            String condition = K_EPC + " = ?";
            String[] selectionArgs = {epc};

            // Query the database
            cursor = db.query(
                    TABLE_OFFLINE_TAG_MASTER,
                    null, // Columns (null means all columns)
                    condition,
                    selectionArgs,
                    null, // groupBy
                    null, // having
                    null // orderBy
            );

            // Check if there are any rows
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
            return false;
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }
    public TagWithDestination getTagWithDestinationByPalletTagId(String batchID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        TagWithDestination tagWithDestination = null;
try{
    String query = "SELECT * FROM " + TABLE_OFFLINE_TAG_WITH_DESTINATION +
            " WHERE " + K_BATCH_ID + "=? " +
            " ORDER BY " + K_DATE_TIME + " ASC";

    cursor = db.rawQuery(query, new String[]{batchID});
    if (cursor != null && cursor.moveToFirst()) {
        tagWithDestination = new TagWithDestination(
                cursor.getString(cursor.getColumnIndexOrThrow(K_BATCH_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(K_WORK_ORDER_NUMBER)),
                cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                cursor.getString(cursor.getColumnIndexOrThrow(K_DESTINATION_EPC)),
                cursor.getString(cursor.getColumnIndexOrThrow(K_WORK_ORDER_TYPE)),
                cursor.getString(cursor.getColumnIndexOrThrow(K_DATE_TIME))
        );
    }
} catch (Exception e){
    Log.e("ASSETMASTEREXC", e.getMessage());
} finally {
    // Close the cursor and database
    if (cursor != null) {
        cursor.close();
    }
    db.close();
}
        return tagWithDestination;
    }
    public List<TagWithDestination> getAllTagWithDestinationDataForPallet(String batchId) {
        List<TagWithDestination> tagList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Specify the condition to retrieve data for a specific batchId
            String selection = K_BATCH_ID + "=?";
            String[] selectionArgs = {batchId};

            // Query the database with the specified condition, ordering by K_DATE_TIME in ascending order
            cursor = db.query(
                    TABLE_OFFLINE_TAG_WITH_DESTINATION,
                    null, // Columns (null means all columns)
                    selection, // condition
                    selectionArgs, // selectionArgs
                    null, // groupBy
                    null,
                    K_DATE_TIME+ " ASC"
            );

            // Check if there are any rows
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Use the constructor of TagBean to create an object
                    TagWithDestination tag = new TagWithDestination(
                            cursor.getString(cursor.getColumnIndexOrThrow(K_BATCH_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_WORK_ORDER_NUMBER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_EPC)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_DESTINATION_EPC)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_WORK_ORDER_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(K_DATE_TIME))

                    );
                    // Add the TagBean object to the list
                    tagList.add(tag);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return tagList;
    }
    public String getTopBatch() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String topBatchId = null;

        try {
            // Query the database to get the top row, ordering by K_DATE_TIME in ascending order
            cursor = db.query(
                    TABLE_OFFLINE_TAG_WITH_DESTINATION,
                    new String[]{K_BATCH_ID}, // Columns to retrieve (only batch ID)
                    null, // condition (null means no condition)
                    null, // selectionArgs (null means no arguments)
                    null, // groupBy
                    null, // having
                    K_DATE_TIME + " ASC", // orderBy
                    "1" // limit to 1 row
            );

            // Check if there is a row
            if (cursor != null && cursor.moveToFirst()) {
                // Retrieve the batch ID
                topBatchId = cursor.getString(cursor.getColumnIndexOrThrow(K_BATCH_ID));
            }
        } catch (Exception e) {
            Log.e("ASSETMASTEREXC", e.getMessage());
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return topBatchId;
    }
    public long getOfflineTagWithDestinationCount() {
        SQLiteDatabase db = this.getReadableDatabase(); // Use getReadableDatabase() instead of getWritableDatabase() since you are performing a read operation
        try {
            return DatabaseUtils.queryNumEntries(db, TABLE_OFFLINE_TAG_WITH_DESTINATION);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
    public boolean checkAssetNameByProductTagId(String tagId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT 1 FROM " + TABLE_PRODUCT_MASTER +
                " WHERE " + K_PRODUCT_TAG_ID + " =? AND " + K_PRODUCT_NAME + " BETWEEN 'P2051' AND 'P2100' AND " + K_PRODUCT_NAME + " NOT IN ('P206', 'P207','P208', 'P209','P21','P210')";
        Cursor cursor = db.rawQuery(query, new String[]{tagId});
        try {
            return cursor.getCount() > 0;
        } catch (Exception e) {
            return false;
        } finally {
            cursor.close();
        }
    }
    public ArrayList<String> getBinName(){
//        ArrayList<String> binDetails = new ArrayList<String>();
//        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT "+K_PRODUCT_NAME+" FROM " + TABLE_PRODUCT_MASTER+" WHERE "+K_PRODUCT_TAG_ID+" LIKE '1403%' ORDER BY "+K_PRODUCT_NAME+" ASC" ;
//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        if (cursor.moveToFirst()) {
//            do {
//                //AppConstants.ASSET_TYPE_SPLIT_DATA
//                String binName = cursor.getString(cursor.getColumnIndexOrThrow(K_PRODUCT_NAME));
//                binDetails.add(binName);
//            } while (cursor.moveToNext());
//        }
        return getStringList(selectQuery, K_PRODUCT_NAME);
    }

    public ArrayList<String> getStringList(String sql, String columnName){
        ArrayList<String> list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                String value = cursor.getString(cursor.getColumnIndexOrThrow(columnName));
                list.add(value);
            } while (cursor.moveToNext());
        }
        return list;
    }
    public void deleteSKUMaster() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SKU_MASTER, null, null);
        db.close();
    }

    public void storeSKUMaster(List<SKUMaster> lst) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO sku_master (K_ITEM_NAME,K_ITEM_CODE) VALUES (? ,?)";
        db.beginTransactionNonExclusive();
        SQLiteStatement stmt = db.compileStatement(sql);
        try {
            for (int i = 0; i < lst.size(); i++) {
                stmt.bindString(1, ""+lst.get(i).getItemName());
                stmt.bindString(2, lst.get(i).getItemCode());
                stmt.execute();
                stmt.clearBindings();
            }
            db.setTransactionSuccessful();
            // db.endTransaction();
        } catch (Exception e) {
            Log.e("SKUMASTEREXC", e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }
    public boolean isSKUExist(String skuCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Define the condition
            String condition = K_ITEM_CODE + " = ?";
            String[] selectionArgs = {skuCode};

            // Query the database
            cursor = db.query(
                    TABLE_SKU_MASTER,
                    null, // Columns (null means all columns)
                    condition,
                    selectionArgs,
                    null, // groupBy
                    null, // having
                    null // orderBy
            );

            // Check if there are any rows
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("SKUMASTEREXC", e.getMessage());
            return false;
        } finally {
            // Close the cursor and database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }
    public String getItemNameByItemCode(String skuCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SKU_MASTER, new String[]{K_ITEM_NAME}, K_ITEM_CODE + "='" + skuCode + "'", null, null, null, null);
        System.out.println("Cursor" + cursor.getCount());
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return AppConstants.UNKNOWN_ASSET;
            } else {
                if (cursor.getCount() > 0) {
                    //PID Note
                    cursor.moveToFirst();
                    return cursor.getString(cursor.getColumnIndexOrThrow(K_ITEM_NAME));
                } else {
                    //PID Not Found
                    return AppConstants.UNKNOWN_ASSET;
                }
            }
        } catch (Exception e) {
            return AppConstants.UNKNOWN_ASSET;
        } finally {
            cursor.close();
        }
    }
}
