package com.atoolkit.apermission.uistate

import com.atoolkit.apermission.PermissionData

sealed interface APermissionUiState {
    object InitUiState : APermissionUiState

    data class ResultUiState(
        val grantedPermissions: List<String>, val deniedPermissions: List<String>
    ) : APermissionUiState

    data class ExplainUiState(
        val title: String,
        val msg: String,
        val rightText: String,
        val leftText: String? = null,
        val callback: (Boolean) -> Unit
    ) : APermissionUiState

    data class PermissionUiState(
        val permissionData: PermissionData
    ) : APermissionUiState
}


