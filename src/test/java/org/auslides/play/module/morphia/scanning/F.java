package org.auslides.play.module.morphia.scanning;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * Created by zhanggf on 2016/11/24.
 */
@Entity
public class F {
    @Id
    private ObjectId id;
}
