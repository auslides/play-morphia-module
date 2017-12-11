package org.auslides.play.module.morphia;

/**
 * Created by guofeng on 2015/5/28.
 */
import com.google.common.collect.ImmutableList;
import org.auslides.play.module.morphia.provider.DefaultMorphiaApi;
import org.auslides.play.module.morphia.provider.MorphiaApi;
import org.auslides.play.module.morphia.provider.MorphiaProvider;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.*;
import play.libs.Scala;
import scala.collection.Seq;

import java.util.List;

public class MorphiaModule extends play.api.inject.Module {

    @Override
    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
        String defaultDBPrefix = configuration.underlying().getString("play.modules.morphia.defaultPrefix") ;
        List<String> prefixes = configuration.underlying().getStringList("play.modules.morphia.prefixes") ;

        ImmutableList.Builder<Binding<?>> list = new ImmutableList.Builder<Binding<?>>();

        list.add(bind(MorphiaApi.class).to(DefaultMorphiaApi.class));
        list.add(bind(IMorphia.class).to(new MorphiaProvider(defaultDBPrefix)));  // for no prefixed injection

        list.add(bindDB(defaultDBPrefix)) ;

        for ( String prefix : prefixes) {
            list.add(bindDB(prefix)) ;
        }
        return Scala.toSeq(list.build());
    }

    // Creates a prefix qualifier
    private ConfigPrefix prefixedWith(String name) {
        return new ConfigPrefixImpl(name) ;
    }

    // bind to the given prefix name
   private Binding<?> bindDB(String name) {
       ConfigPrefix configPrefix = prefixedWith(name) ;
       BindingKey<IMorphia> key = bind(IMorphia.class).qualifiedWith(configPrefix) ;
       Binding<?> binding = key.to(new MorphiaProvider(name)) ;
       return binding ;
   }
}