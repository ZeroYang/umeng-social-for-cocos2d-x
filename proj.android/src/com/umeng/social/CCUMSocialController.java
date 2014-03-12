package com.umeng.social;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.umeng.scrshot.adapter.UMBaseAdapter;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeConfig;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.db.OauthHelper;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sensor.UMSensor.OnSensorListener;
import com.umeng.socialize.sensor.UMSensor.WhitchButton;
import com.umeng.socialize.sensor.controller.UMShakeService;
import com.umeng.socialize.sensor.controller.impl.UMShakeServiceFactory;

/**
 * 
 * @author mrsimple
 * 
 */
public class CCUMSocialController {

	/**
	 * 
	 */
	private static UMSocialService mController;
	private static UMShakeService mShakeController;
	private static Activity mActivity;
	private static final String TAG = CCUMSocialController.class
			.getSimpleName();

	private static Handler mSDKHandler = new Handler(Looper.getMainLooper());;
	private static final int DELAY_MS = 100;
	private static String DESCRIPTOR;

	/**
	 * 初始化SDK
	 * 
	 * @param descriptor
	 *            SDK的字符串描述符
	 */
	public static void initSocialSDK(Activity activity, String descriptor) {

		if (mController == null) {
			synchronized (CCUMSocialController.class) {
				DESCRIPTOR = descriptor;
				mController = UMServiceFactory.getUMSocialService(DESCRIPTOR,
						RequestType.SOCIAL);
			}
		}
		mActivity = activity;
		if (mActivity == null || mActivity.isFinishing()) {
			Log.d(TAG, "#### mActivity == null || mActivity.isFinishing() ");
		}
		checkActivity();
	}

