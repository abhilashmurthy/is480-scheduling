/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.jsonadapters;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import model.Milestone;

/**
 *
 * @author ABHILASHM.2010
 */
public class MilestoneAdapter implements JsonSerializer<Milestone> {
    
    @Override
    public JsonElement serialize(Milestone milestone, Type type, JsonSerializationContext jsc) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", milestone.getId());
        jsonObject.addProperty("name", milestone.getName());
        jsonObject.addProperty("slotDuration", milestone.getSlotDuration());
        Type stringType = new TypeToken<List<String>>(){}.getType();
        jsonObject.addProperty("attendees", new Gson().toJson(milestone.getRequiredAttendees(), stringType));
        return jsonObject;
    }
    
}
