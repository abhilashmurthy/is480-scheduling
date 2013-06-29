package model.dao;

import java.math.BigInteger;
import java.util.List;

import model.Team;
import model.User;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HibernateUtil;

public class TeamDAO {

    static final Logger logger = LoggerFactory.getLogger(TeamDAO.class);
    static Session session;

    static {
        logger.info("TeamDAO called");
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        session = sessionFactory.openSession();
        session.beginTransaction();
    }

    public static Team findByUserId(int id) {
        BigInteger bigIntId = BigInteger.valueOf(id);
        Query query = session.createQuery("from team where id = :id ");
        query.setParameter("id", bigIntId);
        List<Team> list = (List<Team>) query.list();
        session.getTransaction().commit();
        return (Team) list.get(0);
    }
}
