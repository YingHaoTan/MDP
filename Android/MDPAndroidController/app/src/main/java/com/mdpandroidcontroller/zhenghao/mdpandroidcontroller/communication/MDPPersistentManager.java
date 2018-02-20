package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.communication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ernes on 19/2/2018.
 */

public class MDPPersistentManager extends Activity{
    private static final String PERSIST_NAME = "persistname";

    private static final String DEFAULT_VALUE = "hello";

    public static final String PERSIST_1 = "persist1";
    public static final String PERSIST_2 = "persist2";

    private static MDPPersistentManager instance = null;
    private static SharedPreferences sp = null;

    public MDPPersistentManager(){
        // prevent initializing outside
    }

    public static MDPPersistentManager getInstance(Context context){
        if(instance == null){
            instance = new MDPPersistentManager();
            sp = context.getSharedPreferences(PERSIST_NAME, 0);
        }

        return instance;
    }

    public String getPersistString(String name){
        String message = sp.getString(name, null);

        if(message == null){
            message = DEFAULT_VALUE;
            savePersistString(name,message);
        }

        return message;
    }

    public void savePersistString(String name, String value){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(name , value);
        editor.commit();
    }

}
