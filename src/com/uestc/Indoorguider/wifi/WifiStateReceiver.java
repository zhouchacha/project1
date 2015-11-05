package com.uestc.Indoorguider.wifi;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.uestc.Indoorguider.Constant;
import com.uestc.Indoorguider.map.MapActivity;
import com.uestc.Indoorguider.util.ClientAgent;
import com.uestc.Indoorguider.util.ConnectTool;
import com.uestc.Indoorguider.util.SendToServerThread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;

public class WifiStateReceiver extends BroadcastReceiver {
	int N = 3;//ɨ��N�κ���ƽ������
	int[] count = new int[50];//N��ɨ���ÿ��wifi�ź���ƽ����������

	int num = 0 ;//�ɼ�������
	    private  List<ScanResult> listWifi =new ArrayList<ScanResult>();;
        WifiManager wifi ;
        JSONArray array ;
        
        public WifiStateReceiver( WifiManager wifi )
        {
    	   this.wifi = wifi;
        }
	    public void onReceive(Context context, Intent intent) {
		    List<ScanResult> temp = new ArrayList<ScanResult>();
		    // []ssid={"20:dc:e6:6d:13:0e","30:49:3b:09:68:25","30:49:3b:09:68:27","30:49:3b:09:6a:4f","30:49:3b:09:6b:49"};
		    if(num == 0)
		    {
		    	listWifi = wifi.getScanResults();
		    	num ++;
		    }
		    else{
		    	temp =  wifi.getScanResults();
		    	sumRssi(temp);
		    	num ++;
		    	if(num == N)
		    	{
		    		wifiLocation();
		    		num =0;
		    		listWifi =  new ArrayList<ScanResult>();//һ�ֽ��������
		    	    count = new int[50];//���
		    	}
		    }
			
	        /*array = new JSONArray();
			for (int i = 0; i < listWifi.size(); i++) {
			     
					JSONObject obj = new JSONObject();
					try {
						obj.put("ssid", listWifi.get(i).BSSID);
						obj.put("level", listWifi.get(i).level);
						array.put(obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
			}*/
	}
	public  void wifiLocation()
	{   
		String rssi= "";
		int i = 0;
		
		for (; i < listWifi.size()-1; i++) 
		{
			//����count��ʼ��Ϊ0 ���ʼ���1
		     rssi += listWifi.get(i).BSSID + "," + (int)(listWifi.get(i).level/(count[i]+1))+";";
		}
		rssi += listWifi.get(i).BSSID + "," + (int)(listWifi.get(i).level/(count[i]+1));
			JSONObject msgObj = new JSONObject();
			try {
				if(MapActivity.isForeground == true)
				{
					msgObj.put("typecode", Constant.LOCATION_REQUEST_WIFI);
				}else{
					msgObj.put("typecode", Constant.LOCATION_REQUEST_WIFI2);
				}
				msgObj.put("rssi", rssi);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Handler handler = SendToServerThread.getHandler();
			if(handler!= null)
			{
				Message msg = handler.obtainMessage();
				msg.obj = msgObj;		
				handler.sendMessage(msg);
			}
	}
	private void sumRssi( List<ScanResult> temp)
	{
		   
	      	for(int i = 0; i<temp.size();i++)
	      	{
	      		//�Ȳ鿴��ͬ�±���ǲ���ͬһ���ź����ƣ��ӿ������ٶ�
	      		String bssid = temp.get(i).BSSID;
	      		
	      		if( i < listWifi.size() && bssid.equals( listWifi.get(i).BSSID))
	      		{
	      			listWifi.get(i).level += temp.get(i).level;
	      			count[i]++;
	      		}
	      		else
	      		{
	      			Boolean flag =  true;//�Ƿ���ҵ�
	      			int j = 0;
	      			//���֮ǰû���ҵ�����������
	      			for(; j<listWifi.size();j++)
	      			{
	      				if(bssid.equals( listWifi.get(j).BSSID))
	    	      		{
	    	      			listWifi.get(j).level += temp.get(i).level;
	    	      			count[j]++;
	    	      			flag = false;
	    	      		}
	      				
	      			}
	      			if(flag)//û�в��ҵ�
		      		{
	      				//count[listWifi.size()]++;//���ã���count���¼����
	      				listWifi.add(temp.get(i));//���µ�WiFi��¼��ӵ��б�
	      				
		      			
		      		}
	      		}
	      		
	      	}
	}
	
	

}
