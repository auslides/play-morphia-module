package org.auslides.play.module.morphia.provider;

import org.auslides.play.module.morphia.IMorphia;

/**
 * Created by guofeng on 2015/5/28.
 */
public interface MorphiaApi {
    IMorphia get(String prefix) ;
}