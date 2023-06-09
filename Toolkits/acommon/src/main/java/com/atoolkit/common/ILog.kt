package com.atoolkit.common


/**
 * Author:summer
 * Time: 2023/3/18 19:21
 * Description: ILog是log的抽象接口，用来规范所有工具库中日志使用；
 */
interface ILog {
    /**
     * v级别的log
     */
    fun v(tag: String? = null, msg: String, t: Throwable? = null)

    /**
     * d级别的log
     */
    fun d(tag: String? = null, msg: String, t: Throwable? = null)

    /**
     * i级别的log
     */
    fun i(tag: String? = null, msg: String, t: Throwable? = null)

    /**
     * w级别的log
     */
    fun w(tag: String? = null, msg: String, t: Throwable? = null)

    /**
     * e级别的log
     */
    fun e(tag: String? = null, msg: String, t: Throwable? = null)
}