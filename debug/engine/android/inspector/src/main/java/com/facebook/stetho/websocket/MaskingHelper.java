/*
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.stetho.websocket;

class MaskingHelper {
    public static void unmask(byte[] key, byte[] data, int offset, int count) {
        // INSPECTOR ADD BEGIN:
        if (key == null || data == null) {
            return;
        }
        // END
        int index = 0;
        while (count-- > 0) {
            data[offset++] ^= key[index++ % key.length];
        }
    }
}
