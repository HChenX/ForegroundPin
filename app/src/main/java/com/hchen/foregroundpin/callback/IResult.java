/*
 * This file is part of ForegroundPin.

 * ForegroundPin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2024 ForegroundPin Contributions
 */
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
