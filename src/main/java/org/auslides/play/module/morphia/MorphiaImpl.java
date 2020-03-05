package org.auslides.play.module.morphia;

import com.blogspot.mydailyjava.weaklockfree.WeakConcurrentMap;
import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.typesafe.config.Config;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.ValidationExtension;
import dev.morphia.ext.entityscanner.EntityScanner;
import dev.morphia.mapping.DefaultCreator;
import dev.morphia.mapping.MapperOptions;
import org.auslides.play.module.morphia.utils.ConfigKey;
import org.auslides.play.module.morphia.utils.MorphiaLogger;
import org.auslides.play.module.morphia.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Application;
import play.Environment;
import play.inject.ApplicationLifecycle;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Created by guofeng on 2015/5/28.
 */
public class MorphiaImpl implements IMorphia {
    private static final Logger logger = LoggerFactory.getLogger(MorphiaImpl.class);

    private Application application;
    private ApplicationLifecycle lifecycle ;
    private IPasswordDecryptor passwordDecryptor ;
    private String prefixName ;

    private Morphia morphia = null;
    private MongoClient mongo = null;
    private Datastore ds = null;
    private GridFS gridfs;

    private Config configuration ;
    private Environment environment ;

    private final WeakConcurrentMap<String, Datastore> dataStores = new WeakConcurrentMap<String, Datastore>(false);

    public MorphiaImpl(String prefixName,
                       Application application,
                       ApplicationLifecycle lifecycle,
                       Config configuration, Environment environment, IPasswordDecryptor passwordDecryptor) {
        this.prefixName = prefixName ;
        this.application = application ;
        this.lifecycle = lifecycle ;
        this.configuration = configuration ;
        this.environment = environment ;
        this.passwordDecryptor = passwordDecryptor ;
		//void addStopHook(Callable<? extends CompletionStage<?>> hook);
        lifecycle.addStopHook(() -> {
            stop();
            return CompletableFuture.completedFuture(null);
        });

        init() ;
    }

    @Override
    public Morphia underlying() {
        return morphia ;
    }

    @Override
    public Datastore ds() {
        return ds;
    }

    @Override
    public Datastore ds(String dbName) {
        if (StringUtils.isBlank(dbName)) {
            return ds();
        }
        Datastore ds = dataStores.get(dbName);
        if (null == ds) {
            Datastore ds0 = morphia.createDatastore(mongo, dbName);
            ds = dataStores.put(dbName, ds0);
            if (null == ds) {
                ds = ds0;
            }
        }
        return ds;
    }

    @Override
    public GridFS gridFs() {
        return gridfs;
    }

    @Override
    public DB db() {
        return ds().getDB();
    }

    public void stop() {
        morphia = null;
        ds = null;
        gridfs = null;
        if ( mongo != null )
            mongo.close();
    }

