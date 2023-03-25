package com.atoolkit.apermission

/**
 * Description: 权限申请解释信息
 * Author: summer
 * Date: 2023/3/20 19:41
 * LastModifyTime:
 */
data class ExplanationData(
    val explainTitle: String, // 解释标题
    val explainMsg: String, // 解释信息内容
    val rightText: String, // 右侧按钮文字
    val leftText: String? = null // 左侧按钮文字，可以为null，此时就只有一个按钮
)

/**
 * Description: 权限数据
 * Author: summer
 * Date: 2023/3/20 19:42
 * LastModifyTime:
 */
data class PermissionData(
    val permissions: Array<String>, // 待申请的权限
    val isAbort: Boolean // 如果该权限不拒绝后，时候还继续申请后续的权限（如果有的话），此字段只有在单权限申请模式下有效
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermissionData

        if (!permissions.contentEquals(other.permissions)) return false
        if (isAbort != other.isAbort) return false

        return true
    }

    override fun hashCode(): Int {
        var result = permissions.contentHashCode()
        result = 31 * result + isAbort.hashCode()
        return result
    }
}

/**
 * Description: 权限申请的数据类，包含权限数据和申请说明数据；权限数据必须有，通过 [withPermission] 或 [Builder]构造方法获取Builder实例时传入；
 * 其他通过构造各种需要的参数，最后调用[Builder]的build()方法构建实例；申请说明数据可以为null，则认为申请权限前不需要弹窗说明；
 * Author: summer
 * Date: 2023/3/20 20:24
 * LastModifyTime:
 */
class APermission private constructor() {
    internal lateinit var permissionData: PermissionData

    internal var explanationData: ExplanationData? = null

    fun getPermissions(): Array<String> {
        return permissionData.permissions
    }

    class Builder(private val permission: Array<String>) {
        private var isAbort = false
        private var explainTitle: String? = null
        private var explainMsg: String? = null
        private var leftText: String? = null
        private var rightText: String? = null

        /**
         * Description: 当前权限被拒绝后，是否中断后续权限的申请，只在单权限申请模式下有效
         * Author: summer
         */
        fun isAbortWhenDeny(isAbort: Boolean): Builder {
            this.isAbort = isAbort
            return this
        }

        /**
         * Description: 权限申请解释弹窗的标题
         * Author: summer
         */
        fun explainTitle(title: String): Builder {
            this.explainTitle = title
            return this
        }

        /**
         * Description: 权限申请解释弹窗的信息
         * Author: summer
         */
        fun explainMsg(msg: String): Builder {
            this.explainMsg = msg
            return this
        }

        /**
         * Description: 权限申请解释弹窗的左侧按钮文案
         * Author: summer
         */
        fun explainLeftText(leftText: String): Builder {
            this.leftText = leftText
            return this
        }

        /**
         * Description: 权限申请解释弹窗的右侧按钮文案
         * Author: summer
         */
        fun explainRightText(rightText: String): Builder {
            this.rightText = rightText
            return this
        }

        /**
         * 构建权限数据
         */
        fun build(): APermission {
            val aPermission = APermission()
            aPermission.permissionData = PermissionData(permission, isAbort)
            explainMsg?.let { msg ->
                aPermission.explanationData = ExplanationData(
                    explainTitle = explainTitle ?: (application.getString(R.string.permission_request)),
                    explainMsg = msg,
                    rightText = rightText ?: (application.getString(R.string.permission_ok)),
                    leftText = leftText
                )
            }
            return aPermission
        }
    }
}