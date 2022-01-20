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

/**
 * Replaces {@link Database.DatabaseDriver} to enforce that the generic type must extend {@link
 * DatabaseDescriptor}.
 */
public abstract class DatabaseDriver2<T extends DatabaseDescriptor>
        extends BaseDatabaseDriver<T> {
    public DatabaseDriver2(Context context) {
        super(context);
    }
}
