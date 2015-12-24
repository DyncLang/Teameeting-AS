package org.dync.teameeting.activity;

import org.dync.teameeting.R;
import org.dync.teameeting.app.NetWork;
import org.dync.teameeting.app.TeamMeetingApp;
import org.dync.teameeting.structs.EventType;
import org.dync.teameeting.structs.NetType;
import org.dync.teameeting.utils.LocalUserInfo;

import android.R.animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Switch;
import cn.jpush.android.api.CustomPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener;

import com.ypy.eventbus.EventBus;

/**
 * 
 * @author zhangqilu org.dync.teammeeting.activity StartFlashActivity create at
 *         2015-12-11 17:00:42
 */

public class StartFlashActivity extends BaseActivity
{

	private static final String TAG = "StartFlashActivity";
	private boolean mDebug = TeamMeetingApp.mIsDebug;
	public SweetAlertDialog mNetErrorSweetAlertDialog;
	
	private ImageView mView;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_flash);
		mView = (ImageView) findViewById(R.id.splash_image);
		context = this;
		initData();

		// 设置推送的样式
		setPushNotificationBuilderIcon();
	}

	public void setPushNotificationBuilderIcon()
	{
		CustomPushNotificationBuilder builder = new CustomPushNotificationBuilder(
				this, R.layout.customer_notitfication_layout, R.id.icon,
				R.id.title, R.id.text);
		// 指定定制的 Notification Layout
		builder.statusBarDrawable = R.drawable.ic_richpush_actionbar_back;
		// 指定层状态栏小图标
		builder.layoutIconDrawable = R.drawable.ic_richpush_actionbar_back;

		// 指定下拉状态栏时显示的通知图标
		JPushInterface.setPushNotificationBuilder(2, builder);

	}

	/**
	 * inintData
	 */
	private void initData()
	{
		showNetErroDilaog();
		Animation loadAnimation = AnimationUtils.loadAnimation(this,
				R.anim.splash);
		loadAnimation.setAnimationListener(mAnimationListener);
		mView.setAnimation(loadAnimation);
	}

	private AnimationListener mAnimationListener = new AnimationListener()
	{

		@Override
		public void onAnimationStart(Animation arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationRepeat(Animation arg0)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationEnd(Animation arg0)
		{
			// TODO Auto-generated method stub
			// startProgress();
			String userid = TeamMeetingApp.getTeamMeetingApp().getDevId();
			mNetWork.inint(userid, "2", "2", "2", "TeamMeeting");
		}
	};

	private void initNetWork()
	{
		String userid = TeamMeetingApp.getTeamMeetingApp().getDevId();
		mNetWork.inint(userid, "2", "2", "2", "TeamMeeting");
	}

	/**
	 * interfacejump
	 */
	private void interfacejump()
	{

		boolean firstLogin = LocalUserInfo.getInstance(StartFlashActivity.this)
				.getUserInfoBoolean("firstLogin");
		Intent intent;
		if (firstLogin == false)
		{
			intent = new Intent(StartFlashActivity.this, GuideActivity.class);
			LocalUserInfo.getInstance(StartFlashActivity.this)
					.setUserInfoBoolean("firstLogin", true);
		} else
		{
			intent = new Intent(StartFlashActivity.this, MainActivity.class);
		}

		startActivity(intent);
		finish();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		JPushInterface.onPause(this);
	}

	OnSweetClickListener sweetClickListener = new OnSweetClickListener()
	{
		@Override
		public void onClick(SweetAlertDialog sweetAlertDialog)
		{
			sweetAlertDialog.dismiss();
			initNetWork();
		}
	};

	/**
	 * For EventBus callback.
	 */
	public void onEventMainThread(Message msg)
	{
		switch (EventType.values()[msg.what])
		{
		case MSG_ININT_SUCCESS:
			if (mDebug)
				Log.e(TAG, "MSG_ININT_SUCCESS");
			String sign = TeamMeetingApp.getMyself().getmAuthorization();
			mNetWork.getRoomList(sign, 1 + "", 20 + "");
			break;
		case MSG_ININT_FAILED:
			if (mDebug)
				Log.e(TAG, "MSG_ININT_FAILED");
			break;
		case MSG_SIGNOUT_SUCCESS:
			if (mDebug)
				Log.e(TAG, "MSG_SIGNOUT_SUCCESS");
			finish();
			System.exit(0);
			break;

		case MSG_SIGNOUT_FAILED:
			if (mDebug)
				Log.e(TAG, "MSG_SIGNOUT_FAILED");
			break;

		case MSG_GET_ROOM_LIST_SUCCESS:
			if (mDebug)
				Log.e(TAG, "MSG_GET_ROOM_LIST_SUCCESS");
			mNetErrorSweetAlertDialog.dismiss();
			interfacejump();
			break;

		case MSG_GET_ROOM_LIST_FAILED:
			if (mDebug)
				Log.e(TAG, "MSG_GET_ROOM_LIST_FAILED");
			break;

		case MSG_NET_WORK_TYPE:
			if (mDebug)
				Log.e(TAG, "MSG_NET_WORK_TYPE");
			int type = msg.getData().getInt("net_type");
			netWorkTypeStart(type);
			break;
		case MSG_RESPONS_ESTR_NULl:
			if (mDebug)
				Log.e(TAG, "MSG_NET_WORK_TYPE");
			mNetErrorSweetAlertDialog.show();

		default:
			break;
		}
	}

	public void netWorkTypeStart(int type)
	{

		if (type == NetType.TYPE_NULL.ordinal())
		{
			mNetErrorSweetAlertDialog.show();
		} else
		{
			String userid = TeamMeetingApp.getTeamMeetingApp().getDevId();
			mNetWork.inint(userid, "2", "2", "2", "TeamMeeting");
		}
	}

	void showNetErroDilaog()
	{
		mNetErrorSweetAlertDialog = new SweetAlertDialog(this,
				SweetAlertDialog.ERROR_TYPE).setTitleText("无网络连接...")
				.setConfirmText("重试").setContentText("请连接网络!")
				.setConfirmClickListener(sweetClickListener);
	}

}
