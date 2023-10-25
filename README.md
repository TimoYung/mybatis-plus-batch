# mybatis-plus Mysql批量插入扩展

### 项目背景

　　由于mybatis-plus项目提供的批量插入方式的底层实现实际上在内部做循环单条插入，这就导致在大批次数据插入时<br/>
的效率非常差，所以本项目是对mybatis-plus的批量插入方式做增强。


### 优点与缺点
* 优点<br/>
能够实现Mysql批量插入语法<br/><br/>

* 缺点<br/>
只支持Mysql语法的批量插入方式。

### Get Start
1. 引入依赖
    ```xml
            <dependency>
                <groupId>io.github.timoyung</groupId>
                <artifactId>mybatis-plus-batch-core</artifactId>
                <version>Latest Version</version>
            </dependency>
    ```

2. 业务层继承BatchServiceImpl类
    ```java
        public interface UserService extends IBatchService<User> {
   
        }
   
        @Service
        public class UserServiceImpl extends BatchServiceImpl<UserMapper, User> implements UserService {
        
        }

    ```    

3. 使用批量插入方法（默认模板）

    ```java
        User user1 = new User();
        user1.setName("张三");
        user1.setAge(18);

        User user2 = new User();
        user2.setName("李四");
        user2.setAge(88);
        //使用默认的批量插入条数：1000条/次
        this.userService.insertBatch(Arrays.asList(user1, user2));
   
        //指定批量插入条数
        this.userService.insertBatch(Arrays.asList(user1, user2), 500);

    ```    

4. 使用批量插入方法（指定模板）

    ```java
        User template = new User();
        template.setName("AA");
        template.setAge(20);
        template.setNickName("小A")
   
        User user1 = new User();
        user1.setName("张三");
        user1.setAge(18);
        user1.setNickName("小张")

        User user2 = new User();
        user2.setName("李四");
        user2.setAge(88);
        
        //使用默认的批量插入条数：1000条/次
        this.userService.insertBatchWithTemplate(Arrays.asList(user1, user2), template);
   
   
        //指定批量插入条数
        this.userService.insertBatchWithTemplate(Arrays.asList(user1, user2), template, 500);
    ```    

### 特别说明

<strong>项目中绝大部代码是mybatis-plus项目中直接拿过来做的修改</strong>

