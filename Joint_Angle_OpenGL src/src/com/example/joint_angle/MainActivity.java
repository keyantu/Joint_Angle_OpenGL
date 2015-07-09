package com.example.joint_angle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	private LEE_GL_View mGLSurfaceView;
	TextView result_text;
	Button start,clear;
	BluetoothAdapter adapter;
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP����UUID��
	BluetoothDevice _device = null;     //�����豸
	BluetoothSocket _socket = null;      //����ͨ��socket

	myHandler mmhandler;
	String rec="";
	public String mydatabuffer="";
	public String mysubstring,sendstring;
	public static final String PACKHEAD = "6162";
	double capvalue;
	double CapValueConvert;
	int SV_w,SV_h;
	float pre_value;
	float nex_value;
	/**ʱ������*/
	Date prev_date,next_date;
	String prev_data,final_data;

	public boolean flag_rec_thread=false;
	public static byte[] result = new byte[1024];
	Timer timer = new Timer();

	final String FILE_NAME = "data12";
	  
		@Override
		protected void onCreate(Bundle savedInstanceState)
		{
			// ����Ϊȫ��
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			// ��ʼ��GLSurfaceView
			mGLSurfaceView = new LEE_GL_View(this);
			// �л���������
			
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			 //���Զ����SurfaceView��ӵ����LinearLayout��
	        LinearLayout ll=(LinearLayout)findViewById(R.id.LinearLayout1); 
	        ll.addView(mGLSurfaceView);
			mGLSurfaceView.requestFocus();// ��ȡ����
			mGLSurfaceView.setFocusableInTouchMode(true);// ����Ϊ�ɴ���	
			
			result_text=(TextView)findViewById(R.id.result_text);
			start = (Button) findViewById(R.id.start);
			clear = (Button) findViewById(R.id.clear);
			//��ȡϵͳĬ������
			adapter = BluetoothAdapter.getDefaultAdapter();
		}
		
		
		@Override
		protected void onPause() {
			// TODO Auto-generated method stub
			super.onPause();
			mGLSurfaceView.onPause();
		}


		@Override
		protected void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			mGLSurfaceView.onResume();
		}


		public void onstart(View v)
		{
			adapter.enable();
			//��ʼ����
			//adapter.startDiscovery();
			//_device = adapter.getRemoteDevice("81:F2:6D:98:0E:A0");
			_device = adapter.getRemoteDevice("98:D3:31:40:0E:88");
	        // �÷���ŵõ�socket
	        try{
	        	_socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
	        }catch(IOException e){
	        //Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
	        }
	        try
			{	
				_socket.connect();
				Log.i("SOCKET", "����"+_device.getName()+"�ɹ���");
				result_text.setText("���ӳɹ�");
				//Toast.makeText(this, "����"+_device.getName()+"�ɹ���", Toast.LENGTH_SHORT).show();
			} catch (IOException e)
			{
				
	    		try
				{
//	    		Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
				_socket.close();
				result_text.setText("��������");
				_socket = null;
				} catch (IOException e1)
				{
				}            		
				return;
			}

	        //�򿪽����߳�
	        try{
	    		//blueStream = _socket.getInputStream();   //�õ���������������
	        	//getThread.start();//�߳�����
	        	new RecvData_Thread(_socket).start();
	        	prev_date = new Date();
				mmhandler = new myHandler();
	    		}catch(Exception e){
	    			return;
	    		}
	        
	        flag_rec_thread=true;
			if (flag_rec_thread)
			{
				result_text.setText("���ڽ���");
			}else {
				result_text.setText("ֹͣ����");
			}
		}
		
		
		public void onread(View v){
			result_text.setText(read());
		}
		
		
		private String read()
		{
			try
			{
				// ���ļ�������
				FileInputStream fis = openFileInput(FILE_NAME);
				byte[] buff = new byte[1024];
				int hasRead = 0;
				StringBuilder sb = new StringBuilder("");
				// ��ȡ�ļ�����
				while ((hasRead = fis.read(buff)) > 0)
				{
					sb.append(new String(buff, 0, hasRead));
				}
				// �ر��ļ�������
				fis.close();
				return sb.toString();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}

		private void write(String content)
		{
			try
			{
				// ��׷��ģʽ���ļ������
				FileOutputStream fos = openFileOutput(FILE_NAME, MODE_APPEND);
				//lee debug
				//Log.d("file", "above can write data in file");
				//��FileOutputStream��װ��PrintStream
				PrintStream ps = new PrintStream(fos);
				// ����ļ�����
				ps.println(content);
				// �ر��ļ������
				ps.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		public void onclear(View v){
			result_text.setText("");

		}

		public class myHandler extends Handler{
		  
			@Override
			public void handleMessage(Message msg) {

				
				String databufferfrombluetooth = "";
					if(msg.what == 0x123)
					{
						
						databufferfrombluetooth = (String) msg.obj;
						try {
							double temp = (new Double(databufferfrombluetooth)).doubleValue(); 
							capvalue = temp/100.0;
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
						//if(capvalue > 1.5 && capvalue < 2.5){
						CapValueConvert = -50.7464*capvalue*capvalue*capvalue+337.154*capvalue*capvalue-777.8272*capvalue+686.8225;
						//CapValueConvert = java.lang.Math.abs(CapValueConvert);
						//������޸� ��֤�Ƕ���0-180����
						CapValueConvert = ((CapValueConvert%180)+180)%180;
						BigDecimal bg = new BigDecimal(CapValueConvert);  
			            double cap_acc2 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();  
						String Show_Str = "����ֵ��" + String.valueOf(capvalue)+ " | �Ƕ�ֵ��" + String.valueOf(cap_acc2);
						result_text.setText(Show_Str);
						new File_Task(Show_Str).run();
						nex_value = new Float(cap_acc2).floatValue();
						mGLSurfaceView.mRenderer.mAngleX = nex_value;
						//new Chart_Task(cap_acc2).run();
						//Log.d("$$$", "surface implements");
					}
				super.handleMessage(msg);
			}
		}

		public class RecvData_Thread extends Thread {
			  private final BluetoothSocket _socket;      //����ͨ��socket
			  private InputStream blueStream;    //������������������������
			  
			  public RecvData_Thread(BluetoothSocket socket) {

				_socket = socket;
				InputStream tmpIn = null; //���涨���Ϊfinal����ʹ��temp��ʱ����
				
				try {
						tmpIn = socket.getInputStream(); //ʹ��getInputStream��Ϊһ��������
					} 
				catch (IOException e) { }
				blueStream = tmpIn;
				}
			@Override
			public void run() {
				
				while (!currentThread().isInterrupted()) {
					try{
							byte[] buffer =new byte[1024];
							int count = 0;
							while (count == 0) {
							   count = blueStream.available();
							}
							int readCount = 0; // �Ѿ��ɹ���ȡ���ֽڵĸ���
						    while (readCount < count) {
							 	readCount += blueStream.read(buffer, readCount, count - readCount);
     						}
						    next_date = new Date();
						    long prev = prev_date.getTime();
						    long next = next_date.getTime();
						    
						    /*
							Calendar CD = Calendar.getInstance();
							int SS = CD.get(Calendar.SECOND);
							int MI = CD.get(Calendar.MILLISECOND);
							*/
						    
							String data_blth = "";
							for(int i = 0 ; i < count; i++)
							{ 		  
								data_blth += Integer.toHexString(buffer[i]&0xff);
							}
							//data_blth += "in"+SS+"s"+MI+"ms";
							
							
							if(count<10){
								if (next-prev<100) {
									final_data = prev_data+data_blth;
								}
								else{
									prev_data = data_blth;
								}
								prev_date = next_date;		
							}
							//��ȡ����һ�����������ݺ���post��ȥ
							Message message = mmhandler.obtainMessage();  
				            message.what = 0x123;  
				            message.obj = final_data;  
				            
			
				            mmhandler.sendMessage(message);  
						}catch(IOException e) {  
			                break;  
						}						
					}
					
				}
				
				
				 
			public void cancel() {
				try {
						_socket.close();
					} catch (IOException e) { }
				
			}
		}
		
		public class File_Task extends TimerTask{
			String file_str = "";
			public File_Task(String str) {
				file_str = str;
			}
			public void run()
			{
				//
				//Calendar CD = Calendar.getInstance();
				//int SS = CD.get(Calendar.SECOND);
				//int MI = CD.get(Calendar.MILLISECOND);
				write(file_str);
			}
		}
		
}


