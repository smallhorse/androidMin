package com.ubtechinc.alpha.mini.repository;

import android.app.Activity;
import android.content.Intent;

import com.ubtechinc.alpha.mini.AlphaMiniApplication;
import com.ubtechinc.alpha.mini.tvs.IRobotTvsManager;
import com.ubtechinc.alpha.mini.tvs.TVSManager;
import com.ubtechinc.alpha.mini.tvs.entity.LoginInfo;

public class TVSAuthRepository {

    TVSManager tvsManager;

    public TVSAuthRepository() {
        tvsManager = TVSManager.getInstance(AlphaMiniApplication.getInstance());
    }

    public void loginWX(Activity activity, final AuthCallBack authCallBack) {
        tvsManager.init(activity);
        tvsManager.wxLogin(activity, new TVSManager.TVSLoginListener() {
            @Override
            public void onSuccess(LoginInfo t) {
                callBackSuccess(authCallBack, t);
            }

            @Override
            public void onError() {
                callBackError(authCallBack);
            }

            @Override
            public void onCancel() {
                callBackCancel(authCallBack);
            }
        });
    }

    public void loginQQ(Activity activity, final AuthCallBack authCallBack) {
        tvsManager.init(activity);
        tvsManager.qqLogin(activity, new TVSManager.TVSLoginListener() {
            @Override
            public void onSuccess(LoginInfo t) {
                callBackSuccess(authCallBack, t);
            }

            @Override
            public void onError() {
                callBackError(authCallBack);
            }

            @Override
            public void onCancel() {
                callBackCancel(authCallBack);
            }
        });
    }

    public boolean isWXInstall(){
        return tvsManager.isWXInstall();
    }

    public boolean isWXSupport(){
        return tvsManager.isWXSupport();
    }

    public void refreshLogin(Activity activity, final AuthCallBack authCallBack) {
        tvsManager.init(activity);
        tvsManager.refreshLoginToken(new TVSManager.TVSLoginListener() {
            @Override
            public void onSuccess(LoginInfo loginInfo) {
                callBackSuccess(authCallBack, loginInfo);
            }

            @Override
            public void onError() {
                callBackError(authCallBack);
            }

            @Override
            public void onCancel() {
                callBackCancel(authCallBack);
            }
        });
    }

    public void logout() {
        tvsManager.logout();
    }

    public boolean isTokenExist(Activity activity) {
        return tvsManager.hasExistToken(activity);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        tvsManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onResume(){
        tvsManager.onResume();
    }

    public interface AuthCallBack {
        public void onTVSLoginSuccess(LoginInfo userInfo);

        public void onError();

        public void onCancel();
    }

    public interface BindingCallBack {
        public void onBindingSuccess();

        public void onError();
    }


    private void callBindingSuccess(BindingCallBack bindingCallBack) {
        if (bindingCallBack != null) {
            bindingCallBack.onBindingSuccess();
        }
    }

    private void callBindingError(BindingCallBack bindingCallBack) {
        if (bindingCallBack != null) {
            bindingCallBack.onError();
        }
    }

    private void callBackSuccess(AuthCallBack authCallBack, LoginInfo loginInfo) {
        if (authCallBack != null) {
            authCallBack.onTVSLoginSuccess(loginInfo);
        }
    }

    private void callBackError(AuthCallBack authCallBack) {
        if (authCallBack != null) {
            authCallBack.onError();
        }
    }

    private void callBackCancel(AuthCallBack authCallBack) {
        if (authCallBack != null) {
            authCallBack.onCancel();
        }
    }
}
