package com.atoolkit.aqrcode.widget

import com.google.zxing.Result

internal sealed interface AScanUiState {
    object InitUiState : AScanUiState
    class PermissionUiState(val permission: String) : AScanUiState

    class ResultUiState(val result: Result) : AScanUiState
}