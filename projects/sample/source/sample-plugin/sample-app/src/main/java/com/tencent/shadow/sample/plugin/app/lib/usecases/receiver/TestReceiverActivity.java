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

package com.tencent.shadow.sample.plugin.app.lib.usecases.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tencent.shadow.sample.plugin.app.lib.R;
import com.tencent.shadow.sample.plugin.app.lib.gallery.BaseActivity;
import com.tencent.shadow.sample.plugin.app.lib.gallery.cases.entity.UseCase;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

public class TestReceiverActivity extends BaseActivity {
    Context appContext = null;
    public static class Case extends UseCase {
        @Override
        public String getName() {
            return "静态广播测试";
        }

        @Override
        public String getSummary() {
            return "测试静态广播的发送和接收是否工作正常";
        }

        @Override
        public Class getPageClass() {
            return TestReceiverActivity.class;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = this.getApplicationContext();
        setContentView(R.layout.layout_receiver);
        Button button = findViewById(R.id.button);
        button.setText("测试静态广播发送");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("MobclickRT", "--->>> 插件进程中触发 SubProcessEkv 事件");
                UMConfigure.getUMIDString(appContext);
                MobclickAgent.onEvent(appContext, "SubProcessEkv");
                Intent intent = new Intent("com.tencent.test.action");
                intent.putExtra("msg", "收到测试静态广播发送");
                sendBroadcast(intent);
            }
        });
    }

}
