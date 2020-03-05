package org.auslides.play.module.morphia.scanning;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

/**
 * Created by zhanggf on 2016/11/24.
 */
@Entity
public class F {
    @Id
    private ObjectId id;
}
