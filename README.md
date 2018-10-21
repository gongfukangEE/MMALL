## 基于 SSM 的 B2C 网络零售平台_服务端

### 1、单点登陆

**基于 Nginx+Redis+Cookie+Jackson+Filter 实现 Tomcat 集群环境的单点登录系统** 

#### 1.1 Http 无状态

Http 是无状态协议，浏览器的每一次请求，服务器都会单独处理，不与之前或之后的请求产生联系。

#### 1.2 Session 机制

浏览器第一次请求服务器，服务器会创建一个 Session，并将这个 Session 的 Id 作为响应的一部分发送给浏览器，浏览器本地通过 K/V 形式的 Cooike 机制来存储 SessionID，并在后续请求中带上 SessionID，这样服务器就能根据 SessionID 判读是不是同一个用户。

Tomcat Session 机制也实现了 Cookie，访问 Tomcat 服务器的时候，浏览器中可以看到一个名字为 Jsessionid 的 Cookie，这就是 Tomcat 的 SessionID。

#### 1.3 单点登陆

单系统登录解决方案的核心是 Cookie，Cookie 携带 SessionID 在浏览器与服务器之间维护会话状态。在集群环境下，我们可以将 Token 和用户信息存储在 Redis 中，依靠 Redis 来维持会话状态。

首先对前台登录表单中传来的用户名和密码到数据库中进行验证，如果正确，则生成该用户对应的 Token，Token 的生成规则可以自定义，我采用的是 “前缀+用户名+当前时间” 的 MD5 哈希值的形式。生成 Token 后，首先将 Token 添加到 Cookie 中，然后将 Token 和序列化后的用户信息存储到 Redis 中；在用户刷新网页或者跳转其他页面时，首先跳转到过滤器 SessionExpireFilter，从 Cookie 中获取 loginToken，根据 loginToken 从 Redis 中获取用户信息，并重置 Token 有效期。

#### 1.4 参考

