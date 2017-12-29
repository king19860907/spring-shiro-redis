package org.majun.shiro.session.dao;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Created by majun on 30/12/2017.
 */
public abstract class AbstractRedisSessionDao<VALUE> extends AbstractSessionDAO {

    protected RedisTemplate<java.io.Serializable,VALUE> redisTemplate;

    protected String keyPrefix=this.getClass().getName()+"_";

    public void setRedisTemplate(RedisTemplate<Serializable, VALUE> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session,sessionId);
        save(session);
        return sessionId;
    }

    @Override
    public void delete(Session session) {
        redisTemplate.delete(getSessionId(session.getId()));
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        if (session instanceof ValidatingSession && !((ValidatingSession) session).isValid()) {
            delete(session);
            return;
        }
        save(session);
    }

    /**
     * 保存session
     * @param session
     */
    private void save(Session session){
        doSave(session);
        touch(session);
    }

    /**
     * 具体保存的实现
     * @param session
     */
    abstract protected void doSave(Session session);

    /**
     *
     * @param needInit
     */
    public void setInit(boolean needInit){
        if(redisTemplate != null && needInit){
            redisTemplate.afterPropertiesSet();
            RedisConnectionFactory redisConnectionFactory = redisTemplate.getConnectionFactory();
            if(redisConnectionFactory instanceof JedisConnectionFactory){
                ((JedisConnectionFactory)redisConnectionFactory).afterPropertiesSet();
            }
        }
    }

    protected Serializable getSessionId(Serializable sessionId){
        return keyPrefix+sessionId;
    }

    private void touch(Session session){
        redisTemplate.expire(getSessionId(session.getId()),session.getTimeout(), TimeUnit.MILLISECONDS);
    }

}
