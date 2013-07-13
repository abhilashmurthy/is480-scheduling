/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dao;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import model.Term;
import model.User;
import static model.dao.UserDAO.session;
import org.hibernate.Query;
import org.hibernate.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HibernateUtil;

/**
 *
 * @author ABHILASHM.2010
 */
public class TermDAO {
    
    static final Logger logger = LoggerFactory.getLogger(TermDAO.class);
   
    static Session session = HibernateUtil.getSession();

    public static void save(Term term) {
        session.beginTransaction();
        session.saveOrUpdate(term);
        session.getTransaction().commit();
        logger.info("Added term " + term.getId());
    }

    public static void update(Term term) {
        session.beginTransaction();
        session.update(term);
        session.getTransaction().commit();
        logger.info("Updated term " + term.getId());
    }

    public static void delete(Term term) {
        session.beginTransaction();
        session.delete(term);
        session.getTransaction().commit();
        logger.info("Deleted term " + term.getId());
    }
    
	public static List<Term> getAllTerms() {
        session.beginTransaction();
        Query query = session.createQuery("from Term");
        List<Term> terms = (List<Term>) query.list();
        session.getTransaction().commit();
        return terms;
    }
	
    public static Term findByYearAndTerm(int yearInt, int termInt) {
        session.beginTransaction();
		Calendar calYear = Calendar.getInstance();
		calYear.set(yearInt, 0, 1, 0, 0, 0);
        Timestamp yearDate = new Timestamp(calYear.getTimeInMillis());
		logger.debug("Timestamp: " + yearDate.toString());
        Query query = session.createQuery("from Term where year = :year and term = :term")
                .setParameter("year", yearDate)
                .setParameter("term", termInt);
        Term term = (Term) query.uniqueResult();
        session.getTransaction().commit();
        logger.info("Returned term " + term);
        return term;
    }

    public static Term findByTermId(int id) {
        session.beginTransaction();
        BigInteger bigIntId = BigInteger.valueOf(id);
        Query query = session.createQuery("from Term where id = :term_id ");
        query.setParameter("id", bigIntId);
        Term term = (Term) query.uniqueResult();
        session.getTransaction().commit();
        return term;
    }
}
