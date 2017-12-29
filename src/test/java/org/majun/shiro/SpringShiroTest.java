package org.majun.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.subject.Subject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.AbstractMap;
import java.util.Map.Entry;

/**
 * Created by majun on 29/12/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-redis-shiro.xml"})
public class SpringShiroTest {

    @Test
    public void testSessionWriteAndRead() throws InterruptedException {
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken("majun","123");
        subject.login(token);

        Entry entry = new AbstractMap.SimpleEntry("key","value");
        Session session = subject.getSession();
        session.setAttribute(entry.getKey(),entry.getValue());

        session = subject.getSession();
        Assert.assertEquals(entry.getValue(),session.getAttribute(entry.getKey()));

        Assert.assertEquals(true,subject.isAuthenticated());
        Assert.assertEquals(true,subject.hasRole("admin"));
        Assert.assertEquals(true,subject.isPermittedAll("user:create","user:update","menu:create"));

        //update
        entry.setValue("value2");
        session.setAttribute(entry.getKey(),entry.getValue());
        session = subject.getSession();
        Assert.assertEquals(entry.getValue(),session.getAttribute(entry.getKey()));

        //logout
        subject.logout();
        try{
            Assert.assertEquals(null,session.getAttribute(entry.getKey()));
        }catch(Exception e){
            Assert.assertEquals(UnknownSessionException.class.getName(),e.getClass().getName());
        }

        subject.login(token);
        session = subject.getSession();
        session.setTimeout(2000L);
        session.setAttribute(entry.getKey(),entry.getValue());
        Assert.assertEquals(entry.getValue(),session.getAttribute(entry.getKey()));
        Thread.sleep(5000L);
        try{
            Assert.assertEquals(null,session.getAttribute(entry.getKey()));
        }catch(Exception e){
            Assert.assertEquals(UnknownSessionException.class.getName(),e.getClass().getName());
        }
    }

}
