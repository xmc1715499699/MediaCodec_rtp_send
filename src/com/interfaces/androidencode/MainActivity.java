package com.interfaces.androidencode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.android.screenrecorder.rtp.RtpSenderWrapper;
import com.encode.androidencode.AvcEncoder;

public class MainActivity extends Activity implements SurfaceHolder.Callback, PreviewCallback {

	DatagramSocket socket;
	InetAddress address;
	
	AvcEncoder avcCodec;
    public Camera m_camera;  
    SurfaceView   m_prevewview;
    SurfaceHolder m_surfaceHolder;
    //屏幕分辨率，每个机型不一样，机器连上adb后输入wm size可获取
    int width = 800;
    int height = 480;
    int framerate = 30;//每秒帧率
    int bitrate = 2500000;//编码比特率，
    private RtpSenderWrapper mRtpSenderWrapper;
    
    byte[] h264 = new byte[width*height*3];

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v("xmc", "MainActivity__onCreate");
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectAll()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build());
StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectLeakedSqlLiteObjects()
        .detectLeakedClosableObjects()
        .penaltyLog()
        .penaltyDeath()
        .build());
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//创建rtp并填写需要发送数据流的地址，直播中需要动态获取客户主动请求的地址
		mRtpSenderWrapper = new RtpSenderWrapper("192.168.253.15", 5004, false);
		avcCodec = new AvcEncoder(width,height,framerate,bitrate);
		
		m_prevewview = (SurfaceView) findViewById(R.id.SurfaceViewPlay);
		m_surfaceHolder = m_prevewview.getHolder(); // 绑定SurfaceView，取得SurfaceHolder对象
		m_surfaceHolder.setFixedSize(width, height); // 预览大小設置
		m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		m_surfaceHolder.addCallback((Callback) this);	
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		Log.v("xmc", "MainActivity+surfaceCreated");
		try {
			m_camera = Camera.open();
			m_camera.setPreviewDisplay(m_surfaceHolder);
			Camera.Parameters parameters = m_camera.getParameters();
			parameters.setPreviewSize(width, height);
			parameters.setPictureSize(width, height);
			parameters.setPreviewFormat(ImageFormat.YV12);
			m_camera.setParameters(parameters);	
			m_camera.setPreviewCallback((PreviewCallback) this);
			m_camera.startPreview();
		} catch (IOException e){
			e.printStackTrace();
		}	
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		Log.v("xmc", "MainActivity+surfaceDestroyed");
		m_camera.setPreviewCallback(null);  //！！这个必须在前，不然退出出错
		m_camera.release();
		m_camera = null; 
		avcCodec.close();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Log.v("xmc", "MainActivity+h264 start");
		int ret = avcCodec.offerEncoder(data, h264);
		if(ret > 0){
			//实时发送数据流
		    mRtpSenderWrapper.sendAvcPacket(h264, 0, ret, 0);
		}
		Log.v("xmc", "MainActivity+h264 end");
		Log.v("xmc", "-----------------------------------------------------------------------");
	}
}
