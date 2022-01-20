/*
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.stetho.inspector.network;

import android.annotation.SuppressLint;
import java.util.ArrayList;
import javax.annotation.Nullable;

public class MimeMatcher<T> {
    private final ArrayList<MimeMatcherRule> mRuleMap = new ArrayList<MimeMatcherRule>();

    /**
     * Add a matching rule in the canonical MIME T form such as "image/*" or a MIME T literal such as
     * "text/html".
     *
     * @param ruleExpression  Expression to match, in the order it was added.
     * @param resultIfMatched Result if this expression matches.
     */
    public void addRule(String ruleExpression, T resultIfMatched) {
        mRuleMap.add(new MimeMatcherRule(ruleExpression, resultIfMatched));
    }

    public void clear() {
        mRuleMap.clear();
    }

    @Nullable
    public T match(String mimeT) {
        int ruleMapN = mRuleMap.size();
        for (int i = 0; i < ruleMapN; i++) {
            MimeMatcherRule rule = mRuleMap.get(i);
            if (rule.match(mimeT)) {
                return rule.getResultIfMatched();
            }
        }
        return null;
    }

    @SuppressLint("BadMethodUse-java.lang.String.length")
    private class MimeMatcherRule {
        private final boolean mHasWildcard;
        private final String mMatchPrefix;
        private final T mResultIfMatched;

        public MimeMatcherRule(String ruleExpression, T resultIfMatched) {
            if (ruleExpression.endsWith("*")) {
                mHasWildcard = true;
                mMatchPrefix = ruleExpression.substring(0, ruleExpression.length() - 1);
            } else {
                mHasWildcard = false;
                mMatchPrefix = ruleExpression;
            }
            if (mMatchPrefix.contains("*")) {
                throw new IllegalArgumentException(
                        "Multiple wildcards present in rule expression " + ruleExpression);
            }
            mResultIfMatched = resultIfMatched;
        }

        public boolean match(String mimeType) {
            // Make sure the string literal matches.
            if (!mimeType.startsWith(mMatchPrefix)) {
                return false;
            }

            // If we have a wildcard and the prefix matches, then we've matched; otherwise if the
            // string literal and the mime T are the same length then we must have a match.
            return (mHasWildcard || mimeType.length() == mMatchPrefix.length());
        }

        public T getResultIfMatched() {
            return mResultIfMatched;
        }
    }
}
