package me.ele.micservice.plugins.mongo;

import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.codecs.IdGenerator;
import org.bson.codecs.ObjectIdGenerator;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by frankliu on 15/8/23.
 */
public class MongoKit {

    private static MongoClient client;
    private static MongoDatabase defaultDb;
    private static final IdGenerator idG = new ObjectIdGenerator();
    public static final String ID_FIELD_NAME = "_id";

    public static void init(MongoClient client, String database) {
        MongoKit.client = client;
        MongoKit.defaultDb = client.getDatabase(database);
    }

    public static List<String> save(String collectionName, List<Record> records) {

        Date now = new Date();

        List<String> ids = new ArrayList<>(records.size());
        List<Document> objs = new ArrayList<>(records.size());
        for (Record record : records) {
            record.set("created", now).set("updated", now);
            Document document = toDocument(record);
            Object id = idG.generate();
            document.append(ID_FIELD_NAME, id);
            objs.add(document);
            ids.add(id.toString());
        }
        MongoKit.getCollection(collectionName).insertMany(objs);

        return ids;
    }

    public static String save(String collectionName, Record record) {

        Date now = new Date();
        record.set("created", now).set("updated", now);
        Document document = toDocument(record);
        Object id = idG.generate();
        document.append(ID_FIELD_NAME, id);
        MongoKit.getCollection(collectionName).insertOne(document);

        return id.toString();
    }

    public static void delete(String collectionName, Map<String, Object> filter) {
        Bson filters = buildFilter(filter);
        MongoKit.getCollection(collectionName).deleteOne(filters);
    }

    public static void update(String collectionName, Map<String, Object> filter, Map<String, Object> updates) {

        Bson filters = buildFilter(filter);
        Document document = new Document();
        for(Map.Entry<String, Object> entry : updates.entrySet()) {
            document.append(entry.getKey(), entry.getValue());
        }

        MongoKit.getCollection(collectionName).updateMany(filters,
                new Document("$set", document).append("$currentDate", new Document("updated", true)));
    }

    public static void deletekey(String collectionName, Map<String, Object> filter, Map<String, Object> updates) {

        Bson filters = buildFilter(filter);
        Document document = new Document();
        for(Map.Entry<String, Object> entry : updates.entrySet()) {
            document.append(entry.getKey(), entry.getValue());
        }

        MongoKit.getCollection(collectionName).updateOne(filters,
                new Document("$unset", document));
    }

    public static void push(String collectionName, Map<String, Object> filter, String field, List<Object> updates) {

        Bson filters = buildFilter(filter);

        MongoKit.getCollection(collectionName).updateMany(filters,
                new Document("$push", new Document(field, new Document("$each", updates))).append("$currentDate", new Document("updated", true)));
    }

    public static void pushdocument(String collectionName, Map<String, Object> filter, String field, List<Record> updates) {

        Bson filters = buildFilter(filter);
        List<Object> docList = new ArrayList<>();
        for (Record r : updates) {
            docList.add(toDocument(r));
        }
        MongoKit.getCollection(collectionName).updateMany(filters,
                new Document("$push", new Document(field, new Document("$each", docList))).append("$currentDate", new Document("updated", true)));
    }

    public static Record findOne(String collection, Map<String, Object> filter) {

        MongoCollection logs = MongoKit.getCollection(collection);

        FindIterable<Document> iterable = logs.find(buildFilter(filter));
        Document o = iterable.first();

        return o != null ? toRecord(o) : null;
    }

    public static List<Record> find(String collection, Map<String, Object> filter) {

        MongoCollection logs = MongoKit.getCollection(collection);

        FindIterable<Document> iterable = logs.find(buildFilter(filter));

        MongoCursor<Document> cursor = iterable.iterator();
        List<Record> rds = new ArrayList<>();
        while(cursor.hasNext()) {
            rds.add(toRecord(cursor.next()));
        }

        return rds;

    }

    public static Page<Record> paginate(String collection, int pageNumber, int pageSize) {
        return paginate(collection, pageNumber, pageSize, null, null, null);
    }

    public static Page<Record> paginate(String collection, int pageNumber, int pageSize, Map<String, Object> filter) {
        return paginate(collection, pageNumber, pageSize, filter, null, null);
    }

    public static Page<Record> paginate(String collection, int pageNumber, int pageSize, Map<String, Object> filter, Map<String, Object> like) {
        return paginate(collection, pageNumber, pageSize, filter, like, null);
    }

