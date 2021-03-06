package org.bricolages.streaming.filter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import lombok.*;

@Slf4j
public class Record {
    static final ObjectMapper MAPPER = new ObjectMapper();

    static public Record parse(String json) throws JSONException {
        try {
            Map<String, Object> obj = (Map<String, Object>)MAPPER.readValue(json, Map.class);
            return new Record(obj);
        }
        catch (JsonProcessingException ex) {
            throw new JSONException(ex.getMessage());
        }
        catch (IOException ex) {
            log.error("IO exception while processing JSON???", ex);
            return null;
        }
    }

    final Map<String, Object> object;

    public Record() {
        this(new HashMap<String, Object>());
    }

    public Record(Map<String, Object> object) {
        this.object = object;
    }

    public String serialize() throws JSONException {
        try {
            return MAPPER.writeValueAsString(object);
        }
        catch (JsonProcessingException ex) {
            throw new JSONException(ex.getMessage());
        }
    }

    public Map<String, Object> getObject() {
        return object;
    }

    public Object get(String columnName) {
        return object.get(columnName);
    }

    public void put(String columnName, Object value) {
        object.put(columnName, value);
    }

    public void remove(String columnName) {
        object.remove(columnName);
    }

    public boolean isEmpty() {
        return object.isEmpty();
    }

    public Iterator<Map.Entry<String, Object>> entries() {
        return object.entrySet().iterator();
    }
}
