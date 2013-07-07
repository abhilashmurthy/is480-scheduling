/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.dao.TermDAO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ABHILASHM.2010
 */
public class JPAUtil {
    
    static final Logger logger = LoggerFactory.getLogger(JPAUtil.class);
    private static EntityManagerFactory emf;
 
    static {
        try {
            emf = Persistence.createEntityManagerFactory("tutorialPU");
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            logger.error("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
 
    public static void shutdown() {
    	// Close caches and connection pools
    	emf.close();
    }
    
}