    public static Page<Record> paginate(String collection, int pageNumber, int pageSize, Map<String, Object> filter, Map<String, Object> like, Map<String, Object> sort) {

        MongoCollection logs = MongoKit.getCollection(collection);

        Bson conditons = buildFilter(filter);
        FindIterable<Document> iterable = logs.find(conditons);
        iterable.skip((pageNumber - 1) * pageSize).limit(pageSize);

        sort(sort, iterable);

        List<Record> records = new ArrayList<Record>();
        MongoCursor<Document> cursor = iterable.iterator();
        while (cursor.hasNext()) {
            records.add(toRecord(cursor.next()));
        }
        long totalRow = logs.count(conditons);
        if (totalRow <= 0) {
            return new Page<>(new ArrayList<>(0), pageNumber, pageSize, 0, 0);
        }
        long totalPage = totalRow / pageSize;
        if (totalRow % pageSize != 0) {
            totalPage++;
        }
        Page<Record> page = new Page<>(records, pageNumber, pageSize,(int)totalPage, (int)totalRow);
        return page;
    }

    private static void sort(Map<String, Object> sort, FindIterable iterable) {
        if (sort != null) {
            BasicDBObject dbo = new BasicDBObject();
            Set<Map.Entry<String, Object>> entrySet = sort.entrySet();
            for (Map.Entry<String, Object> entry : entrySet) {
                String key = entry.getKey();
                Object val = entry.getValue();
                dbo.put(key, "asc".equalsIgnoreCase(val + "") ? 1 : -1);
            }
            iterable = iterable.sort(dbo);
        }
    }

    private static Bson buildFilter(Map<String, Object> filter) {
        if (filter != null) {
            List<Bson> filters = new ArrayList<>();
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();
                if(val instanceof Collection) {
                    if(key.equalsIgnoreCase(ID_FIELD_NAME)) {
                        List<ObjectId> ids = new ArrayList<>();
                        for(String id : (Collection<String>)val) {
                            ids.add(new ObjectId(id));
                        }
                        filters.add(Filters.in(key, ids.toArray()));
                    } else {
                        filters.add(Filters.in(key, ((Collection) val).toArray()));
                    }
                } else if(val instanceof Map) {
                    for (Map.Entry<String, Object> e : ((Map<String, Object>) val).entrySet()) {
                        String k = e.getKey();
                        Object v = e.getValue();
                        if(k.equals("$gt")) {
                            filters.add(Filters.gt(key, v));
                        }else if(k.equals("$lt")){
                            filters.add(Filters.lt(key, v));
                        }else if(k.equals("$lte")){
                            filters.add(Filters.lte(key, v));
                        }else if(k.equals("$gte")){
                            filters.add(Filters.gte(key, v));
                        }else if(k.equals("$ne")){
                            filters.add(Filters.ne(key, v));
                        }
                    }
                } else {
                    if(key.equalsIgnoreCase(ID_FIELD_NAME)) {
                        filters.add(Filters.eq(key, new ObjectId((String) val)));
                    } else {
                        filters.add(Filters.eq(key, val));
                    }
                }
            }
            return Filters.and(filters);
        } else {
            return new BasicDBObject();
        }

    }

    @SuppressWarnings("unchecked")
    public static Record toRecord(Document document) {
        return new Record().setColumns(document);
    }

    public static BasicDBObject getLikeStr(Object findStr) {
        Pattern pattern = Pattern.compile("^.*" + findStr + ".*$", Pattern.CASE_INSENSITIVE);
        return new BasicDBObject("$regex", pattern);
    }

    public static MongoDatabase getDB() {
        return defaultDb;
    }

    public static MongoDatabase getDB(String dbName) {
        return client.getDatabase(dbName);
    }

    public static MongoCollection getCollection(String name) {
        return defaultDb.getCollection(name);
    }

    public static MongoCollection getDBCollection(String dbName, String collectionName) {
        return getDB(dbName).getCollection(collectionName);
    }

    private static Document toDocument(Map<String, Object> map) {
        Document dbObject = new Document();
        Set<Map.Entry<String, Object>> entrySet = map.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            String key = entry.getKey();
            Object val = entry.getValue();
            dbObject.append(key, val);
        }
        return dbObject;
    }

    private static Document toDocument(Record record) {
        Document object = new Document();
        for (Map.Entry<String, Object> e : record.getColumns().entrySet()) {
            object.append(e.getKey(), e.getValue());
        }
        return object;
    }

}
