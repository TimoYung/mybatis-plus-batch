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

package com.github.timoyung.mybatisplusbatch.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author timoyung
 * @since 1.0.0
 */
public interface IBatchService<T> extends IService<T> {

    /**
     * 批量插入，使用插入列表的第一个实体类作为模板
     * @param entityList 待插入的实体列表
     * @return
     */
    default boolean insertBatch(List<T> entityList){
        return this.insertBatch(entityList, DEFAULT_BATCH_SIZE);
    };

    /**
     * 批量插入，使用插入列表的第一个实体类作为模板
     * @param entityList 待插入的实体列表
     * @param batchSize 指定批次插入条数
     * @return
     */
    boolean insertBatch(List<T> entityList, int batchSize);

    /**
     * 批量插入，通过指定实体类作为插入模板
     * @param entityList 待插入的实体列表
     * @param templateEntity 使用做模板的实体类
     * @return
     */
    default  boolean insertBatchWithTemplate(List<T> entityList, T templateEntity){
        return insertBatchWithTemplate(entityList, templateEntity, DEFAULT_BATCH_SIZE);
    }

    /**
     * 批量插入，通过指定实体类作为插入模板
     * @param entityList 待插入的实体列表
     * @param templateEntity 使用做模板的实体类
     * @param batchSize 指定批次插入条数
     * @return
     */
    boolean insertBatchWithTemplate(List<T> entityList, T templateEntity, int batchSize);
}
