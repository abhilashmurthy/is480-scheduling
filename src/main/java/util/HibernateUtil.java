/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

/**
 *
 * @author suresh
 */
public class HibernateUtil {
    
    private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;
 
    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            Configuration configuration = new Configuration();
            sessionFactory = configuration.configure().buildSessionFactory();
//            serviceRegistry = new ServiceRegistryBuilder()
//                    .applySettings(configuration.getProperties()).buildServiceRegistry();        
//            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
 
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
 
    public static void shutdown() {
    	// Close caches and connection pools
    	getSessionFactory().close();
    }
    
}
