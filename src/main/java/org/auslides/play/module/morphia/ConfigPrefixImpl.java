
package org.auslides.play.module.morphia;

import java.io.Serializable;
import java.lang.annotation.Annotation;

public class ConfigPrefixImpl implements ConfigPrefix, Serializable {

    private final String value;

    public ConfigPrefixImpl(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public int hashCode() {
        // This is specified in java.lang.Annotation.
        return (127 * "value".hashCode()) ^ value.hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof ConfigPrefix)) {
            return false;
        }

        ConfigPrefix other = (ConfigPrefix) o;
        return value.equals(other.value());
    }

    public String toString() {
        return "@" + ConfigPrefix.class.getName() + "(value=" + value + ")";
    }

    public Class<? extends Annotation> annotationType() {
        return ConfigPrefix.class;
    }

    private static final long serialVersionUID = 0;
}
