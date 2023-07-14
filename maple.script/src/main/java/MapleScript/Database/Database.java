package Database;

import java.io.*;
import java.util.*;

public class Database {
    private static final String DATABASE_FILE = "database.db";

    private static Database instance;
    private Map<String, Map<String, String>> collections;
    private Map<String, Map<String, Set<String>>> indexes;

    private Database() {
        collections = new HashMap<>();
        indexes = new HashMap<>();
        loadDatabase();
    }
    
    public interface QueryFilter {
    boolean filter(String key, String value);
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public void createCollection(String collectionName) {
        if (!collections.containsKey(collectionName)) {
            collections.put(collectionName, new HashMap<>());
            indexes.put(collectionName, new HashMap<>());
        }
    }

    public void dropCollection(String collectionName) {
        collections.remove(collectionName);
        indexes.remove(collectionName);
    }

    public void setValue(String collectionName, String key, String value) {
        Map<String, String> collection = collections.get(collectionName);
        if (collection != null) {
            collection.put(key, value);
            updateIndexes(collectionName, key, value);
        }
    }

    public String getValue(String collectionName, String key) {
        Map<String, String> collection = collections.get(collectionName);
        if (collection != null) {
            return collection.get(key);
        }
        return null;
    }

    public void removeValue(String collectionName, String key) {
        Map<String, String> collection = collections.get(collectionName);
        if (collection != null) {
            String value = collection.remove(key);
            removeIndexes(collectionName, key, value);
        }
    }

    public List<String> query(String collectionName, QueryFilter filter) {
        List<String> results = new ArrayList<>();
        Map<String, String> collection = collections.get(collectionName);
        if (collection != null) {
            for (Map.Entry<String, String> entry : collection.entrySet()) {
                if (filter.filter(entry.getKey(), entry.getValue())) {
                    results.add(entry.getValue());
                }
            }
        }
        return results;
    }

    public List<String> queryByIndex(String collectionName, String indexName, String indexValue) {
        List<String> results = new ArrayList<>();
        Map<String, Set<String>> index = indexes.get(collectionName);
        if (index != null) {
            Set<String> keys = index.get(indexName + ":" + indexValue);
            if (keys != null) {
                for (String key : keys) {
                    String value = collections.get(collectionName).get(key);
                    if (value != null) {
                        results.add(value);
                    }
                }
            }
        }
        return results;
    }

    private void updateIndexes(String collectionName, String key, String value) {
        Map<String, Set<String>> index = indexes.get(collectionName);
        if (index != null) {
            for (String indexName : index.keySet()) {
                String indexValue = value.substring(value.indexOf(":") + 1);
                String indexKey = indexName + ":" + indexValue;
                index.computeIfAbsent(indexKey, k -> new HashSet<>()).add(key);
            }
        }
    }

    private void removeIndexes(String collectionName, String key, String value) {
        Map<String, Set<String>> index = indexes.get(collectionName);
        if (index != null) {
            for (String indexName : index.keySet()) {
                String indexValue = value.substring(value.indexOf(":") + 1);
                String indexKey = indexName + ":" + indexValue;
                Set<String> keys = index.get(indexKey);
                if (keys != null) {
                    keys.remove(key);
                    if (keys.isEmpty()) {
                        index.remove(indexKey);
                    }
                }
            }
        }
    }

    private void loadDatabase() {
        File file = new File(DATABASE_FILE);
        if (file.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(file);
                 ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                Map<String, Map<String, String>> data = (Map<String, Map<String, String>>) objectInputStream.readObject();
                if (data != null) {
                    collections = data;
                    buildIndexes();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void buildIndexes() {
        indexes.clear();
        for (Map.Entry<String, Map<String, String>> collectionEntry : collections.entrySet()) {
            String collectionName = collectionEntry.getKey();
            Map<String, String> collection = collectionEntry.getValue();
            Map<String, Set<String>> index = new HashMap<>();
            indexes.put(collectionName, index);
            for (Map.Entry<String, String> entry : collection.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                updateIndexes(collectionName, key, value);
            }
        }
    }

    public void saveDatabase() {
        try (FileOutputStream fileOutputStream = new FileOutputStream(DATABASE_FILE);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(collections);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
