package com.uestc.Indoorguider;

import com.easemob.EMCallBack;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class IndoorGuiderManager extends IndoorGuiderManagerModel {
	 private static final String PREF_USERNAME = "username";
	 private static final String PREF_PWD = "pwd";
	 private static final String PREF_ALREADY_LOGIN = "already_login";
	 private Context context = null;
	 private static IndoorGuiderManager me;
	 public IndoorGuiderManager(Context context){
		 this.context = context;
		 me = this;
	 }
	 
	 public static IndoorGuiderManager getInstance()
	 {
		 return me;
	 }
	 
	 
	 @Override
	public boolean setUserName(String username) {
		// TODO Auto-generated method stub
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.edit().putString(PREF_USERNAME, username).commit();
	
	}
	@Override
	public String getUserName() {
		// TODO Auto-generated method stub
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_USERNAME, "unkonwname");
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_PWD, "unknowpsw");
	}
	@Override
	public boolean setPassword(String password) {
		// TODO Auto-generated method stub
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.edit().putString(PREF_PWD, password).commit();
	}
	@Override
	public boolean saveAlreadyLogin(boolean alreadyLogin) {
		// TODO Auto-generated method stub
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.edit().putBoolean(PREF_ALREADY_LOGIN, alreadyLogin).commit();
	}

	@Override
	public boolean getAlreadyLogin() {
		// TODO Auto-generated method stub
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean(PREF_ALREADY_LOGIN, false);
	}
	
	
	public void logout(final CallBack emCallBack){
		
		
	}

}
