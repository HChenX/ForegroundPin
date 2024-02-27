package com.hchen.foregroundpin.callback;

public interface IResult {
    /**
     * 重写本方法可以实时获取常规流数据。
     *
     * @param out 常规流数据
     */
    default void readOutput(String out, boolean finish) {
    }

    /**
     * 重写本方法可以实时获取错误流数据。
     *
     * @param out 错误流数据
     */
    default void readError(String out) {
    }

    /**
     * 重写本方法可以实时获取每条命令的执行结果。
     *
     * @param command 命令
     * @param result  结果
     */
    default void result(String command, int result) {
    }

    /**
     * 无 Root 权限时的报错回调。
     *
     * @param reason 原因
     */
    default void error(String reason) {
    }
}
