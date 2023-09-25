/*
 * Copyright (c) 2011-2023, timoyung.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.timoyung.mybatisplusbatch.toolkit;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;

import java.util.Objects;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.EMPTY;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.NEWLINE;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.COMMA;
import static java.util.stream.Collectors.joining;

/**
 * @author timoyung
 * @since 1.0.0
 */
public class SqlScriptHelper {

    /**
     * 获取所有 insert 时候插入值 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "值" 部位</p>
     *
     * <li> 自动选部位,根据规则会生成 if 标签 </li>
     *
     * @return sql 脚本片段
     */
    public static String getAllInsertSqlPropertyMaybeIf(TableInfo tableInfo, final String condPrefix, String valPrefix) {
        final String newCondPrefix = condPrefix == null ? EMPTY : condPrefix;
        final String newValPrefix =  valPrefix  == null ? EMPTY : valPrefix;
        return getKeyInsertSqlProperty(tableInfo, false, valPrefix, true) + tableInfo.getFieldList().stream()
                .map(i -> getInsertSqlPropertyMaybeIf(i, newCondPrefix, newValPrefix)).filter(Objects::nonNull).collect(joining(NEWLINE));
    }


    /**
     * 获取 insert 时候主键 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "值" 部位</p>
     *
     * @return sql 脚本片段
     */
    public static String getKeyInsertSqlProperty(TableInfo tableInfo, final boolean batch, final String prefix, final boolean newLine) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        if (tableInfo.havePK()) {
            final String prefixKeyProperty = newPrefix + tableInfo.getKeyProperty();
            String keyColumn = SqlScriptUtils.safeParam(prefixKeyProperty) + COMMA;
            if (tableInfo.getIdType() == IdType.AUTO) {
                if (batch) {
                    // 批量插入必须返回空自增情况下
                    return EMPTY;
                }
                return SqlScriptUtils.convertIf(keyColumn, String.format("%s != null", prefixKeyProperty), newLine);
            }
            return keyColumn + (newLine ? NEWLINE : EMPTY);
        }
        return EMPTY;
    }

    /**
     * 获取 insert 时候插入值 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "值" 部位</p>
     *
     * <li> 根据规则会生成 if 标签 </li>
     *
     * @return sql 脚本片段
     */
    public static String getInsertSqlPropertyMaybeIf(TableFieldInfo tfi, final String condPrefix, String valPrefix) {
        final String newCondPrefix = condPrefix == null ? EMPTY : condPrefix;
        final String newValPrefix =  valPrefix  == null ? EMPTY : valPrefix;
        String sqlScript = getInsertSqlProperty(newValPrefix, tfi.getEl());
        if (tfi.isWithInsertFill()) {
            return sqlScript;
        }
        return convertIf(tfi, sqlScript, newCondPrefix + tfi.getProperty());
    }

    /**
     * 获取 insert 时候插入值 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "值" 部位</p>
     *
     * <li> 不生成 if 标签 </li>
     *
     * @return sql 脚本片段
     */
    public static String getInsertSqlProperty(final String prefix, String el) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        return SqlScriptUtils.safeParam(newPrefix + el) + COMMA;
    }

    /**
     * 转换成 if 标签的脚本片段
     *
     * @param sqlScript     sql 脚本片段
     * @return if 脚本片段
     */
    private static String convertIf(TableFieldInfo tfi, final String sqlScript, final String property) {
        final FieldStrategy fieldStrategy = tfi.getInsertStrategy();
        if (fieldStrategy == FieldStrategy.NEVER) {
            return null;
        }
        if (tfi.isPrimitive() || fieldStrategy == FieldStrategy.IGNORED) {
            return sqlScript;
        }
        if (fieldStrategy == FieldStrategy.NOT_EMPTY && tfi.isCharSequence()) {
            return SqlScriptUtils.convertIf(sqlScript, String.format("%s != null and %s != ''", property, property),
                    false);
        }
        return SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", property), false);
    }
}