    private void init() {
        String dbName = null;
        String username = null;
        String password = null;
        int connectionsPerHost = -1 ; // the defualt
        Config morphiaConf = null ;

        try {
            morphiaConf = configuration.getConfig(prefixName) ;
            if (morphiaConf == null) {
                throw new RuntimeException(prefixName + ": Missing Morphia configuration");
            }

            MorphiaLogger.debug(morphiaConf);

            String mongoURIstr = getConfigValueByKey(morphiaConf, ConfigKey.DB_MONGOURI.getKey());
            logger.debug("mongoURIstr:" + mongoURIstr);

            if(StringUtils.isNotBlank(mongoURIstr)) {
                MongoClientURI mongoURI = new MongoClientURI(mongoURIstr);
                if (mongoURI.getDatabase() != null) {
                    dbName = mongoURI.getDatabase();  // used by morphia.createDatastore() in the following
                }
            }

            String seeds = null ;
            if(environment.isDev()) {
                seeds = getConfigValueByKey(morphiaConf, ConfigKey.DB_DEV_SEEDS.getKey());
            } else {
                seeds = getConfigValueByKey(morphiaConf, ConfigKey.DB_SEEDS.getKey());
            }

            if (StringUtils.isBlank(dbName)) {
                dbName = getConfigValueByKey(morphiaConf, ConfigKey.DB_NAME.getKey());
                if (StringUtils.isBlank(dbName)) {
                    throw new RuntimeException(ConfigKey.DB_NAME.getKey() + ": Missing Morphia configuration");
                }
            }

            //Check if credentials parameters are present
            if (StringUtils.isBlank(username)) {
                username = getConfigValueByKey(morphiaConf, ConfigKey.DB_USERNAME.getKey());
            }
            if (StringUtils.isBlank(password)) {
                password = getConfigValueByKey(morphiaConf, ConfigKey.DB_PASSWORD.getKey());
            }

            MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
            if (morphiaConf.hasPath(ConfigKey.CONNECTIONS_PER_HOST.getKey()))
                connectionsPerHost = morphiaConf.getInt(ConfigKey.CONNECTIONS_PER_HOST.getKey()) ;
            if ( connectionsPerHost != -1 )
                builder.connectionsPerHost(connectionsPerHost);
            MongoClientOptions options = builder.build();

            logger.debug("Max connections per host: " + options.getConnectionsPerHost());

            if(StringUtils.isNotBlank(mongoURIstr)) {
                MongoClientURI mongoURI = new MongoClientURI(mongoURIstr);
                mongo = connect(mongoURI);
            } else if (StringUtils.isNotBlank(seeds)) {
                mongo = connect(seeds, dbName, username, password, options);
            } else {
                mongo = connect(
                        getConfigValueByKey(morphiaConf, ConfigKey.DB_HOST.getKey()),
                        getConfigValueByKey(morphiaConf, ConfigKey.DB_PORT.getKey()),
                        dbName, username, password, options);
            }

            morphia = new Morphia();
            // To prevent problem during hot-reload
            if (environment.isDev()) {
                MapperOptions.Builder mapperOptionsBuilder = MapperOptions.builder() ;
                mapperOptionsBuilder.objectFactory(new DefaultCreator()) ;
                morphia.getMapper().setOptions(mapperOptionsBuilder.build());
            }
            // Configure validator
            // http://mongodb.github.io/morphia/1.2/guides/validationExtension/
            new ValidationExtension(morphia);

            // Create datastore
            ds = morphia.createDatastore(mongo, dbName);

            MorphiaLogger.debug("Datastore [%s] created", dbName);
            // Create GridFS
            String uploadCollection = getConfigValueByKey(morphiaConf, ConfigKey.COLLECTION_UPLOADS.getKey());
            if (StringUtils.isBlank(uploadCollection)) {
                uploadCollection = "uploads";
                MorphiaLogger.warn("Missing Morphia configuration key [%s]. Use default value instead [%s]", ConfigKey.COLLECTION_UPLOADS, "uploads");
            }
            gridfs = new GridFS(ds.getDB(), uploadCollection);
            MorphiaLogger.debug("GridFS created", "");
            if ( morphiaConf.hasPath(ConfigKey.SCANNER_PACKAGES.getKey())) {
                List<String> pkgList = morphiaConf.getStringList(ConfigKey.SCANNER_PACKAGES.getKey());
                scanPackages(pkgList);
            }
            if ( morphiaConf.hasPath(ConfigKey.SCANNER_CLASSES.getKey())) {
                List<String> clsList = morphiaConf.getStringList(ConfigKey.SCANNER_CLASSES.getKey());
                scanClasses(clsList);
            }

            ds.ensureCaps(); //creates capped collections from @Entity
            ds.ensureIndexes(); //creates indexes from @Index annotations in your entities

            MorphiaLogger.debug("End of initializing Morphia", "");
        } catch (MongoException e) {
            MorphiaLogger.error(e, "Problem connecting MongoDB");
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            MorphiaLogger.error(e, "Problem connecting MongoDB");
            throw new RuntimeException(e);
        }
    }

