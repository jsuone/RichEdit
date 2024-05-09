package com.example.android_note.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    //或许可以使用策略模式
    @Autowired
    private RedisTemplate redisTemplate;
    TimeUnit unit = TimeUnit.SECONDS;//时间粒度.考虑到多线程时如果使用静态则每个redis连接的时间粒度都是一样的。这不是我所希望的。
/***
 * boundValueOps(K key) 方法和 opsForValue() 方法都是用于获取操作特定类型的值（Value）的操作接口，但它们在使用方式和返回类型上有一些区别。
 *
 * boundValueOps(K key) 方法：
 *
 * 方法签名：BoundValueOperations<K, V> boundValueOps(K key)
 * 返回类型：BoundValueOperations<K, V>
 * 功能：返回一个绑定到指定键（Key）的值操作接口，该接口提供了对该键对应的值进行操作的方法。通过该接口可以直接对该键对应的值进行操作，而无需再次指定键。
 * 示例：
 *
 * String key = "myKey";
 * BoundValueOperations<String, String> boundValueOps = redisTemplate.boundValueOps(key);
 * boundValueOps.set("myValue");
 * String value = boundValueOps.get();
 * 在上述示例中，我们首先通过 boundValueOps(key) 方法获取一个绑定到键 "myKey" 的值操作接口，然后可以直接使用该接口对该键对应的值进行操作，例如设置值和获取值。
 *
 * opsForValue() 方法：
 *
 * 方法签名：ValueOperations<K, V> opsForValue()
 * 返回类型：ValueOperations<K, V>
 * 功能：返回一个用于操作值（Value）的操作接口，该接口提供了对值进行操作的方法。在使用该接口的方法时，需要显式指定键（Key）作为参数。
 * 示例：
 *
 * String key = "myKey";
 * ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
 * valueOps.set(key, "myValue");
 * String value = valueOps.get(key);
 * 在上述示例中，我们首先通过 opsForValue() 方法获取一个值操作接口，然后使用该接口的方法时，需要显式指定要操作的键作为参数，例如设置值和获取值时都需要指定键。
 *
 * 总结：
 *
 * boundValueOps(K key) 方法返回一个绑定到指定键的值操作接口，可以直接对该键对应的值进行操作，无需再次指定键。
 * opsForValue() 方法返回一个通用的值操作接口，需要在使用其方法时显式指定要操作的键。
 * boundValueOps(K key) 方法适用于在同一个键上进行多次操作时的便利性，而 opsForValue() 方法更适用于对不同键的值进行操作时。
 **/
//设置有效时间，得到有效时间
public boolean expire(String key,long time){
    return redisTemplate.expire(key,time,unit);
}
public boolean expire(String key,long time,TimeUnit unit){
    return redisTemplate.expire(key,time,unit);
}
public boolean expireAt(String key, Date date){
    return redisTemplate.expireAt(key,date);
}
public long getExpire(String key,TimeUnit unit){
    return redisTemplate.getExpire(key,unit);
}
public long getExpire(String key){
    return redisTemplate.getExpire(key);
}
//键是否存在
public boolean hasKey(String key){
    return redisTemplate.hasKey(key);
}
public boolean delete(String key){
    return redisTemplate.delete(key);
}
public boolean delete(List<String> key){
    if(!key.isEmpty()) {
        if (key.size() == 1) {
            return delete(key.get(0));
        } else if (key.size() > 1) {
            long result = redisTemplate.delete(key);
            return result == key.size() ? true : false;
        }
    }
    return false;
}
//字符串
public boolean set(String key,String value){
    try {
        redisTemplate.opsForValue().set(key,value);
        return true;
    }catch (Exception e){
        e.printStackTrace();
        return false;
    }
}
public boolean set(String key,String value,long time){
    try {
        redisTemplate.opsForValue().set(key,value,time,unit);
        return true;
    }catch (Exception e){
        e.printStackTrace();
        return false;
    }
}
public Object get(String key){
   return  key==null?null:redisTemplate.opsForValue().get(key);
}
//hash
public boolean mHashSet(String key, Map<String,Object> map){
    try {
        redisTemplate.opsForHash().putAll(key,map);
        return true;
    }catch (Exception e){
        e.printStackTrace();
        return false;
    }
}
public boolean mHashSet(String key,Map<String,Object> map,long time){
    try {
        redisTemplate.opsForHash().putAll(key,map);
        if(time>0){
            expire(key,time);
        }
        return true;
    }catch (Exception e){
        e.printStackTrace();
        return false;
    }
}
public <T> boolean hashSet (String key,String hashKey,T value){
    try {
        redisTemplate.opsForHash().put(key,hashKey,value);
        return true;
    }catch (Exception e){
        e.printStackTrace();
        return false;
    }
}
public boolean hashDelete(String key,String... hashkey){
    long result =  redisTemplate.opsForHash().delete(key,hashkey);
    return result == hashkey.length? true : false;
}
public <T> T hashGet(String key,String hashkey){
    HashOperations<String,String,T> hashOperations = redisTemplate.opsForHash();
    T value = hashOperations.get(key,hashkey);
    return value;
}
public Map<String,Object> hashGetMap(String key){
    Map<String,Object> value = redisTemplate.opsForHash().entries(key);
    return  value;
}
    public boolean set(String key,Object value){
        try {
            redisTemplate.opsForValue().set(key,value);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public boolean set(String key,Object value,long time){
        try {
            redisTemplate.opsForValue().set(key,value,time,unit);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


}
