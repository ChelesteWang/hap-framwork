/*
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.stetho.inspector.protocol.module;

import android.content.Context;
import androidx.annotation.Nullable;
import com.facebook.stetho.common.ProcessUtil;
import com.facebook.stetho.inspector.domstorage.SharedPreferencesHelper;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcPeer;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcResult;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsMethod;
import com.facebook.stetho.inspector.screencast.ScreencastDispatcher;
import com.facebook.stetho.json.ObjectMapper;
import com.facebook.stetho.json.annotation.JsonProperty;
import com.facebook.stetho.json.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.hapjs.inspector.ReportInspectorInfo;
import org.hapjs.inspector.V8Inspector;
import org.json.JSONObject;

public class Page implements ChromeDevtoolsDomain {
    private final Context mContext;
    private final ObjectMapper mObjectMapper = new ObjectMapper();
    @Nullable
    private ScreencastDispatcher mScreencastDispatcher;

    public Page(Context context) {
        mContext = context;
    }

    private static FrameResourceTree createSimpleFrameResourceTree(
            String id, String parentId, String name, String securityOrigin) {
        Frame frame = new Frame();
        frame.id = id;
        frame.parentId = parentId;
        frame.loaderId = "1";
        frame.name = name;
        frame.url = getSourcemapUrl(); // INSPECTOR ADD
        frame.securityOrigin = securityOrigin;
        frame.mimeType = "text/plain";
        FrameResourceTree tree = new FrameResourceTree();
        tree.frame = frame;
        tree.resources = Collections.emptyList();
        tree.childFrames = null;
        return tree;
    }

    // INSPECTOR ADD:
    private static String getSourcemapUrl() {
        return "http://" + ReportInspectorInfo.getInstance().getHostURL() + "/sourcemap/";
    }

    @ChromeDevtoolsMethod
    public void enable(JsonRpcPeer peer, JSONObject params) {
        // INSPECTOR DEL BEGIN
        // notifyExecutionContexts(peer);
        // sendWelcomeMessage(peer);
        // END
    }

    @ChromeDevtoolsMethod
    public void disable(JsonRpcPeer peer, JSONObject params) {
    }

    private void notifyExecutionContexts(JsonRpcPeer peer) {
        ExecutionContextDescription context = new ExecutionContextDescription();
        context.frameId = "1";
        context.id = 1;
        ExecutionContextCreatedParams params = new ExecutionContextCreatedParams();
        params.context = context;
        peer.invokeMethod("Runtime.executionContextCreated", params, null /* callback */);
    }

    private void sendWelcomeMessage(JsonRpcPeer peer) {
        Console.ConsoleMessage message = new Console.ConsoleMessage();
        message.source = Console.MessageSource.JAVASCRIPT;
        message.level = Console.MessageLevel.LOG;
        message.text =
            // Note: not using Android resources so we can maintain .jar distribution for now.
            "_____/\\\\\\\\\\\\\\\\\\\\\\_______________________________________________/\\\\\\_______________________\n"
                +
                " ___/\\\\\\/////////\\\\\\____________________________________________\\/\\\\\\_______________________\n"
                +
                "  __\\//\\\\\\______\\///______/\\\\\\_________________________/\\\\\\______\\/\\\\\\_______________________\n"
                +
                "   ___\\////\\\\\\__________/\\\\\\\\\\\\\\\\\\\\\\_____/\\\\\\\\\\\\\\\\___/\\\\\\\\\\\\\\\\\\\\\\_\\/\\\\\\_"
                    + "____________/\\\\\\\\\\____\n"
                + "    ______\\////\\\\\\______\\////\\\\\\////____/\\\\\\/////\\\\\\_\\////\\\\\\////__\\/\\\\\\\\\\\\\\\\\\\\_"
                    + "___/\\\\\\///\\\\\\__\n"
                +
                "     _________\\////\\\\\\______\\/\\\\\\_______/\\\\\\\\\\\\\\\\\\\\\\_____\\/\\\\\\______\\/\\\\\\/////\\\\\\"
                    + "__/\\\\\\__\\//\\\\\\_\n"
                +
                "      __/\\\\\\______\\//\\\\\\_____\\/\\\\\\_/\\\\__\\//\\\\///////______\\/\\\\\\_/\\\\__\\/\\\\\\___\\/\\\\\\"
                    + "_\\//\\\\\\__/\\\\\\__\n"
                +
                "       _\\///\\\\\\\\\\\\\\\\\\\\\\/______\\//\\\\\\\\\\____\\//\\\\\\\\\\\\\\\\\\\\____\\//\\\\\\\\\\___\\/\\\\"
                    + "\\___\\/\\\\\\__\\///\\\\\\\\\\/___\n"
                +
                "        ___\\///////////_________\\/////______\\//////////______\\/////____\\///____\\///_____\\/////_____\n"
                + "         Welcome to Stetho\n"
                + "          Attached to "
                + ProcessUtil.getProcessName()
                + "\n";
        Console.MessageAddedRequest messageAddedRequest = new Console.MessageAddedRequest();
        messageAddedRequest.message = message;
        peer.invokeMethod("Console.messageAdded", messageAddedRequest, null /* callback */);
    }

    // Dog science...
    @ChromeDevtoolsMethod
    public JsonRpcResult getResourceTree(JsonRpcPeer peer, JSONObject params) {
        // The DOMStorage module expects one key/value store per "security origin" which has a 1:1
        // relationship with resource tree frames.
        List<String> prefsTags = SharedPreferencesHelper.getSharedPreferenceTags(mContext);
        Iterator<String> prefsTagsIter = prefsTags.iterator();

        FrameResourceTree tree =
                createSimpleFrameResourceTree(
                        "1", null /* parentId */, "Stetho",
                        V8Inspector.getInstance().getDebugPackage());
        // INSPECTOR MOD
        // prefsTagsIter.hasNext() ? prefsTagsIter.next() : "");
        if (tree.childFrames == null) {
            tree.childFrames = new ArrayList<FrameResourceTree>();
        }

        // INSPECTOR DELETE
        // int nextChildFrameId = 1;
        // while (prefsTagsIter.hasNext()) {
        //   String frameId = "1." + (nextChildFrameId++);
        //   String prefsTag = prefsTagsIter.next();
        //   FrameResourceTree child = createSimpleFrameResourceTree(
        //       frameId,
        //       "1",
        //      "Child #" + frameId,
        //      prefsTag);
        //   tree.childFrames.add(child);
        // }
        // INSPECTOR END

        GetResourceTreeParams resultParams = new GetResourceTreeParams();
        resultParams.frameTree = tree;
        return resultParams;
    }
    // END

    @ChromeDevtoolsMethod
    public JsonRpcResult canScreencast(JsonRpcPeer peer, JSONObject params) {
        return new SimpleBooleanResult(true);
    }

    @ChromeDevtoolsMethod
    public JsonRpcResult hasTouchInputs(JsonRpcPeer peer, JSONObject params) {
        return new SimpleBooleanResult(false);
    }

    @ChromeDevtoolsMethod
    public void setDeviceMetricsOverride(JsonRpcPeer peer, JSONObject params) {
    }

    @ChromeDevtoolsMethod
    public void clearDeviceOrientationOverride(JsonRpcPeer peer, JSONObject params) {
    }

    @ChromeDevtoolsMethod
    public void startScreencast(final JsonRpcPeer peer, JSONObject params) {
        final StartScreencastRequest request =
                mObjectMapper.convertValue(params, StartScreencastRequest.class);
        if (mScreencastDispatcher == null) {
            mScreencastDispatcher = new ScreencastDispatcher();
            mScreencastDispatcher.startScreencast(peer, request);
        }
        // INSPECTOR ADD BEGIN:
        if (request != null) {
            org.hapjs.inspector.Input.getInstance()
                    .setScreencastSize(request.maxWidth, request.maxHeight);
        }
        // END
    }

    @ChromeDevtoolsMethod
    public void stopScreencast(JsonRpcPeer peer, JSONObject params) {
        if (mScreencastDispatcher != null) {
            mScreencastDispatcher.stopScreencast();
            mScreencastDispatcher = null;
        }
    }

    @ChromeDevtoolsMethod
    public void screencastFrameAck(JsonRpcPeer peer, JSONObject params) {
        // Nothing to do here, just need to make sure Chrome doesn't get an error that this method
        // isn't implemented
    }

    @ChromeDevtoolsMethod
    public void clearGeolocationOverride(JsonRpcPeer peer, JSONObject params) {
    }

    @ChromeDevtoolsMethod
    public void setTouchEmulationEnabled(JsonRpcPeer peer, JSONObject params) {
    }

    @ChromeDevtoolsMethod
    public void setEmulatedMedia(JsonRpcPeer peer, JSONObject params) {
    }

    @ChromeDevtoolsMethod
    public void setShowViewportSizeOnResize(JsonRpcPeer peer, JSONObject params) {
    }

    public enum ResourceType {
        DOCUMENT("Document"),
        STYLESHEET("Stylesheet"),
        IMAGE("Image"),
        FONT("Font"),
        SCRIPT("Script"),
        XHR("XHR"),
        WEBSOCKET("WebSocket"),
        OTHER("Other");

        private final String mProtocolValue;

        private ResourceType(String protocolValue) {
            mProtocolValue = protocolValue;
        }

        @JsonValue
        public String getProtocolValue() {
            return mProtocolValue;
        }
    }

    private static class GetResourceTreeParams implements JsonRpcResult {
        @JsonProperty(required = true)
        public FrameResourceTree frameTree;
    }

    private static class FrameResourceTree {
        @JsonProperty(required = true)
        public Frame frame;

        @JsonProperty
        public List<FrameResourceTree> childFrames;

        @JsonProperty(required = true)
        public List<Resource> resources;
    }

    private static class Frame {
        @JsonProperty(required = true)
        public String id;

        @JsonProperty
        public String parentId;

        @JsonProperty(required = true)
        public String loaderId;

        @JsonProperty
        public String name;

        @JsonProperty(required = true)
        public String url;

        @JsonProperty(required = true)
        public String securityOrigin;

        @JsonProperty(required = true)
        public String mimeType;
    }

    private static class Resource {
        // Incomplete...
    }

    private static class ExecutionContextCreatedParams {
        @JsonProperty(required = true)
        public ExecutionContextDescription context;
    }

    private static class ExecutionContextDescription {
        @JsonProperty(required = true)
        public String frameId;

        @JsonProperty(required = true)
        public int id;
    }

    public static class ScreencastFrameEvent {
        @JsonProperty(required = true)
        public String data;

        @JsonProperty(required = true)
        public ScreencastFrameEventMetadata metadata;
    }

    public static class ScreencastFrameEventMetadata {
        @JsonProperty(required = true)
        public int pageScaleFactor;

        @JsonProperty(required = true)
        public int offsetTop;

        @JsonProperty(required = true)
        public int deviceWidth;

        @JsonProperty(required = true)
        public int deviceHeight;

        @JsonProperty(required = true)
        public int scrollOffsetX;

        @JsonProperty(required = true)
        public int scrollOffsetY;
    }

    public static class StartScreencastRequest {
        @JsonProperty
        public String format;
        @JsonProperty
        public int quality;
        @JsonProperty
        public int maxWidth;
        @JsonProperty
        public int maxHeight;
    }

    public static class ScreencastVisibilityChangedEvent {
        @JsonProperty(required = true)
        public boolean visible;
    }
}
