package me.buom.shiro.test;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.apache.shiro.util.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ebab on 3/5/14.
 */
public class RequestListener implements ServletRequestListener {

    private static final Logger log = LoggerFactory.getLogger(RequestListener.class);

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        log.info("RequestListener -> requestDestroyed -> {}", Thread.currentThread());
        ThreadContext.remove();
    }

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        log.info("RequestListener -> requestInitialized -> {}", Thread.currentThread());
    }
}
