package me.buom.shiro.test;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by ebab on 3/5/14.
 */
public class ContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().addListener(RequestListener.class);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {

    }
}
