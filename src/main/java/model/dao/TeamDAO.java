/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dao;

import java.math.BigInteger;
import model.Team;
import org.hibernate.Query;
import org.hibernate.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HibernateUtil;

/**
 *
 * @author ABHILASHM.2010
 */
public class TeamDAO {
    
    static final Logger logger = LoggerFactory.getLogger(TeamDAO.class);

    static Session session = HibernateUtil.getSession();

    public static void save(Team team) {
        session.beginTransaction();
        session.saveOrUpdate(team);
        session.getTransaction().commit();
        logger.info("Added team " + team.getId());
    }

    public static void update(Team team) {
        session.beginTransaction();
        session.update(team);
        session.getTransaction().commit();
        logger.info("Updated team " + team.getId());
    }

    public static void delete(Team team) {
        session.beginTransaction();
        session.delete(team);
        session.getTransaction().commit();
        logger.info("Deleted team " + team.getId());
    }

    public static Team findByTeamId(int id) {
        session.beginTransaction();
        BigInteger bigIntId = BigInteger.valueOf(id);
        Query query = session.createQuery("from Team where id = :id ");
        query.setParameter("id", bigIntId);
		Team team = (Team) query.uniqueResult();
		session.update(team.getMembers().get(0));
        session.getTransaction().commit();
        return team;
    }
    
    public static Team findTeamByName(String teamName) {
        session.beginTransaction();
        Query query = session.createQuery("from Team where name = :name")
                .setParameter("name", teamName);
		Team team = (Team) query.uniqueResult();
        session.getTransaction().commit();
        logger.info("Returned team " + team);
        return team;
    }
    
}
