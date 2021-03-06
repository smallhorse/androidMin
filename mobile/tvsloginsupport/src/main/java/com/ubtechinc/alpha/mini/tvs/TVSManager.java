package com.ubtechinc.alpha.mini.tvs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tencent.ai.tvs.AuthorizeListener;
import com.tencent.ai.tvs.LoginApplication;
import com.tencent.ai.tvs.LoginProxy;
import com.tencent.ai.tvs.comm.CommOpInfo;
import com.tencent.ai.tvs.env.ELoginEnv;
import com.tencent.ai.tvs.env.ELoginPlatform;
import com.ubtechinc.alpha.mini.tvs.entity.LoginInfo;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.ai.tvs.env.EUserAttrType.HOMEPAGE;
import static com.tencent.ai.tvs.env.EUserAttrType.RECHARGE;

public class TVSManager implements AuthorizeListener, BaseClient.ClientResultListener {

    public static final String TAG = "TVSManager";

    public static final String WX_APPId = BuildConfig.WX_APP_ID;
    public static final String QQ_OPEN_APPId = BuildConfig.QQ_OPEN_APP_ID;

    public static final String DEVICE_OEM = "wukong_robot";
    public static final String DEVICE_TYPE = "ROBOT";
    public static final String PRODUCT_ID = "b0851325-3056-4853-921b-dcba21b491a3:8c901ad100ad44d98b6276adeb861058";


    private BaseClient wxClient;
    private BaseClient qqClient;
    private List<TVSLoginListener> loginListeners;
    private List<TVSAuthListener> authListeners;

    private volatile static TVSManager instance;

    private LoginProxy proxy;

    public static TVSManager getInstance(Context context) {
        if (instance == null) {
            instance = new TVSManager(context);
        }
        return instance;
    }

    private TVSManager(Context context) {
        proxy = LoginProxy.getInstance(WX_APPId, QQ_OPEN_APPId, context);
        proxy.setLoginEnv(ELoginEnv.FORMAL);
        wxClient = new WXClient(proxy, this);
        qqClient = new QQClient(proxy, this);
        proxy.setAuthorizeListener(this);
    }


    public void init(Activity activity) {
        proxy.setOwnActivity(activity);
    }

    public void wxLogin(Activity activity, TVSLoginListener listener) {
        addLoginListener(listener);
        wxClient.login(activity);
    }

    public void qqLogin(Activity activity, TVSLoginListener listener) {
        proxy.setAuthorizeListener(this);
        qqClient.logout(activity);
        addLoginListener(listener);
        qqClient.login(activity);
    }

    public void refreshLoginToken(TVSLoginListener listener) {
        addLoginListener(listener);
        if (wxClient.isTokenExist(LoginApplication.getInstance())) {
            wxClient.refreshLoginToken();
        } else if (qqClient.isTokenExist(LoginApplication.getInstance())) {
            qqClient.refreshLoginToken();
        }
    }

    public void tvsAuth(String productId, String dsn, TVSAuthListener tvsAuthListener) {
        addAuthListener(tvsAuthListener);
        if (wxClient.isTokenExist(LoginApplication.getInstance())) {
            wxClient.tvsAuth(productId, dsn);
        } else if (qqClient.isTokenExist(LoginApplication.getInstance())) {
            qqClient.tvsAuth(productId, dsn);
        } else {
            onAuthError(403);
        }
    }

    public void tvsAuth(IRobotTvsManager robotTvsManager, TVSAuthListener tvsAuthListener) {
        addAuthListener(tvsAuthListener);
        if (wxClient.isTokenExist(LoginApplication.getInstance())) {
            wxClient.tvsAuth(robotTvsManager);
        } else if (qqClient.isTokenExist(LoginApplication.getInstance())) {
            qqClient.tvsAuth(robotTvsManager);
        } else {
            onAuthError(403);
        }
    }

    public boolean hasExistToken(Context context) {
        return wxClient.isTokenExist(context)
                || qqClient.isTokenExist(context);
    }

    public ELoginPlatform getELoginPlatform() {
        return wxClient.isTokenExist(LoginApplication.getInstance()) ? ELoginPlatform.WX : ELoginPlatform.QQOpen;
    }

    public void logout() {
        wxClient.logout(LoginApplication.getInstance());
        qqClient.logout(LoginApplication.getInstance());
        proxy.setOwnActivity(null);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        proxy.setAuthorizeListener(this);
//        wxClient.onActivityResult(requestCode,resultCode,data);
        qqClient.onActivityResult(requestCode, resultCode, data);
    }

    public void onResume() {
        proxy.setAuthorizeListener(this);
        if (qqClient != null) {
            qqClient.onResume();
        }
    }

