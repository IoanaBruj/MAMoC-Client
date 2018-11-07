package uk.ac.st_andrews.cs.mamoc_client.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import uk.ac.st_andrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.st_andrews.cs.mamoc_client.Model.OffloadedTask;

import static uk.ac.st_andrews.cs.mamoc_client.DB.DBHelper.TABLE_MOBILE_DEVICES;
import static uk.ac.st_andrews.cs.mamoc_client.DB.DBHelper.TABLE_OFFLOAD;

public class DBAdapter {
    private static DBAdapter instance;
    private static Object lockObject = new Object();
    private Context context;

    private SQLiteDatabase db = null;

    private DBAdapter(Context context) {
        this.context = context;
        DBHelper dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public static DBAdapter getInstance(Context context) {
        if (instance == null) {
            synchronized (DBAdapter.class) {
                if (instance == null) {
                    instance = new DBAdapter(context);
                }
            }
        }
        return instance;
    }

    public long addTaskOffload(OffloadedTask task){

        if (task == null) { return -1; }

        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_APP_NAME, context.getPackageName());
        values.put(DBHelper.COL_APP_NAME, task.getTaskName());
        values.put(DBHelper.COL_EXEC_LOCATION, task.getExecLocation().getValue());
        values.put(DBHelper.COL_COMMUNICATION_OVERHEAD, task.getCommOverhead());
        values.put(DBHelper.COL_NETWORK_TYPE, task.getNetworkType().getValue());
        values.put(DBHelper.COL_RTT_SPEED, task.getRttSpeed());
        values.put(DBHelper.COL_OFFLOAD_DATE, task.getOffloadedDate());

        return db.insert(TABLE_OFFLOAD, null, values);
    }


    public long addMobileDevice(MobileNode device) {
        if (device == null || device.getIp() == null || device.getPort() == 0) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_DEV_IP, device.getIp());
        values.put(DBHelper.COL_DEV_NAME, device.getDeviceID());
        values.put(DBHelper.COL_DEV_CPU_FREQ, device.getCpuFreq());
        values.put(DBHelper.COL_DEV_CPU_NUM, device.getNumberOfCPUs());
        values.put(DBHelper.COL_DEV_MEMORY, device.getMemoryMB());
        values.put(DBHelper.COL_DEV_JOINED, device.getJoinedDate());
        values.put(DBHelper.COL_DEV_BATTERY_LEVEL, device.getBatteryLevel());
        values.put(DBHelper.COL_DEV_BATTERY_STATE, device.getBatteryState().name());
        values.put(DBHelper.COL_OFFLOADING_SCORE, device.getOffloadingScore());

        if (!deviceExists(device.getIp())) {
            return db.insert(TABLE_MOBILE_DEVICES, null, values);
        }

        return -1;
    }

    public boolean removeMobileDevice(MobileNode device) {
        int rowsAffected = db.delete(TABLE_MOBILE_DEVICES, DBHelper.COL_DEV_IP + "=?"
                , new String[]{device.getIp()});
        return (rowsAffected > 0);
    }

    public MobileNode getMobileDevice(String senderIP) {
        MobileNode device = null;

        Cursor cursor = db.query(TABLE_MOBILE_DEVICES, null, DBHelper.COL_DEV_IP + "=?",
                new String[]{senderIP}, null, null, DBHelper.COL_DEV_ID);

        if (cursor != null) {
            device = new MobileNode(context);
        } else {
            return device;
        }

        int idIndex = cursor.getColumnIndex(DBHelper.COL_DEV_ID);
        int ipIndex = cursor.getColumnIndex(DBHelper.COL_DEV_IP);
        int cpuNumIndex = cursor.getColumnIndex(DBHelper.COL_DEV_CPU_NUM);
        int cpuFreqIndex = cursor.getColumnIndex(DBHelper.COL_DEV_CPU_FREQ);
        int memIndex = cursor.getColumnIndex(DBHelper.COL_DEV_MEMORY);
        int joinedIndex = cursor.getColumnIndex(DBHelper.COL_DEV_JOINED);
        int blIndex = cursor.getColumnIndex(DBHelper.COL_DEV_BATTERY_LEVEL);
        int bsIndex = cursor.getColumnIndex(DBHelper.COL_DEV_BATTERY_STATE);
        int osIndex = cursor.getColumnIndex(DBHelper.COL_OFFLOADING_SCORE);

        while (cursor.moveToNext()) {
            device.setIp(cursor.getString(ipIndex));
            device.setDeviceID(cursor.getString(idIndex));
            device.setCpuFreq(Integer.parseInt(cursor.getString(cpuFreqIndex)));
            device.setNumberOfCPUs(Integer.parseInt(cursor.getString(cpuNumIndex)));
            device.setMemoryMB(Long.parseLong(cursor.getString(memIndex)));
            device.setJoinedDate(Long.parseLong(cursor.getString(joinedIndex)));
            device.setBatteryLevel(Integer.parseInt(cursor.getString(blIndex)));
            device.setBatteryState(cursor.getString(bsIndex));
            device.setOffloadingScore(cursor.getInt(osIndex));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return device;
    }

    public ArrayList<MobileNode> getMobileDevicesList() {
        ArrayList<MobileNode> devices = null;

        Cursor cursor = db.query(TABLE_MOBILE_DEVICES, null, null, null, null, null,
                DBHelper.COL_DEV_ID);

        if (cursor != null) {
            devices = new ArrayList<>();
        } else {
            return devices;
        }

        int idIndex = cursor.getColumnIndex(DBHelper.COL_DEV_ID);
        int ipIndex = cursor.getColumnIndex(DBHelper.COL_DEV_IP);
        int cpuNumIndex = cursor.getColumnIndex(DBHelper.COL_DEV_CPU_NUM);
        int cpuFreqIndex = cursor.getColumnIndex(DBHelper.COL_DEV_CPU_FREQ);
        int memIndex = cursor.getColumnIndex(DBHelper.COL_DEV_MEMORY);
        int joinedIndex = cursor.getColumnIndex(DBHelper.COL_DEV_JOINED);
        int blIndex = cursor.getColumnIndex(DBHelper.COL_DEV_BATTERY_LEVEL);
        int bsIndex = cursor.getColumnIndex(DBHelper.COL_DEV_BATTERY_STATE);
        int osIndex = cursor.getColumnIndex(DBHelper.COL_OFFLOADING_SCORE);

        while (cursor.moveToNext()) {
            MobileNode device = new MobileNode(context);

            device.setIp(cursor.getString(ipIndex));
            device.setDeviceID(cursor.getString(idIndex));
            device.setCpuFreq(Integer.parseInt(cursor.getString(cpuFreqIndex)));
            device.setNumberOfCPUs(Integer.parseInt(cursor.getString(cpuNumIndex)));
            device.setMemoryMB(Long.parseLong(cursor.getString(memIndex)));
            device.setJoinedDate(Long.parseLong(cursor.getString(joinedIndex)));
            device.setBatteryLevel(Integer.parseInt(cursor.getString(blIndex)));
            device.setBatteryState(cursor.getString(bsIndex));
            device.setOffloadingScore(cursor.getInt(osIndex));

            devices.add(device);
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return devices;
    }

    private boolean deviceExists(String ip) {
        Cursor cursor = db.query(TABLE_MOBILE_DEVICES, null, DBHelper.COL_DEV_IP + "=?", new
                String[]{ip}, null, null, null);

        return (cursor.getCount() > 0);
    }

    public int clearDatabase() {
        int rowsAffected = db.delete(TABLE_MOBILE_DEVICES, null, null);
        return rowsAffected;
    }
}
