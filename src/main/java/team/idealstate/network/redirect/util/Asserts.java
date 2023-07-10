/*
 *    Copyright 2023 ideal-state
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package team.idealstate.network.redirect.util;

/**
 * <p>断言相关工具</p>
 *
 * <p>Created on 2023/2/16 19:50</p>
 *
 * @author ketikai
 * @since 0.0.1
 */
public abstract class Asserts {

    /**
     * 断言表达式的结果为 true
     *
     * @param expr 表达式
     * @param msg  异常信息
     */
    public static void isTrue(Boolean expr, String msg) {
        if (!Boolean.TRUE.equals(expr)) {
            if (msg == null) {
                throw new IllegalArgumentException();
            }
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * 断言对象为空
     *
     * @param obj     对象
     * @param argName 参数名
     */
    public static void isNull(Object obj, String argName) {
        isNull(obj, argName, "must be null");
    }

    /**
     * 断言对象为空
     *
     * @param obj     对象
     * @param argName 参数名
     * @param msg     异常信息
     */
    public static void isNull(Object obj, String argName, String msg) {
        isTrue(obj == null, generateMsg(argName, msg));
    }

    /**
     * 断言对象非空
     *
     * @param obj     对象
     * @param argName 参数名
     */
    public static void notNull(Object obj, String argName) {
        notNull(obj, argName, "must not be null");
    }

    /**
     * 断言对象非空
     *
     * @param obj     对象
     * @param argName 参数名
     * @param msg     异常信息
     */
    public static void notNull(Object obj, String argName, String msg) {
        isTrue(obj != null, generateMsg(argName, msg));
    }

    /**
     * 断言字符串有文本内容（不包括空白字符）
     *
     * @param str     字符
     * @param argName 参数名
     */
    public static void hasText(String str, String argName) {
        hasText(str, argName, "must be a valid text");
    }

    /**
     * 断言字符串有文本内容（不包括空白字符）
     *
     * @param str     字符
     * @param argName 参数名
     * @param msg     异常信息
     */
    public static void hasText(String str, String argName, String msg) {
        isTrue(!(str.isEmpty() || str.isBlank()), generateMsg(argName, msg));
    }

    private static String generateMsg(String argName, String msg) {
        if (msg == null) {
            throw new RuntimeException("断言提示消息不允许为空");
        }
        argName = argName == null ? "" : "[argName: " + argName + "] ";
        return argName + msg;
    }
}
