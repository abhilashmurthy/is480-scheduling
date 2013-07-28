/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import model.Settings;
import model.Term;

/**
 *
 * @author suresh
 */
public class SettingsManager {
	
	/**
	 * Method to get the currently active terms.
	 * @param em EntityManager with an already open transaction
	 * @return List of terms that are active
	 */
	public static ArrayList<Term> getActiveTerms(EntityManager em) {
		Query q = em.createQuery("select s from Settings s where name = :name ");
		Settings result = (Settings) q.setParameter("name", "activeTerms").getSingleResult();
		Type listType = new TypeToken<ArrayList<Long>>(){}.getType();
		ArrayList<Long> termIds = new Gson().fromJson(result.getValue(), listType);
		ArrayList<Term> activeTerms = new ArrayList<Term>();
		for (Long l : termIds) {
			activeTerms.add(TermManager.findTermById(em, l));
		}
		return activeTerms;
	}
}
