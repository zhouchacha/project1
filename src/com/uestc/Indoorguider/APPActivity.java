package com.uestc.Indoorguider;

import org.json.JSONException;
import org.json.JSONObject;

import com.uestc.Indoorguider.util.ClientAgent;


import android.app.Activity;
import android.content.Context;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public abstract class APPActivity  extends Activity{
	/**
	 * handler ���
	 * 0 - MainActivity
	 * 1 - LoginActivity
	 * 2 - RegisterActivity
	 * 3 - MoreActivity
	 * 4 - MyActivity
	 * 5 - captureActivity
	 */
	protected int handlerID  ;
	protected abstract void handleResult(JSONObject obj);
	protected static int handlerNum = 6;
	int sWidth;
	protected Handler handler = new MyHandler();
	class  MyHandler extends Handler{
		@Override
		public void handleMessage(Message msg)
		{
			//shduiahidh
			JSONObject obj;
			try {
				obj = new JSONObject(msg.obj.toString());
				if(obj.getInt("typecode") == Constant.NETWORK_EXCEPTION )	
				{
					Toast.makeText(APPActivity.this, obj.getInt("network_ex"), Toast.LENGTH_LONG).show();
				}
				handleResult(obj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	 public void onCreate(Bundle savedInstanceState) {  
	 super.onCreate(savedInstanceState);
		DisplayMetrics metric = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metric);
	     sWidth = metric.widthPixels;     // ��Ļ��ȣ����أ�
	    int height = metric.heightPixels;   // ��Ļ�߶ȣ����أ�
	}
	
	@Override
	   protected void onPause()
	   {
		   super.onPause();
		   ClientAgent.setHandler(null);
	   }
	   @Override
	   protected void onResume()
	   {
		   super.onResume();
		   ClientAgent.setHandler(handler);
	   }
	   
	   /**
	      * ����EditText����������û������������Աȣ����ж��Ƿ����ؼ��̣���Ϊ���û����EditTextʱû��Ҫ����
	      * 
	      * @param v
	      * @param event
	      * @return
	      */
	     private boolean isShouldHideInput(View v, MotionEvent event) {
	         if (v != null ) {
	            if(v instanceof EditText )
	            {
	            	 int[] l = { 0, 0 };
	                 v.getLocationInWindow(l);
	                 int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
	                         + v.getWidth();
	                 if (event.getX() > left && event.getX() < sWidth
	                         && event.getY() > top && event.getY() < bottom) {
	                     // ���EditText���¼�����������
	                     return false;
	                 } else {
	                     return true;
	                 }
	            }
	         }
	         // ������㲻��EditText����ԣ������������ͼ�ջ����꣬��һ�����㲻��EditView�ϣ����û��ù켣��ѡ�������Ľ���
	         return false;
	     }
	 
	     /**
	      * ������������̷���������һ��
	      * 
	      * @param token
	      */
	     private void hideSoftInput(IBinder token) {
	         if (token != null) {
	             InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	             im.hideSoftInputFromWindow(token,
	                     InputMethodManager.HIDE_NOT_ALWAYS);
	         }
	     }
	     @Override
	     public boolean dispatchTouchEvent(MotionEvent ev) {
	         if (ev.getAction() == MotionEvent.ACTION_DOWN) {

	             // ��õ�ǰ�õ������View��һ������¾���EditText������������ǹ켣�����ʵ�尸�����ƶ����㣩
	             View v = getCurrentFocus(); 
	             if (isShouldHideInput(v, ev)) {
	                 hideSoftInput(v.getWindowToken());
	             }
	         }
	         return super.dispatchTouchEvent(ev);
	     }
	     
	   
	
	
}