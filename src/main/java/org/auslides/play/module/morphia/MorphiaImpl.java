package org.auslides.play.module.morphia;

import com.blogspot.mydailyjava.weaklockfree.WeakConcurrentMap;
import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import org.auslides.play.module.morphia.utils.ConfigKey;
import org.auslides.play.module.morphia.utils.MorphiaLogger;
import org.auslides.play.module.morphia.utils.PlayCreator;
import org.auslides.play.module.morphia.utils.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.ValidationExtension;
import org.mongodb.morphia.ext.entityscanner.EntityScanner;
import play.*;
import play.inject.ApplicationLifecycle;
import play.libs.F;

import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by guofeng on 2015/5/28.
 */
public class MorphiaImpl implements IMorphia {
    private Application application;
    private ApplicationLifecycle lifecycle ;
    private IPasswordDecryptor passwordDecryptor ;
    private String prefixName ;

    private Morphia morphia = null;
    private MongoClient mongo = null;
    private Datastore ds = null;
    private GridFS gridfs;

    private Configuration configuration ;
    private Environment environment ;

    private final WeakConcurrentMap<String, Datastore> dataStores = new WeakConcurrentMap<String, Datastore>(false);

    public MorphiaImpl(String prefixName,
                       Application application,
                       ApplicationLifecycle lifecycle,
                       Configuration configuration, Environment environment, IPasswordDecryptor passwordDecryptor) {
        this.prefixName = prefixName ;
        this.application = application ;
        this.lifecycle = lifecycle ;
        this.configuration = configuration ;
        this.environment = environment ;
        this.passwordDecryptor = passwordDecryptor ;
        lifecycle.addStopHook(() -> {
            stop();
            return F.Promise.pure(null);
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
        Configuration morphiaConf = null ;

        try {
            morphiaConf = configuration.getConfig(prefixName) ;
            if (morphiaConf == null) {
                throw Configuration.root().reportError(prefixName, "Missing Morphia configuration", null);
            }

            MorphiaLogger.debug(morphiaConf);

            String mongoURIstr = morphiaConf.getString(ConfigKey.DB_MONGOURI.getKey());
            Logger.debug("mongoURIstr:" + mongoURIstr);

            if(StringUtils.isNotBlank(mongoURIstr)) {
                MongoClientURI mongoURI = new MongoClientURI(mongoURIstr);
                if (mongoURI.getDatabase() != null) {
                    dbName = mongoURI.getDatabase();  // used by morphia.createDatastore() in the following
                }
            }

            String seeds = null ;
            if(environment.isDev()) {
                seeds = morphiaConf.getString(ConfigKey.DB_DEV_SEEDS.getKey());
            } else {
                seeds = morphiaConf.getString(ConfigKey.DB_SEEDS.getKey());
            }

            if (StringUtils.isBlank(dbName)) {
                dbName = morphiaConf.getString(ConfigKey.DB_NAME.getKey());
                if (StringUtils.isBlank(dbName)) {
                    throw morphiaConf.reportError(ConfigKey.DB_NAME.getKey(), "Missing Morphia configuration", null);
                }
            }

            //Check if credentials parameters are present
            if (StringUtils.isBlank(username)) {
                username = morphiaConf.getString(ConfigKey.DB_USERNAME.getKey());
            }
            if (StringUtils.isBlank(password)) {
                password = morphiaConf.getString(ConfigKey.DB_PASSWORD.getKey());
            }

            connectionsPerHost = morphiaConf.getInt(ConfigKey.CONNECTIONS_PER_HOST.getKey(), connectionsPerHost) ;
            MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
            if ( connectionsPerHost != -1 )
                builder.connectionsPerHost(connectionsPerHost);
            MongoClientOptions options = builder.build();

            Logger.debug("Max connections per host: " + options.getConnectionsPerHost());

            if(StringUtils.isNotBlank(mongoURIstr)) {
                MongoClientURI mongoURI = new MongoClientURI(mongoURIstr);
                mongo = connect(mongoURI);
            } else if (StringUtils.isNotBlank(seeds)) {
                mongo = connect(seeds, dbName, username, password, options);
            } else {
                mongo = connect(
                        morphiaConf.getString(ConfigKey.DB_HOST.getKey()),
                        morphiaConf.getString(ConfigKey.DB_PORT.getKey()),
                        dbName, username, password, options);
            }

            morphia = new Morphia();
            // To prevent problem during hot-reload
            if (environment.isDev()) {
                morphia.getMapper().getOptions().setObjectFactory( new PlayCreator()) ;
            }
            // Configure validator
            // http://mongodb.github.io/morphia/1.2/guides/validationExtension/
            new ValidationExtension(morphia);

            // Create datastore
            ds = morphia.createDatastore(mongo, dbName);

            MorphiaLogger.debug("Datastore [%s] created", dbName);
            // Create GridFS
            String uploadCollection = morphiaConf.getString(ConfigKey.COLLECTION_UPLOADS.getKey());
            if (StringUtils.isBlank(uploadCollection)) {
                uploadCollection = "uploads";
                MorphiaLogger.warn("Missing Morphia configuration key [%s]. Use default value instead [%s]", ConfigKey.COLLECTION_UPLOADS, "uploads");
            }
            gridfs = new GridFS(ds.getDB(), uploadCollection);
            MorphiaLogger.debug("GridFS created", "");
            MorphiaLogger.debug("Classes mapping...", "");
            mapClasses(morphiaConf.getString(ConfigKey.SCANNER_PACKAGES.getKey()), morphiaConf.getString(ConfigKey.SCANNER_CLASSES.getKey()));
            MorphiaLogger.debug("End of initializing Morphia", "");
        } catch (MongoException e) {
            MorphiaLogger.error(e, "Problem connecting MongoDB");
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            MorphiaLogger.error(e, "Problem mapping class");
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            MorphiaLogger.error(e, "Problem connecting MongoDB");
            throw new RuntimeException(e);
        }
    }

    private void mapClasses(String packages, String classes) throws ClassNotFoundException {
        // Register all entity classes
        scanPackages(packages);
        scanClasses(classes);

        ds.ensureCaps(); //creates capped collections from @Entity
        ds.ensureIndexes(); //creates indexes from @Index annotations in your entities
    }

    private void scanPackages(String packages) {
        final List<String> listPackages = Arrays.asList(packages.split(",")) ;
        new EntityScanner(morphia, (s) ->listPackages.stream().anyMatch((p)->s.startsWith(p)));
    }

    private void scanClasses(String classes) {
        if ( classes == null || classes.isEmpty())
            return ;
        String[] arrClasses = classes.split(",") ;
        final List<String> listClasses = new ArrayList<>() ;
        for (String cls : arrClasses) {
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
            throw Configuration.root().reportError(ConfigKey.DB_SEEDS.getKey(), "Cannot connect to mongodb: no replica can be connected", null);
        }
        MongoCredential mongoCredential = getMongoCredential(dbName, username, password) ;
        return mongoCredential == null ? new MongoClient(addrs, options) : new MongoClient(addrs, Arrays.asList(mongoCredential), options) ;
    }

    private MongoClient connect(String host, String port, String dbName, String username, String password, MongoClientOptions options) {
        String[] ha = host.split("[,\\s;]+");
        String[] pa = port.split("[,\\s;]+");
        int len = ha.length;
        if (len != pa.length) {
            throw Configuration.root().reportError(ConfigKey.DB_HOST.getKey() + "-" + ConfigKey.DB_PORT.getKey(), "host and ports number does not match", null);
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
            throw Configuration.root().reportError(
                    ConfigKey.DB_HOST.getKey() + "-" + ConfigKey.DB_PORT.getKey(), "Cannot connect to mongodb: no replica can be connected",
                    null);
        }

        MongoCredential mongoCredential = getMongoCredential(dbName, username, password) ;
        return mongoCredential == null ? new MongoClient(addrs, options) : new MongoClient(addrs, Arrays.asList(mongoCredential), options);
    }

    private MongoCredential getMongoCredential(String dbName, String username, String password) {
        if (StringUtils.isBlank(username) && StringUtils.isBlank(password))
            return null ;

        if (StringUtils.isNotBlank(username) ^ StringUtils.isNotBlank(password)) {
            throw Configuration.root().reportError(ConfigKey.DB_NAME.getKey(), "Missing username or password", null);
        }

        String decryptedPassword = passwordDecryptor.decrypt(password) ;  // CHANGED: decrypt the password
        return MongoCredential.createCredential(username, dbName, decryptedPassword.toCharArray());
    }

}
