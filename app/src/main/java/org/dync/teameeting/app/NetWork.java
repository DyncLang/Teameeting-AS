package org.dync.teameeting.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.dync.teameeting.structs.EventType;
import org.dync.teameeting.structs.RoomItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.ypy.eventbus.EventBus;

public class NetWork
{

	private static final String TAG = "NetWork";
	private static final boolean mDebug = TeamMeetingApp.mIsDebug;

	// public static final String VIDEO_URL = "123.59.68.21";//公网
	// public static final String VIDEO_URL = "192.168.7.45";//内网

	// public static final String NODE_URL =
	// "http://123.59.68.21:7080/1.0/";//公网
	public static final String NODE_URL = "http://192.168.7.45:8055/";// 内网

	public static final String RETURN_TYPE_JSON = "application/json"; // 返回json
	public static final String RETURN_TYPE_XML = "application/xml"; // 返回xml

	/** Http */
	private DefaultHttpClient mHttpClient;
	private MySelf mMySelf;

	public NetWork(Context context)
	{
		mHttpClient = new DefaultHttpClient();
		mMySelf = TeamMeetingApp.getTeamMeetingApp().getMyself();
	}

	/*
	 * request
	 * =========================Service========================================
	 */

	/**
	 * 
	 * @param params
	 * @return
	 */
	private List<NameValuePair> getNameValuePairList(Map<String, String> params)
	{
		List<NameValuePair> listParams = new ArrayList<NameValuePair>();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<String, String> enter = iterator.next();
			listParams.add(new BasicNameValuePair(enter.getKey(), enter
					.getValue()));
		}
		return listParams;
	}

	/**
	 * get Response String
	 * 
	 * @param params
	 * @param url
	 * @return String
	 */
	private String getResponseStr(Map<String, String> params, String url)
	{
		String enityStr = null;
		HttpPost httpPost = new HttpPost(NODE_URL + url);
		httpPost.addHeader("Accept", RETURN_TYPE_JSON);
		Message msg = new Message();

		List<NameValuePair> listParams = getNameValuePairList(params);
		try
		{
			HttpEntity requestHttpEntity = new UrlEncodedFormEntity(listParams);
			httpPost.setEntity(requestHttpEntity);
			HttpResponse httpResponse = mHttpClient.execute(httpPost);
			int responseCode = httpResponse.getStatusLine().getStatusCode();
			if (mDebug)
				Log.e(TAG, "responseCode" + responseCode);
			if (responseCode == 200)
			{
				enityStr = EntityUtils.toString(httpResponse.getEntity());
				if (mDebug)
					Log.e(TAG, "enityStr" + enityStr);
				return enityStr;
			}

		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		} catch (ClientProtocolException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		msg.what = EventType.MSG_RESPONS_ESTR_NULl.ordinal();
		EventBus.getDefault().post(msg);
		return null;
	}

	/**
	 * inint
	 * 
	 * @param userid
	 * @param uactype
	 * @param uregtype
	 * @param ulogindev
	 * @param upushtoken
	 */

	public void inint(final String userid, final String uactype,
			final String uregtype, final String ulogindev,
			final String upushtoken)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				// TODO Auto-generated method stub
				super.run();

				Map<String, String> params = new HashMap<String, String>();
				params.put("userid", userid);
				params.put("uactype", uactype);
				params.put("uregtype", "uregtype");
				params.put("ulogindev", ulogindev);
				params.put("upushtoken", upushtoken);

				String responseStr = getResponseStr(params, "users/init");
				Bundle bundle = new Bundle();
				Message msg = new Message();

				if (responseStr != null)
				{
					try
					{
						JSONObject jsonObject = new JSONObject(responseStr);
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");

						if (code == 200)
						{
							String authorization = jsonObject
									.getString("authorization");
							int requestid = jsonObject.getInt("requestid");
							String information = jsonObject
									.getString("information");
							JSONObject informationJson = new JSONObject(
									information);
							String userid = informationJson.getString("userid");
							String uname = informationJson.getString("uname");
							String uregtype = informationJson
									.getString("uregtype");
							String ulogindev = informationJson
									.getString("ulogindev");
							String uactype = informationJson
									.getString("uactype");
							String uregtime = informationJson
									.getString("uregtime");
							String upushtoken = informationJson
									.getString("upushtoken");

							mMySelf.setmAuthorization(authorization);
							mMySelf.setmUserId(userid);
							mMySelf.setmName(uname);
							mMySelf.setmRegisterType(uregtype);
							mMySelf.setmUserType(uactype);
							mMySelf.setmLoginType(ulogindev);
							mMySelf.setmRegisterTime(uregtime);
							mMySelf.setmPushToken(upushtoken);

							msg.what = EventType.MSG_ININT_SUCCESS.ordinal();

						} else
						{

							msg.what = EventType.MSG_ININT_FAILED.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					} catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
			}
		}.start();

	}

	/**
	 * 更新网络参数
	 * 
	 * @param userid
	 * @param upushtoken
	 */
	public void updatePushtoken(final String sign, final String upushtoken)
	{

		new Thread()
		{
			@Override
			public synchronized void run()
			{
				// TODO Auto-generated method stub
				super.run();
				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("upushtoken", upushtoken);

				String responseStr = getResponseStr(params,
						"users/updatePushtoken");

				if (responseStr != null)
				{
					try
					{
						JSONObject jsonObject = new JSONObject(responseStr);
						Bundle bundle = new Bundle();
						Message msg = new Message();

						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_UPDAT_EPUSH_TOKEN_SUCCESS
									.ordinal();
						} else
						{
							msg.what = EventType.MSG_UPDAT_EPUSH_TOKEN_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					} catch (JSONException e)
					{

						e.printStackTrace();
					}

				}

			}
		}.start();

	}

	/**
	 * signOut
	 * 
	 * @param sign
	 */
	public void signOut(final String sign)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				// TODO Auto-generated method stub
				super.run();

				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				String responseStr = getResponseStr(params, "users/signout");

				if (responseStr != null)
				{
					try
					{
						JSONObject jsonObject = new JSONObject(responseStr);
						Bundle bundle = new Bundle();
						Message msg = new Message();

						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_SIGNOUT_SUCCESS.ordinal();

						} else
						{

							msg.what = EventType.MSG_SIGNOUT_FAILED.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					} catch (JSONException e)
					{

						e.printStackTrace();
					}

				}

			}
		}.start();

	}

	/**
	 * getRoomList
	 * 
	 * @param sign
	 * @param pageNum
	 * @param pageSize
	 * 
	 */
	public void getRoomList(final String sign, final String pageNum,
			final String pageSize)
	{

		new Thread()
		{
			@Override
			public synchronized void run()
			{
				// TODO Auto-generated method stub
				super.run();

				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("pageNum", pageNum);
				params.put("pageSize", pageSize);

				try
				{
					String responseStr = getResponseStr(params,
							"meeting/getRoomList");
					if (responseStr != null)
					{
						JSONObject jsonObject = new JSONObject(responseStr);

						Bundle bundle = new Bundle();
						Message msg = new Message();

						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							int requestid = jsonObject.getInt("requestid");
							String meetingList = jsonObject
									.getString("meetingList");
							JSONArray meetingListJson = new JSONArray(
									meetingList);
							List<RoomItem> roomList = new ArrayList<RoomItem>();
							for (int i = 0; i < meetingListJson.length(); i++)
							{
								int meetusable = meetingListJson.getJSONObject(
										i).getInt("meetusable");
								int pushable = meetingListJson.getJSONObject(i)
										.getInt("pushable");
								int meettype1 = meetingListJson
										.getJSONObject(i).getInt("meettype");
								int meettype2 = 1;
								int memnumber = meetingListJson
										.getJSONObject(i).getInt("memnumber");
								String meetingid = meetingListJson
										.getJSONObject(i)
										.getString("meetingid");
								long jointime = meetingListJson
										.getJSONObject(i).getLong("jointime");
								int owner = meetingListJson.getJSONObject(i)
										.getInt("owner");
								String userid = meetingListJson
										.getJSONObject(i).getString(
												"meetinguserid");
								String meetname = meetingListJson
										.getJSONObject(i).getString("meetname");
								String meetdesc = meetingListJson
										.getJSONObject(i).getString("meetdesc");
								// String remain1 =
								// meetingListJson.getJSONObject(i).getString("remain1");

								SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
										"yyyy/MM/dd HH:mm");
								String createRoomTime = simpleDateFormat
										.format(jointime);

								RoomItem roomItem = new RoomItem(null,
										meetusable, pushable, meettype1,
										meettype2, memnumber, meetingid,
										createRoomTime, owner, userid,
										meetname, meetdesc);
								roomList.add(roomItem);
								mMySelf.setmRoomList(roomList);
							}
							msg.what = EventType.MSG_GET_ROOM_LIST_SUCCESS
									.ordinal();

						} else
						{
							msg.what = EventType.MSG_GET_ROOM_LIST_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * applyRoom
	 * 
	 * @param sign
	 * @param meetingname
	 * @param meettype
	 * @param meetdesc
	 * @param meetingid
	 */
	public void applyRoom(final String sign, final String meetingname,
			final String meetingtype, final String meetdesc,
			final String pushable)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{

				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("meetingname", meetingname);
				params.put("meetingtype", meetingtype);
				params.put("meetdesc", meetdesc);
				params.put("pushable", "1");

				try
				{
					String ss = getResponseStr(params, "meeting/applyRoom");
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_APPLY_ROOM_SUCCESS
									.ordinal();
						} else
						{
							msg.what = EventType.MSG_APPLY_ROOMT_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{

					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * deleteRoom
	 * 
	 * @param sign
	 * @param meetingid
	 */

	public void deleteRoom(final String sign, final String meetingid)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{

				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("meetingid", meetingid);
				try
				{
					String ss = getResponseStr(params, "meeting/deleteRoom");

					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_DELETE_ROOM_SUCCESS
									.ordinal();

						} else
						{

							msg.what = EventType.MSG_DELETE_ROOM_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * updateRoomMinuxMemNumber
	 * 
	 * @param sign
	 * @param meetingid
	 */
	public void updateRoomMinuxMemNumber(final String sign,
			final String meetingid)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				// TODO Auto-generated method stub
				super.run();

				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("meetingid", meetingid);

				try
				{
					String ss = getResponseStr(params,
							"meeting/updateRoomMinuxMemNumber");
					if (mDebug)
						Log.e(TAG, "ss " + ss);
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_UPDATE_ROOM_Minux_MEM_NUMBER_SUCCESS
									.ordinal();
						} else
						{
							msg.what = EventType.MSG_UPDATE_ROOM_Minux_MEM_NUMBER_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * updateRoomAddMemNumber
	 * 
	 * @param sign
	 * @param meetingid
	 */
	public void updateRoomAddMemNumber(final String sign, final String meetingid)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				// TODO Auto-generated method stub
				super.run();

				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("meetingid", meetingid);

				try
				{
					String ss = getResponseStr(params,
							"meeting/updateRoomAddMemNumber");

					if (mDebug)
						Log.e(TAG, "ss " + ss);
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{

							msg.what = EventType.MSG_UPDATE_ROOM_ADD_MEM_NUMBER_SUCCESS
									.ordinal();

						} else
						{

							msg.what = EventType.MSG_UPDATE_ROOM_ADD_MEM_NUMBER_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * updateRoomEnable
	 * 
	 * @param sign
	 * @param meetingid
	 * @param enable
	 * 
	 */
	public void updateRoomEnable(final String sign, final String meetingid,
			final String enable)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				// TODO Auto-generated method stub
				super.run();

				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("meetingid", meetingid);
				params.put("enable", enable);

				try
				{
					String ss = getResponseStr(params,
							"meeting/updateRoomEnable");
					if (mDebug)
						Log.e(TAG, "ss " + ss);
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_UPDATE_ROOM_ENABLE_SUCCESS
									.ordinal();
						} else
						{
							msg.what = EventType.MSG_UPDATE_ROOM_ENABLE_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * updateRoomMemNumber 07
	 * 
	 * @param sign
	 * @param meetingid
	 * @param enable
	 */
	public void updateRoomMemNumber(final String sign, final String meetingid,
			final String meetingMemNumber)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				// TODO Auto-generated method stub
				super.run();

				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("meetingid", meetingid);
				params.put("meetingMemNumber", meetingMemNumber);

				try
				{
					String ss = getResponseStr(params,
							"meeting/updateRoomMemNumber");
					if (mDebug)
						Log.e(TAG, "ss " + ss);
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_UPDATE_ROOM_ENABLE_SUCCESS
									.ordinal();
						} else
						{
							msg.what = EventType.MSG_UPDATE_ROOM_ENABLE_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * updateRoomPushable
	 * 
	 * @param sign
	 * @param meetingid
	 * @param pushable
	 * 
	 */
	public void updateRoomPushable(final String sign, final String meetingid,
			final String pushable)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("meetingid", meetingid);
				params.put("pushable", pushable);

				try
				{
					String ss = getResponseStr(params,
							"meeting/updateRoomPushable");
					if (mDebug)
						Log.e(TAG, "ss " + ss);
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_UPDATE_ROOM_PUSHABLE_SUCCESS
									.ordinal();

						} else
						{

							msg.what = EventType.MSG_UPDATE_ROOM_PUSHABLE_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * updateMeetRoomName
	 * 
	 * @param sign
	 * @param meetingid
	 * @param roomName
	 * 
	 */
	public void updateMeetRoomName(final String sign, final String meetingid,
			final String roomName)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("meetingid", meetingid);
				params.put("meetingname", roomName);
				try
				{
					String ss = getResponseStr(params,
							"meeting/updateMeetRoomName");
					if (mDebug)
						Log.e(TAG, "ss " + ss);
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_UPDATE_MEET_ROOM_NAME_SUCCESS
									.ordinal();
						} else
						{
							msg.what = EventType.MSG_UPDATE_MEET_ROOM_NAME_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * getMeetingMsgList 10
	 * 
	 * @param sign
	 * @param meetingid
	 * @param pageNum
	 * @param pageSize
	 */

	public void getMeetingMsgList(final String sign, final String meetingid,
			final String pageNum, final String pageSize)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{

				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("meetingid", meetingid);
				params.put("pageNum", pageNum);
				params.put("pageSize", pageSize);
				try
				{

					String ss = getResponseStr(params,
							"meeting/getMeetingMsgList");

					if (mDebug)
						Log.e(TAG, "ss " + ss);
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{

							msg.what = EventType.MSG_GET_MEETING_MSG_LIST_SUCCESS
									.ordinal();

						} else
						{

							msg.what = EventType.MSG_GET_MEETING_MSG_LIST_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * insertMeetingMsg 11
	 * 
	 * @param meetingid
	 * @param messageid
	 * @param messagetype
	 * @param sessionid
	 * @param strMsg
	 * @param userid
	 */
	public void insertMeetingMsg(final String meetingid,
			final String messageid, final String messagetype,
			final String sessionid, final String strMsg, final String userid)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{

				Map<String, String> params = new HashMap<String, String>();
				params.put("meetingid", meetingid);
				params.put("messageid", messageid);
				params.put("messagetype", messagetype);
				params.put("sessionid", sessionid);
				params.put("strMsg", strMsg);
				params.put("userid", userid);

				try
				{

					String ss = getResponseStr(params,
							"meeting/insertMeetingMsg");

					if (mDebug)
						Log.e(TAG, "ss " + ss);
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_INSERT_MEETING_MSG_SUCCESS
									.ordinal();

						} else
						{
							msg.what = EventType.MSG_INSERT_MEETING_MSG_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * insertSessionMeetingInfo 12
	 * 
	 * @param meetingid
	 * @param sessionid
	 * @param sessionstatus
	 * @param sessiontype
	 * @param sessionnumber
	 */

	public void insertSessionMeetingInfo(final String meetingid,
			final String sessionid, final String sessionstatus,
			final String sessiontype, final String sessionnumber)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				Map<String, String> params = new HashMap<String, String>();
				params.put("meetingid", meetingid);
				params.put("sessionid", sessionid);
				params.put("sessionstatus", sessionstatus);
				params.put("sessiontype", sessiontype);
				params.put("sessionnumber", sessionnumber);

				try
				{

					String ss = getResponseStr(params,
							"meeting/insertSessionMeetingInfo");

					if (mDebug)
						Log.e(TAG, "ss " + ss);
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{

							msg.what = EventType.MSG_INSERT_SESSION_MEETING_INFO_SUCCESS
									.ordinal();

						} else
						{

							msg.what = EventType.MSG_INSERT_SESSION_MEETING_INFO_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * updateSessionMeetingStatus 13
	 * 
	 * @param sessionid
	 * @param sessionstatus
	 */

	public void updateSessionMeetingStatus(final String sessionid,
			final String sessionstatus)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{

				Map<String, String> params = new HashMap<String, String>();
				params.put("sessionid", sessionid);
				params.put("sessionstatus", sessionstatus);
				try
				{
					String ss = getResponseStr(params,
							"meeting/updateSessionMeetingStatus");

					if (mDebug)
						Log.e(TAG, "ss " + ss);
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_UPDATE_SESSION_MEETING_STATUS_SUCCESS
									.ordinal();
						} else
						{

							msg.what = EventType.MSG_UPDATE_SESSION_MEETING_STATUS_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * updateSessionMeetingEndtime14
	 * 
	 * @param sessionid
	 */

	public void updateSessionMeetingEndtime(final String sessionid)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				super.run();

				Map<String, String> params = new HashMap<String, String>();
				params.put("sessionid", sessionid);

				try
				{
					String ss = getResponseStr(params,
							"meeting/updateSessionMeetingEndtime");

					if (mDebug)
						Log.e(TAG, "ss " + ss);
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{

							msg.what = EventType.MSG_UPDATE_SESSION_MEETING_ENDTIME_SUCCESS
									.ordinal();

						} else
						{

							msg.what = EventType.MSG_UPDATE_SESSION_MEETING_ENDTIME_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * updateSessionMeetingNumber15
	 * 
	 * @param sessionid
	 * @param sessionnumber
	 */

	public void updateSessionMeetingNumber(final String sessionid,
			final String sessionnumber)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				Map<String, String> params = new HashMap<String, String>();
				params.put("sessionid", sessionid);
				params.put("sessionnumber", sessionnumber);

				try
				{

					String ss = getResponseStr(params,
							"meeting/updateSessionMeetingNumber");

					if (mDebug)
						Log.e(TAG, "ss " + ss);
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_UPDATE_SESSION_MEETING_NUMBER_SUCCESS
									.ordinal();

						} else
						{

							msg.what = EventType.MSG_UPDATE_SESSION_MEETING_NUMBER_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);

						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	// ******************************************
	/**
	 * .获取会议室的信息 单独封装 提交响应的设置 16
	 * 
	 * @param mettinString
	 */
	String getMeetingInfo(String mettinString)
	{
		return null;
	}

	/**
	 * /meeting/updateUserMeetingJointime 17
	 * 
	 * @param sessionid
	 * @param sessionnumber
	 */
	public void updateUserMeetingJointime(final String sign,
			final String meetingid)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				super.run();

				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("meetingid", meetingid);

				try
				{

					String ss = getResponseStr(params,
							"meeting/updateUserMeetingJointime");
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_UP_DATE_USER_MEETING_JOIN_TIME_SUCCESS
									.ordinal();
						} else
						{
							msg.what = EventType.MSG_UP_DATE_USER_MEETING_JOIN_TIME_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						// 测试
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * updateUserMeetingJointime 18
	 * 
	 * @param sessionid
	 * @param sessionnumber
	 */
	public void insertUserMeetingRoom(final String sign, final String meetingid)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				super.run();

				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("meetingid", meetingid);
				try
				{
					String ss = getResponseStr(params,
							"meeting/insertUserMeetingRoom");
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_INSERT_USER_MEETING_ROOM_SUCCESS
									.ordinal();
						} else
						{
							msg.what = EventType.MSG_INSERT_USER_MEETING_ROOM_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						// 测试
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * pushMeetingMsg 19
	 * 
	 * @param sessionid
	 * @param sessionnumber
	 */
	public void pushMeetingMsg(final String sign, final String meetingid,
			final String pushMsg, final String notification)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				super.run();

				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("meetingid", meetingid);
				params.put("pushMsg", pushMsg);
				params.put("notification", notification);

				try
				{

					String ss = getResponseStr(params, "jpush/pushMeetingMsg");
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_PUSH_MEETING_MSG_SUCCESS
									.ordinal();
						} else
						{
							msg.what = EventType.MSG_PUSH_MEETING_MSG_FAILED
									.ordinal();
						}

						bundle.putString("message", message);
						msg.setData(bundle);
						// 测试
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * pushCommonMsg 20
	 * 
	 * @param sign
	 * @param meetingid
	 * @param pushMsg
	 * @param notification
	 */
	public void pushCommonMsg(final String sign, final List<String> targetid,
			final String pushMsg, final String notification)
	{
		new Thread()
		{
			@Override
			public synchronized void run()
			{
				Map<String, String> params = new HashMap<String, String>();
				params.put("sign", sign);
				params.put("pushMsg", pushMsg);
				params.put("targetid", targetid.toString());
				params.put("notification", notification);
				try
				{
					String ss = getResponseStr(params, "jpush/pushCommonMsg");
					if (mDebug)
						Log.e(TAG, "ss " + ss);
					if (ss != null)
					{
						JSONObject jsonObject = new JSONObject(ss);
						Bundle bundle = new Bundle();
						Message msg = new Message();
						int code = jsonObject.getInt("code");
						String message = jsonObject.getString("message");
						if (code == 200)
						{
							msg.what = EventType.MSG_PUSH_COMMO_NSG_SUCCESS
									.ordinal();
						} else
						{
							msg.what = EventType.MSG_PUSH_OMMO_NMSG_FAILED
									.ordinal();
						}
						bundle.putString("message", message);
						msg.setData(bundle);
						EventBus.getDefault().post(msg);
					}

				} catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}

}
