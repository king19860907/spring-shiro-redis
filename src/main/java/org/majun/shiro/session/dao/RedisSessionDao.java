package org.majun.shiro.session.dao;

/**
 * Created by majun on 29/12/2017.
 */

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by majun on 28/12/2017.
 */
public class RedisSessionDao extends AbstractSessionDAO {


    private RedisTemplate<Serializable,Session> redisTemplate;

    private String keyPrefix=this.getClass().getName()+"_";

    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session,sessionId);
        touch(session);
        return sessionId;
    }

    protected Session doReadSession(Serializable sessionId) {
        return redisTemplate.opsForValue().get(getSessionId(sessionId));
    }

    public void update(Session session) throws UnknownSessionException {
        if (session instanceof ValidatingSession && !((ValidatingSession) session).isValid()) {
            delete(session);
            return;
        }
        touch(session);
    }

    public void delete(Session session) {
        redisTemplate.delete(getSessionId(session.getId()));
    }

    /**
     * 有效的session过多的情况下可能会对性能造成影响,尤其是内存
     * @return
     */
    public Collection<Session> getActiveSessions() {
        Set<Serializable> sessionIds = redisTemplate.keys(this.keyPrefix+"*");
        Collection<Session> sessions = redisTemplate.opsForValue().multiGet(sessionIds);
        return sessions;
    }

    private void touch(Session session){
        redisTemplate.opsForValue().set(getSessionId(session.getId()),session);
        redisTemplate.expire(getSessionId(session.getId()),session.getTimeout(), TimeUnit.MILLISECONDS);
    }

    private String getSessionId(Serializable sessionId){
        return keyPrefix+sessionId;
    }

    public void setRedisTemplate(RedisTemplate<Serializable, Session> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

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

}
