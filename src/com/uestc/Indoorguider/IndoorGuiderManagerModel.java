package com.uestc.Indoorguider;
/**
 * manager the user data and preference
 */
public abstract class IndoorGuiderManagerModel {
	public abstract String getUserName();
	public abstract String getPassword();
	public abstract boolean setUserName(String username);
	public abstract boolean setPassword(String password);
    // 保存登陆信息
    public abstract boolean saveAlreadyLogin(boolean alreadyLogin);
    // 获取登陆信息
    public abstract boolean getAlreadyLogin();
	
	
	
	
	
	

}
