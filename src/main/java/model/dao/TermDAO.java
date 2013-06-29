/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dao;

import java.math.BigInteger;
import java.util.List;
import model.Term;
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
public class TermDAO {
    
    static final Logger logger = LoggerFactory.getLogger(TermDAO.class);

    static Session session;

    public static void save(Term term) {
        logger.info("UserDAO called");
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(term);
        session.getTransaction().commit();
        logger.info("Added term " + term.getId());
    }

    public static void update(Term term) {
        logger.info("UserDAO called");
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        session = sessionFactory.openSession();
        session.beginTransaction();
        session.update(term);
        session.getTransaction().commit();
        logger.info("Updated term " + term.getId());
    }

    public static void delete(Term term) {
        logger.info("UserDAO called");
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        session = sessionFactory.openSession();
        session.beginTransaction();
        session.delete(term);
        session.getTransaction().commit();
        logger.info("Deleted term " + term.getId());
    }

    public static User findByUserId(int id) {
        logger.info("UserDAO called");
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        session = sessionFactory.openSession();
        session.beginTransaction();
        BigInteger bigIntId = BigInteger.valueOf(id);
        Query query = session.createQuery("from term where id = :id ");
        query.setParameter("id", bigIntId);
        List<User> list = (List<User>) query.list();
        session.getTransaction().commit();
        return (User) list.get(0);
    }
}
