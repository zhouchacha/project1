package com.uestc.Indoorguider.history;
/*
 * �������Ҫ�����ǽ����ݿ��е����ݣ���PATHPOINTS_TABLE�ͱ�HISTORY_TABLE����ȡ��������
 * ת����ArrayList<HistoryItem>
 * ���ݿ��е����ű���������ģ����ݹ����Խ���ת����
 * 
 */



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.util.Log;
import com.uestc.Indoorguider.history.HistoryItem;
public class DBConvertUtil {

	static public ArrayList<HistoryItem> historyAndPointsToList(ArrayList<Map<String,String>> history, 
			Map<Integer, ArrayList<Site>> points) {
		
		//Log.v(TAG, "into historyAndPointsToList");
		ArrayList<HistoryItem> result = new ArrayList<HistoryItem>();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date now = null;
		
		for (Map<String, String> hy:history) {			
			int historyId = Integer.parseInt(hy.get("historyId"));
			int mapId = Integer.parseInt(hy.get("mapId"));
			String date  = hy.get("date");
			String startTime = hy.get("starttime");
			String endTime = hy.get("endtime");			
			try {
				now = simpleDateFormat.parse(date);
			} catch (ParseException e) {				
				e.printStackTrace();
			};
			ArrayList<Site> sites = new ArrayList<Site>();
			sites = points.get(historyId);
			//Log.v(TAG, "historyid = " + historyId);
			//if(sites == null)
			
				////Log.v(TAG, "sites are null, and historyid = " + historyId);
			HistoryItem item = new HistoryItem(mapId, now, startTime, endTime, sites);
			result.add(item);
			sites = null;
		}		
		return result;
	}
	
	//��PATHPOINTS_TABLE������ת��ΪMap
	static	public Map<Integer, ArrayList<Site>> pointsCursorToMap(Cursor cursor) {
					
			//Log.v(TAG, "into pointsCursorToList");
			int preId = -1;
			int currentId = -2;
			Map<Integer, ArrayList<Site>> result = new HashMap<Integer, ArrayList<Site>>();
			ArrayList<Site> tmpSite = new ArrayList<Site>();
			
			if (cursor.moveToNext()) {	
				//Log.v(TAG, "00");
				preId = cursor.getInt(0);
				tmpSite.add(new Site(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),  cursor.getString(4)));
			}
			//Log.v(TAG, "01");
			while (cursor.moveToNext()) {
				currentId = cursor.getInt(0);
				if (currentId != preId) {				
					result.put(preId, tmpSite);
					tmpSite = null;
					tmpSite = new ArrayList<Site>();
					tmpSite.add(new Site(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),  cursor.getString(4)));
					preId = currentId;
				} else {				
					tmpSite.add(new Site(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),  cursor.getString(4)));							
				}			
			}
			//Log.v(TAG, "02");
			result.put(preId, tmpSite);
			//Log.v(TAG, "pointsCursorToList over");
			return result;
		}
		
		//��HISTORY_TABLE������ת��ΪArrayList
	static	public ArrayList<Map<String, String>> historyCursorToList(Cursor cursor) {
			
			//Log.v(TAG, "into historyCursorToList");
			ArrayList<Map<String, String>> result = 
					new ArrayList<Map<String, String>>();
			while (cursor.moveToNext()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("historyId", cursor.getInt(0) +"");
				map.put("mapId", cursor.getInt(1)+"");
				map.put("date", cursor.getString(2));
				map.put("starttime", cursor.getString(3));
				map.put("endtime", cursor.getString(4));
				result.add(map);
			}	
			//Log.v(TAG, "historyCursorToList over");
			return result;
		}
		
}
