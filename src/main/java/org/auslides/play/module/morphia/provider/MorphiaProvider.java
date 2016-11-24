package org.auslides.play.module.morphia.provider;

import org.auslides.play.module.morphia.IMorphia;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by guofeng on 2015/5/28.
 */
public class MorphiaProvider implements Provider<IMorphia> {
    @Inject
    private MorphiaApi morphiaApi = null;
    private final String prefix;

    public MorphiaProvider(String prefix) {
        this.prefix = prefix;
    }
    @Override
    public IMorphia get() {
       return morphiaApi.get(prefix) ;
    }
}
