package com.mwreader.bluetooth;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import android.widget.TextView;

/**
 * 导
 * 
 * @author rfstar
 * 
 */

public class NavigateView extends RelativeLayout {
	private TextView title = null;
	public Button leftBtn = null;
	public Button rightBtn = null;
	public RelativeLayout navigateBg = null;
	private static boolean enabled = false;

	public NavigateView(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
	}

	@SuppressWarnings("static-access")
	public NavigateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(Res.layout.navigate_view, this,
				true);
		title = (TextView) findViewById(Res.id.navigateTitle);
		leftBtn = (Button) findViewById(Res.id.leftBtn);
		rightBtn = (Button) findViewById(Res.id.rightBtn);
		navigateBg = (RelativeLayout) findViewById(Res.id.navigate_bg);
		navigateBg.setEnabled(enabled);
	}

	public void setTitle(String title) {
		this.title.setText(title);
	}

	public void setTitle(int title) {
		this.title.setText(title);
	}

	public void setLeftHideBtn(boolean boo) {
		this.setViewHide(leftBtn, boo);
	}

	public void setRightHideBtn(boolean boo) {
		this.setViewHide(rightBtn, boo);
	}

	private void setViewHide(View view, boolean boo) {
		if (boo) {
			view.setVisibility(View.GONE);
		} else {
			view.setVisibility(View.VISIBLE);
		}
	}

	public void setEnable(boolean boo) {
		navigateBg.setEnabled(boo);
		enabled = boo;
	}

	public void refreshBG() {
		navigateBg.setEnabled(enabled);
	}
}
