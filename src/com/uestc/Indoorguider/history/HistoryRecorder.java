package com.uestc.Indoorguider.history;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.uestc.Indoorguider.APPActivity;
import com.uestc.Indoorguider.Constant;
import com.uestc.Indoorguider.R;
import com.uestc.Indoorguider.more.MoreActivity;
import com.uestc.Indoorguider.util.SendToServerThread;
import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.content.DialogInterface.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryRecorder extends APPActivity implements OnScrollListener {

	private static String TAG = "HISTORY";	
	List<HistoryItem> items;
	ListView mListView;      
	HistoryAdapter mAdapter;    //�Զ���������
	private LayoutInflater mInflater;
	TextView tvTitle;   //����
	private View moreView; //���ظ���ҳ��
	private int lastItem;
    private int count;   //Ŀǰ������ʷ��¼������
    SharedPreferences mPrefrences;
    PopupMenu popup;
    private int currentPosition;
    private static int times = 1;
    private static int mTimes = 1;
    
    static private int loadDateNums;  //��ȡ����ʷ��¼����
    private final static int MSG_LOADMORE_SUCCESS = 0;
    private final static int MSG_LOADMORE_FAIL = 1;
    private final static int MSG_DELONE_SUCCESS = 2;
    private final static int MSG_DELONE_FAIL = 3;
    private final static int MSG_DELALL_SUCCESS = 4;
    private final static int MSG_DELALL_FAIL = 5;
    
    private static final int POPUPMENU_DEL = 100;
	private static final int POPUPMENU_CANCEL = 200;
	private static MyDBHelper dbHelper;
	private static Cursor historyCursor = null;
	private static Cursor pointsCursor = null;
	
    @Override
	protected void handleResult(JSONObject obj) {
    	try {
			switch (obj.getInt("typecode"))
			{
			case Constant.HISTORY_QR_SUCCESS: //������ʷ��¼�ɹ�
				Log.v(TAG, "request ok!");
				if (obj != null) {
					loadDateNums = obj.getInt("nums");
					if (loadDateNums > 0) {
						JSONArray pathArray = obj.getJSONArray("history_items");
						Log.v(TAG, "receive ok!");
						
						//ȡ����һ��������items
						//HistoryItem itm = getItemFromJson((JSONObject) pathArray.get(0));
						//String date = itm.getDate();
						if(!items.isEmpty())
							//if(isOldDateBeforeNew(date, items.get(0).getDate())) 
							if (times++ == 1)
								items.clear();
					
						//
						for (int i = 0; i < pathArray.length(); i++) {
							JSONObject node = (JSONObject) pathArray.get(i);
							HistoryItem item = getItemFromJson(node);
							if (item != null) {
								items.add(item);
							}
						}				
						count = items.size();
						//updateDB();
					}	
				}
				mHandler.sendEmptyMessage(MSG_LOADMORE_SUCCESS);
				break;
			
			case Constant.HISTORY_QR_FAIL:     //������ʷ��¼ʧ��
				loadDateNums = -1;   //ʧ��
				mHandler.sendEmptyMessage(MSG_LOADMORE_FAIL);
				break;
				
			case Constant.HISTORY_DELALL_RQ_SUCCESS:   //ɾ��������ʷ��¼�ɹ�
				mHandler.sendEmptyMessage(MSG_DELALL_SUCCESS);
				break;
				
			case Constant.HISTORY_DELALL_RQ_FAIL:    //ɾ��������ʷ��¼ʧ��
				mHandler.sendEmptyMessage(MSG_DELALL_FAIL);
				break;
				
				
			case Constant.HISTORY_DELGIVEN_RQ_SUCCESS:   //ɾ���ض���ʷ��¼�ɹ�
				mHandler.sendEmptyMessage(MSG_DELONE_SUCCESS);
				break;
				
			case Constant.HISTORY_DELGIVEN_RQ_FAIL:    //ɾ���ض���ʷ��¼ʧ��
				mHandler.sendEmptyMessage(MSG_DELONE_FAIL);
				break;
			}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
    /*
    private boolean isOldDateBeforeNew(String oldDate, String newDate) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date odDate = null;
    	Date nwDate = null;
    	try {
			odDate = simpleDateFormat.parse(oldDate);
			nwDate = simpleDateFormat.parse(newDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (odDate.before(nwDate))
    		return true;
   	
    	return true;
    }
    */
       
    //��JSONObject��ȡ��HistoryItem
    private static HistoryItem getItemFromJson(JSONObject obj) {
    	
    	if (obj == null)
    		return null;
    	HistoryItem itm = null;
    	long mapId = 0;
    	Date dt = null ;
    	String startTime = null;
    	String endTime = null;
    	ArrayList<Site> pathItem = new ArrayList<Site>();
    	SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd"); 
    	try {
    		//mapId = obj.getLong("mapid");
			dt = ft.parse(obj.getString("date"));
			startTime = obj.getString("starttime");
			endTime = obj.getString("endtime");
			JSONArray path = obj.getJSONArray("path");
			for (int i = 0; i < path.length(); i++) {
				JSONObject node = (JSONObject) path.get(i);
				int x = node.getInt("x");
				int y = node.getInt("y");
				int z = node.getInt("z");
				//test*********
				String time = node.getString("time");
				pathItem.add(new Site(x,  y,  z, time));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	itm = new HistoryItem(0, dt, startTime, endTime, pathItem);    	
    	return itm;
    }
  
   //��������
private boolean sendRequest(int msgType, HistoryItem item, int startItem, int endItem) {
		
		JSONObject obj = new JSONObject();		
		try {
			obj.put("username",mPrefrences.getString("UserName", ""));
			switch (msgType){
			case Constant.HISTORY_DELALL_REQUEST:  //ɾ������
				obj.put("typecode", Constant.HISTORY_DELALL_REQUEST);
				break;
			case Constant.HISTORY_DELGIVEN_REQUEST: //ɾ���ض�
				obj.put("typecode", Constant.HISTORY_DELGIVEN_REQUEST);
				obj.put("date", item.getDate());
				obj.put("starttime", item.getStartTime());
				obj.put("endtime", item.getEndTime());
				break;
			case Constant.HISTORY_QUERY: //�����ض���Ŀ
				obj.put("typecode", Constant.HISTORY_QUERY);
				//userid Ҫ������			
				obj.put("start_item", startItem);
				obj.put("end_item", endItem);
				break;
			//case Constant
			}
			Handler handler = SendToServerThread.getHandler();
			if(handler!= null)
			{
				Message msg = handler.obtainMessage();
				msg.obj = obj;		
				handler.sendMessage(msg);
				return true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}	 	
		return false;
	}
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_app_history);
		Log.v(TAG, "into oncreate!");
		initView();		
		//����ض���Ŀ
		mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {				
				Bundle data = new Bundle();
				HistoryItem itm = items.get(position);
				data.putSerializable("historyitem", itm);
				//����һ��Intent
				Intent intent = new Intent(HistoryRecorder.this
					, HistoryPathShow.class);
				intent.putExtras(data);
				//����intent��Ӧ��Activity
				startActivity(intent);
			}
		
		}); 
		//�����ض���Ŀ
		mListView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {				
				final int delId = currentPosition = position;
				Builder builder  = new Builder(HistoryRecorder.this);
				builder.setTitle("ȷ��ɾ��������¼��?");
				builder.setPositiveButton("ȷ��", new OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {						
						dialog.dismiss();
						//deleteData(delId);
						progressDialog = ProgressDialog.show(HistoryRecorder.this, "���Ե�...", "����ɾ����...", true);	
						Thread thread = new Thread()
			            {
			                public void run()
			                {
			                    try
			                    {			  
			                    	if (!sendRequest(Constant.HISTORY_DELGIVEN_REQUEST, items.get(delId), 0, 0)) {
										mHandler.sendEmptyMessage(MSG_DELONE_FAIL);							
									}
			                        sleep(800);
			                    } catch (InterruptedException e)
			                    {
			                        e.printStackTrace();
			                    }
			                    progressDialog.dismiss();
			                }
			            };
			            thread.start();
					
					}			
				});
				builder.setNegativeButton("ȡ��", null);
				builder.create().show();
				return true;
			}
		});
			
	}
	
	//��ʼ��
	private void initView() {

		loadDateNums = -1;
		dbHelper = new MyDBHelper();		
		mPrefrences = PreferenceManager.getDefaultSharedPreferences(this);
		items = new ArrayList<HistoryItem>();
		mInflater = LayoutInflater.from(this);   
		mListView = (ListView) findViewById(R.id.history_list);
		moreView = getLayoutInflater().inflate(R.layout.history_load, null);
		tvTitle = (TextView) findViewById(R.id.history_map_title);
		tvTitle.setText("��ʷ�켣");
		mAdapter = new HistoryAdapter(this, items);
		Log.v(TAG, "into db!");
		initData();  //��ʼ����ʷ����
		count = items.size();
		Log.v(TAG, "count = " + count);
		mListView.addFooterView(moreView); //��ӵײ�view
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(this); //����listview�Ĺ����¼�				
	}
	
	//test*****************
	@SuppressWarnings("deprecation")
	private void initData() {		
		try{
			dbHelper.open();	
			//�жϱ������ݿ����Ƿ�������
			if(!dbHelper.checkIfDBHasHistory()) {			
				if (!sendRequest(Constant.HISTORY_QUERY, null, 1, 5) )
					mHandler.sendEmptyMessage(MSG_LOADMORE_FAIL);//����ʧ��
			//test();
		} else {			
			historyCursor = dbHelper.getAllHistory();
			//historyCursor.moveToFirst();
			pointsCursor = dbHelper.getAllPathPoints();
			//pointsCursor.moveToFirst();
			items.addAll(historyAndPointsToList(historyCursorToList(historyCursor), 
					pointsCursorToMap(pointsCursor)));
			Log.v(TAG, "init data over");
		}
		} finally {
			dbHelper.close();
		}
	//	test();
	}

	//just for test****************	
	private void test() {
		
		List<Site> sites = new ArrayList<Site>();
		Site site;
		HistoryItem itm;
		for (int i = 200; i < 2200; i+=29) 
			
			{			
			site = new Site(i,  i, 0,"13:2"+i/222);
			sites.add(site);
			}
			
		itm = new HistoryItem(0, new Date(2015-1900, 7-1, 14), "13:20", "13:29", sites);
		dbHelper.insertPath(itm);
		items.add(itm);
		
		Log.v(TAG, "initData()!");
				
	}
	
	
	//���Ȼ�ɾ�����ݿ����������ݣ�Ȼ��ʹ��items��ǰ�������������±��ؿ�������
	//
	private boolean updateDB() {
		int num = 0;
		dbHelper.open();
		dbHelper.delAllHistory();
		/*
		if (!items.isEmpty() ) 
			dbHelper.delAllHistory();
		else {
			dbHelper.close();
			return false;
		} */
		for (HistoryItem itm:items) {
			if(num == 6)
				break;
			Log.v(TAG, "insert path :" + num );
			if (dbHelper.insertPath(itm) < 0) {
				dbHelper.close();
				return false;
			}
			num += 1;
		}	
		dbHelper.close();
		return true;
	}
	
	//�������
	private void delAllData() {
		
		//items.removeAll(items);
		items.clear();
		Log.v(TAG, "delallData  count = " + items.size() );
		mAdapter.notifyDataSetChanged();
		mListView.removeFooterView(moreView); 
		mListView.setAdapter(mAdapter);
	}
	
	private void deleteData(int position){
		items.remove(position);
		ArrayList<HistoryItem>newList=new ArrayList<HistoryItem>();
		//items.remove(position);
		newList.addAll(items);
		items.clear();
		items.addAll(newList);
		Log.v(TAG, "count = " + items.size() + "  postition: " + position);
		//if (items.size() == 1)
		//	mListView.removeFooterView(moreView); 
		mListView.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
		/*
		dbHelper.open();
		if (!items.isEmpty()) 
			dbHelper.deleteHistory(position);
		dbHelper.close();
		*/
	}
	
	
	//��HISTORY_TABLE������ת��ΪArrayList
	private ArrayList<Map<String, String>> historyCursorToList(Cursor cursor) {
		
		Log.v(TAG, "into historyCursorToList");
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
		Log.v(TAG, "historyCursorToList over");
		return result;
	}
	
	//��PATHPOINTS_TABLE������ת��ΪMap
	private Map<Integer, ArrayList<Site>> pointsCursorToMap(Cursor cursor) {
				
		Log.v(TAG, "into pointsCursorToList");
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
		Log.v(TAG, "pointsCursorToList over");
		return result;
	}
	
	//��historyCursorToList��Cursor��������pointsCursorToList��Cursor���������ص�ֵת��Ϊ
	private ArrayList<HistoryItem> historyAndPointsToList(ArrayList<Map<String,String>> history, 
			Map<Integer, ArrayList<Site>> points) {
		
		Log.v(TAG, "into historyAndPointsToList");
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
			Log.v(TAG, "historyid = " + historyId);
			if(sites == null)
				Log.v(TAG, "sites are null, and historyid = " + historyId);
			HistoryItem itm  = new HistoryItem(mapId, now, startTime, endTime, sites);	
			result.add(itm);
			sites = null;
		}		
		return result;
	}
	//test************************
	private void loadMoreData(){ //���ظ�������		
		count = mAdapter.getCount(); 
		//����
		if (mTimes++ == 1) {
			if(!sendRequest(Constant.HISTORY_QUERY, null, 1, 5)) 	
				mHandler.sendEmptyMessage(MSG_LOADMORE_FAIL);//����ʧ��
	
		} else {
			if(!sendRequest(Constant.HISTORY_QUERY, null, count+1, count+5)) 			
				mHandler.sendEmptyMessage(MSG_LOADMORE_FAIL);//����ʧ��
		}
	} 
	
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub		
		count = mAdapter.getCount(); 
		if(lastItem == count  && scrollState == this.SCROLL_STATE_IDLE){ 
			//Log.i(TAG, "������ײ�");
			moreView.setVisibility(view.VISIBLE);
			 loadMoreData();  //���ظ�������
		    //mHandler.sendEmptyMessage(MSG_LOADMORE_);
		}
	}
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		lastItem = firstVisibleItem + visibleItemCount - 1;		
	}
	
	//����Handler
	private Handler mHandler = new Handler(){
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				
				case MSG_LOADMORE_SUCCESS:
					if (loadDateNums == -1) {
						//mListView.removeFooterView(moreView); //�Ƴ��ײ���ͼ
						Toast.makeText(HistoryRecorder.this, "����������", Toast.LENGTH_LONG).show();
						break;
					}
					if (loadDateNums == 0) {
						Toast.makeText(HistoryRecorder.this, "ľ�и������ݣ�", Toast.LENGTH_LONG).show();
				         mListView.removeFooterView(moreView); //�Ƴ��ײ���ͼ
				         break;
					}
					mAdapter.notifyDataSetChanged();
				    moreView.setVisibility(View.GONE);
				    Toast.makeText(HistoryRecorder.this, "���سɹ���", 3000).show();
				    //updateDB();
					break;
				case MSG_LOADMORE_FAIL:
					Toast.makeText(HistoryRecorder.this, "��ȡʧ�ܣ���������������", 4000).show();
					break;
				case MSG_DELALL_SUCCESS:
					//items.removeAll(items);
					delAllData();	
					Toast.makeText(HistoryRecorder.this, "ɾ���ɹ���", 3000).show();
					break;
				case MSG_DELALL_FAIL:
					//**************test
					delAllData();	
					Toast.makeText(HistoryRecorder.this, "ɾ��ʧ�ܣ�", 3000).show();
					break;
				case MSG_DELONE_SUCCESS:
					//mListView.addFooterView(moreView); //��ӵײ�view
					deleteData(currentPosition);
					Toast.makeText(HistoryRecorder.this, "ɾ���ɹ���", 3000).show();
					mListView.setAdapter(mAdapter);
					mAdapter.notifyDataSetChanged();
					break;
				case MSG_DELONE_FAIL:
					//test
					deleteData(currentPosition);
					Toast.makeText(HistoryRecorder.this, "ɾ��ʧ�ܣ�", 3000).show();
					break;
	      
				default:
					break;
				}
			};
		};		
	
		//������ذ�ť jiantou
	public void clickHandler(View source) {		
		Intent clickIntent;
		switch(source.getId()) {	
		case R.id.back_to_more:
			clickIntent = new Intent(this, MoreActivity.class);
			//finish();
			updateDB();
			//deleteData();
			startActivity(clickIntent);
			
			return;		
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
        if(keyCode == KeyEvent.KEYCODE_BACK && 
        		event.getAction() == KeyEvent.ACTION_DOWN){   
        	updateDB();
        	finish();
            return true;   
        }
         return super.onKeyDown(keyCode, event);
     }
	
	//�˵�
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.history_menu, menu);
		return super.onCreateOptionsMenu(menu);	
	}
			
	 private Handler handler = new Handler();
	 private ProgressDialog progressDialog = null;	
	//ɾ�����в˵�
	@Override
	public boolean onOptionsItemSelected(MenuItem mi) {
		
		switch(mi.getItemId()) {		
		case R.id.menu_delallhistory:	
			progressDialog = ProgressDialog.show(HistoryRecorder.this, "���Ե�...", "����ɾ����...", true);				
			Thread thread = new Thread() {
                public void run() {
                    try {
                    	if (!sendRequest(Constant.HISTORY_DELALL_REQUEST, null, 0, 0)) 
            				mHandler.sendEmptyMessage(MSG_DELALL_FAIL);
                        sleep(800);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                }
            };
            thread.start();
			break;			
		case R.id.menu_canceldel:
		break;
		}
		return true;
	}

	@SuppressLint("NewApi") public void onPopupImageButtonClick(View button) {		
		popup = new PopupMenu(this, button);
		//MenuInflater inflater = popup.getMenuInflater();
		popup.getMenu().add(0, POPUPMENU_DEL, 0, "ɾ������");
		popup.getMenu().add(0, POPUPMENU_CANCEL, 0, "ȡ��");
		//inflater.inflate(R.menu.run_together, popup.getMenu());
		popup.setOnMenuItemClickListener(
				new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						switch(item.getItemId()) {
						case POPUPMENU_DEL:
							progressDialog = ProgressDialog.show(HistoryRecorder.this, "���Ե�...", "����ɾ����...", true);									
							Thread thread = new Thread() {
				                public void run() {
				                    try  {
				                    	if (!sendRequest(Constant.HISTORY_DELALL_REQUEST, null, 0, 0)) 
				            				mHandler.sendEmptyMessage(MSG_DELALL_FAIL);
				                        sleep(800);
				                    } catch (InterruptedException e) {
				                        e.printStackTrace();
				                    }
				                    progressDialog.dismiss();
				                }
				            };
				            thread.start();
							break;		
							
						case POPUPMENU_CANCEL:
							break;
						}
						return true;
					}					
				});
		popup.show();
		
		}
	/*	
	@Override
	public void onDestroy() {
		super.onDestroy();
		updateDB();
		if(dbHelper != null)
			dbHelper.close();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		updateDB();
		if(dbHelper != null)
			dbHelper.close();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		updateDB();
		
	}
	*/
	public class HistoryAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		//private List<HistoryItem> items;
		private String TAG = "HISTORY";
		Context ctx;
	    public HistoryAdapter(Context context, List<HistoryItem> itms) {  	
	    	ctx = context;
	    	//items = itms;
	        this.mInflater = LayoutInflater.from(context);        
	    }
	    
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub		
			HistoryItem item = items.get(position);
			ViewHolder viewHolder = null;
			//Log.v(TAG, "into getView");
			RelativeLayout layout = null;
			if(convertView == null) {			
				convertView = mInflater.inflate(R.layout.history_item, null);
				//Log.v(TAG, " getView !!");
		    	viewHolder = new ViewHolder();
		    	viewHolder.llHistoryItem = (RelativeLayout) convertView.findViewById(R.id.history_item_date);
		    	viewHolder.tvDate = (TextView) convertView.findViewById(R.id.history_clock_tv);
		    	viewHolder.tvStartToEndTime = (TextView) convertView.findViewById(R.id.history_start_to_endtime_tv);
				convertView.setTag(viewHolder);
				viewHolder.llHistoryItem.setTag(position);
				//layout.setTag(viewHolder);
				//Log.v(TAG, "getView findViewById over!");
			} else {			
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			String dateTime = item.getDate();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date now = null;
			try {
				now = simpleDateFormat.parse(dateTime);
			} catch (ParseException e1) {
				
				e1.printStackTrace();
			};
			
			if (position > 0) {			
				String preday = items.get(position-1).getDate();			
				Date pre = null;
				try {			
					pre = simpleDateFormat.parse(preday);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(!pre.before(now) && !pre.after(now)) {			
					viewHolder.llHistoryItem.setVisibility(View.GONE);
				} else {			
					viewHolder.llHistoryItem.setVisibility(View.VISIBLE);
					dateTime+=" "+getWeekOfDate(now);
					viewHolder.tvDate.setText(dateTime);
				}
			} else {
				
				viewHolder.llHistoryItem.setVisibility(View.VISIBLE);
				dateTime+=" "+getWeekOfDate(now);
				viewHolder.tvDate.setText(dateTime);
			}		
			viewHolder.tvStartToEndTime.setText(item.getStartTime()+" - "+item.getEndTime());
			return convertView;
		}
		
		class ViewHolder{			
			public RelativeLayout llHistoryItem;
		    public TextView  tvDate;
		    public TextView tvStartToEndTime;
		    //public TextView tvEndTime;
		}
		
		public  String getWeekOfDate(Date date) { 
			  String[] weekDaysName = { "������", "����һ", "���ڶ�", "������", "������", "������", "������" }; 
			  Calendar calendar = Calendar.getInstance(); 
			  calendar.setTime(date); 
			  int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; 
			  return weekDaysName[intWeek]; 
			} 
			
	} 
}