    private void scanPackages(List<String> packages) {
        if (packages == null || packages.isEmpty())
            return ;

        for (String pkg : packages ) {
            MorphiaLogger.debug("Mapping classes in ...", pkg);
        }
        new EntityScanner(morphia, (s) ->packages.stream().anyMatch((p)->s.startsWith(p)));
    }

    private void scanClasses(List<String> classes) {
        if ( classes == null || classes.isEmpty())
            return ;
        final List<String> listClasses = new ArrayList<>() ;
        for (String cls : classes) {
            MorphiaLogger.debug("Mapping classes...", cls);
            listClasses.add(cls + ".class");
        }

        new EntityScanner(morphia, (s)->{
            return listClasses.contains(s) ;
        });
    }

    private MongoClient connect(MongoClientURI mongoURI) throws UnknownHostException {
        return new MongoClient(mongoURI);
    }

    private MongoClient connect(String seeds, String dbName, String username, String password, MongoClientOptions options) throws UnknownHostException {
        String[] sa = seeds.split("[;,\\s]+");
        List<ServerAddress> addrs = new ArrayList<ServerAddress>(sa.length);
        for (String s : sa) {
            String[] hp = s.split(":");
            if (0 == hp.length) {
                continue;
            }
            String host = hp[0];
            int port = 27017;
            if (hp.length > 1) {
                port = Integer.parseInt(hp[1]);
            }
            addrs.add(new ServerAddress(host, port));
        }
        if (addrs.isEmpty()) {
            throw new RuntimeException(ConfigKey.DB_SEEDS.getKey() + ": Cannot connect to mongodb: no replica can be connected");
        }
        MongoCredential mongoCredential = getMongoCredential(dbName, username, password) ;
        return mongoCredential == null ? new MongoClient(addrs, options) : new MongoClient(addrs, Arrays.asList(mongoCredential), options) ;
    }

    private String getConfigValueByKey(Config config, String key) {
        if (config.hasPath(key))
            return config.getString(key) ;
        else
            return "" ;
    }

    private MongoClient connect(String host, String port, String dbName, String username, String password, MongoClientOptions options) {
        String[] ha = host.split("[,\\s;]+");
        String[] pa = port.split("[,\\s;]+");
        int len = ha.length;
        if (len != pa.length) {
            throw new RuntimeException(ConfigKey.DB_HOST.getKey() + "-" + ConfigKey.DB_PORT.getKey() + ": host and ports number does not match", null);
        }

        List<ServerAddress> addrs = new ArrayList<ServerAddress>(ha.length);
        for (int i = 0; i < len; ++i) {
            try {
                addrs.add(new ServerAddress(ha[i], Integer.parseInt(pa[i])));
            } catch (Exception e) {
                MorphiaLogger.error(e, "Error creating mongo connection to %s:%s", host, port);
            }
        }
        if (addrs.isEmpty()) {
            throw new RuntimeException(
                    ConfigKey.DB_HOST.getKey() + "-" + ConfigKey.DB_PORT.getKey() + ": Cannot connect to mongodb: no replica can be connected" );
        }

        MongoCredential mongoCredential = getMongoCredential(dbName, username, password) ;
        return mongoCredential == null ? new MongoClient(addrs, options) : new MongoClient(addrs, Arrays.asList(mongoCredential), options);
    }

    private MongoCredential getMongoCredential(String dbName, String username, String password) {
        if (StringUtils.isBlank(username) && StringUtils.isBlank(password))
            return null ;

        if (StringUtils.isNotBlank(username) ^ StringUtils.isNotBlank(password)) {
            throw new RuntimeException(ConfigKey.DB_NAME.getKey() + ": Missing username or password");
        }

        String decryptedPassword = passwordDecryptor.decrypt(password) ;  // CHANGED: decrypt the password
        return MongoCredential.createCredential(username, dbName, decryptedPassword.toCharArray());
    }

}
