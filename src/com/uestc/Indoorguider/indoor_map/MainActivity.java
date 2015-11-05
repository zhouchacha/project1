package com.uestc.Indoorguider.indoor_map;

import java.io.File;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.uestc.Indoorguider.APPActivity;
import com.uestc.Indoorguider.Constant;
import com.uestc.Indoorguider.R;
import com.uestc.Indoorguider.R.id;
import com.uestc.Indoorguider.R.layout;
import com.uestc.Indoorguider.more.MoreActivity;
import com.uestc.Indoorguider.network.NetworkStateBroadcastReceiver;
import com.uestc.Indoorguider.orientation.OrientationTool;
import com.uestc.Indoorguider.site_show.SearchNearestSite;
import com.uestc.Indoorguider.site_show.SiteActivity;
import com.uestc.Indoorguider.site_show.SiteInfo;
import com.uestc.Indoorguider.ticket.TicketRequestActivity;
import com.uestc.Indoorguider.util.ClientAgent;
import com.uestc.Indoorguider.util.ConnectTool;
import com.uestc.Indoorguider.util.SendToServerThread;
import com.uestc.Indoorguider.wifi.WifiStateReceiver;
import com.uestc.Indoorguider.zxing_view.CaptureActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
//Washington and Lee University: kdtree, http://home.wlu.edu/~levys/software/kd/docs/
import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeyMissingException;
import edu.wlu.cs.levy.CG.KeySizeException;
//����android2.3��javascript����������
//http://code.google.com/p/android/issues/detail?id=12987
public class MainActivity extends APPActivity implements OnClickListener{

	private final static int MinDistance = 300;
	private static  MyWebView webView = null;
	private LinearLayout myLocation = null;
	private LinearLayout near = null;
	private LinearLayout ticket = null;
	private LinearLayout more = null;
	private LinearLayout main_bar,facility_infor,facility_go = null;
    private TextView facility_name ;//��ʩ����
    float[] srcLocation = new float[3] ;
	private float[] destLocation = new float[3]; //��¼Ŀ����ʩλ�ã����ڵ�������
	public static Boolean isForeground = true;//�Ƿ�λ����ǰ��foreground process
	private NetworkStateBroadcastReceiver networkReceiver =null ;
	
	private boolean isGuided;
	WifiManager wifiManager;
	Boolean flag = false;
	
	// double array
	double [][] sites;
	KDTree<Integer> kdtree;
	
	File tfile,mypath;
	String TAG ="scale";
	public final static int REQUEST_MYLOCATION = 0;
	public final static int REQUEST_NEAR = 1;
	public final static int RESULT_MYLOCATION = 10;

	public static int windowHeight ,windowWidth;
	private float[] locationOld = {854,7541,1};//��ʷx����
	private float[] locationNow = {20000,20000,1};//����x����
	
	double angle = 0;//���˷�λ���ɴ�������ȡ����

