package org.dync.teameeting.helper;

import org.dync.teameeting.activity.MeetingActivity;

import android.R.string;
import android.content.ClipboardManager;
import android.content.Context;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class DialogHelper
{
	@SuppressWarnings("deprecation")
	public static void onClickCopy(Context context, String conpyUrl)
	{
		// TextView tvCopyTextView = (TextView) findViewById(R.id.tv_copy_link);
		ClipboardManager cmb = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		cmb.setText(conpyUrl);
		new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
				.setTitleText("复制成功").setContentText("粘贴给朋友邀请加入会议!").show();
	}

}