    @Override
    public void onSuccess(int i, CommOpInfo var2) {
        Log.d(TAG, "tvs accessToken success : " + i);
        switch (i) {
            case AUTH_TYPE:
                break;
            case REFRESH_TYPE:
                break;
            case WX_TVSIDRECV_TYPE:
                wxClient.onSuccess(i, var2);
                break;
            case QQOPEN_TVSIDRECV_TYPE:
                qqClient.onSuccess(i, var2);
                break;
            case TOKENVERIFY_TYPE:
//                UserInfoManager.getInstance().idType = 1;
//                qqClient.onSuccess(i);
                break;
            case USERINFORECV_TYPE:
//                switch (UserInfoManager.getInstance().idType) {
//                    case 0:
//                        wxClient.onSuccess(i);
//                        break;
//                    case 1:
//                        qqClient.onSuccess(i);
//                        break;
//                    default:
//                        break;
//                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onError(int i, CommOpInfo var2) {
        Log.e(TAG, "tvs accessToken error : " + i);
        switch (i) {
            case AUTH_TYPE:
                onLoginError();
                break;
            case REFRESH_TYPE:
                onLoginError();
                break;
            case WX_TVSIDRECV_TYPE:
                wxClient.onError(i, var2);
                break;
            case QQOPEN_TVSIDRECV_TYPE:
                qqClient.onError(i, var2);
                break;
            case TOKENVERIFY_TYPE:
                qqClient.onError(i, var2);
                break;
            case MANAGEACCT_TYPE:
                qqClient.onError(i, var2);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCancel(int i) {
        Log.e(TAG, "tvs accessToken cancel : " + i);
        doOnCancle();
    }


    @Override
    public void onLoginSuccess(LoginInfo info) {
        if (loginListeners != null) {
            for (TVSLoginListener loginListener : loginListeners) {
                if (loginListener != null) {
                    loginListener.onSuccess(info);
                }
            }
            loginListeners.clear();
        }
    }

    @Override
    public void onLoginError() {
        if (loginListeners != null) {
            for (TVSLoginListener loginListener : loginListeners) {
                if (loginListener != null) {
                    loginListener.onError();
                }
            }
            loginListeners.clear();
        }
    }

    @Override
    public void onAuthSuccess(String clientId) {
        if (authListeners != null) {
            for (TVSAuthListener authListener : authListeners) {
                if (authListener != null) {
                    authListener.onSuccess(clientId);
                }
            }
            authListeners.clear();
        }
    }

    @Override
    public void onAuthError(int code) {
        if (authListeners != null) {
            for (TVSAuthListener authListener : authListeners) {
                if (authListener != null) {
                    authListener.onError(code);
                }
            }
            authListeners.clear();
        }
    }

    @Override
    public void onClientCancel() {
        doOnCancle();
    }

    public void doOnCancle() {
        if (loginListeners != null) {
            for (TVSLoginListener loginListener : loginListeners) {
                loginListener.onCancel();
            }
            loginListeners.clear();
        }
    }

    public boolean isWXInstall() {
        return wxClient.isAppInstall();
    }

    public boolean isWXSupport() {
        return wxClient.isSupportLogin();
    }

    private void addLoginListener(TVSLoginListener listener) {
        if (authListeners == null) {
            loginListeners = new ArrayList<>();
        }
        loginListeners.add(listener);
    }

    private void addAuthListener(TVSAuthListener listener) {
        if (authListeners == null) {
            authListeners = new ArrayList<>();
        }
        authListeners.add(listener);
    }

    public void removeLoginListener(TVSLoginListener listener) {
        if (authListeners == null) {
            return;
        }
        loginListeners.remove(listener);
    }

    private void removeAuthListener(TVSAuthListener listener) {
        if (authListeners == null) {
            return;
        }
        authListeners.remove(listener);
    }

    public void toUserCenter(String dsn) {
        toUserCenter(dsn, false);
    }

    public void toUserCenter(String dsn, boolean recharge) {
        if (wxClient.isTokenExist(LoginApplication.getInstance())) {
            wxClient.toUserCenter(dsn, recharge ? RECHARGE : HOMEPAGE);
        } else if (qqClient.isTokenExist(LoginApplication.getInstance())) {
            qqClient.toUserCenter(dsn, recharge ? RECHARGE : HOMEPAGE);
        }
    }

    public interface TVSLoginListener {

        public void onSuccess(LoginInfo t);

        public void onError();

        public void onCancel();
    }

    public interface TVSAuthListener {

        public void onSuccess(String clientId);

        public void onError(int code);
    }

    public void resetAuthListener(Activity activity) {
        proxy.setOwnActivity(activity);
        proxy.setAuthorizeListener(this);
    }

    public void bindRobot(String robotId) {
        if (wxClient.isTokenExist(LoginApplication.getInstance())) {
            wxClient.bindRobot(robotId);
        } else if (qqClient.isTokenExist(LoginApplication.getInstance())) {
            qqClient.bindRobot(robotId);
        }

    }

    public void unbindRobot(String robotId) {
        if (wxClient.isTokenExist(LoginApplication.getInstance())) {
            wxClient.unbindRobot(robotId);
        } else if (qqClient.isTokenExist(LoginApplication.getInstance())) {
            qqClient.unbindRobot(robotId);
        }
    }
}
