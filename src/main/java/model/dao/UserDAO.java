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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HibernateUtil;

/**
 *
 * @author ABHILASHM.2010
 */
public class UserDAO {
    
    static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    static Session session = HibernateUtil.getSession();

    public static void save(User user) {
        session.beginTransaction();
        session.saveOrUpdate(user);
        session.getTransaction().commit();
        logger.info("Added user " + user.getUsername());
    }

    public static void update(User user) {
        session.beginTransaction();
        session.update(user);
        session.getTransaction().commit();
        logger.info("Updated user " + user.getUsername());
    }

    public static void delete(User user) {
        session.beginTransaction();
        session.delete(user);
        session.getTransaction().commit();
        logger.info("Deleted user " + user.getUsername());
    }

    public static User findByUserId(int id) {
        session.beginTransaction();
        BigInteger bigIntId = BigInteger.valueOf(id);
        Query query = session.createQuery("from user where id = :id ");
        query.setParameter("id", bigIntId);
        List<User> list = (List<User>) query.list();
        session.getTransaction().commit();
        return (User) list.get(0);
    }
    
    public static User findByUsername(String username) {
        session.beginTransaction();
        Query query = session.createQuery("from User where username = :username ");
        query.setParameter("username", username);
        User user = (User) query.uniqueResult();
        session.getTransaction().commit();
        return user;
    }
    
    public static List<User> getAllUsers() {
        session.beginTransaction();
        Query query = session.createQuery("from User");
        List<User> users = (List<User>) query.list();
        session.getTransaction().commit();
        return users;
    }
}
