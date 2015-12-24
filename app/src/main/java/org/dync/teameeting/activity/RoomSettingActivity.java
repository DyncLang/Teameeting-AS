package org.dync.teameeting.activity;

import org.dync.teameeting.R;
import org.dync.teameeting.helper.ShareHelper;
import org.dync.teameeting.structs.ExtraType;
import org.dync.teameeting.ui.BottomMenu;
import org.dync.teameeting.ui.BottomMenu.OnTouchSpeedListener;
import org.dync.teameeting.ui.SlideSwitch;
import org.dync.teameeting.ui.SlideSwitch.SlideListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RoomSettingActivity extends Activity implements
		android.view.View.OnClickListener
{
	private Context context;
	private TextView mTvRoomName;
	private TextView mTvJoninRoom;
	private TextView mTvIniviteMessage;
	private TextView mTvInviteWeixin;
	private TextView mTvCopyLink;
	private LinearLayout mLlNotifications;
	private TextView mTvRenameRoom;
	private TextView mTvDeleteRoom;
	private TextView mTvClose;
	private SlideSwitch mSlideSwitch;

	private String mMeetingName;
	private String mMeetingId;

	private int mPosition;

	private ShareHelper mShareHelper;

	private String mShareUrl = "没有设置连接";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_room_setting);
		mShareHelper = new ShareHelper(RoomSettingActivity.this);
		context = this;
		initLayout();
	}

	void initLayout()
	{

		mMeetingName = getIntent().getStringExtra("meetingName");
		mMeetingId = getIntent().getStringExtra("meetingId");
		mPosition = getIntent().getIntExtra("position", 0);

		mTvRoomName = (TextView) findViewById(R.id.tv_room_name);
		mTvRoomName.setText(mMeetingName);
		mTvJoninRoom = (TextView) findViewById(R.id.tv_join_room);
		mTvJoninRoom.setOnClickListener(this);

		mTvIniviteMessage = (TextView) findViewById(R.id.tv_invite_message);
		mTvIniviteMessage.setOnClickListener(this);

		mTvInviteWeixin = (TextView) findViewById(R.id.tv_invite_weixin);
		mTvInviteWeixin.setOnClickListener(this);

		mTvCopyLink = (TextView) findViewById(R.id.tv_copy_link);
		mTvCopyLink.setOnClickListener(this);

		mLlNotifications = (LinearLayout) findViewById(R.id.ll_notifications);
		mLlNotifications.setOnClickListener(this);
		// mLlNotifications.setOnTouchListener();

		mTvRenameRoom = (TextView) findViewById(R.id.tv_rename_room);
		mTvRenameRoom.setOnClickListener(this);

		mTvDeleteRoom = (TextView) findViewById(R.id.tv_delete_room);
		mTvDeleteRoom.setOnClickListener(this);

		mTvClose = (TextView) findViewById(R.id.tv_close);
		mTvClose.setOnClickListener(this);

		mSlideSwitch = (SlideSwitch) findViewById(R.id.ss_SlideSwitch);
		mSlideSwitch.setState(true);
		mSlideSwitch.setSlideListener(slideListener);
		
		
		BottomMenu bottomMenu = (BottomMenu) findViewById(R.id.bottomMenu);
		bottomMenu.setOnTouchQuickSpeedListener(onTouchSpeedListener);
	}

	/**
	 * 　Touch slide Listener
	 */
	OnTouchSpeedListener onTouchSpeedListener = new OnTouchSpeedListener()
	{

		@Override
		public void touchSpeed(int velocityX, int velocityY)
		{
			finish();
			overridePendingTransition(R.anim.activity_close_enter,
					R.anim.activity_close_exit);
		}
	};

	/**
	 * slideListener
	 */
	SlideListener slideListener = new SlideListener()
	{

		@Override
		public void open()
		{
			Toast.makeText(context, "打开", 0).show();
		}

		@Override
		public void close()
		{
			Toast.makeText(context, "关闭", 0).show();
		}
	};

	@Override
	public void onClick(View view)
	{
		Intent intent = null;
		switch (view.getId())
		{
		case R.id.tv_close:
			finishActivity();
			return;
		case R.id.tv_join_room:

			intent = new Intent(RoomSettingActivity.this, MeetingActivity.class);
			intent.putExtra("meetingName", mMeetingName);
			finishActivity();
			break;
		case R.id.tv_invite_message:
			// SMS
			finishActivity();
			mShareHelper.shareSMS("邀请你加入我们的会议", mShareUrl);
			break;
		case R.id.tv_invite_weixin:
			// weixin
			mShareHelper
					.shareWeiXin("分享到... ", "诚挚邀请你加入我们会议吧！点击链接：", mShareUrl);
			finishActivity();
			break;
		case R.id.tv_copy_link:
			setResult(ExtraType.RESULT_CODE_ROOM_SETTING_COPY_LINK);
			finish();

			break;
		case R.id.ll_notifications:
			if (mSlideSwitch.isOpen)
			{
				mSlideSwitch.moveToDest(false);
			} else
			{
				mSlideSwitch.moveToDest(true);
			}
			break;
		case R.id.tv_rename_room:
			intent = new Intent();
			intent.putExtra("position", mPosition);
			intent.putExtra("meetingId", mMeetingId);
			intent.putExtra("meetingName", mMeetingName);
			setResult(ExtraType.RESULT_CODE_ROOM_SETTING_RENAME, intent);
			finishActivity();
			break;
		case R.id.tv_delete_room:
			intent = new Intent();
			intent.putExtra("position", mPosition);
			intent.putExtra("meetingId", mMeetingId);
			setResult(ExtraType.RESULT_CODE_ROOM_SETTING_DELETE, intent);
			finishActivity();
			break;

		default:
			break;
		}

	}

	private void finishActivity()
	{
		finish();
		overridePendingTransition(R.anim.activity_close_enter,
				R.anim.activity_close_exit);
	}

}
