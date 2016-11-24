# play2-morphia-plugin [![Build Status](https://secure.travis-ci.org/leodagdag/play2-morphia-plugin.png)](http://travis-ci.org/leodagdag/play2-morphia-plugin)
Plug-in to use Morphia/MongoDB with [Play! framework](https://www.playframework.com/documentation/2.4.x/Home) 2.4

_inpired by [greenlaw110 / play-morphia](https://github.com/greenlaw110/play-morphia)_

# Publish it

Using
````
sbt publishLocal
````
to publish it to the local repository, so that you could reference it in your play application

# Configuration

Add the following to your build's library dependency:
``````
"leodagdag"  %% "play2-morphia-plugin"  % "0.2.5.0"
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

### Dependency Injection 

Play 2.5 supports [Dependency Injection](https://www.playframework.com/documentation/2.55.x/JavaDependencyInjection). There is a morphia module defined. You could inject it by
``````
  @Inject IMorphia morphia ;
``````
in your component. This will use the configuration prefixed with 'morphia' in the application.conf. You could also use your own prefix name, for example:
``````
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
    public Morphia morphia();
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

see **Providing custom bindings** in [Dependency Injection](https://www.playframework.com/documentation/2.4.x/JavaDependencyInjection).

In application.conf, you should disable the default password decryptor module:
``````
   play.modules.disabled += "leodagdag.play2morphia.PasswordDecryptorModule"
``````

see **Excluding modules** in [Dependency Injection](https://www.playframework.com/documentation/2.4.x/JavaDependencyInjection).