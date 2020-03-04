package org.auslides.play.module.morphia;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

/**
 * Created by guofeng on 2015/5/28.
 */
public interface IMorphia {
    public Morphia underlying();
    public Datastore ds();
    public Datastore ds(String dbName);
    public DB db();
    public GridFS gridFs();

}