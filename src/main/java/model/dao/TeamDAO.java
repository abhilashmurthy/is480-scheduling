/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dao;

import java.math.BigInteger;
import java.sql.Date;
import java.util.List;
import model.Team;
import model.Term;
import static model.dao.TermDAO.session;
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
        session.save(team);
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
        List<Team> list = (List<Team>) query.list();
        session.getTransaction().commit();
        return (Team) list.get(0);
    }
    
    public static Team getTeamByName(String teamName) {
        session.beginTransaction();
        Query query = session.createQuery("from Team where name = :name")
                .setParameter("name", teamName);
        List<Team> list = (List<Team>) query.list();
        session.getTransaction().commit();
        logger.info("Returned team " + list.get(0));
        return list.get(0);
    }
    
}
