package com.uestc.Indoorguider.zxing_view;

import java.io.IOException;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;


import com.uestc.Indoorguider.R;
import com.uestc.Indoorguider.R.id;
import com.uestc.Indoorguider.R.layout;
import com.uestc.Indoorguider.R.raw;
import com.uestc.Indoorguider.indoor_map.MainActivity;
import com.uestc.Indoorguider.network.NetworkStateBroadcastReceiver;
import com.uestc.Indoorguider.zxing_camera.CameraManager;
import com.uestc.Indoorguider.zxing_decoding.CaptureActivityHandler;
import com.uestc.Indoorguider.zxing_decoding.InactivityTimer;

public class CaptureActivity extends Activity implements Callback
{

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private NetworkStateBroadcastReceiver receiver =null ;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;//����ʱ�䲻��Ծ�����activity
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);
		TextView title = (TextView) findViewById(R.id.title_text);
		title.setText("ɨ�붨λ");
		// ��ʼ�� CameraManager
		CameraManager.init(getApplication());//getApp()�õ���ǰӦ�ö���ľ��
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
        
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);//���ڻ��Ƶ����
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface)
		{
			initCamera(surfaceHolder);
		}
		else
		{
			surfaceHolder.addCallback(this);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);//������������ģʽ
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)
		{
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (receiver != null) { 
			unregisterReceiver(receiver); 
			} 

		if (handler != null)
		{
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy()
	{
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder)
	{
		try
		{
			CameraManager.get().openDriver(surfaceHolder);//�������Ԥ��
		}
		catch (IOException ioe)
		{
			return;
		}
		catch (RuntimeException e)
		{
			return;
		}
		if (handler == null)
		{
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)//Callback �ķ���
	{
		if (!hasSurface)
		{
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView()
	{
		return viewfinderView;
	}

	public Handler getHandler()
	{
		return handler;
	}

	public void drawViewfinder()
	{
		viewfinderView.drawViewfinder();

	}

	public void handleDecode(final Result obj, Bitmap barcode)
	{
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();//��������
	    JSONObject jObj = new JSONObject();	
	    String[] data = new String[2];
	    String[] location = new String[5];
		
		try {
			String rawdata = obj.getText(); 
			data = rawdata.split(";",2);
			location = data[1].split(" ",5);
			jObj.put("type", location[0]);
		    jObj.put("ID", location[1]);
		    jObj.put("x", location[2]);
		    jObj.put("y", location[3]);
			jObj.put("z", location[4]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(this,"ɨ��\"��λר��\"��ά����ܶ�λŶ��", Toast.LENGTH_SHORT).show();
			//��ֹ�������Ƶ��
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//�������¿��������߳�
			Message msg = new Message();
			msg.what =R.id.restart_preview;
			handler.sendMessage(msg);
			e.printStackTrace();
			return;
		}
		Intent i = new Intent(CaptureActivity.this,MainActivity.class);
		Bundle bd = new Bundle();
		bd.putStringArray("addr", location);
		i.putExtra("location", bd);
		CaptureActivity.this.setResult(MainActivity.RESULT_MYLOCATION,i);
		this.finish();
		//--------------Thread  locationThread = new SocketClient(jObj);
		//---------------locationThread.start();


	}

	private void initBeepSound()
	{
		if (playBeep && mediaPlayer == null)
		{
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);//��ȡ��������Դ.����file descriptor 
			try
			{
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			}
			catch (IOException e)
			{
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate()
	{
		if (playBeep && mediaPlayer != null)
		{
			mediaPlayer.start();
		}
		if (vibrate)
		{
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener()
	{
		public void onCompletion(MediaPlayer mediaPlayer)
		{
			mediaPlayer.seekTo(0);//ָ��ʱ���
		}
	};

}