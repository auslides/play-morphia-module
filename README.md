# play-morphia-module
A module to use Morphia/MongoDB with [Play! framework](https://www.playframework.com/documentation/2.5.x/Home) 2.5

# Publish it

Using
````
sbt publishLocal
````
to publish it to the local repository, so that you could reference it in your play application

# Configuration

Add the following to your build's library dependency:
``````
"org.auslides"  %% "play-morphia-module"  % "2.5.10"
``````

### Configuring the connection in conf/application.conf
``````
morphia.db.host="127.0.0.1"
morphia.db.port="27017"
morphia.db.username=<username>
morphia.db.password=<password>
``````
or
`````
mongodb.uri="mongodb://username:password@localhost:27017/dbname"
`````
For clustering:
``````
morphia.db.seeds="127.0.0.1:27017"
morphia.db.dev.seeds="127.0.0.1:27017"
morphia.db.username=<username>
morphia.db.password=<password>
``````

### Configuring the entity classes in conf/application.conf

````
morphia.scan.packages=<a comma separated list of fully qualified package names>
````
and/or
````
morphia.scan.classes=<a comma separated list of fully qualified class names>
````

### Dependency Injection 

Play 2.5 supports [Dependency Injection](https://www.playframework.com/documentation/2.5.x/JavaDependencyInjection). There is a morphia module defined. You could inject it by
``````
  @Inject IMorphia morphia ;
``````
in your component. This will use the configuration prefixed with 'morphia' in the application.conf. You could also use your own prefix name, for example:
``````
play.modules.morphia.prefixes=[mymorphia,yourmorphia]
......
mymorphia.db.host="127.0.0.1"
.....
``````
then injected it by:
``````
  @Inject @ConfigPrefix("mymorphia") IMorphia morphia ;
``````

IMorphia Interface:
``````
public interface IMorphia {
    public Morphia underlying();
    public Datastore ds(String dbName);
    public Datastore ds();
    public DB db();
    public GridFS gridFs();

}
``````

##### Password Decryptor

The password is default to plain text. If a encrypted password is used, you need to define a password decryptor by implementing IPasswordDecryptor interface:
``````
`   public class MyPasswordDecryptor implements IPasswordDecryptor {
        @Override
        public String decrypt(String encrypted) {
	        String decryptedPwd = ....
            return decryptedPwd;
        }
    }
``````
Then binding this implementation to IPasswordDecryptor in your module:
``````
   bind(IPasswordDecryptor.class).to(MyPasswordDecryptor.class)
``````

see **Providing custom bindings** in [Dependency Injection](https://www.playframework.com/documentation/2.5.x/JavaDependencyInjection).

In application.conf, you should disable the default password decryptor module:
``````
   play.modules.disabled += "org.auslides.play.module.morphia.PasswordDecryptorModule"
``````

see **Excluding modules** in [Dependency Injection](https://www.playframework.com/documentation/2.5.x/JavaDependencyInjection).

_inpired by [leodagdag/play2-morphia-plugin](https://github.com/leodagdag/play2-morphia-plugin)_
