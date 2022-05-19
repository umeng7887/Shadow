package com.tencent.shadow.sample.plugin.app.lib;

import static com.tencent.shadow.sample.plugin.app.lib.gallery.cases.UseCaseManager.useCases;

import android.app.Application;
import android.content.Context;
import android.os.Debug;
import android.util.Log;

import com.tencent.shadow.sample.plugin.app.lib.gallery.cases.UseCaseManager;
import com.tencent.shadow.sample.plugin.app.lib.gallery.cases.entity.UseCase;
import com.tencent.shadow.sample.plugin.app.lib.gallery.cases.entity.UseCaseCategory;
import com.tencent.shadow.sample.plugin.app.lib.usecases.activity.TestActivityOnCreate;
import com.tencent.shadow.sample.plugin.app.lib.usecases.activity.TestActivityOptionMenu;
import com.tencent.shadow.sample.plugin.app.lib.usecases.activity.TestActivityOrientation;
import com.tencent.shadow.sample.plugin.app.lib.usecases.activity.TestActivityReCreate;
import com.tencent.shadow.sample.plugin.app.lib.usecases.activity.TestActivityReCreateBySystem;
import com.tencent.shadow.sample.plugin.app.lib.usecases.activity.TestActivitySetTheme;
import com.tencent.shadow.sample.plugin.app.lib.usecases.activity.TestActivityWindowSoftMode;
import com.tencent.shadow.sample.plugin.app.lib.usecases.context.ActivityContextSubDirTestActivity;
import com.tencent.shadow.sample.plugin.app.lib.usecases.context.ApplicationContextSubDirTestActivity;
import com.tencent.shadow.sample.plugin.app.lib.usecases.dialog.TestDialogActivity;
import com.tencent.shadow.sample.plugin.app.lib.usecases.fragment.TestDialogFragmentActivity;
import com.tencent.shadow.sample.plugin.app.lib.usecases.fragment.TestDynamicFragmentActivity;
import com.tencent.shadow.sample.plugin.app.lib.usecases.fragment.TestXmlFragmentActivity;
import com.tencent.shadow.sample.plugin.app.lib.usecases.host_communication.PluginUseHostClassActivity;
import com.tencent.shadow.sample.plugin.app.lib.usecases.packagemanager.TestPackageManagerActivity;
import com.tencent.shadow.sample.plugin.app.lib.usecases.provider.TestDBContentProviderActivity;
import com.tencent.shadow.sample.plugin.app.lib.usecases.provider.TestFileProviderActivity;
import com.tencent.shadow.sample.plugin.app.lib.usecases.receiver.TestDynamicReceiverActivity;
import com.tencent.shadow.sample.plugin.app.lib.usecases.receiver.TestReceiverActivity;
import com.tencent.shadow.sample.plugin.app.lib.usecases.webview.WebViewActivity;
import com.umeng.analytics.process.DBPathAdapter;
import com.umeng.commonsdk.UMConfigure;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UseCaseApplication extends Application {
    public static final String DEFAULT_APPKEY = "64632267";
    public static final String DEFAULT_CHANNEL = "Aliyun";
    public static final String DEFAULT_HOST = "https://log-api.aplus.emas-poc.com";

    @Override
    public void onCreate() {
        super.onCreate();
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
                return "sample-plugin-app";
            }

            @Override
            public String getPostfix4DBPath() {
                return "";
            }
        };
        UMConfigure.setDBPathAdapter(customAdapter);
        initCase();

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

    private static void initCase() {

        if (UseCaseManager.sInit) {
            throw new RuntimeException("不能重复调用init");
        }

        UseCaseManager.sInit = true;

        UseCaseCategory activityCategory = new UseCaseCategory("Activity测试用例", new UseCase[]{
                new TestActivityOnCreate.Case(),
                new TestActivityReCreate.Case(),
                new TestActivityReCreateBySystem.Case(),
                new TestActivityOrientation.Case(),
                new TestActivityWindowSoftMode.Case(),
                new TestActivitySetTheme.Case(),
                new TestActivityOptionMenu.Case(),
                new WebViewActivity.Case()
        });
        useCases.add(activityCategory);

        UseCaseCategory broadcastReceiverCategory = new UseCaseCategory("广播测试用例", new UseCase[]{
                new TestReceiverActivity.Case(),
                new TestDynamicReceiverActivity.Case()
        });
        useCases.add(broadcastReceiverCategory);


        UseCaseCategory providerCategory = new UseCaseCategory("ContentProvider测试用例", new UseCase[]{
                new TestDBContentProviderActivity.Case(),
                new TestFileProviderActivity.Case()
        });
        useCases.add(providerCategory);


        UseCaseCategory fragmentCategory = new UseCaseCategory("fragment测试用例", new UseCase[]{
                new TestDynamicFragmentActivity.Case(),
                new TestXmlFragmentActivity.Case(),
                new TestDialogFragmentActivity.Case()
        });
        useCases.add(fragmentCategory);

        UseCaseCategory dialogCategory = new UseCaseCategory("Dialog测试用例", new UseCase[]{
                new TestDialogActivity.Case(),
        });
        useCases.add(dialogCategory);

        UseCaseCategory packageManagerCategory = new UseCaseCategory("PackageManager测试用例", new UseCase[]{
                new TestPackageManagerActivity.Case(),
        });
        useCases.add(packageManagerCategory);


        UseCaseCategory contextCategory = new UseCaseCategory("Context相关测试用例", new UseCase[]{
                new ActivityContextSubDirTestActivity.Case(),
                new ApplicationContextSubDirTestActivity.Case(),
        });
        useCases.add(contextCategory);

        UseCaseCategory communicationCategory = new UseCaseCategory("插件和宿主通信相关测试用例", new UseCase[]{
                new PluginUseHostClassActivity.Case(),
        });
        useCases.add(communicationCategory);
    }
}
