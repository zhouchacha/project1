package com.uestc.Indoorguider.history;
/*
 * ÿ��listView������ʾ�����ݣ����а���ʱ�䡢�������켣��
 */
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HistoryItem implements Serializable{

	private Date date;
	private long mapId;                      //����
	private String startTime;
	private String endTime;
	private List<Site> path;
	
	public HistoryItem() {		
		this.mapId = 0;
		this.date = null;
		this.startTime = null;
		this.endTime = null;
		this.path = null;
	}
	
	public HistoryItem(long mpId, Date dt, String startTime, String endTime, List<Site> path) {		
		this.date = dt;
		this.startTime = startTime;
		this.endTime = endTime;
		this.path = path;
		this.mapId = mpId;
	}
	
	public long getMapId() {
		return this.mapId;
	}
	
	public String getDate() {		
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(date);
		return ft.format(cal.getTime());	
	}
	
	public String getStartTime(){
		return this.startTime;		
	}
	
	public String getEndTime(){
		return this.endTime;		
	}
	
	public List getPath() {		
		return this.path;		
	}
}
