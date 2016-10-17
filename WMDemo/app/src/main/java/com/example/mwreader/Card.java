package com.example.mwreader;

import java.util.ArrayList;
import java.util.List;

import com.example.mwreader.MainActivity;
import com.example.mwreader.R;
//import com.mwcard.CardReaderException;

import android.R.string;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Card extends Activity {

	private Button btn_cpureset,btn_sendCMD,btn_cpudown;
	private Button btn_opencard,btn_CMDsend,btn_closecard;  
	//private ArrayAdapter<String> adapter;  
	private ViewPager viewPager;//页卡内容
	private ImageView imageView;// 动画图片
	private TextView textView1,textView2,textView3;
	private List<View> views;// Tab页面列表
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private View view1,view3;//各个页卡
	private MainActivity Mactivity;//声明主ACTIVITY对象
	private int st=0;//函数返回状态码
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.card);
		// View contentView = LayoutInflater.from(this.getParent()).inflate(R.layout.lay1, null);
		  //setContentView(R.layout.search_activity);
		//  setContentView(contentView);
		 //AlertDialog.Builder(Card.this)AlertDialog.Builder(this.getParent()) 
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
		InitImageView();
		InitTextView();
		InitViewPager();
		
	}

	private void InitViewPager() {
		viewPager=(ViewPager) findViewById(R.id.vPager);
		views=new ArrayList<View>();
		LayoutInflater inflater=getLayoutInflater();
		view1=inflater.inflate(R.layout.lay1, null);
		//view2=inflater.inflate(R.layout.lay2, null);
		view3=inflater.inflate(R.layout.lay3, null);
		views.add(view1);
		//views.add(view1);
		views.add(view3);
		viewPager.setAdapter(new MyViewPagerAdapter(views));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}
	 /**
	  *  初始化头标
	  */

	private void InitTextView() {
		textView1 = (TextView) findViewById(R.id.text1);
		//textView2 = (TextView) findViewById(R.id.text2);
		textView3 = (TextView) findViewById(R.id.text3);

		textView1.setOnClickListener(new MyOnClickListener(0));
		//textView2.setOnClickListener(new MyOnClickListener(1));
		textView3.setOnClickListener(new MyOnClickListener(2));
	}

	/**
	 2      * 初始化动画
	 3 */

	private void InitImageView() {
		imageView= (ImageView) findViewById(R.id.cursor);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.a).getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		offset = (screenW / 3 - bmpW) / 2;// 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		imageView.setImageMatrix(matrix);// 设置动画初始位置
	}
	//单选按钮状态实时获取
	private int radiovalue()
	{
		int i =0;String msg="";
		RadioGroup radioGroup;
		radioGroup = (RadioGroup) this.findViewById(R.id.idGroup);
		int radioCount = radioGroup.getChildCount();
		RadioButton radioButton;
		
		for (i= 0; i < radioCount; i++) {
			radioButton =(RadioButton) radioGroup.getChildAt(i) ;
			if(radioButton.isChecked()){
				//msg = radioButton.getText().toString();
				break;
			}
		}
		msg=""+i;
		Toast.makeText(Card.this,msg,Toast.LENGTH_SHORT).show();
		return i;
			
	}
	private int lay3radiovalue()
	{
		int i =0;String msg="";
		RadioGroup radioGroup;
		radioGroup = (RadioGroup) this.findViewById(R.id.lay3_radio_Group);
		int radioCount = radioGroup.getChildCount();
		RadioButton radioButton;
		
		for (i= 0; i < radioCount; i++) {
			radioButton =(RadioButton) radioGroup.getChildAt(i) ;
			if(radioButton.isChecked()){
				//msg = radioButton.getText().toString();
				break;
			}
		}
		//msg=""+i;
		//Toast.makeText(Card.this,msg,Toast.LENGTH_SHORT).show();
		return i;
			
	}
	
	public void a_cpureset(View source)//CPU操作卡片命令触发函数
	{
		EditText PUTip=(EditText) findViewById(R.id.lay1_Tip); //提示信息框
		EditText Recv=(EditText) findViewById(R.id.lay1_edit_recv); //返回数据
		
		String tip1="",tip2="",tip="",str="";
		int len=0,v=0;
		byte []atr=new byte[200];
		byte []atr_asc=new byte[400];
		int []atrlen =new int[100];
		v=radiovalue();
		try
		{
			String data=MainActivity.myReader.smartCardReset(v,0);
			PUTip.setText("上电复位成功");
			Recv.setText(data);
		}
		catch (Exception ex)
    	{
			Recv.setText("");
			PUTip.setText(ex.getMessage());
    	}
	}
	public void a_sendCMD(View source)//CPU操作卡片命令触发函数
	{
		String strsend="",data="";
		int v=0;
		EditText PUTip=(EditText) findViewById(R.id.lay1_Tip); //提示信息框
		EditText Recv=(EditText) findViewById(R.id.lay1_edit_recv); //返回数据
		EditText Send=(EditText) findViewById(R.id.lay1_edit_send); //发送数据 
		
		strsend=Send.getText().toString();
		v=radiovalue();
		try
		{
			data=MainActivity.myReader.smartCardCommand(v, strsend);
			PUTip.setText("发送命令成功");
			Recv.setText(data);
		}
		catch (Exception ex)
    	{
			Recv.setText("");
			PUTip.setText(ex.getMessage());
    	}
	}
	public void a_cpudown(View source)//CPU操作卡片命令触发函数
	{
		EditText PUTip=(EditText) findViewById(R.id.lay1_Tip); //提示信息框
		EditText Recv=(EditText) findViewById(R.id.lay1_edit_recv); //返回数据
		String tip="";
		int v=radiovalue();
		try
		{
			MainActivity.myReader.smartCardPowerDown(v);
			PUTip.setText("发送命令成功");
			Recv.setText("");
		}
		catch (Exception ex)
    	{
			Recv.setText("");
			PUTip.setText(ex.getMessage());
    	}
	}

	public void bt_rfcard(View source)/*以下是M1卡操作触发函数*/
	{
		EditText PUTip=(EditText) findViewById(R.id.lay3_edit_tip); //提示信息框
		EditText E_CardNo=(EditText) findViewById(R.id.lay3_edit_cardno); //提示信息框
		
		try
		{
			MainActivity.myReader.halt();
			String snr=MainActivity.myReader.openCard(1);
			E_CardNo.setText(snr);
			PUTip.setText("打开卡片成功。");
		}
		catch (Exception ex)
    	{
			E_CardNo.setText("");
			PUTip.setText(ex.getMessage());
    	}
	}
	public void bt_rfauth(View source)
	{
		byte[] key_asc=new byte[40];
		byte[] key=new byte[20];
		int addr=0,nSector=0,len=0;
		EditText PUTip=(EditText) findViewById(R.id.lay3_edit_tip); //提示信息框
		EditText E_Blockaddr=(EditText) findViewById(R.id.lay3_edit_blockaddr); //提示信息框
		EditText E_Section=(EditText) findViewById(R.id.lay3_edit_section); //提示信息框
		EditText E_Secrect=(EditText) findViewById(R.id.lay3_edit_secrect); //提示信息框
		int v=lay3radiovalue();
		String str_addr=E_Blockaddr.getText().toString();
		String str_key=E_Secrect.getText().toString();
		String str_section=E_Section.getText().toString();
		if(str_addr.isEmpty()||str_section.isEmpty())
		{	PUTip.setText("扇区号或块地址不能为空");
			return;
		}
		
		try
		{
			MainActivity.myReader.mifareAuth(v, Integer.parseInt(str_section), str_key);
			PUTip.setText("认证成功。");
		}
		catch (Exception ex)
    	{
			PUTip.setText(ex.getMessage());
    	}
	}
	public void bt_rfread(View source)
	{
		byte[] rdata_asc=new byte[64];
		byte[] rdata=new byte[32];
		int addr=0,nSector=0,len=0;
		EditText PUTip=(EditText) findViewById(R.id.lay3_edit_tip); //提示信息框
		EditText E_Blockaddr=(EditText) findViewById(R.id.lay3_edit_blockaddr); //提示信息框
		EditText E_Section=(EditText) findViewById(R.id.lay3_edit_section); //提示信息框
		EditText E_Read=(EditText) findViewById(R.id.lay3_edit_read); //提示信息框
		String str_addr=E_Blockaddr.getText().toString();
		String str_section=E_Section.getText().toString();
		if(str_addr.isEmpty()||str_section.isEmpty())
		{	PUTip.setText("扇区号或块地址不能为空");
			return;
		}
		
		try
		{
			int blockNo=MainActivity.myReader.mifareBlockAbs(Integer.parseInt(str_section));
			blockNo=blockNo+Integer.parseInt(str_addr);
			String data=MainActivity.myReader.mifareRead(blockNo);
			E_Read.setText(data);
			PUTip.setText("读数据成功。");
		}
		catch (Exception ex)
    	{
			E_Read.setText("");
			PUTip.setText(ex.getMessage());
    	}
	}
	public void bt_rfwrite(View source)
	{
		byte[] wdata_asc=new byte[64];
		byte[] wdata=new byte[32];
		int addr=0,nSector=0,len=0;
		EditText PUTip=(EditText) findViewById(R.id.lay3_edit_tip); //提示信息框
		EditText E_Blockaddr=(EditText) findViewById(R.id.lay3_edit_blockaddr); //提示信息框
		EditText E_Section=(EditText) findViewById(R.id.lay3_edit_section); //提示信息框
		EditText E_Write=(EditText) findViewById(R.id.lay3_edit_write); //提示信息框
		EditText E_Read=(EditText) findViewById(R.id.lay3_edit_read); //提示信息框
		String str_wdata=E_Write.getText().toString();
		String str_addr=E_Blockaddr.getText().toString();
		String str_section=E_Section.getText().toString();
		if(str_addr.isEmpty()||str_section.isEmpty())
		{	PUTip.setText("扇区号或块地址不能为空");
			return;
		}
		if (str_wdata.length()>32)
		{
			PUTip.setText("写数据长度超过了32个字符。");
			return;
		}
		if (Integer.parseInt(str_addr)==3)
		{
			PUTip.setText("密码区，请勿操作.");
			return;
		}
		
		try
		{
			int blockNo=MainActivity.myReader.mifareBlockAbs(Integer.parseInt(str_section));
			blockNo=blockNo+Integer.parseInt(str_addr);
			MainActivity.myReader.mifareWrite(blockNo, str_wdata);
			E_Read.setText("");
			PUTip.setText("写数据成功。");
		}
		catch (Exception ex)
    	{
			E_Read.setText("");
			PUTip.setText(ex.getMessage());
    	}
	}
	public void bt_rfreadval(View source)
	{
		int []Ivalue=new int[50];
		int addr=0,nSector=0,value=0,len=0;
		EditText PUTip=(EditText) findViewById(R.id.lay3_edit_tip); //提示信息框
		EditText E_Blockaddr=(EditText) findViewById(R.id.lay3_edit_blockaddr); //提示信息框
		EditText E_Section=(EditText) findViewById(R.id.lay3_edit_section); //提示信息框
		EditText E_Value=(EditText) findViewById(R.id.lay3_edit_blockvalue); //提示信息框
		String str_addr=E_Blockaddr.getText().toString();
		String str_section=E_Section.getText().toString();
		if(str_addr.isEmpty()||str_section.isEmpty())
		{	PUTip.setText("扇区号/块地址不能为空");
			return;
		}
		
		try
		{
			int blockNo=MainActivity.myReader.mifareBlockAbs(Integer.parseInt(str_section));
			blockNo=blockNo+Integer.parseInt(str_addr);
			long val=MainActivity.myReader.mifareReadVal(blockNo);
			E_Value.setText(String.valueOf(val));
			PUTip.setText("读块值成功。");
		}
		catch (Exception ex)
    	{
			E_Value.setText("");
			PUTip.setText(ex.getMessage());
    	}
	}
	public void bt_rfwriteval(View source)
	{
		int addr=0,nSector=0,value=0,len=0;
		EditText PUTip=(EditText) findViewById(R.id.lay3_edit_tip); //提示信息框
		EditText E_Blockaddr=(EditText) findViewById(R.id.lay3_edit_blockaddr); //提示信息框
		EditText E_Section=(EditText) findViewById(R.id.lay3_edit_section); //提示信息框
		EditText E_Valueop=(EditText) findViewById(R.id.lay3_edit_valueop); //提示信息框
		String str_value=E_Valueop.getText().toString();
		String str_addr=E_Blockaddr.getText().toString();
		String str_section=E_Section.getText().toString();
		if(str_addr.isEmpty()||str_section.isEmpty()||str_value.isEmpty())
		{	PUTip.setText("扇区号/块地址/值 不能为空");
			return;
		}
		
		try
		{
			int blockNo=MainActivity.myReader.mifareBlockAbs(Integer.parseInt(str_section));
			blockNo=blockNo+Integer.parseInt(str_addr);
			int val=MainActivity.myReader.mifareInitVal(blockNo, Integer.parseInt(str_value));
			PUTip.setText("写块值成功。");
		}
		catch (Exception ex)
    	{
			PUTip.setText(ex.getMessage());
    	}
	}

	/** 
	 *     
	 * 头标点击监听 3 */
	private class MyOnClickListener implements OnClickListener{
        private int index=0;
        public MyOnClickListener(int i){
        	index=i;
        }
		public void onClick(View v) {
			viewPager.setCurrentItem(index);			
		}
		
	}
	
	
	public class MyViewPagerAdapter extends PagerAdapter{
		private List<View> mListViews;
		
		public MyViewPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) 	{	
			container.removeView(mListViews.get(position));
		}


		@Override
		public Object instantiateItem(ViewGroup container, int position) {	
			 container.addView(mListViews.get(position), 0); 
			 return mListViews.get(position);
		}
		
		@Override
		public int getCount() {			
			return  mListViews.size();
		}
		
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {			
			return arg0==arg1;
		}
	}

    public class MyOnPageChangeListener implements OnPageChangeListener{

    	int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
		int two = one * 2;// 页卡1 -> 页卡3 偏移量
		public void onPageScrollStateChanged(int arg0) {
			
			
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
			
		}

		public void onPageSelected(int arg0) {
			String strtip;
			Animation animation = new TranslateAnimation(one*currIndex, one*arg0, 0, 0);
			currIndex = arg0;
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
			imageView.startAnimation(animation);
			//Toast.makeText(WeiBoActivity.this, "您选择了"+ viewPager.getCurrentItem()+"页卡", Toast.LENGTH_SHORT).show();
			int num=viewPager.getCurrentItem();
			if(num==0)
			{
				strtip="您可以操作CPU卡";
			}
			else if(num==1)
			{	
				strtip="您可以操作M1卡";
			}
			else
			{
				strtip="您可以操作M1卡";
			}
			Toast.makeText(Card.this, strtip, Toast.LENGTH_SHORT).show();
		}
    	
    }

}
