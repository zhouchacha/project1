package com.uestc.Indoorguider.network;

import com.uestc.Indoorguider.util.ClientAgent;
import com.uestc.Indoorguider.util.ConnectTool;
import com.uestc.Indoorguider.util.SendToServerThread;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class NetworkStateBroadcastReceiver extends BroadcastReceiver {
	//private Boolean flag ;
	private WifiManager wifiManager;
	String Tag = "networkBroadcast";
    public NetworkStateBroadcastReceiver(WifiManager wifiManager)
	{
	   // this.flag =  ClientAgent.flag ;
		this.wifiManager = wifiManager;
		
	}
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager=
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null)
        {
        	//�ֻ�wifi
        	NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();  
        	if(infos!= null)
        	{
        		for(NetworkInfo ni : infos){
        			//wifi��������������
            		if(ni.getTypeName().equals("WIFI") && ni.isConnected()){      			
            			 Log.i(Tag,"wifi ���ã�");
            		}
        	     }
        	}
        	//û����������
	        NetworkInfo mNetworkInfo = connectivityManager.getActiveNetworkInfo(); 
	    	if (mNetworkInfo == null || !mNetworkInfo.isAvailable() )
	        	{
		    		String hint = "have no available network";
		    		Toast.makeText(context, hint, Toast.LENGTH_LONG).show();
	    		  //have no available network
		    		Log.i(Tag,hint);
	        		return;
	        	}
	        	
	       
	         NetworkInfo WiFiInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	            if(WiFiInfo != null)
	            {
	            	if(WiFiInfo.isAvailable())//��wifi�����Ƿ����
	            	{
	            		Log.i(Tag,"ʹ�� wifi ");
	            		String hint = "ʹ�� wifi";
			    		Toast.makeText(context, hint, Toast.LENGTH_LONG).show();
	            		if(ClientAgent.flag == false)
	            		{
	            			ConnectTool.startConnectThreads(context, wifiManager);
	            		}
	            		return;
	            	}
	            	
	            }
	         NetworkInfo MobileInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	            if(MobileInfo != null)
	            {
	            	if(MobileInfo.isAvailable())
	            	{
	            		String hint = "mobile";
			    		Toast.makeText(context, hint, Toast.LENGTH_LONG).show();
			    		Log.i(Tag,"ʹ��  mobile ");
	            		//notice user and request location
	            		if(ClientAgent.flag == false)
	            		{
	            			ConnectTool.startConnectThreads(context,wifiManager);
	            			
	            		}
	            		return;
	            		
	            	}
	            	
	            }
	           // no useful network 
            
        }
         

    }

	

}
