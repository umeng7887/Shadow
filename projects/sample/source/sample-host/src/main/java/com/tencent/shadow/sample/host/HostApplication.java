/*
 * Tencent is pleased to support the open source community by making Tencent Shadow available.
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.tencent.shadow.sample.host;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.webkit.WebView;

import com.tencent.shadow.core.common.LoggerFactory;
import com.tencent.shadow.dynamic.host.DynamicRuntime;
import com.tencent.shadow.dynamic.host.PluginManager;
import com.tencent.shadow.sample.host.lib.HostUiLayerProvider;
import com.tencent.shadow.sample.host.manager.Shadow;
import com.umeng.analytics.process.DBPathAdapter;
import com.umeng.commonsdk.UMConfigure;

import java.io.File;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.os.Process.myPid;

public class HostApplication extends Application {
    private static HostApplication sApp;

    private PluginManager mPluginManager;
    public static final String DEFAULT_APPKEY = "64632267";
    public static final String DEFAULT_CHANNEL = "Aliyun";
    public static final String DEFAULT_HOST = "https://log-api.aplus.emas-poc.com";

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        /* 初始化QT SDK */
        UMConfigure.setCustomDomain(DEFAULT_HOST, null);
        UMConfigure.setLogEnabled(true);
        UMConfigure.preInit(this, DEFAULT_APPKEY, DEFAULT_CHANNEL);
        UMConfigure.setProcessEvent(true);

        DBPathAdapter customAdapter = new DBPathAdapter() {
            @Override
            public String getPrefix4DBPath() {
                String result = "";
                result = "ShadowPlugin_";
                return result;
            }

            @Override
            public String getBusinessName4DBPath() {
                return "";
            }

            @Override
            public String getPostfix4DBPath() {
                return "";
            }
        };
        UMConfigure.setDBPathAdapter(customAdapter);

        detectNonSdkApiUsageOnAndroidP();
        setWebViewDataDirectorySuffix();
        LoggerFactory.setILoggerFactory(new AndroidLogLoggerFactory());

        if (isProcess(this, ":plugin")) {
            //在全动态架构中，Activity组件没有打包在宿主而是位于被动态加载的runtime，
            //为了防止插件crash后，系统自动恢复crash前的Activity组件，此时由于没有加载runtime而发生classNotFound异常，导致二次crash
            //因此这里恢复加载上一次的runtime
            DynamicRuntime.recoveryRuntime(this);
        }

        PluginHelper.getInstance().init(this);

        HostUiLayerProvider.init(this);

        final Context appContext = this.getApplicationContext();
        final String useAppkey = DEFAULT_APPKEY;
        final String useChannel = DEFAULT_CHANNEL;
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                Log.i("MobclickRT", "--->>> 延迟5秒调用初始化接口：UMConfigure.init() ");
                UMConfigure.init(appContext, useAppkey, useChannel, UMConfigure.DEVICE_TYPE_PHONE, null);
            }
        }, 5, TimeUnit.SECONDS);
    }

    private static void setWebViewDataDirectorySuffix() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        WebView.setDataDirectorySuffix(Application.getProcessName());
    }

    private static void detectNonSdkApiUsageOnAndroidP() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        builder.detectNonSdkApiUsage();
        StrictMode.setVmPolicy(builder.build());
    }

    public static HostApplication getApp() {
        return sApp;
    }

    public void loadPluginManager(File apk) {
        if (mPluginManager == null) {
            mPluginManager = Shadow.getPluginManager(apk);
        }
    }

    public PluginManager getPluginManager() {
        return mPluginManager;
    }

    private static boolean isProcess(Context context, String processName) {
        String currentProcName = "";
        ActivityManager manager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == myPid()) {
                currentProcName = processInfo.processName;
                break;
            }
        }

        return currentProcName.endsWith(processName);
    }
}