	/**
	 * 删除平台授权
	 * 
	 * @param platform
	 *            授权平台的字符串表示
	 */
	public static void deleteAuthorization(String platform) {
		final SHARE_MEDIA target = SHARE_MEDIA.convertToEmun(platform);
		if (target == null || target == SHARE_MEDIA.GENERIC) {
			Log.e(TAG, "deleteOauth failed, platform is invalid.");
			return;
		}
		checkActivity();
		// 在UI线程执行授权操作
		mSDKHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mController.deleteOauth(mActivity, target, null);
			}
		}, DELAY_MS);

		Log.d(TAG, "@@@@ deleteAuthorization");

	}

	/**
	 * 授权某个平台
	 * 
	 * @param platform
	 *            授权平台的字符串表示
	 */
	public static void doAuthorize(String platform) {
		final SHARE_MEDIA target = SHARE_MEDIA.convertToEmun(platform);
		if (target == null || target == SHARE_MEDIA.GENERIC) {
			Log.e(TAG, "doAuthorize failed, platform is invalid.");
			return;
		}
		checkActivity();
		// 在UI线程执行授权操作
		mSDKHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mController.doOauthVerify(mActivity, target, mAuthListener);
			}
		}, DELAY_MS);

		Log.d(TAG, "@@@@ doAuthorize");
	}

	/**
	 * 打开分享面板
	 * 
	 * @param registerCallback
	 *            是否注册回调接口
	 */
	public static void openShare(boolean registerCallback) {
		if (registerCallback) {
			// 注册回调接口
			mController.registerListener(mSocialShareListener);
		}

		Log.d(TAG, "@@@@ openShare");

		checkActivity();
		// 在UI线程执行打开分享面板操作
		mSDKHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "#### open share in thread.") ;
				// 打开分享面板
				mController.openShare(mActivity, false);
			}
		}, DELAY_MS);
		Log.d(TAG, "@@@@ openShare");

	}

	/**
	 * 直接分享到某个平台, 不打开分享面板, 直接底层分享
	 * 
	 * @param platform
	 *            平台对应的字符串
	 */
	public static void directShare(String platform) {
		final SHARE_MEDIA target = SHARE_MEDIA.convertToEmun(platform);
		if (target == null || target == SHARE_MEDIA.GENERIC) {
			Log.e(TAG, "directShare failed, platform is invalid.");
			return;
		}
		checkActivity();
		// 在UI线程执行底层分享操作
		mSDKHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mController
						.directShare(mActivity, target, mSocialShareListener);
			}
		}, DELAY_MS);

		Log.d(TAG, "@@@@ directShare");
	}

	/**
	 * 判断某个平台是否授权
	 * 
	 * @param platform
	 *            平台的字符串表示
	 * @return 返回true则表示该平台已经授权, 否则为未授权.
	 */
	public static boolean isAuthorized(String platform) {
		SHARE_MEDIA target = SHARE_MEDIA.convertToEmun(platform);
		if (target != null && target != SHARE_MEDIA.GENERIC) {
			return OauthHelper.isAuthenticated(mActivity, target);
		}

		Log.d(TAG, "@@@@ isAuthorized");
		return false;

	}

	/**
	 * 
	 * @param text
	 */
	public static void setShareContent(String text) {
		mController.setShareContent(text);
	}

	/**
	 * 
	 * @param text
	 */
	public static void setShareImagePath(String path) {
		if (!TextUtils.isEmpty(path)) {
			mController.setShareMedia(new UMImage(mActivity, new File(path)));
		}
	}

	/**
	 * 
	 * @param url
	 */
	public static void setShareImageUrl(String url) {
		if (!TextUtils.isEmpty(url)) {
			mController.setShareMedia(new UMImage(mActivity, url));
		}
	}

	/**
	 * 要支持的新平台, 可以添加的平台主要有微信,微信朋友圈,QQ,易信,来往等. 具体参考线上文档
	 * http://dev.umeng.com/social/android /share/specific-integration
	 * 
	 * @param platform
	 *            平台的字符串表示
	 * @param appkey
	 *            平台的appkey或者称为appid
	 * @param targetUrl
	 *            用户点击分享内容时跳转的页面, 一般为APP主页或者下载页
	 */
	public static void supportPlatfrom(String platform, String appkey,
			String targetUrl) {
		if (!TextUtils.isEmpty(platform)) {
			SHARE_MEDIA target = SHARE_MEDIA.convertToEmun(platform);
			// 判断target
			if (target == null || target == SHARE_MEDIA.GENERIC) {
				return;
			}
			checkActivity();
			SocializeConfig mConfig = mController.getConfig();
			if (target == SHARE_MEDIA.QQ) {
				// 添加QQ平台支持
				mConfig.supportQQPlatform(mActivity, appkey, targetUrl);
			} else if (target == SHARE_MEDIA.WEIXIN) {
				mConfig.supportWXPlatform(mActivity, appkey, targetUrl);
			} else if (target == SHARE_MEDIA.WEIXIN_CIRCLE) {
				mConfig.supportWXCirclePlatform(mActivity, appkey, targetUrl);
			} else {
				Log.e(TAG,
						target
								+ "平台暂不支持该集成方式, 请参考 : http://dev.umeng.com/social/android /share/specific-integration");
			}
		}

		Log.d(TAG, "@@@@ supportPlatfrom");
	}

	/**
	 * 设置平台顺序
	 * 
	 * @param platforms
	 *            平台的顺序数组
	 */
	public static void setPlatformOrder(String[] platforms) {
		if (platforms != null && platforms.length > 0) {
			int length = platforms.length;
			//
			List<SHARE_MEDIA> cacheList = new ArrayList<SHARE_MEDIA>();
			// 迭代
			for (int i = 0; i < length; i++) {
				SHARE_MEDIA target = SHARE_MEDIA.convertToEmun(platforms[i]);
				if (target != null && target != SHARE_MEDIA.GENERIC) {
					// 先将有效的平台缓存到列表中, 最后再转换为数组
					cacheList.add(target);
				}
			}
			// 设置顺序, 可变参数列表兼容数组类型.
			mController.getConfig().setPlatformOrder(
					cacheList.toArray(new SHARE_MEDIA[cacheList.size()]));
		}

		Log.d(TAG, "@@@@ setPlatformOrder");
	}

	/**
	 * 移除平台
	 * 
	 * @param platforms
	 *            要移除的平台数组
	 */
	public static void removePlatfroms(String[] platforms) {
		if (platforms != null && platforms.length > 0) {
			for (String item : platforms) {
				SHARE_MEDIA target = SHARE_MEDIA.convertToEmun(item);
				// 合法的平台
				if (target != null && target != SHARE_MEDIA.GENERIC) {
					mController.getConfig().removePlatform(target);
				}
			}
		}

		Log.d(TAG, "@@@@ removePlatfroms");
	}

	/**
	 * 
	 */
	public static void cleanup() {
		mController = null;
		mActivity = null;
		mSDKHandler = null;
		Log.d(TAG, "@@@@ cleanup");
	}

	/**
	 * 检查mActivity的合法性
	 */
	private static void checkActivity() {
		assert mActivity != null : "in UMSocialController class, mActivity == null, can not continue.";
	}

	/*******************************************************************************
	 * 授权回调接口, 会调用native层对应的回调方法, 开发者可以在Java或者native层进行相应的处理
	 ******************************************************************************/
	private static UMAuthListener mAuthListener = new UMAuthListener() {

		@Override
		public void onStart(SHARE_MEDIA platform) {
			OnAuthorizeStart(platform.toString());
		}

		@Override
		public void onError(SocializeException e, SHARE_MEDIA platform) {
			OnAuthorizeError(e.getMessage(), platform.toString());
		}

		@Override
		public void onComplete(Bundle value, SHARE_MEDIA platform) {
			OnAuthorizeComplete(getAuthData(value), platform.toString());
		}

		@Override
		public void onCancel(SHARE_MEDIA platform) {
			OnAuthorizeCancel(platform.toString());
		}

		/**
		 * 
		 * @param data
		 *            授权的数据
		 * @return 如果含有access_token则获取access_token和过期时间, 传递到native层
		 */
		private String[] getAuthData(Bundle data) {
			if (data != null && data.containsKey("access_token")) {
				String[] authData = new String[2];
				authData[0] = data.getString("access_token");
				authData[1] = data.getString("expires_in");
				return authData;
			} else {
				return new String[] {};
			}
		}
	};

	/**
	 * 回调授权的OnStart方法到native层
	 */
	private native static void OnAuthorizeStart(String platform);

	/**
	 * 回调授权的的onError方法到native层
	 * 
	 * @param e
	 *            异常信息
	 * @param platform
	 *            出现异常的平台
	 */
	private native static void OnAuthorizeError(String errorMsg, String platform);

	/**
	 * 回调授权的OnCancel方法到native层
	 * 
	 * @param platform
	 *            对应的平台
	 */
	private native static void OnAuthorizeCancel(String platform);

	/**
	 * 回调授权的onComplete方法到native层
	 * 
	 * @param value
	 *            授权信息, 包含token等
	 * @param platform
	 *            平台
	 */
	private native static void OnAuthorizeComplete(String[] value,
			String platform);

	/******************************************************************************
	 * 分享回调接口,会调用native层对应的回调方法, 开发者可以在Java或者native层进行相应的处理
	 *****************************************************************************/
	private static SnsPostListener mSocialShareListener = new SnsPostListener() {

		/*
		 * 分享开始 (non-Javadoc)
		 * 
		 * @see com.umeng.socialize.controller.listener.SocializeListeners.
		 * SnsPostListener#onStart()
		 */
		@Override
		public void onStart() {
			OnShareStart();
		}

		/*
		 * 分享完成 (non-Javadoc)
		 * 
		 * @see com.umeng.socialize.controller.listener.SocializeListeners.
		 * SnsPostListener#onComplete(com.umeng.socialize.bean.SHARE_MEDIA, int,
		 * com.umeng.socialize.bean.SocializeEntity)
		 */
		@Override
		public void onComplete(SHARE_MEDIA platform, int eCode,
				SocializeEntity entity) {
			OnShareComplete(platform.toString(), eCode, entity.mDescriptor);
		}
	};

	/**
	 * 回调分享的OnStart方法到native层
	 */
	private native static void OnShareStart();

	/**
	 * 回调分享的onComplete方法到native层
	 * 
	 * @param platform
	 *            平台
	 * @param eCode
	 *            错误码, 200为授权成功
	 * @param entity
	 *            UMSocialService的属性容器
	 */
	private native static void OnShareComplete(String platform, int eCode,
			String descriptor);

	/*******************************************************************************************
	 * 摇一摇截屏分享功能
	 ******************************************************************************************/
	/**
	 * 注册摇一摇截屏分享功能
	 * 
	 * @param speed
	 * @param sound
	 * @param platfroms
	 */
	public static void registerShakeSensor(int speed, boolean sound,
			String[] platfroms) {

		if (mShakeController == null) {
			mShakeController = UMShakeServiceFactory
					.getShakeService(DESCRIPTOR);
		}
		List<SHARE_MEDIA> platformsList = new ArrayList<SHARE_MEDIA>();

		if (platfroms != null && platfroms.length > 0) {
			for (String share_MEDIA : platfroms) {
				SHARE_MEDIA target = SHARE_MEDIA.convertToEmun(share_MEDIA);
				if (target != null && target != SHARE_MEDIA.GENERIC) {
					platformsList.add(target);
				}
			}
		} else {
			// 默认添加两个平台
			platformsList.add(SHARE_MEDIA.SINA);
			platformsList.add(SHARE_MEDIA.QZONE);
		}
		mShakeController.registerShakeListender(mActivity, mCocos2dxAdapter,
				2000, true, platformsList, mShakeListener);
	}

	/**
	 * COCOS2D-X自定义的截屏适配器
	 */
	private static UMBaseAdapter mCocos2dxAdapter = new UMBaseAdapter() {

		@Override
		public Bitmap getBitmap() {
			String scrshotImgPath = getCocos2dxScrShot();
			return BitmapFactory.decodeFile(scrshotImgPath);
		}
	};

	/**
	 * 摇一摇截屏分享监听器, 与直接分享公用一套native 回调函数
	 */
	private static OnSensorListener mShakeListener = new OnSensorListener() {

		@Override
		public void onStart() {
			OnShareStart();
		}

		@Override
		public void onComplete(SHARE_MEDIA platform, int eCode,
				SocializeEntity entity) {
			OnShareComplete(platform.toString(), eCode, entity.mDescriptor);
		}

		@Override
		public void onButtonClick(WhitchButton button) {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.umeng.socialize.sensor.UMSensor.OnSensorListener#onActionComplete
		 * (android.hardware.SensorEvent)
		 */
		@Override
		public void onActionComplete(SensorEvent event) {
			onShakeComplete();
		}
	};

	/**
	 * 注册摇一摇截屏分享功能
	 */
	public static void unregisterShakeSensor() {
		checkActivity();
		mShakeController.unregisterShakeListener(mActivity);
	}

	/**
	 * 获取cocos2d-x游戏截屏的图片保存路径
	 * 
	 * @return
	 */
	private native static String getCocos2dxScrShot();

	/**
	 * 用户摇一摇完成, 回调给native层, 使得开发者可以暂停游戏等
	 */
	private native static void onShakeComplete();

}