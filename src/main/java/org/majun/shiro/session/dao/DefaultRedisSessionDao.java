package org.majun.shiro.session.dao;

/**
 * Created by majun on 29/12/2017.
 */

import org.apache.shiro.session.Session;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * Created by majun on 28/12/2017.
 */
public class DefaultRedisSessionDao extends AbstractRedisSessionDao<Session> {

    @Override
    protected void doSave(Session session) {
        redisTemplate.opsForValue().set(getSessionId(session.getId()),session);
    }

    protected Session doReadSession(Serializable sessionId) {
        return redisTemplate.opsForValue().get(getSessionId(sessionId));
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

}