- [单点登录系统 SSO 解析及开发小结](https://juejin.im/entry/58ada81f0ce463006b254230)
- [单点登录原理与简单实现](http://www.cnblogs.com/ywlaker/p/6113927.html#!comments)

### 2、数据库读写分离

**使用 Spring AOP 实现 MySQL 主从架构的读写分离**

#### 2.1 主从复制

MySQL 的主从复制主要涉及三个线程：binlog 线程，I/O 线程，SQL 线程。主库的 binlog 线程将主库上的数据更改写入二进制文件（binlog）中，从库的 I/O 线程负责不断读取主库的 binlog 日志文件并写入从库的 relaylog 中继日志，然后从库的 SQL 线程读取中继日志并重放其中的 SQL 语句

#### 2.2 读写分离

主服务器用来处理写操作以及最新的读请求，而从服务器用来处理读操作 

读写分离常用代理方式来实现，代理服务器接收应用层传来的读写请求，然后决定转发到哪个服务器。 

MySQL 读写分离能提高性能的原因在于：

- 主从服务器负责各自的读和写，极大程度缓解了锁的争用；
- 从服务器可以配置 MyISAM 引擎，提升查询性能以及节约系统开销；
- 增加冗余，提高可用性。

#### 2.3 AOP 实现读写分离

**原理**

AOP 是 Spring 面向切面编程的特性，它主要是切入点和增强构成。在配置好主从数据源后，将切入点设置为对数据的增删改查请求，这样在接收到请求时，在前置增强 @Before 中获取当前执行方法的方法名，来判断具体请求是读还是写，以 `get,select,count,list,query` 为前缀的方法是 read 请求，使用读库也就是从库；以`add,create,update,delete,remove` 为前缀的方法是 write 请求，使用的是写库也就是主库。

**动态切换数据源**

关于动态切换数据源，主要是通过新建一个继承 Spring 的 AbstractRoutingDataSource 的 dataSource 实现的，并重写 determineCurrentLookupKey 方法，每次通过 dataSource 获取数据库连接的时候，都会根据返回的 key 在 XML 配置文件中找到响应的 connection，比如，key 为 read 就是 readdb。由于每次访问的时候都需要根据 key 来决定选择哪个数据源，这时候就需要使用 ThreadLocal 保证并发情况下，每个线程丢需要找到本该属于自己的 key 的数据源，也就是保证线程安全。

#### 2.4 参考

- [Spring AOP 实现读写分离](http://www.liuhaihua.cn/archives/508020.html)
- [Spring 实现数据库读写分离](http://neoremind.net/2011/06/spring%E5%AE%9E%E7%8E%B0%E6%95%B0%E6%8D%AE%E5%BA%93%E8%AF%BB%E5%86%99%E5%88%86%E7%A6%BB/)
- [使用 Spring AOP 切面解决数据库读写分离](https://blog.csdn.net/linsongbin1/article/details/47167281)
- [使用 Apring AOP 实现 MySQL 数据库读写分离案例分析](https://blog.csdn.net/xlgen157387/article/details/53930382)

### 3、基于 Redis 实现分布式锁

**利用 ShardedJedis 搭建 Redis 集群，构建分布式锁，实现订单超时关闭**

#### 3.1 关闭订单

关闭订单的时候需要更新库存，新库存 = 原库存 + 订单，在获取原库存的时候，需要使用 MySQL 悲观锁

#### 3.2 分布式锁

**Version_1**

*获得锁*

SetNx(key, value): 如果 Key 存在，则返回 0，表示设置失败，获取锁失败；如果 Key 不存在，则返回 1，表示设置成功，获取锁成功；value 为锁有效期

*释放锁*

业务执行完成，主动调用 del 释放锁；锁超时，被动调用 del 删除


*核心代码*

```java
@Scheduled(cron = "0 */1 * * * ?")
public void closeOrderTask {
    log.info("Task start...");
    long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "500"));
    Long setnxResult = RedisShardedPoolUtil
        .setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, 
               String.valueOf(System.currentTimeMillis() + lockTimeout));
    if (setnxResult != null && setnxResult.intValue == 1) {
        closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    } else {
        String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
            RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            Long setnxSecondResult = RedisShardedPoolUtil
        		.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, 
              		 String.valueOf(System.currentTimeMillis() + lockTimeout));
            if (setnxSecondResult != null && setnxSecondResult.intValue == 1) {
                closeOrder(Const.REDIS_LOCK>CLOSE_ORDER_TASK_LOCK);
            } else {
                log.info("The second attempt to get a lock failed...");
            }
        } else {
            log.info("The lock has not expired...");
        }
    }
    log.info("Task ended...");
}
```

*存在问题*

如果进程 T1 获得了锁（执行完 setnx 后），突发故障，断开了与 Redis 的连接，，这是时候进程 P2 和 P3 不断通过比较当前时间与 REDIS_LOCK 的值来检测锁是否已经超时，执行以下流程：

- P2 和 P3 读取 REDIS_LOCK 值，均检测到锁超时
- P2 执行 del 后执行 SETNX 返回 1 ，即 P2 获得锁
- 由于 P3 刚检测到锁超时了，则执行 del 将 P2 设置的 REDIS_LOCK 删掉后执行 SETNX 返回 1，即 P3 获得锁
- 这样 P2 和 P3 就同时获得了锁

**Version_2**

*获得锁*

SetNx(key, value): 如果 Key 存在，则返回 0，表示设置失败，获取锁失败；如果 Key 不存在，则返回 1，表示设置成功，获取锁成功；value 为锁有效期

*释放锁*

业务执行完成，主动调用 del 释放锁；锁超时，被动调用 del 删除

*核心代码*

```java
@Scheduled(cron = "0 */1 * * * ?") 
public void closeOrderTask {
    log.info("Task start...");
    long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "500"));
    Long setnxResult = RedisShardedPoolUtil
        .setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, 
               String.valueOf(System.currentTimeMillis() + lockTimeout));
    if (setnxResult != null && setnxResult.intValue == 1) {
        closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    } else {
        String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
            String getSetResult = RedisShardedPoolUtil.getSet(
                	Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                String.valueOf(System.currentTimeMillis() + lockTimeout));
            if (getSetResult == null || 
                (getSetResult != null && StringUtils.equals(lockValueStr, getSetResult))) {
                closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            } else {
                log.info("The second attempt to get a lock failed...");
            }
        } else {
            log.info("The lock has not expired...");
        }
    }
    log.info("Task ended...");
}
```

*解决的问题*

P2 和 P3 同时检测到锁超时

- P2 通过 getSet 返回旧值，与当前时间对比后，再次确认获得锁，并设置新的有效期
- P3 通过 getSet 返回旧值，这时候返回的是 P2 刚设置过的值，对比后发现此值大于当前时间，表示已经有其他线程获得锁，则 P3 获取锁失败，这样就保证了只有一个线程可以获取锁

*存在的问题*

- 过期时间由客户端自己生成，则需要强制要求全局时钟同步（可以通过服务器设置时间同步）
- 锁不具备拥有着标识，即任何客户端都可以解锁（添加 requestId 进行客户端校验）
- 锁超时时间不能确定，需要根据业务逻辑设置

**Version_3**

使用 Redission 实现分布式锁

*核心代码*

```java
@Scheduled(cron = "0 */1 * * * ?") 
public void closeOrderTask {
    log.info("Task start...");
    Rlock lock = redissionManager.getRedisson.getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    boolean getLock = false;
    try {
        if (getLock = lock.tryLock(0, 5, TimeUnit.SECONDS)) {
            log.info("get Lock:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.hour",2));
            iOrderService.closeOrder(hour);
        } else {
            log.info("Attempt to get a lock failed...");
        }
    } catch (InterruptedException e) {
        log.error("Get Lock exception...", e);
    } finally {
        if (!getLock) {
            return;
        }
        lock.unlock();
        log.info("Unlock...");
    }
    log.info("Task ended...");
}
```

#### 3.3 参考

- [使用Redis SETNX 命令实现分布式锁](https://blog.csdn.net/lihao21/article/details/49104695)
- [Redis实现分布式锁全局锁—Redis客户端Redisson中分布式锁RLock实现](https://my.oschina.net/haogrgr/blog/469439)