	private SensorManager mSensorManager;// �������������
	private Sensor aSensor;  
	private Sensor mSensor;  
	private SensorEventListener myListener;
	Intent intent;
	long facility_go_time = 0;//��ʩ�㰴��ʱ��
	@Override
	protected void handleResult(JSONObject obj) 
	{
		try {
			switch(obj.getInt("typecode"))
			{
				//wifi��λ
				case Constant.LOCATION_WIFI_SUCCESS:
					locationNow[0] = obj.getInt("x"); //CM
					locationNow[1] = obj.getInt("y"); 
					locationNow[2] = obj.getInt("z");
					Log.v("test", "x: " + locationNow[0] );
					Log.v("test", "y: " + locationNow[1]);
					Log.v("test", "destination x: "+ destLocation[0]);
					Log.v("test", "destination y: "+ destLocation[1]);
					if((Math.pow(locationOld[0]-locationNow[0],2)+Math.pow(locationOld[1]-locationNow[1],2))>Math.pow(10,2))//1m=20px
					{
						//webView.loadUrl("javascript:drawcircle('"+x+"','"+y+"')");
						//���� �Ƕȣ�λ��xy
						webView.loadUrl("javascript:setPointer('"+OrientationTool.angle+"','"+homeToMapX(locationOld[0])+"','"+homeToMapY(locationOld[1])+"')");
					}
					locationOld = locationNow;
					
					//get the nearest site, and culculate the distance
					if (isGuided) {
						double dis = 0;
						Log.v("test", "test in calculate");
						double [] location = {locationNow[0], locationNow[1]};
						try {
							dis = culculateNearestDistance(location);
							Log.v("test", "test in calculate");
							Log.v("test", "dis: " + dis);
						} catch (KeySizeException e) {
							e.printStackTrace();
						} catch (KeyDuplicateException e) {
							e.printStackTrace();
						}
						Log.v("test", "in dis calculate!");
						if (dis > MinDistance) {
							Log.v("test", "request");
							requestPath(new float[]
								{locationNow[0],  locationNow[1], 1},  destLocation);
						}
					}
					break;
				case Constant.LOCATION_WIFI_ERROR:
				//·������
					break;
				//���˷�λ�ı�
				case Constant.ORIENTATION:
					angle = obj.getDouble("angle");
					webView.loadUrl("javascript:setPointer('"+angle+"','"+homeToMapX(locationOld[0])+"','"+homeToMapY(locationOld[1])+"')");
					break;
				case Constant.GUIDE_SUCCESS://�����Ե�ͼΪԭ��
					//�ظ�����
					main_bar.setVisibility(View.VISIBLE);
					facility_infor.setVisibility(View.GONE);
					JSONArray pathArray = obj.getJSONArray("path");
					String path = "M";
					JSONObject node = new  JSONObject();
					int i = 0;
					// for kdtree
					sites = new double[pathArray.length()][2];
					Log.v("test", "in response!");
					for(; i<pathArray.length()-1; i++)
					{
						node = (JSONObject) pathArray.get(i);
						path = path + node.getInt("x")+" "+node.getInt("y")+"L";
						//init sites
						sites[i][0] = node.getInt("x");
						sites[i][1] = node.getInt("y");
						Log.v("test", "site[0]: " + sites[i][0]);
						Log.v("test", "site[1]: " + sites[i][1]);
					}
					//���һ���ڵ�
					node = (JSONObject) pathArray.get(i);
					path = path + node.getInt("x")+" "+node.getInt("y");
					//init last site
					sites[i][0] = node.getInt("x");
					sites[i][1] = node.getInt("y");
					Log.v("test", "site[0]: " + sites[i][0]);
					Log.v("test", "site[1]: " + sites[i][1]);
					// build the kdtree
					kdtree = new KDTree<Integer>(2);
					for (int j = 0; j < sites.length; ++j)
						try {
							kdtree.insert(sites[j], j);
						} catch (KeySizeException e) {
							e.printStackTrace();
						} catch (KeyDuplicateException e) {
							e.printStackTrace();
						}
					//����javascript�еķ�������·��
					isGuided = true;     //guided!
			        webView.loadUrl("javascript:drawPath('"+path+"')");
					break;
				case Constant.GUIDE_ERROR:
					Toast.makeText(MainActivity.this, "·�����󲻳ɹ����뻻���ص�λ���ԣ�", Toast.LENGTH_SHORT).show();
					break;
					
					//վ��ƥ��ɹ����������ơ����꣬��ʾ
				case Constant.FACILITY_INFOR:
					main_bar.setVisibility(View.GONE);
					facility_infor.setVisibility(View.VISIBLE);
				
					destLocation[0] = obj.getInt("x");
					destLocation[1] = obj.getInt("y");
					destLocation[2] = 1;
					//facility_name.setText(text);
					break;
				default:
					break;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	double culculateNearestDistance(double[] k) 
			throws KeySizeException, KeyDuplicateException {
		
		Log.v("test", "into calculate");
		Log.v("test", "k[0] and k[1]: " + k[0] + "  " +k[1]);
		int m = kdtree.nearest(new double[]{k[0], k[1]});
		Log.v("test", "find ok");
		return Math.sqrt( Math.pow(k[0] - sites[m][0],  2)  + 
									   Math.pow(k[1] - sites[m][1],  2));		
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initServices();// ��ʼ����������λ�÷���
        intent = new Intent();
        intent.setAction("com.uestc.Indoorguider.util.UtilService");
        startService(intent);
        getWindowSize();
        isGuided = false;
        //����handler����
        super.handlerID = 0;
        webView = (MyWebView) findViewById(R.id.webview);
        main_bar = (LinearLayout) findViewById(R.id.main_bar);
        facility_infor = (LinearLayout) findViewById(R.id.facility_infor);
        facility_name = (TextView)findViewById(R.id.facility_name);
        facility_go = (LinearLayout) findViewById(R.id.facility_go);
        facility_go.setOnClickListener(this);
        myLocation = (LinearLayout) findViewById(R.id.myLocation);
        myLocation.setOnClickListener(this);
        near = (LinearLayout) findViewById(R.id.near);
        near.setOnClickListener(this); 
        more = (LinearLayout) findViewById(R.id.more);
        more.setOnClickListener(this);{
            	//����javascript�еķ���
            	
//            	if(flag ==false)
//            	{
//            		webView.loadUrl("javascript:setVisible()");
//            		flag=true;
//            	}
//            	else{
//            		webView.loadUrl("javascript:setHidden()");
//            		flag=false;
//            	}
//     
        ticket =  (LinearLayout) findViewById(R.id.ticket);
        ticket.setOnClickListener(this) ;
//            public void onClick(View arg0) {
//            	//����javascript�еķ���
//            	String myPath = "M 20 20 L30 40 L 50 40";
//            	String myPath2 = "M 20 20 L30 40 L 50 40";
//                webView.loadUrl("javascript:drawPath('"+myPath+myPath2+"')");
//            }
//        });
        
       
        //��ȡλ��XML�ļ�****************
        /*
        Log.v("xml", "0: "+sites.get(0).getX());
        SearchTree tree = new SearchTree(sites);
		SiteInfo mGoal = new SiteInfo(1, 1574, 233, 0, "nn");
		SiteInfo target = tree.searchNearestNeighbor(mGoal, tree.mTree);
		Log.v("xml", "result x: "+target.getX()+" y: "+target.getY());
        */
        //��ȡ���*************************
		//����֧��JavaScript�ű�
		WebSettings webSettings = webView.getSettings();  
		webSettings.setJavaScriptEnabled(true);
		//webSettings.setUseWideViewPort(false);  
		//���ÿ��Է����ļ�
		webSettings.setAllowFileAccess(true);
		//����֧������
		webSettings.setBuiltInZoomControls(true);	
	    webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);//���ݳɵ�����ʾ����	
		// ��������Ļ�Ĵ�С
		webSettings.setUseWideViewPort(true); 
        webSettings.setLoadWithOverviewMode(true);  
		
		webSettings.setDatabaseEnabled(true);  
		String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
		webSettings.setDatabasePath(dir);	
		//ʹ��localStorage������
		webSettings.setDomStorageEnabled(true);		
		webSettings.setGeolocationEnabled(true);
		//webSettings.setGeolocationDatabasePath(dir);
		webSettings.setJavaScriptEnabled(true);
			 
		//����WebViewClient
		webView.setWebViewClient(new WebViewClient(){   
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {   
		        view.loadUrl(url);   
		        return true;   
		    }  
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}
			@Override
		    public void onScaleChanged(WebView view, float oldScale, float newScale)
			{
				float s = newScale;
				float s2 = s;
			}
		});
		
		
		//����WebChromeClient
		webView.setWebChromeClient(new WebChromeClient(){
			//����javascript�е�alert
			public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
				//����һ��Builder����ʾ��ҳ�еĶԻ���
				Builder builder = new Builder(MainActivity.this);
				builder.setTitle("Alert");
				builder.setMessage(message);
				builder.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								result.confirm();
							}
						});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			};
			//����javascript�е�confirm
			public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
				Builder builder = new Builder(MainActivity.this);
				builder.setTitle("confirm");
				builder.setMessage(message);
				builder.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								result.confirm();
							}
						});
				builder.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								result.cancel();
							}
						});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			};
			
			@Override
			//������ҳ���صĽ�����
			public void onProgressChanged(WebView view, int newProgress) {
				MainActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress * 100);
				super.onProgressChanged(view, newProgress);
			}

			//����Ӧ�ó���ı���title
			public void onReceivedTitle(WebView view, String title) {
				MainActivity.this.setTitle(title);
				super.onReceivedTitle(view, title);
			}

			public void onExceededDatabaseQuota(String url,
					String databaseIdentifier, long currentQuota,
					long estimatedSize, long totalUsedQuota,
					WebStorage.QuotaUpdater quotaUpdater) {
				quotaUpdater.updateQuota(estimatedSize * 2);
			}
			
			public void onGeolocationPermissionsShowPrompt(String origin,
					GeolocationPermissions.Callback callback) {
				callback.invoke(origin, true, false);
				super.onGeolocationPermissionsShowPrompt(origin, callback);
			}
			
			public void onReachedMaxAppCacheSize(long spaceNeeded,
					long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
				quotaUpdater.updateQuota(spaceNeeded * 2);
			}
		});
		// ����Ĭ�Ϻ��˰�ť�����ã��滻��WebView��Ĳ鿴��ʷҳ��  
		webView.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if ((keyCode == KeyEvent.KEYCODE_BACK)
							&& webView.canGoBack()) {
						webView.goBack();
						return true;
					}
				}
				return false;
			}
		});		
		webView.loadUrl("file:///android_res/raw/spot.svg");		
    }
   }
    
	@Override
	public void onResume()
	{
		super.onResume();
		isForeground = true;
		OrientationTool.setMainHandler(handler);
		MyWebView.setMainHandler(handler);
		//ע�ᴫ����������******************
	    mSensorManager.registerListener(myListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);  
	    mSensorManager.registerListener(myListener, mSensor,SensorManager.SENSOR_DELAY_NORMAL);  
	}
	@Override
	public void onPause()
	{
		super.onPause();
		isForeground = false;
		//ע��������������******************
		mSensorManager.unregisterListener(myListener);  
		OrientationTool.setMainHandler(null);
	}
	@Override
	public void onClick(View v) 
	{
		// TODO Auto-generated method stub
		Intent i;
		switch(v.getId()){
			case R.id.myLocation:
				//scan two-dimension code to get location
				i = new Intent(MainActivity.this,CaptureActivity.class);
	            startActivityForResult(i, REQUEST_MYLOCATION);
	            return;
			case R.id.near:
				i = new Intent(MainActivity.this,SiteActivity.class);
	            startActivityForResult(i, REQUEST_NEAR);
	            return;
			case R.id.ticket:
				String s = "��Ʊ��";
				String v1 = "visible";
				webView.loadUrl("javascript:setVisibility('"+s+"','"+v1+"')");
				return;
			case R.id.more:
				i = new Intent(MainActivity.this,MoreActivity.class);
	            startActivity(i);
				return;	
			case R.id.facility_go:
				//�������
				long dur  = System.currentTimeMillis()-facility_go_time;
				if((System.currentTimeMillis()-facility_go_time) < 3000)
				{
					Toast.makeText(this, "����Ŭ��Ϊ������·�ߣ����Եȣ�", Toast.LENGTH_SHORT).show();
					break;
				}
				facility_go_time = System.currentTimeMillis();
				//��ȡ�յ�λ�ã�����·��
				srcLocation[0] = homeToMapX(locationNow[0]);
			    srcLocation[1] = homeToMapY(locationNow[1]);
				//requestPath(srcLocation,destLocation);	
			    srcLocation[0] = 449;
			    srcLocation[1] = 1687;
			    destLocation[0] = 449;
			    destLocation[1]= 2085;
			    requestPath(srcLocation,destLocation);
				return;			
		}
		
	}
	
	//���ؼ�
	private long exitTime = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
        if (keyCode == KeyEvent.KEYCODE_BACK
                 && event.getRepeatCount() == 0) {
            //do something...
        	if(main_bar.getVisibility() == View.GONE)
        	{
        		main_bar.setVisibility(View.VISIBLE);
    			facility_infor.setVisibility(View.GONE);
    			webView.loadUrl("javascript:setAim('"+-50+"','"+-50+"')");
    			 return true;
        	}
            
         }
        // when click the back key twice *****************
        if(keyCode == KeyEvent.KEYCODE_BACK && 
        		event.getAction() == KeyEvent.ACTION_DOWN){   
            if((System.currentTimeMillis()-exitTime) > 2000){  
                Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����", 
                		Toast.LENGTH_SHORT).show();                                
                exitTime = System.currentTimeMillis();   
            } else {
            	stopService(intent);
                finish();
                System.exit(0);
            }
            return true;   
        }
        // **************
         return super.onKeyDown(keyCode, event);
     }
	
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  
    {  if(data!= null)
       {
	        if (requestCode== REQUEST_NEAR)  
	        {  
	            if (resultCode==SiteActivity.RESULT_CODE)  
	            {  
	                Bundle bundle=data.getExtras();  
	                String str = bundle.getString("siteName");  
	                TextView siteText = (TextView) findViewById(R.id.siteText);
	                siteText.setText(str);
	                RelativeLayout siteLayout =(RelativeLayout)findViewById(R.id.siteInf);
	                siteLayout.setVisibility(View.VISIBLE);
	              //����javascript�еķ���
	                    String name = "L_wc";
	            	    String v =" visible";
	            		webView.loadUrl("javascript:setVisibility()");	       	     
	            }  
	        }
	        else if(requestCode== REQUEST_MYLOCATION)
	        {  //ɨ�붨λ��Ϣ
	        	if(resultCode == RESULT_MYLOCATION)
	        	{
	        		Bundle bundle = data.getBundleExtra("location");  
	        		String[] addr = new String[5];
	        		addr = bundle.getStringArray("addr");
	                String x = addr[2];
	                String y = addr[3];
	                String z = addr[4];
	                //������ǰλ��
	                webView.loadUrl("javascript:setScanResult('"+x+"','"+y+"')");	        		
	        	}
	        }

       }
    }  
	
	protected void requestPath(float[] srcLocation,float[] destLocation)
	{
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if(ConnectTool.checkConnect(this,wifiManager))
		{
			JSONObject obj = new JSONObject();
			try {
				obj.put("typecode", Constant.GUIDE_REQUEST);
				JSONObject src = new JSONObject();
				src.put("x", srcLocation[0]);
				src.put("y", srcLocation[1]);
				src.put("z", srcLocation[2]);
				obj.put("sour", src);
				JSONObject dest = new JSONObject();
				dest.put("x", destLocation[0]);
				dest.put("y", destLocation[1]);
				dest.put("z", destLocation[2]);
				obj.put("dest", dest);
				Handler handler = SendToServerThread.getHandler();
				if(handler!= null)
				{
					Message msg = handler.obtainMessage();
					msg.obj = obj;		
					handler.sendMessage(msg);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	 	
		} 		
	}
	
	  //ʵ�����굽��ͼ����
   public int homeToMapX(float home)
   {
	   int map = (int) (home/MyWebView.P+MyWebView.offsetX);
	   return map;
   }
   public int homeToMapY(float home)
   {
	   int map = (int) (home/MyWebView.P+MyWebView.offsetY);
	   return map;
   }

   @Override
   public void onDestroy()
   {   
	   stopService(intent);
	   super.onDestroy();	   
   }

   @TargetApi(13)
   public void getWindowSize()
   {
   	Display display = getWindowManager().getDefaultDisplay();
	    if (Build.VERSION.SDK_INT >= 13 ) {
	    // ʹ��api11 �¼� api�ķ���
			Point size = new Point();
			display.getSize(size);
		    windowHeight = size.y;
			windowWidth = size.x;
	    }
	    else 
	    {
	        // �Ͱ汾�����Դ�����
			windowHeight = display.getHeight();
			windowWidth = display.getWidth();
	    }
   
   }
   private void initServices() {
		// sensor manager
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);	
        aSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);  
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);  
        myListener = OrientationTool.myListener;    
		// Log.i("way", mOrientationSensor.getName());

		// location manager
		// mLocationManager = (LocationManager)
		// getSystemService(Context.LOCATION_SERVICE);
		// Criteria criteria = new Criteria();// �������󣬼�ָ���������˻��LocationProvider
		// criteria.setAccuracy(Criteria.ACCURACY_FINE);// �ϸ߾���
		// criteria.setAltitudeRequired(false);// �Ƿ���Ҫ�߶���Ϣ
		// criteria.setBearingRequired(false);// �Ƿ���Ҫ������Ϣ
		// criteria.setCostAllowed(true);// �Ƿ��������
		// criteria.setPowerRequirement(Criteria.POWER_LOW);// ���õ͵��
		// mLocationProvider = mLocationManager.getBestProvider(criteria,
		// true);// ��ȡ������õ�Provider

	}
	
   //when click the map
   SearchNearestSite search = SearchNearestSite.getInstance();
   @Override
   public boolean onTouchEvent(MotionEvent event) {
	   	
	   facility_name.setText("");
	   SiteInfo goal = new SiteInfo(0, webView.webviewX0, webView.webviewY0, 0, "");	  
	   float scale = webView.scale;
	   String siteName = null;
	   try {
		   siteName = search.findClickedNearestSite(goal);
	   } catch (KeySizeException e) {
		   e.printStackTrace();
	   } catch (KeyDuplicateException e) {
		   e.printStackTrace();
	   } catch (KeyMissingException e) {
		   e.printStackTrace();
	   }

	   if (siteName != null) {
		  // Log.v("dis", ""+siteName);
		   //if (isScaleEnough(scale, siteName))
			   facility_name.setText(siteName);
	   }
	   else {
		   facility_name.setText("");
	   }
    	return super.onTouchEvent(event);
   }
   /*
   private boolean isScaleEnough(float scale, String siteName) {
	   
	   if (scale <= 0.4) {
		   if (siteName.equals("�򳵴�") || siteName.equals("��������") 
				   || siteName.equals("ͣ����") || siteName.equals("�ÿͷ���") 
				   || siteName.equals("���⳵") || siteName.equals("��������")
				   || siteName.equals("����Ĵ洦") //|| siteName.equals("����վ")
				   ||siteName.equals("�ȴ���") || siteName.equals("�в���")
				   || siteName.equals("�����") || siteName.equals("ATM")
				   || siteName.equals("ʧ�����촦") || siteName.equals("��紦")
				   || siteName.equals("¥��") || siteName.equals("����") )
			   return false;
	   } else if (scale > 0.4 && scale <= 0.8) {
		   if (siteName.equals("�ÿͷ���")  || siteName.equals("���⳵")  
				   || siteName.equals("��������") || siteName.equals("����Ĵ洦"))
			   return false;
	   } 
	   return true;
   }
   */
}