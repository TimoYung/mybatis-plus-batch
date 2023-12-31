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


import com.github.timoyung.mybatisplusbatch.enums.SqlMethod;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;


/**
 * @author timoyung
 * @since 1.0.0
 */
public class MysqlSelectedInsertBatch extends AbstractMysqlSelectedInsertBatch {
    private static final String FOREACH_TPL = "<foreach collection=\"list\" item=\"item\" index=\"index\" separator=\",\">\n " +
            "<if test=\"index != 0\"> %s </if> \n </foreach>";

    KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;
    public MysqlSelectedInsertBatch() {
        super(SqlMethod.INSERT_BATCH_SELECTED.getMethod());
    }

    @Override
    protected SqlMethod getSqlMethod() {
        return SqlMethod.INSERT_BATCH_SELECTED;
    }
}
