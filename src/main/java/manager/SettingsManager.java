/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
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
		Settings result = getByName(em, "activeTerms");
		Type listType = new TypeToken<ArrayList<Long>>(){}.getType();
		ArrayList<Long> termIds = new Gson().fromJson(result.getValue(), listType);
		ArrayList<Term> activeTerms = new ArrayList<Term>();
		for (Long l : termIds) {
			activeTerms.add(TermManager.findTermById(em, l));
		}
		return activeTerms;
	}
	
	public static ArrayList<HashMap<String,Object>> getMilestoneSettings (EntityManager em, Settings result) {
		Type type = new TypeToken<ArrayList<HashMap<String,Object>>>(){}.getType();
		ArrayList<HashMap<String,Object>> settingsList = new Gson().fromJson(result.getValue(), type);
		return settingsList;
	}
	
	public static Settings getMilestoneSettings(EntityManager em) {
		return getByName(em, "milestones");
	}
	
	public static Settings getByName(EntityManager em, String name) {
		Query q = em.createQuery("select s from Settings s where name = :name");
		return (Settings) q.setParameter("name", name).getSingleResult();
	}
}
