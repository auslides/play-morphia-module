package org.auslides.play.module.morphia.models;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

@Entity
@Indexes({
        @Index(options = @IndexOptions(name = "title", background = true),
                fields = @Field("title")),
        @Index(options = @IndexOptions(name = "ownerId-contextId", background = true),
                fields = {@Field("title"), @Field("type")})
})

public class Post{

    @Id
    public ObjectId id;

    @Indexed
    public String title;

    public String type;

}
