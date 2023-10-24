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
package com.github.timoyung.mybatisplusbatch.enums;

/**
 * @author timoyung
 * @since 1.0.0
 */
public enum SqlMethod {

    /**
     * 插入
     */
    INSERT_BATCH_SELECTED("insertBatchSelected", "有选择性的批量插入", "<script>\nINSERT INTO %s %s VALUES %s\n</script>"),
    INSERT_BATCH_SELECTED_WITH_TEMPLATE("insertBatchSelectedWithTemplate", "使用模板有选择性的批量插入", "<script>\nINSERT INTO %s %s VALUES %s\n</script>");

    private final String method;
    private final String desc;
    private final String sql;

    SqlMethod(String method, String desc, String sql) {
        this.method = method;
        this.desc = desc;
        this.sql = sql;
    }

    public String getMethod() {
        return method;
    }

    public String getDesc() {
        return desc;
    }

    public String getSql() {
        return sql;
    }
}
