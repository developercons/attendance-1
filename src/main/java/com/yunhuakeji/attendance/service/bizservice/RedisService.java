package com.yunhuakeji.attendance.service.bizservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

  private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

  @Autowired
  private RedisTemplate redisTemplate;

  public Boolean setNX(String key, String value,final Long seconds, TimeUnit timeUnit) {
    Boolean flag = redisTemplate.boundHashOps(key).putIfAbsent("redisLock", System.currentTimeMillis());
    redisTemplate.boundHashOps(key).expire(seconds, timeUnit);
    return flag;
  }


  public boolean setNX(final String key, final String value) {
    Object obj = null;
    try {
      obj = redisTemplate.execute(new RedisCallback<Object>() {
        @Override
        public Object doInRedis(RedisConnection connection) throws DataAccessException {
          StringRedisSerializer serializer = new StringRedisSerializer();
          Boolean success = connection.setNX(serializer.serialize(key), serializer.serialize(value));
          connection.close();
          return success;
        }
      });
    } catch (Exception e) {
      logger.error("setNX redis error, key : {}", key);
    }
    return obj != null ? (Boolean) obj : false;
  }

  public boolean setNX(final String key, final String value,final int expire) {
    Object obj = null;
    try {
      obj = redisTemplate.execute(new RedisCallback<Object>() {
        @Override
        public Object doInRedis(RedisConnection connection) throws DataAccessException {
          StringRedisSerializer serializer = new StringRedisSerializer();
          Boolean success = connection.setNX(serializer.serialize(key), serializer.serialize(value));
          connection.close();
          return success;
        }
      });
    } catch (Exception e) {
      logger.error("setNX redis error, key : {}", key);
    }
    return obj != null ? (Boolean) obj : false;
  }

  /**
   * 写入缓存
   *
   * @param key
   * @param value
   * @return
   */
  public boolean set(final String key, Object value) {
    boolean result = false;
    try {
      ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
      operations.set(key, value);
      result = true;
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return result;
  }

  /**
   * 写入缓存设置时效时间
   *
   * @param key
   * @param value
   * @return
   */
  public boolean set(final String key, Object value, Long expireTime) {
    boolean result = false;
    try {
      ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
      operations.set(key, value);
      redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
      result = true;
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return result;
  }

  /**
   * 批量删除对应的value
   *
   * @param keys
   */
  public void remove(final String... keys) {
    for (String key : keys) {
      remove(key);
    }
  }

  /**
   * 批量删除key
   *
   * @param pattern
   */
  public void removePattern(final String pattern) {
    Set<Serializable> keys = redisTemplate.keys(pattern);
    if (keys.size() > 0)
      redisTemplate.delete(keys);
  }

  /**
   * 删除对应的value
   *
   * @param key
   */
  public void remove(final String key) {
    if (exists(key)) {
      redisTemplate.delete(key);
    }
  }

  /**
   * 判断缓存中是否有对应的value
   *
   * @param key
   * @return
   */
  public boolean exists(final String key) {
    return redisTemplate.hasKey(key);
  }

  /**
   * 读取缓存
   *
   * @param key
   * @return
   */
  public Object get(final String key) {
    Object result = null;
    ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
    result = operations.get(key);
    return result;
  }

  /**
   * 哈希 添加
   *
   * @param key
   * @param hashKey
   * @param value
   */
  public void hmSet(String key, Object hashKey, Object value) {
    HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
    hash.put(key, hashKey, value);
  }

  /**
   * 哈希获取数据
   *
   * @param key
   * @param hashKey
   * @return
   */
  public Object hmGet(String key, Object hashKey) {
    HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
    return hash.get(key, hashKey);
  }

  /**
   * 列表添加
   *
   * @param k
   * @param v
   */
  public void lPush(String k, Object v) {
    ListOperations<String, Object> list = redisTemplate.opsForList();
    list.rightPush(k, v);
  }

  /**
   * 列表获取
   *
   * @param k
   * @param l
   * @param l1
   * @return
   */
  public List<Object> lRange(String k, long l, long l1) {
    ListOperations<String, Object> list = redisTemplate.opsForList();
    return list.range(k, l, l1);
  }

  /**
   * 集合添加
   *
   * @param key
   * @param value
   */
  public void add(String key, Object value) {
    SetOperations<String, Object> set = redisTemplate.opsForSet();
    set.add(key, value);
  }

  /**
   * 集合获取
   *
   * @param key
   * @return
   */
  public Set<Object> setMembers(String key) {
    SetOperations<String, Object> set = redisTemplate.opsForSet();
    return set.members(key);
  }

  /**
   * 有序集合添加
   *
   * @param key
   * @param value
   * @param scoure
   */
  public void zAdd(String key, Object value, double scoure) {
    ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
    zset.add(key, value, scoure);
  }

  /**
   * 有序集合获取
   *
   * @param key
   * @param scoure
   * @param scoure1
   * @return
   */
  public Set<Object> rangeByScore(String key, double scoure, double scoure1) {
    ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
    return zset.rangeByScore(key, scoure, scoure1);
  }

}
