package org.dync.teameeting.activity;

import org.dync.teameeting.app.NetWork;
import org.dync.teameeting.app.TeamMeetingApp;
import org.webrtc.DataChannel.Init;

import cn.jpush.android.api.JPushInterface;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.ypy.eventbus.EventBus;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity
{
	public NetWork mNetWork;
	public String mSign;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		init();

	}

	private void init()
	{
		EventBus.getDefault().register(this);
		mNetWork = new NetWork(this);
		mSign = TeamMeetingApp.getMyself().getmAuthorization();

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		JPushInterface.onResume(this);

	}

	@Override
	protected void onPause()
	{
		super.onPause();
		JPushInterface.onPause(this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

}
