package org.auslides.play.module.morphia;

/**
 * Created by guofeng on 2015/5/29.
 */
public class PlainTextPasswordDecryptor implements IPasswordDecryptor {
    @Override
    public String decrypt(String encrypted) {
        return encrypted;
    }
}
