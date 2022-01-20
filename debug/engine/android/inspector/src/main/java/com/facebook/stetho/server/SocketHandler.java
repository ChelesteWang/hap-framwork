/*
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.stetho.server;

import java.io.IOException;

/**
 * @see SecureSocketHandler
 */
public interface SocketHandler {
    /**
     * Server socket has been accepted and a dedicated thread has been allocated to process this
     * callback. Returning from this method or throwing an exception will attempt an orderly shutdown
     * of the socket, however it will not be treated as an error if returning normally.
     */
    void onAccepted(AbsSocket socket) throws IOException;
}
