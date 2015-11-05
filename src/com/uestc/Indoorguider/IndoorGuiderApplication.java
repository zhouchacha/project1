package com.uestc.Indoorguider;

import java.util.Map;

import com.uestc.Indoorguider.orientation.OrientationTool;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;



public class IndoorGuiderApplication extends Application {
	private static IndoorGuiderApplication instance;
	// login user name
	public final String PREF_USERNAME = "username";
	public static IndoorGuiderManagerModel IGManager = null;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		IGManager = new IndoorGuiderManager(this);
		//��������
	    Intent intent = new Intent();
	    intent.setAction("com.uestc.Indoorguider.util.UtilService");
	    startService(intent);
	}

	public static IndoorGuiderApplication getInstance() {
		return instance;
	}

	
	/**
	 * ��ȡ��ǰ��½�û���
	 *
	 * @return
	 */
	public String getUserName() {
	    return IGManager.getUsername();
	}

	/**
	 * ��ȡ����
	 *
	 * @return
	 */
	public String getPassword() {
		return IGManager.getPassword();
	}

	/**
	 * �����û���
	 *
	 * @param user
	 */
	public void setUserName(String username) {
		IGManager.setUsername(username);
	}

	
	public void setPassword(String password) {
		IGManager.setPassword(password);
	}

	
	public boolean getAlreadyLogin(){
		return IGManager.getAlreadyLogin();
	}
	
	
	public boolean saveAlreadyLogin(boolean alreadyLogin ){
		return IGManager.saveAlreadyLogin(alreadyLogin);
	}
	
	
	/**
	 * �˳���¼,�������
	 */
	public void logout() {
		// �ȵ���sdk logout��������app���Լ�������
		IGManager.logout();
	}
	
}

