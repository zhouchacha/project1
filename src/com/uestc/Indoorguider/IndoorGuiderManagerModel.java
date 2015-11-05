package com.uestc.Indoorguider;
/**
 * manager the user data and preference
 */
public abstract class IndoorGuiderManagerModel {
	public abstract String getUsername();
	public abstract String getPassword();
	public abstract boolean setUsername(String username);
	public abstract boolean setPassword(String password);
    // �����½��Ϣ
    public abstract boolean saveAlreadyLogin(boolean alreadyLogin);
    // ��ȡ��½��Ϣ
    public abstract boolean getAlreadyLogin();
    public abstract void login(String username, String password);
    public abstract void logout();
    
	
	
	
	
	

}
