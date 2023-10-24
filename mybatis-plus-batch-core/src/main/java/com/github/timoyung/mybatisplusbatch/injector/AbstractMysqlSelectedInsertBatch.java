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
package com.github.timoyung.mybatisplusbatch.injector;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.github.timoyung.mybatisplusbatch.enums.SqlMethod;
import com.github.timoyung.mybatisplusbatch.toolkit.SqlScriptHelper;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @author timoyung
 * @since 1.1.0
 */
public abstract class AbstractMysqlSelectedInsertBatch extends AbstractMethod {
    private static final String FOREACH_TPL = "<foreach collection=\"list\" item=\"item\" index=\"index\" separator=\",\">\n " +
            "<if test=\"index != 0\"> %s </if> \n </foreach>";
    KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;

    public AbstractMysqlSelectedInsertBatch(String sqlMethod) {
        super(sqlMethod);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {

        SqlMethod sqlMethod = this.getSqlMethod();
        String columnScript = SqlScriptUtils.convertTrim(tableInfo.getAllInsertSqlColumnMaybeIf("list[0]."),
                LEFT_BRACKET, RIGHT_BRACKET, null, COMMA);

        String values = SqlScriptHelper.getAllInsertSqlPropertyMaybeIf(tableInfo, "list[0].", "item.");
        String valuesScript = SqlScriptUtils.convertTrim(values,
                LEFT_BRACKET, RIGHT_BRACKET, null, COMMA);
        String newValues = String.format(FOREACH_TPL, valuesScript);


        String keyProperty = null;
        String keyColumn = null;
        // 表包含主键处理逻辑,如果不包含主键当普通字段处理
        if (StringUtils.isNotBlank(tableInfo.getKeyProperty())) {
            if (tableInfo.getIdType() == IdType.AUTO) {
                /* 自增主键 */
                keyGenerator = Jdbc3KeyGenerator.INSTANCE;
                keyProperty = tableInfo.getKeyProperty();
                keyColumn = tableInfo.getKeyColumn();
            } else {
                if (null != tableInfo.getKeySequence()) {
                    keyGenerator = TableInfoHelper.genKeyGenerator(this.methodName, tableInfo, builderAssistant);
                    keyProperty = tableInfo.getKeyProperty();
                    keyColumn = tableInfo.getKeyColumn();
                }
            }
        }
        String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), columnScript, newValues);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addInsertMappedStatement(mapperClass, modelClass, sqlMethod.getMethod(), sqlSource, keyGenerator, keyProperty, keyColumn);
    }

    protected abstract SqlMethod getSqlMethod();
}
