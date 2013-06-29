/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dao;

import java.math.BigInteger;
import java.util.List;
import model.User;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HibernateUtil;

/**
 *
 * @author ABHILASHM.2010
 */
public class UserDAO {
    
    static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    static Session session;

    static {
        logger.info("UserDAO called");
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        session = sessionFactory.openSession();
        session.beginTransaction();
    }

    public static void save(User user) {
        session.save(user);
        session.getTransaction().commit();
        logger.info("Added user " + user.getEmail());
    }

    public static void update(User user) {
        session.update(user);
        session.getTransaction().commit();
        logger.info("Updated user " + user.getEmail());
    }

    public static void delete(User user) {
        session.delete(user);
        session.getTransaction().commit();
        logger.info("Deleted user " + user.getEmail());
    }

    public static User findByUserId(int id) {
        BigInteger bigIntId = BigInteger.valueOf(id);
        Query query = session.createQuery("from user where id = :id ");
        query.setParameter("id", bigIntId);
        List<User> list = (List<User>) query.list();
        session.getTransaction().commit();
        return (User) list.get(0);
    }
}
