/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dao;

import java.math.BigInteger;
import java.util.List;
import model.Team;
import model.Term;
import model.User;
import static model.dao.UserDAO.session;
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
public class TeamDAO {
    
    static final Logger logger = LoggerFactory.getLogger(TeamDAO.class);

    static Session session;

    static {
        logger.info("UserDAO called");
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        session = sessionFactory.openSession();
        session.beginTransaction();
    }

    public static void save(Team team) {
        session.save(team);
        session.getTransaction().commit();
        logger.info("Added team " + team.toString());
    }

    public static void update(Team team) {
        session.update(team);
        session.getTransaction().commit();
        logger.info("Added team " + team.toString());
    }

    public static void delete(Team team) {
        session.delete(team);
        session.getTransaction().commit();
        logger.info("Added team " + team.toString());
    }
    
    public static int getIdByName(String teamName) {
        Query query = session.createQuery("from team where name = :name ");
        query.setParameter("name", teamName);
        List list = query.list();
        session.getTransaction().commit();
        logger.info("Returned team");
        Team team = (Team) list.get(0);
        return team.getId();
    }
    
}
