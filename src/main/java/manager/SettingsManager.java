/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
	
	public static HashMap<String, ArrayList<String>> getSettings (EntityManager em) {
		Settings result = getByName(em, "milestones");
//		Type type = new TypeToken<HashMap<String, String>>(){}.getType();
//		HashMap<String, String> settingsMap = new Gson().fromJson(result.getValue(), type);
		JsonObject object = (JsonObject) new com.google.gson.JsonParser().parse(result.getValue());
		Set<Map.Entry<String, JsonElement>> set = object.entrySet();
		Iterator<Map.Entry<String, JsonElement>> iterator = set.iterator();
		HashMap<String, ArrayList<String>> settingsMap = new HashMap<String, ArrayList<String>>();
		 while (iterator.hasNext()) {
			Map.Entry<String, JsonElement> entry = iterator.next();
			String key = entry.getKey();
			JsonElement value = entry.getValue();
//			settingsMap.put(key, value.getAsString());
		}
		return settingsMap;
	}
	
	public static Settings getByName(EntityManager em, String name) {
		Query q = em.createQuery("select s from Settings s where name = :name");
		return (Settings) q.setParameter("name", name).getSingleResult();
	}
}
