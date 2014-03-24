package com.zjy.headsup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AssetsDatabaseManager {
	
	private static String databasePath = "/data/data/%s/databases";
	private Map<String, SQLiteDatabase> databaseTable = new HashMap<String, SQLiteDatabase>();
	private Context context = null;
	private static AssetsDatabaseManager mInstance = null;
	
	public static void initManager(Context context) {
		if (mInstance == null) {
			mInstance = new AssetsDatabaseManager(context);
		}
	}
	
	public static AssetsDatabaseManager getManager() {
		return mInstance;
	}
	
	private AssetsDatabaseManager(Context context) {
		this.context = context;
	}
	
	public SQLiteDatabase getDatabase(String dbFileName) {
		if(databaseTable.get(dbFileName) != null) {
			return (SQLiteDatabase) databaseTable.get(dbFileName);
		}
		if (context == null) {
			return null;
		}
		
		String copiedDbFilePath = getDatabaseFilePath();
		String copiedDbFileName = getDatabaseFile(dbFileName);
		
		File file = new File(copiedDbFileName);
		SharedPreferences dbs = context.getSharedPreferences(AssetsDatabaseManager.class.toString(), 0);
		boolean flag = dbs.getBoolean(dbFileName, false);
		if (!flag || !file.exists()) {
			file = new File(copiedDbFilePath);
			if (!file.exists() && !file.mkdirs()) {
				Log.i("AssetsDatabase", "Create \"" + copiedDbFilePath + "\" fail!");
				return null;
			}
			if (!copyAssetsToFilesystem(dbFileName, copiedDbFileName)) {
				Log.i("AssetsDatabase", String.format("Copy %s to %s fail!", dbFileName, copiedDbFileName));
			}
			
			dbs.edit().putBoolean(dbFileName, true).commit();
		}
		
		SQLiteDatabase db = SQLiteDatabase.openDatabase(copiedDbFileName, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		if (db != null) {
			databaseTable.put(dbFileName, db);
		}
		return db;
	}
	
	private String getDatabaseFilePath(){  
		return String.format(databasePath, context.getApplicationInfo().packageName); 
	}
	
	private String getDatabaseFile(String dbFileName){
		return getDatabaseFilePath() + "/" + dbFileName;
	}
	
	private boolean copyAssetsToFilesystem(String assetsSrc, String des){
		InputStream istream = null;
		OutputStream ostream = null;  
        try{  
            AssetManager am = context.getAssets();  
            istream = am.open(assetsSrc);  
            ostream = new FileOutputStream(des);  
            byte[] buffer = new byte[1024];  
            int length;  
            while ((length = istream.read(buffer))>0){  
                ostream.write(buffer, 0, length);  
            }  
            istream.close();  
            ostream.close();  
        } catch(Exception e){  
            e.printStackTrace();  
            try{  
                if(istream!=null)  
                    istream.close();  
                if(ostream!=null)  
                    ostream.close();  
            }  
            catch(Exception ee){  
                ee.printStackTrace();  
            }  
            return false;  
        }  
        return true;
	}
	
	public boolean closeDatabase(String dbFileName){  
        if(databaseTable.get(dbFileName) != null){  
            SQLiteDatabase db = (SQLiteDatabase) databaseTable.get(dbFileName);  
            db.close();  
            databaseTable.remove(dbFileName);  
            return true;  
        }  
        return false;
	}
	
	static public void closeAllDatabase(){  
        if(mInstance != null){  
            for(int i=0; i<mInstance.databaseTable.size(); ++i){  
                if(mInstance.databaseTable.get(i)!=null){  
                    mInstance.databaseTable.get(i).close();  
                }  
            }  
            mInstance.databaseTable.clear();  
        }  
    }  
}
