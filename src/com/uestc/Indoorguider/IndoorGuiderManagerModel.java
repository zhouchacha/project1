package com.uestc.Indoorguider;
/**
 * manager the user data and preference
 */
public abstract class IndoorGuiderManagerModel {
	public abstract String getUserName();
	public abstract String getPassword();
	public abstract boolean setUserName(String username);
	public abstract boolean setPassword(String password);
    // �����½��Ϣ
    public abstract boolean saveAlreadyLogin(boolean alreadyLogin);
    // ��ȡ��½��Ϣ
    public abstract boolean getAlreadyLogin();
	
	
	
	
	
	

}
