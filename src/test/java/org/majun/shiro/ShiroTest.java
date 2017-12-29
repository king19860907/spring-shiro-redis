package org.majun.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.Map.Entry;

/**
 * Created by majun on 28/12/2017.
 */
public class ShiroTest {

    @After
    public void tearDown() throws Exception {
        ThreadContext.unbindSubject();//退出时请解除绑定Subject到线程 否则对下次测试造成影响
    }

    @Test
    public void testSessionWriteAndRead() throws InterruptedException {
        Entry entry = new AbstractMap.SimpleEntry("key","value");
        login("classpath:shiro.ini","majun","123");

        Session session = subject().getSession();
        session.setAttribute(entry.getKey(),entry.getValue());

        session = subject().getSession();
        Assert.assertEquals(entry.getValue(),session.getAttribute(entry.getKey()));

        Assert.assertEquals(true,subject().isAuthenticated());
        Assert.assertEquals(true,subject().hasRole("admin"));
        Assert.assertEquals(true,subject().isPermittedAll("user:create","user:update","menu:create"));

        //update
        entry.setValue("value2");
        session.setAttribute(entry.getKey(),entry.getValue());
        session = subject().getSession();
        Assert.assertEquals(entry.getValue(),session.getAttribute(entry.getKey()));

        //logout
        subject().logout();
        try{
            Assert.assertEquals(null,session.getAttribute(entry.getKey()));
        }catch(Exception e){
            Assert.assertEquals(UnknownSessionException.class.getName(),e.getClass().getName());
        }

        login("classpath:shiro.ini","majun","123");
        session = subject().getSession();
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

    private void login(String config,String username,String password){

        Factory<SecurityManager> factory = new IniSecurityManagerFactory(config);
        SecurityManager securityManager = factory.getInstance();

        SecurityUtils.setSecurityManager(securityManager);
        Subject subject = SecurityUtils.getSubject();

        AuthenticationToken token = new UsernamePasswordToken(username,password);
        subject.login(token);

    }

    private Subject subject(){
        return SecurityUtils.getSubject();
    }

}
