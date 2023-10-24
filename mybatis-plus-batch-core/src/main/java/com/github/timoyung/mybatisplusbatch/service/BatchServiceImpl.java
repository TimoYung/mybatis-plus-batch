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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.timoyung.mybatisplusbatch.enums.SqlMethod;
import lombok.SneakyThrows;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.mybatis.spring.SqlSessionHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.ArrayList;;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author timoyung
 * @since 1.0.0
 */
public class BatchServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements IBatchService<T>{

    @Override
    @Transactional
    public boolean insertBatch(List<T> entityList, int batchSize) {
        String sqlStatement = getSqlStatement(SqlMethod.INSERT_BATCH_SELECTED.getMethod());
        if(entityList == null || entityList.isEmpty()){
            return false;
        }
        return this.insertBatch(entityList, entityList.get(0), batchSize, sqlStatement);
    }

    @Override
    @Transactional
    public boolean insertBatchWithTemplate(List<T> entityList, T templateEntity, int batchSize) {
        String sqlStatement = getSqlStatement(SqlMethod.INSERT_BATCH_SELECTED_WITH_TEMPLATE.getMethod());
        if(entityList == null || entityList.isEmpty()){
            return false;
        }

        Assert.isFalse(templateEntity == null, "templateEntity must not be null");
        return this.insertBatch(entityList, templateEntity, batchSize, sqlStatement);
    }


    private boolean executeBatch(Class<?> entityClass, Log log, Collection<T> list, T templateEntity, int batchSize, BiConsumer<SqlSession, Collection<T>> consumer) {
        Assert.isFalse(batchSize < 1, "batchSize must not be less than one");
        return !CollectionUtils.isEmpty(list) && executeBatch(entityClass, log, sqlSession -> {
            List<List<T>> lists = this.splitList(list, batchSize);
            for (List<T> ts : lists) {
                ts.add(0, templateEntity);
                consumer.accept(sqlSession, ts);
                sqlSession.flushStatements();
            }

        });
    }


    @SneakyThrows
    private boolean executeBatch(Class<?> entityClass, Log log, Consumer<SqlSession> consumer) {
        SqlSessionFactory sqlSessionFactory = SqlHelper.sqlSessionFactory(entityClass);
        SqlSessionHolder sqlSessionHolder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sqlSessionFactory);
        boolean transaction = TransactionSynchronizationManager.isSynchronizationActive();
        if (sqlSessionHolder != null) {
            SqlSession sqlSession = sqlSessionHolder.getSqlSession();
            //原生无法支持执行器切换，当存在批量操作时，会嵌套两个session的，优先commit上一个session
            //按道理来说，这里的值应该一直为false。
            sqlSession.commit(!transaction);
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        if (!transaction) {
            log.warn("SqlSession [" + sqlSession + "] Transaction not enabled");
        }
        try {
            consumer.accept(sqlSession);
            //非事务情况下，强制commit。
            sqlSession.commit(!transaction);
            return true;
        } catch (Throwable t) {
            sqlSession.rollback();
            Throwable unwrapped = ExceptionUtil.unwrapThrowable(t);
            if (unwrapped instanceof PersistenceException) {
                MyBatisExceptionTranslator myBatisExceptionTranslator
                        = new MyBatisExceptionTranslator(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true);
                Throwable throwable = myBatisExceptionTranslator.translateExceptionIfPossible((PersistenceException) unwrapped);
                if (throwable != null) {
                    throw throwable;
                }
            }
            throw ExceptionUtils.mpe(unwrapped);
        } finally {
            sqlSession.close();
        }
    }

    private String getSqlStatement(String method) {
        return mapperClass.getName() + "." + method;
    }

    private List<List<T>> splitList(Collection<T> collection, int size) {
        final List<List<T>> result = new ArrayList<>();
        ArrayList<T> subList = new ArrayList<>(size);
        for (T t : collection) {
            if (subList.size() >= size) {
                result.add(subList);
                subList = new ArrayList<>(size);
            }
            subList.add(t);
        }
        result.add(subList);
        return result;
    }

    private boolean insertBatch(List<T> entityList, T templateEntity, int batchSize, String sqlStatement) {

        if(entityList.size() == 1){
            T entity = entityList.get(0);
            return baseMapper.insert(entity) > 0;
        }

        return this.executeBatch(this.entityClass, this.log, entityList, templateEntity, batchSize, (sqlSession, entities) -> {
            sqlSession.insert(sqlStatement, entities);
        });
    }
}
