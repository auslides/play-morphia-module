package org.auslides.play.module.morphia.utils;

import org.mongodb.morphia.mapping.DefaultCreator;
import com.mongodb.DBObject;
import play.Play;

public class PlayCreator extends DefaultCreator {

    @Override
    protected ClassLoader getClassLoaderForClass() {
        return Play.application().classloader();
    }
}
