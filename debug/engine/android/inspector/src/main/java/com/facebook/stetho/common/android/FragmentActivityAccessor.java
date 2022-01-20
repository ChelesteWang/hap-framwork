/*
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.stetho.common.android;

import android.app.Activity;
import javax.annotation.Nullable;

public interface FragmentActivityAccessor<T extends Activity, S> {
    @Nullable
    S getFragmentManager(T activity);
}
