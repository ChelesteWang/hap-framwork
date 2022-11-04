/*
 * Copyright (c) 2021, the hapjs-platform Project Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hapjs.mockup;

import org.hapjs.PlatformRuntime;
import org.hapjs.bridge.annotation.DependencyAnnotation;
import org.hapjs.distribution.DistributionProvider;
import org.hapjs.logging.LogProvider;
import org.hapjs.mockup.impl.CanvasProviderImpl;
import org.hapjs.mockup.impl.DistributionProviderImpl;
import org.hapjs.mockup.impl.LogProviderImpl;
import org.hapjs.net.DefaultNetLoaderProviderImpl;
import org.hapjs.net.NetLoaderProvider;
import org.hapjs.render.cutout.CutoutProvider;
import org.hapjs.render.cutout.DefaultCutoutProvider;
import org.hapjs.runtime.ProviderManager;


// import org.hapjs.widgets.canvas.CanvasProvider;
// import org.hapjs.widgets.video.manager.DefaultPlayerManagerProviderImpl;
// import org.hapjs.widgets.video.manager.PlayerManagerProvider;


// app 启动入口 依赖注入后的时机湖入口
@DependencyAnnotation(key = MockupRuntime.PROPERTY_RUNTIME_IMPL_CLASS)
public class MockupRuntime extends PlatformRuntime {

    @Override
    protected void onAllProcessInit() {
        super.onAllProcessInit();

        // 初始化相关功能无用功能可以干掉

        // 调用方法单例
        ProviderManager pm = ProviderManager.getDefault();
        // rpk 分发使用
        pm.addProvider(DistributionProvider.NAME, new DistributionProviderImpl(mContext));
        // log 日志使用
        pm.addProvider(LogProvider.NAME, new LogProviderImpl());
        // 屏幕裁剪（水滴屏刘海屏适配）
        pm.addProvider(CutoutProvider.NAME, new DefaultCutoutProvider());
        // pm.addProvider(CanvasProvider.NAME, new CanvasProviderImpl());
        // pm.addProvider(NetLoaderProvider.NAME, new DefaultNetLoaderProviderImpl());
        // pm.addProvider(PlayerManagerProvider.NAME, new DefaultPlayerManagerProviderImpl());
    }
}
