package com.sso.core.util;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class JedisUtil {

    private static String ssoRedisAddress;
    public static void init(String ssoRedisAddress){
        JedisUtil.ssoRedisAddress = ssoRedisAddress;
    }

    /**
     * 获取方式：
     * 方式1：Redis单节点+Jedis单例：Redis单节点压力过重，Jedis单例存在并发瓶颈，不可用于线上。
     *      new Jedis("127.0.0.1",6379).get("cache_key");
     * 方式2：Redis单节点+Jedis单节点连接池：Redis单节点压力过重，负载和容灾较差。
     *      new JedisPool(new JedisPoolConfig(),"127.0.0.1",6379,10000).getResource().get("cache_key");
     * 方式3：Redis分片（通过clien端集群，一致性哈希方式实现）+Jedis多节点连接池：Redis集群负载和容灾较好，ShardedJedisPool一致性哈希分片，读写均匀，动态扩展。
     *      new ShardedJedisPool(new JedisPoolConfig(),new LinkList<JedisShardInfo>());
     * 方式4：Redis集群。
     *      new JedisCluster(JedisClusterNodes);
     */
    private static ShardedJedisPool shardedJedisPool;
    private static final ReentrantLock INSTANCE_INIT_LOCK = new ReentrantLock(false);


    /**
     * 获取ShardedJedis实例
     * @return
     */
    public static ShardedJedis getInstance(){
        if (shardedJedisPool==null){
            try {
                //使用tryLock,设置获取锁的等待时间，若获取到锁返回true,否则返回false。lock：能获取到锁就返回true,不能的化就一直等待。
                if (INSTANCE_INIT_LOCK.tryLock(2, TimeUnit.SECONDS)){
                    if (shardedJedisPool==null){
                        JedisPoolConfig poolConfig = new JedisPoolConfig();
                        poolConfig.setMaxWaitMillis(10000);//设置连接最大等待时间
                        poolConfig.setMaxTotal(100);//设置对打连接数
                        poolConfig.setMaxIdle(50);//设置实例最大空闲数
                        /**
                         * 设置容器中最小的连接数，仅仅此值为正数且timeBetweenEvictionRunsMills值大于0时有效。
                         * 确保在对象逐出线程工作后线程池中有最小的实例数。
                         */
                        poolConfig.setMinIdle(10);
                        //在borrow一个实例时，是否提前进行validate操作：如果为true，则得到的jedis实例是可用的。
                        poolConfig.setTestOnBorrow(true);
                        /**
                         * 表示idle object evictor(空闲对象逐出器)线程对idle object扫描，
                         * 如果validate失败，此object会从pool中drop掉，此项只有在timeBetweenEvictionRunsMills大于0时有效。
                         */
                        poolConfig.setTestWhileIdle(true);
                        /**
                         * 表示idle object evictor两次扫描之间要sleep的毫秒数，
                         * 逐出扫描时间间隔，如果为非正数，则不运行逐出线程。默认-1
                         */
                        poolConfig.setTimeBetweenEvictionRunsMillis(30000);
                        /**
                         * 表示一个对象停留在idle状态的最短时间，然后被idle object evictor扫描并逐出。
                         * 此项只有在timeBetweenEvictionRunsMills大于0时有效。
                         */
                        poolConfig.setMinEvictableIdleTimeMillis(50000);
                        poolConfig.setNumTestsPerEvictionRun(10);//设置空闲对象逐出器每次扫描最多的扫描数

                        List<JedisShardInfo> shardInfos = new LinkedList<JedisShardInfo>();
                        String[] addrStr = ssoRedisAddress.split(",");
                        for (String addr:addrStr){
                            JedisShardInfo info = new JedisShardInfo(addr);
                            shardInfos.add(info);
                        }

                        shardedJedisPool = new ShardedJedisPool(poolConfig,shardInfos);
                    }
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage(),e);
            }finally {
                INSTANCE_INIT_LOCK.unlock();//释放锁
            }
        }

        if(shardedJedisPool==null){
            return null;
        }

        return shardedJedisPool.getResource();
    }


    public static void close(){
        if (shardedJedisPool!=null){
            shardedJedisPool.close();
        }
    }

    /**
     * redis存储用户信息
     * @param redisKey
     * @param object
     * @return
     */
    public static String setValue(String redisKey, Object object,int seconds){
        String result = null;
        ShardedJedis client = getInstance();
        try {
            if (client!=null){
                result = client.setex(redisKey.getBytes(),seconds,serialize(object));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }finally {
            client.close();
        }
        return result;
    }

    /**
     *
     * @param redisKey
     * @return
     */
    public static Object getRedisValue(String redisKey){
        Object object = null;
        if (redisKey!=null){
            ShardedJedis client = getInstance();
            try{
                if (client!=null){
                    byte[] bytes = client.get(redisKey.getBytes());
                    if (bytes!=null){
                        object = unSerialize(bytes);
                    }
                }
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }finally {
                client.close();
            }
        }
        return object;
    }

    public static void del(String redisKey){
        ShardedJedis client = getInstance();
        try {
            client.del(redisKey);
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }finally {
            client.close();
        }
    }

    /**
     * 将字节转为对象，实现反序列化
     * @param bytes
     * @return
     */
    public static Object unSerialize(byte[] bytes){
        ByteArrayInputStream bis = null;
        try{
            bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            return ois.readObject();
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }finally {
            try {
                bis.close();
            } catch (IOException e) {
                log.error(e.getMessage(),e);
            }
        }
        return null;
    }

    /**
     * 因为redis不直接存储对象，所以将其序列化转字节存储
     * @param object
     * @return
     */
    public static byte[] serialize(Object object){
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            return baos.toByteArray();
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }finally {
            try {
                oos.close();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void main(String[] args){
        String address = "redis://127.0.0.1:6379";
        init(address);
//        setValue("ceshi","value123",2*60);
//        System.out.println(getRedisValue("ceshi"));
        del("ceshi");
        System.out.println(getRedisValue("ceshi"));
    }
}
