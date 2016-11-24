package org.auslides.play.module.morphia;

/**
 * Created by guofeng on 2015/5/28.
 */

import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

public class PasswordDecryptorModule extends Module {
    @Override
    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
        return seq(bind(IPasswordDecryptor.class).to(PlainTextPasswordDecryptor.class)) ;
   }
}