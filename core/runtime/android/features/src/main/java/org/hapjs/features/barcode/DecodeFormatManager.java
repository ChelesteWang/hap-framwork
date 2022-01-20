/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hapjs.features.barcode;

import com.google.zxing.BarcodeFormat;
import java.util.Vector;

final class DecodeFormatManager {

    static final Vector<BarcodeFormat> PRODUCT_FORMATS;
    static final Vector<BarcodeFormat> ONE_D_FORMATS;
    static final Vector<BarcodeFormat> QR_CODE_FORMATS;
    static final Vector<BarcodeFormat> DATA_MATRIX_FORMATS;
    static final Vector<BarcodeFormat> INDUSTRIAL_FORMATS;

    static {
        PRODUCT_FORMATS = new Vector(6);
        PRODUCT_FORMATS.add(BarcodeFormat.UPC_A);
        PRODUCT_FORMATS.add(BarcodeFormat.UPC_E);
        PRODUCT_FORMATS.add(BarcodeFormat.EAN_13);
        PRODUCT_FORMATS.add(BarcodeFormat.EAN_8);
        PRODUCT_FORMATS.add(BarcodeFormat.RSS_14);
        PRODUCT_FORMATS.add(BarcodeFormat.RSS_EXPANDED);
        INDUSTRIAL_FORMATS = new Vector<>(5);
        INDUSTRIAL_FORMATS.add(BarcodeFormat.CODE_39);
        INDUSTRIAL_FORMATS.add(BarcodeFormat.CODE_93);
        INDUSTRIAL_FORMATS.add(BarcodeFormat.CODE_128);
        INDUSTRIAL_FORMATS.add(BarcodeFormat.ITF);
        INDUSTRIAL_FORMATS.add(BarcodeFormat.CODABAR);
        ONE_D_FORMATS =
                new Vector<BarcodeFormat>(PRODUCT_FORMATS.size() + INDUSTRIAL_FORMATS.size());
        ONE_D_FORMATS.addAll(PRODUCT_FORMATS);
        ONE_D_FORMATS.addAll(INDUSTRIAL_FORMATS);
        QR_CODE_FORMATS = new Vector<BarcodeFormat>(1);
        QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE);
        DATA_MATRIX_FORMATS = new Vector<BarcodeFormat>(1);
        DATA_MATRIX_FORMATS.add(BarcodeFormat.DATA_MATRIX);
    }

    private DecodeFormatManager() {
    }
}
