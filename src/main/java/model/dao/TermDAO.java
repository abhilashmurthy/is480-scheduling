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

    static {
        logger.info("UserDAO called");
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        session = sessionFactory.openSession();
        session.beginTransaction();
    }

    public static void save(Term term) {
        session.save(term);
        session.getTransaction().commit();
        logger.info("Added term " + term.getYear().toString() + ", " + term.getTerm());
    }

    public static void update(Term term) {
        session.update(term);
        session.getTransaction().commit();
        logger.info("Updated term " + term.getYear().toString() + ", " + term.getTerm());
    }

    public static void delete(Term term) {
        session.delete(term);
        session.getTransaction().commit();
        logger.info("Deleted term " + term.getYear().toString() + ", " + term.getTerm());
    }
}
