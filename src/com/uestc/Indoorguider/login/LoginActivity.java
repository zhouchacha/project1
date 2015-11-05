package com.uestc.Indoorguider.login;
import org.json.JSONException;
import org.json.JSONObject;

import com.uestc.Indoorguider.APPActivity;
import com.uestc.Indoorguider.Constant;
import com.uestc.Indoorguider.R;
import com.uestc.Indoorguider.more.MoreActivity;
import com.uestc.Indoorguider.util.ClientAgent;
import com.uestc.Indoorguider.util.ConnectTool;
import com.uestc.Indoorguider.util.SendToServerThread;


import android.app.Activity;
import android.app.Dialog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
/* ��½���棬�����û��������룬*/
public class LoginActivity extends APPActivity 
{
	 public  SharedPreferences sp;
	 //��ʾ����Ի���
	 public Dialog errorDialog;
	 public String errorMsg;
	 //��¼��¼�˵�id������
	 String userName;
	 String userpw;
	 String did;//��¼��SharedPreferences��ȡ�ļ�¼������
	 Bundle b;
	 //��Ҫʹ�õĿؼ�
	 CheckBox autoLogin;
	 EditText loginName;
	 EditText loginPw;
	 TextView registertText,title;
	 Button LoginBut;
	 protected void handleResult(JSONObject obj)
	 {
		  try {
			switch(obj.getInt("typecode"))
			  {
			    //��¼�ɹ�
			    case Constant.LOGIN_SUCCESS:
			    	//�Ƿ��Զ���¼
			    	SharedPreferences.Editor editor=sp.edit();
				     if(autoLogin.isChecked())
				       {
					    		
					    		editor.putString("UserName",userName);
								editor.putString("UserPw",userpw);
								editor.putBoolean("AutoLogin", true);
								
				    	}
				     else{
				    	    editor.putString("UserName",userName);
							editor.putString("UserPw",userpw);
				    	    editor.putBoolean("AutoLogin", false);
							
				    	 
				     }
				     editor.putBoolean("HaveLogin",true);
				     editor.commit();
			    	 Intent intent=new Intent(LoginActivity.this,MoreActivity.class);
			    	 intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);  
					 startActivity(intent);
					 LoginActivity.this.finish();
					 //overridePendingTransition(R.anim.in_from_left,R.anim.out_to_right);//�л�Ч��
			    break;
			    
			    //�������
			    case Constant.LOGIN_ERROR_PS:
			    	//��ȡ��Ϣ�е�����
			    	String hint ="�������";
				    Toast.makeText(LoginActivity.this, hint, Toast.LENGTH_LONG).show();
			    break;
			    
			    //�û���δע��
			    case Constant.LOGIN_ERROR_NO:
			    	//��ȡ��Ϣ�е�����
			    	String hint1 = "�û��������ڣ�";
				    Toast.makeText(LoginActivity.this, hint1, Toast.LENGTH_LONG).show();
			    break;
			    
			  }
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	     

//	 	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
//	    	{
//	 		super.onActivityResult(requestCode, resultCode, data);
//	 		if(requestCode==2 && resultCode==2)
//	 		{
//	 			 Intent intent=new Intent(LoginActivity.this,SelectVegeActivity.class);
//	 			 startActivity(intent);
//	 			 LoginActivity.this.finish();
//	 		}
//	 	 }
//	     
	   @Override
	  public void onCreate(Bundle savedInstanceState)
	  {
//		   this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
//	       this.getWindow().getDecorView().setSystemUiVisibility(4);
		   super.onCreate(savedInstanceState);
	       setContentView(R.layout.activity_login);
	       super.handlerID = 2;
	       TextView title = (TextView) findViewById(R.id.title_text);
		   title.setText("�û���¼");
		   sp = PreferenceManager.getDefaultSharedPreferences(this);
	     //  this.setFinishOnTouchOutside(false);
	        //��ȡ�ؼ�
	        LoginBut=(Button)findViewById(R.id.ok);
	        loginName=(EditText)this.findViewById(R.id.loginid);
		    loginPw=(EditText) this.findViewById(R.id.loginpw);
    	    autoLogin= (CheckBox) this.findViewById(R.id.auto_login_check);	
			registertText = (TextView)findViewById(R.id.register);
			registertText.setOnClickListener(
					new OnClickListener()
			{

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent i = new Intent(LoginActivity.this,RegisterActivity.class);
					startActivity(i);
				}
				
			});
			
			//����½����ť�ļ���
			LoginBut.setOnClickListener
	        (
	         new OnClickListener()
	           {
				 public void onClick(View v) 
				   {
					 //��ȡ������Ϣ
						 userName=loginName.getText().toString();
						 userpw=loginPw.getText().toString();
					     
						//�жϵ�¼�����Ȩ��
						 if(userName.equals(""))
						  {
							 Toast.makeText(LoginActivity.this, "�������û���!!!", Toast.LENGTH_SHORT).show(); 
						      return;
						  }
						   if(userpw.equals(""))
						  {
							  Toast.makeText(LoginActivity.this, "����������!!!", Toast.LENGTH_SHORT).show(); 
						      return;
						  }
						   
						 //��½�߳�
						 login();
						 
				    }	
		       }        	
	         );
	      
	    
	  }
	   
	 //����Ҫ�������ݶ�����һ���߳���
	   public void login()
	   {
		   WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		   if(ConnectTool.checkConnect(this,wifiManager))
		   {
			   JSONObject obj = new JSONObject();
				
				try {
					obj.put("typecode",Constant.LOGIN_REQUEST_NAME);
				    obj.put("username", userName);
					obj.put("password",userpw);
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
	   
	   public void onClick(View v){
	    	this.finish();
	    }
	  
}
