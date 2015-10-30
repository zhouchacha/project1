package com.uestc.Indoorguider;

import java.util.Map;

import android.app.Application;
import android.content.Context;



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
	    return IGManager.getUserName();
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
		IGManager.setUserName(username);
	}

	
	public void setPassword(String password) {
		IGManager.setPassword(password);
	}

	/**
	 * �˳���¼,�������
	 */
	public void logout(final EMCallBack emCallBack) {
		// �ȵ���sdk logout��������app���Լ�������
		IGManager.logout(emCallBack);
	}
}

