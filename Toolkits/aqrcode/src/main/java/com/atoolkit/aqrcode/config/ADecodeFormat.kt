package com.atoolkit.aqrcode.config

import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import java.util.EnumMap

sealed interface IDecodeFormat

sealed class ADecodeFormat : IDecodeFormat {
    val hints: MutableMap<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)

    init {
        hints[DecodeHintType.TRY_HARDER] = true
        hints[DecodeHintType.CHARACTER_SET] = "UTF-8"
        hints[DecodeHintType.POSSIBLE_FORMATS] = getPossibleFormats()
    }

    abstract fun getPossibleFormats(): List<BarcodeFormat>
}

class AllDecodeFormat() : ADecodeFormat() {
    override fun getPossibleFormats(): List<BarcodeFormat> {
        return listOf(
            BarcodeFormat.AZTEC,
            BarcodeFormat.CODABAR,
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.CODE_128,
            BarcodeFormat.DATA_MATRIX,
            BarcodeFormat.EAN_8,
            BarcodeFormat.EAN_13,
            BarcodeFormat.ITF,
            BarcodeFormat.MAXICODE,
            BarcodeFormat.PDF_417,
            BarcodeFormat.QR_CODE,
            BarcodeFormat.RSS_14,
            BarcodeFormat.RSS_EXPANDED,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.UPC_EAN_EXTENSION,
        )
    }
}

class OneDimensionalFormat() : ADecodeFormat() {
    override fun getPossibleFormats(): List<BarcodeFormat> {
        return listOf(
            BarcodeFormat.CODABAR,
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.CODE_128,
            BarcodeFormat.EAN_8,
            BarcodeFormat.EAN_13,
            BarcodeFormat.ITF,
            BarcodeFormat.RSS_14,
            BarcodeFormat.RSS_EXPANDED,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.UPC_EAN_EXTENSION,
        )
    }
}

class TwoDimensionalFormat() : ADecodeFormat() {
    override fun getPossibleFormats(): List<BarcodeFormat> {
        return listOf(
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX,
            BarcodeFormat.MAXICODE,
            BarcodeFormat.PDF_417,
            BarcodeFormat.QR_CODE
        )
    }
}

class DefaultFormats() : ADecodeFormat() {
    override fun getPossibleFormats(): List<BarcodeFormat> {
        return listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.UPC_A,
            BarcodeFormat.EAN_13,
            BarcodeFormat.CODE_128
        )
    }
}
