/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import constant.BookingStatus;
import constant.Role;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import model.Milestone;
import model.Schedule;
import model.Term;
import model.Timeslot;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class TermManager {

    private static Logger logger = LoggerFactory.getLogger(TermManager.class);
    
    public static boolean update(EntityManager em, Term term, EntityTransaction transaction) {
        logger.trace("Updating term: " + term);
        try {
            transaction = em.getTransaction();
            transaction.begin();
            if (term != null) {
                em.merge(term);
            }
            transaction.commit();
            return true;
        } catch (PersistenceException ex) {
            //Rolling back data transactions
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error making database call for Create Term Details");
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            
        }
        return false;
    }

    public static List<Term> getAllTerms(EntityManager em) {
        logger.trace("Getting all term objects");
        List<Term> sourceList = null;
        try {
            Query q = em.createQuery("Select t from Term t");
            sourceList = q.getResultList();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
        }
        return sourceList;
    }

    public static Term findByYearAndSemester(EntityManager em, int year, String semester) {
        logger.trace("Getting term by year and semester");
        Term result = null;
        try {
            Query q = em.createNativeQuery("select * from Term where academicYear = :year "
                    + "and semester = :semester", Term.class);
            q.setParameter("year", year);
            q.setParameter("semester", semester);
            result = (Term) q.getSingleResult();
        } catch (NoResultException e) {
			return null;
        } catch (Exception e) {
            logger.error("Database Operation Error");
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
        }
        return result;
    }
	
	public static Term findTermById(EntityManager em, long id) {
		logger.trace("Getting term by id");
		Term term = null;
		try {
			Query q = em.createQuery("SELECT t FROM Term t WHERE t.id = :id", Term.class);
			q.setParameter("id", id);
			term = (Term) q.getSingleResult();
			return term;
		} catch (Exception e) {
            logger.error("Database Operation Error");
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
		}
		return null;
	}
	
	public static Term getTermByDisplayName (EntityManager em, String displayName) {
		logger.trace("Getting term by display name");
		Term term = null;
		try {
			Query q = em.createQuery("SELECT t FROM Term t WHERE t.displayName = :displayName", Term.class);
			q.setParameter("displayName", displayName);
			term = (Term) q.getSingleResult();
			return term;
		} catch (Exception e) {
            logger.error("Database Operation Error");
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
		}
		return null;
	}
	
	/**
	 * Method to get the default active term for a user with multiple roles across various terms. Default active term is only the current term calculated by the first milestone's startDate and the last milestone's endDate. If outside these parameters, load the nearest future term and if cannot find, return nearest past term.
	 * @param em
	 * @param username
	 * @return Calculated Default Term
	 */
	public static Term getDefaultActiveTerm(EntityManager em) {
		logger.trace("Getting latest term");
		Calendar cal = Calendar.getInstance();
		Timestamp now = new Timestamp(cal.getTimeInMillis());
		List<Term> terms = getAllTerms(em);
		Map<Term, Timestamp> currentTerms = new HashMap<Term, Timestamp>();
		for (Term term : terms) {
			if (!isActive(em, term)) continue;
			List<Schedule> termSchedules = ScheduleManager.findByTerm(em, term);
			Collections.sort(termSchedules, new Comparator<Schedule>(){
				//Sort schedules by milestone orders
				public int compare(Schedule o1, Schedule o2) {
					return Integer.valueOf(o1.getMilestone().getMilestoneOrder()).compareTo(Integer.valueOf(o2.getMilestone().getMilestoneOrder()));
				}
			});
			//TESTING
			logger.info("TERM: " + term.getDisplayName());
			logger.info("START: " + termSchedules.get(0).getStartDate());
			logger.info("END: " + termSchedules.get(termSchedules.size() - 1).getEndDate());
			if (now.after(termSchedules.get(0).getStartDate()) && now.before(termSchedules.get(termSchedules.size() - 1).getEndDate())) {
				currentTerms.put(term, termSchedules.get(termSchedules.size() - 1).getEndDate());
			}
		}
		if (currentTerms.size() > 1) {
			Term nearestCurrentTerm = null;
			long difference = Long.MAX_VALUE;
			for (Term currentTerm : currentTerms.keySet()) {
				Timestamp endDate = currentTerms.get(currentTerm);
				if (endDate.getTime() - now.getTime() < difference) {
					nearestCurrentTerm = currentTerm;
					difference = endDate.getTime() - now.getTime();
				}
			}
			return nearestCurrentTerm;
		} else if (currentTerms.size() > 0) {
			return currentTerms.keySet().iterator().next();
		}
		List<Schedule> allSchedules = ScheduleManager.getAllSchedules(em);
		Collections.sort(allSchedules, new Comparator<Schedule>(){
			public int compare(Schedule o1, Schedule o2) {
				return o1.getStartDate().compareTo(o2.getStartDate());
			}
		});
		for (Schedule s : allSchedules) {
			if (!isActive(em, s.getMilestone().getTerm())) continue;
			if (s.getStartDate().after(now)) {
				return s.getMilestone().getTerm();
			}
		}
		Collections.sort(allSchedules, new Comparator<Schedule>(){
			public int compare(Schedule o1, Schedule o2) {
				return o1.getStartDate().compareTo(o2.getStartDate()) * -1;
			}
		});
		for (Schedule s : allSchedules) {
			if (!isActive(em, s.getMilestone().getTerm())) continue;
			return s.getMilestone().getTerm();
		}
		return null;
	}
	
	/**
	 * Method to get the default active term for a user with multiple roles across various terms. Default active term is only the current term calculated by the first milestone's startDate and the last milestone's endDate. If outside these parameters, load the nearest future term and if cannot find, return nearest past term.
	 * @param em
	 * @param username
	 * @return Calculated Default Term
	 */
	public static Term getDefaultActiveTerm(EntityManager em, String username) {
		logger.trace("Getting default active term for user: " + username);
		em.getTransaction().begin();
		try {
			List<User> userObjects = UserManager.getUserObjectsForAllTerms(em, username);
			Calendar cal = Calendar.getInstance();
			Timestamp now = new Timestamp(cal.getTimeInMillis());
			Map<Term, Integer> pendingBookingsMap = new HashMap<Term ,Integer>();
			Map<Term, Timestamp> currentTermsMap = new HashMap<Term, Timestamp>();
			for (User userObject : userObjects) {
				Term userTerm = userObject.getTerm();
				if (userObject.getRole().equals(Role.ADMINISTRATOR) && userTerm == null) {
					return getDefaultActiveTerm(em); //Return latest term if admin
				}
				if (!isActive(em, userTerm)) continue;
				List<Schedule> termSchedules = ScheduleManager.findByTerm(em, userTerm);
				Collections.sort(termSchedules, new Comparator<Schedule>(){
					//Sort schedules by milestone orders
					public int compare(Schedule o1, Schedule o2) {
						return Integer.valueOf(o1.getMilestone().getMilestoneOrder()).compareTo(Integer.valueOf(o2.getMilestone().getMilestoneOrder()));
					}
				});
				if (now.after(termSchedules.get(0).getStartDate()) && now.before(termSchedules.get(termSchedules.size() - 1).getEndDate())) {
					//Check if now is between first schedule's startDate and last schedule's endDate
					currentTermsMap.put(userTerm, termSchedules.get(termSchedules.size() - 1).getEndDate());
				}
				int pendingBookingCount = 0;
				for (Schedule s : termSchedules) {
					for (Timeslot t : s.getTimeslots()) {
						if (t.getCurrentBooking() != null && t.getCurrentBooking().getBookingStatus().equals(BookingStatus.PENDING)) pendingBookingCount++;
					}
				}
				if (pendingBookingCount > 0) pendingBookingsMap.put(userTerm, pendingBookingCount);
			}
			if (pendingBookingsMap.size() > 0) {
				int maxPendingBookings = 0;
				Term termWithMostPendingBookings = null;
				for (Term pendingBookingsTerm : pendingBookingsMap.keySet()) {
					if (pendingBookingsMap.get(pendingBookingsTerm) > maxPendingBookings) {
						maxPendingBookings = pendingBookingsMap.get(pendingBookingsTerm);
						termWithMostPendingBookings = pendingBookingsTerm;
					}
				}
				return termWithMostPendingBookings;
			}
			if (currentTermsMap.size() > 1) {
				Term nearestCurrentTerm = null;
				long difference = Long.MAX_VALUE;
				for (Term currentTerm : currentTermsMap.keySet()) {
					Timestamp endDate = currentTermsMap.get(currentTerm);
					if (endDate.getTime() - now.getTime() < difference) {
						nearestCurrentTerm = currentTerm;
						difference = endDate.getTime() - now.getTime();
					}
				}
				return nearestCurrentTerm;
			} else if (currentTermsMap.size() > 0) {
				return currentTermsMap.keySet().iterator().next();
			}
			List<Schedule> allSchedules = new ArrayList<Schedule>();
			for (User userObject : userObjects) {
				Term userTerm = userObject.getTerm();
				List<Schedule> termSchedules = ScheduleManager.findByTerm(em, userTerm);
				allSchedules.addAll(termSchedules);
			}
			//Get nearest future or past term
			Collections.sort(allSchedules, new Comparator<Schedule>(){
				public int compare(Schedule o1, Schedule o2) {
					return o1.getStartDate().compareTo(o2.getStartDate());
				}
			});
			//Get earliest future term
			for (Schedule s : allSchedules) {
				if (!isActive(em, s.getMilestone().getTerm())) continue;
				if (s.getStartDate().after(now)) {
					return s.getMilestone().getTerm();
				}
			}
			//Get earliest past term
			Collections.sort(allSchedules, new Comparator<Schedule>(){
				public int compare(Schedule o1, Schedule o2) {
					return o1.getStartDate().compareTo(o2.getStartDate()) * -1;
				}
			});
			for (Schedule s : allSchedules) {
				if (!isActive(em, s.getMilestone().getTerm())) continue;
				return s.getMilestone().getTerm();
			}
			return null;
		} catch (Exception e) {
            logger.error("User " + username + " not found. Must be guest. Retrieving latest term instead.");
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.trace(s.toString());
                }
            }
			//Return latest term if user not found 
			return getDefaultActiveTerm(em);
        } finally {
			em.getTransaction().commit();
		}
	}
	
	public static boolean isActive(EntityManager em, Term term) {
		List<Term> activeTerms = SettingsManager.getActiveTerms(em);
		for (Term activeTerm : activeTerms) {
			if (activeTerm.equals(term)) {
				return true;
			}
		}
		return false;
	}
	
}
