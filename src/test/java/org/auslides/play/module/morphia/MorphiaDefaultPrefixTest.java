package org.auslides.play.module.morphia;

import com.google.inject.Guice;
import com.mongodb.WriteResult;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import dev.morphia.query.UpdateResults;
import org.auslides.play.module.morphia.models.Post;
import org.auslides.play.module.morphia.scanning.E;
import org.auslides.play.module.morphia.scanning.F;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.Environment;
import play.Mode;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.test.Helpers;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanggf on 2016/11/24.
 */
public class MorphiaDefaultPrefixTest {
    @Inject
    Application application;

    @Inject
    IMorphia morphia ;

    @Before
    public void setup() {
        Map<String, Object> configs = new HashMap<>() ;
        configs.put("morphia.db.seeds", "127.0.0.1:27017") ;
        configs.put("morphia.db.name", "test_db") ;
        //configs.put("morphia.db.name", "") ;
        //configs.put("morphia.db.name", "") ;
        configs.put("morphia.scan.classes", Arrays.asList("org.auslides.play.module.morphia.models.Post")) ;
        configs.put("morphia.scan.packages", Arrays.asList("org.auslides.play.module.morphia.scanning")) ;
        GuiceApplicationBuilder builder = new GuiceApplicationLoader()
                .builder(new GuiceApplicationLoader.Context(Environment.simple(), configs)) ;
        Guice.createInjector(builder.applicationModule()).injectMembers(this);

        Helpers.start(application);
    }

    @Test
    public void testScan() {
        // classes scanning
        Assert.assertTrue(morphia.underlying().isMapped(Post.class));
        // packages scanning
        Assert.assertTrue(morphia.underlying().isMapped(E.class));
        Assert.assertTrue(morphia.underlying().isMapped(F.class));
    }

    @Test
    public void testCRUD() {
        // clean
        morphia.ds().delete(morphia.ds().createQuery(Post.class)) ;

        // create
        Post post = new Post();
        post.title = "fake post";
        morphia.ds().save(post) ;
        Assert.assertNotNull(post.id) ;

        // load
        Post postLoaded = morphia.ds().get(Post.class, post.id) ;
        Assert.assertEquals(post.title, postLoaded.title);

        // update
        Query<Post> query = morphia.ds().createQuery(Post.class).field("title").equal("fake post");
        UpdateOperations<Post> ops = morphia.ds()
                .createUpdateOperations(Post.class)
                .set("title", "real post");
        UpdateResults results = morphia.ds().update(query, ops);
        Assert.assertEquals(results.getUpdatedCount(), 1);

        // remove
        WriteResult writeResult = morphia.ds().delete(post) ;
        Assert.assertEquals(writeResult.getN(), 1) ;
    }

    @After
    public void teardown() {
        Helpers.stop(application);
    }
}
