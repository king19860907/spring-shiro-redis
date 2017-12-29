package org.majun.shiro;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.*;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by majun on 28/12/2017.
 */
public class RedisTemplateTest {

    private RedisTemplate<String,String> redisTemplate;

    @Before
    public void before(){
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-redis-shiro.xml");
        redisTemplate = (RedisTemplate)context.getBean("redisTemplate");
    }

    @Test
    public void redisTemplateTest(){

        String keyPrefix = "test_";

        Map<String,String> map = new HashMap<>();
        map.put(keyPrefix+"aaa","aaa");
        map.put(keyPrefix+"bbb","bbb");
        map.put(keyPrefix+"ccc","ccc");
        map.put(keyPrefix+"ddd","ddd");
        map.put(keyPrefix+"eee","eee");

        Set<Entry<String,String>> entries = map.entrySet();
        for(Entry<String,String> entry : entries){
            redisTemplate.opsForValue().set(entry.getKey(),entry.getValue());
        }

        for(Entry<String,String> entry : entries) {
            Assert.assertEquals(entry.getValue(),redisTemplate.opsForValue().get(entry.getKey()));
        }

        Assert.assertEquals(entries.size(),redisTemplate.keys(keyPrefix+"*").size());

        Assert.assertEquals(entries.size(),redisTemplate.opsForValue().multiGet(redisTemplate.keys(keyPrefix+"*")).size());

        for(Entry<String,String> entry : entries) {
            redisTemplate.delete(entry.getKey());
        }

        Assert.assertEquals(0,redisTemplate.keys(keyPrefix+"*").size());
    }

    @Test
    public void testExpire() throws InterruptedException {
        Entry<String,String> entry = new AbstractMap.SimpleEntry("key","value");
        redisTemplate.opsForValue().set(entry.getKey(),entry.getValue());
        redisTemplate.expire(entry.getKey(),10000, TimeUnit.MILLISECONDS);
        Thread.sleep(5000);
        Assert.assertEquals(entry.getValue(),redisTemplate.opsForValue().get(entry.getKey()));
        Thread.sleep(6000);
        Assert.assertEquals(null,redisTemplate.opsForValue().get(entry.getKey()));
    }

}
