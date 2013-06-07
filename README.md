# jetty-nosql-memcached

[![Build Status](https://secure.travis-ci.org/yyuu/jetty-nosql-memcached.png?branch=master)](http://travis-ci.org/yyuu/jetty-nosql-memcached)
[![Build Status](https://buildhive.cloudbees.com/job/yyuu/job/jetty-nosql-memcached/badge/icon)](https://buildhive.cloudbees.com/job/yyuu/job/jetty-nosql-memcached/)

## Overview

SessionManager implementation for Jetty based on jetty-nosql supporting Couchbase & Memcached.

## Install

jetty-nosql-memcached is an extension for Jetty-9.
You have to install jars into jetty's `${jetty.home}/lib/ext`.

Built jars of jetty-nosql-memcached can be found on Maven Central.
You can install one of them without building.

- http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jetty-nosql-memcached%22

*NOTE*

You must install jetty-nosql-memcached into Jetty with all dependent jars, such like jetty-nosql and SpyMemcached.
If you're not sure, it's better to use all-in-one jar like `jetty-nosql-memcached-${version}-jar-with-dependencies.jar`.
You don't have to be aware of missing dependencies since all-in-one jar includes all dependencies in single jar file.


## Configuration

You need to configure both "session manager" and "session ID manager".


### Configuring "session ID manager"

SessionIdManagers can be configured in files under `${JETTY_HOME}/etc`.  In following example, using `${JETTY_HOME}/etc/jetty.xml`.

    <?xml version="1.0"?>
    <Configure id="Server" class="org.eclipse.jetty.server.Server">
      
      (... snip ...)
      
      <Set name="sessionIdManager">
        <New id="memcachedSessionIdManager" class="org.eclipse.jetty.nosql.memcached.MemcachedSessionIdManager">
          <Arg><Ref id="Server" /></Arg>
          <Set name="serverString">localhost:11211</Set>
          <Set name="keyPrefix">session:</Set>
        </New>
      </Set>
      <Call name="setAttribute">
        <Arg>memcachedSessionIdManager</Arg>
        <Arg><Ref id="memcachedSessionIdManager" /></Arg>
      </Call>
    <!-- // equivalent in Java:
      Server server = ...;
      MemcachedSessionIdManager memcachedSessionIdManager = new MemcachedSessionIdManager(server);
      memcachedSessionIdManager.setServerString("localhost:11211");
      memcachedSessionIdManager.setKeyPrefix("session:");
      server.setSessionIdManager(memcachedSessionIdManager);
      server.setAttribute("memcachedSessionIdManager", memcachedSessionIdManager);
      -->
    </Configure>

#### Extra options for "session ID manager"

You can configure the behavior of session ID manager with following setters.

* setClientFactory(AbstractMemcachedClientFactory cf)
  * set memcached client. org.eclipse.jetty.nosql.memcached.spymemcached.SpyMemcachedClientFactory is used by default.
* setDefaultExpiry(int defaultExpiry)
  * set default expiry of sessions on memcached.
* setKeyPrefix(String keyPrefix)
  * use keyPrefix for session key prefix on memcached.
* setKeySuffix(String keySuffix)
  * use keySuffix for session key suffix on memcached.
* setServerString(String serverString)
  * specify server address and port in string. multiple hosts can be specified with spaces.
* setTimeoutInMs(int timeoutInMS)
  * set timeout for memcached connections.


### Configuring "session manager"

SessionManagers can be configured by either `${APP_ROOT}/WEB-INF/jetty-web.xml` or `${JETTY_HOME}/context/${APP_NAME}.xml`.

Sample configuration for `${APP_ROOT}/WEB-INF/jetty-web.xml`:

    <?xml version="1.0" encoding="UTF-8"?>
    <Configure class="org.eclipse.jetty.webapp.WebAppContext">
      
      (... snip ...)
      
      <Get name="server">
        <Get id="memcachedSessionIdManager" name="sessionIdManager" />
      </Get>
      <Set name="sessionHandler">
        <New class="org.eclipse.jetty.server.session.SessionHandler">
          <Arg>
            <New class="org.eclipse.jetty.nosql.memcached.MemcachedSessionManager">
              <Set name="sessionIdManager">
                <Ref id="memcachedSessionIdManager" />
              </Set>
            </New>
          </Arg>
        </New>
      </Set>
    </Configure>
    <!-- // equivalent in Java
      WebAppContext context = ...
      Server server = ...;
      MemcachedSessionIdManager sessionIdManager = server.getAttribute("memcachedSessionIdManager");
      MemcachedSessionManager sessionManager = new MemcachedSessionManager();
      sessionManager.setSessionIdManager(sessionIdManager);
      context.setSessionHandler(new SessionHandler(sessionManager));
      -->

Sample configuration for `${JETTY_HOME}/context/${APP_NAME}.xml`:

    <?xml version="1.0"  encoding="ISO-8859-1"?>
    <!DOCTYPE Configure PUBLIC "-//Mort Bay Consulting//DTD Configure//EN" "http://jetty.eclipse.org/configure.dtd">
    <Configure class="org.eclipse.jetty.webapp.WebAppContext">
      
      (... snip ...)
      
      <Ref name="Server" id="Server">
        <Call id="sessionIdManager" name="getAttribute">
          <Arg>memcachedSessionIdManager</Arg>
        </Call>
      </Ref>
      <Set name="sessionHandler">
        <New class="org.eclipse.jetty.server.session.SessionHandler">
          <Arg>
            <New id="memcachedSessionManaegr" class="org.eclipse.jetty.nosql.memcached.MemcachedSessionManager">
              <Set name="sessionIdManager">
                <Ref id="memcachedSessionIdManager" />
              </Set>
              <Set name="sessionFactory">
                <Ref id="org.eclipse.jetty.nosql.kvs.session.xstream.XStreamSessionFactory" />
              </Set>
            </New>
          </Arg>
        </New>
      </Set>
    </Configure>
    <!-- // equivalent in Java
      WebAppContext context = ...
      Server server = ...;
      MemcachedSessionIdManager sessionIdManager = server.getAttribute("memcachedSessionIdManager");
      MemcachedSessionManager sessionManager = new MemcachedSessionManager();
      sessionManager.setSessionIdManager(sessionIdManager);
      sessionManager.setSessionFactory(new XStreamSessionFactory());
      context.setSessionHandler(new SessionHandler(sessionManager));
      -->


#### Extra options for "session manager"

You can configure the behavior of session manager with following setters.

* setSessionIdManager(SessionIdManager idManager)
  * session id manager you created.
* setSessionFactory(AbstractSessionFactory sf)
  * set session serializer. org.eclipse.jetty.nosql.kvs.session.serializable.SerializableSessionFactory is used by default.



### Configuring "session ID manager" for Couchbase

SessionIdManagers can be configured in files under `${JETTY_HOME}/etc`.  In following example, using `${JETTY_HOME}/etc/jetty.xml`.

    <?xml version="1.0"?>
    <Configure id="Server" class="org.eclipse.jetty.server.Server">
      
      (... snip ...)
	  <Set name="sessionIdManager">
	    <New id="couchbaseSessionIdManager" class="org.eclipse.jetty.nosql.couchbase.CouchbaseSessionIdManager">
	      <Arg><Ref id="Server"/></Arg>
	      <Set name="serverString">localhost:8091</Set>
	      <Set name="BucketName">jettysession</Set>
	      <Set name="BucketPassword">password</Set>
	      <Set name="keyPrefix">session:</Set>
	    </New>
	  </Set>
	  <Call name="setAttribute">
	    <Arg>couchbaseSessionIdManager</Arg>
	    <Arg><Ref id="couchbaseSessionIdManager"/></Arg>
	  </Call>
    <!-- // equivalent in Java:
      Server server = ...;
      CouchbaseSessionIdManager couchbaseSessionIdManager = new CouchbaseSessionIdManager(server);
      CouchbaseSessionIdManager.setServerString("localhost:8091");
      CouchbaseSessionIdManager.setKeyPrefix("session:");
      server.setSessionIdManager(couchbaseSessionIdManager);
      server.setAttribute("couchbaseSessionIdManager", couchbaseSessionIdManager);
      -->
    </Configure>

#### Extra options for "session ID manager"

You can configure the behavior of session ID manager with following setters.
* If the session key is not found in primary bucket it would be searched in fallback bucket described by following 3 params
* setFallbackServerString(String serverString)
  * Space separated fallback host:port . This could be usefull during bucket migration. Eg: "host:port host:port"
* setFallbackBucketName(String bucketName)
  * Fallback couchbase bucket name.
* setFallbackBucketPassword(int bucketPassword)
  * Fallback couchbase bucket's password

* setClientFactory(AbstractMemcachedClientFactory cf)
  * set memcached client. org.eclipse.jetty.nosql.memcached.spymemcached.SpyMemcachedClientFactory is used by default.
* setDefaultExpiry(int defaultExpiry)
  * set default expiry of sessions on memcached.
* setKeyPrefix(String keyPrefix)
  * use keyPrefix for session key prefix on memcached.
* setKeySuffix(String keySuffix)
  * use keySuffix for session key suffix on memcached.
* setServerString(String serverString)
  * specify server address and port in string. multiple hosts can be specified with spaces.
* setTimeoutInMs(int timeoutInMS)
  * set timeout for memcached connections.


### Configuring "session manager" couchbase

SessionManagers can be configured by either `${APP_ROOT}/WEB-INF/jetty-web.xml` or `${JETTY_HOME}/context/${APP_NAME}.xml`.

Sample configuration for `${APP_ROOT}/WEB-INF/jetty-web.xml`:

    <?xml version="1.0" encoding="UTF-8"?>
    <Configure class="org.eclipse.jetty.webapp.WebAppContext">
      
      (... snip ...)
      
      <Get name="server">
        <Get id="couchbaseSessionIdManager" name="sessionIdManager" />
      </Get>
      <Set name="sessionHandler">
        <New class="org.eclipse.jetty.server.session.SessionHandler">
          <Arg>
            <New class="org.eclipse.jetty.nosql.memcached.CouchbaseSessionManager">
              <Set name="sessionIdManager">
                <Ref id="couchbaseSessionIdManager" />
              </Set>
            </New>
          </Arg>
        </New>
      </Set>
    </Configure>
    <!-- // equivalent in Java
      WebAppContext context = ...
      Server server = ...;
      CouchbaseSessionIdManager sessionIdManager = server.getAttribute("couchbaseSessionIdManager");
      CouchbaseSessionManager sessionManager = new CouchbaseSessionManager();
      sessionManager.setSessionIdManager(sessionIdManager);
      context.setSessionHandler(new SessionHandler(sessionManager));
      -->

Sample configuration for `${JETTY_HOME}/context/${APP_NAME}.xml`:

    <?xml version="1.0"  encoding="ISO-8859-1"?>
    <!DOCTYPE Configure PUBLIC "-//Mort Bay Consulting//DTD Configure//EN" "http://jetty.eclipse.org/configure.dtd">
    <Configure class="org.eclipse.jetty.webapp.WebAppContext">
      
      (... snip ...)
      
	  <Ref name="Server" id="Server">
	    <Call id="sessionIdManager" name="getAttribute">
	      <Arg>couchbaseSessionIdManager</Arg>
	    </Call>
	  </Ref>
	  <Set name="sessionHandler">
	    <New class="org.eclipse.jetty.server.session.SessionHandler">
	      <Arg>
		<New id="couchbaseSessionManager" class="org.eclipse.jetty.nosql.couchbase.CouchbaseSessionManager">
		  <Set name="sessionIdManager">
		    <Ref id="sessionIdManager"/>
		  </Set>
		</New>
	      </Arg>
	    </New>
	  </Set>
    </Configure>
    <!-- // equivalent in Java
      WebAppContext context = ...
      Server server = ...;
      CouchbaseSessionIdManager sessionIdManager = server.getAttribute("couchbaseSessionIdManager");
      CouchbaseSessionManager sessionManager = new CouchbaseSessionManager();
      sessionManager.setSessionIdManager(sessionIdManager);
      sessionManager.setSessionFactory(new XStreamSessionFactory());
      context.setSessionHandler(new SessionHandler(sessionManager));
      -->


#### Extra options for "session manager"

You can configure the behavior of session manager with following setters.

* setSessionIdManager(SessionIdManager idManager)
  * session id manager you created.
* setSessionFactory(AbstractSessionFactory sf)
  * set session serializer. org.eclipse.jetty.nosql.kvs.session.serializable.SerializableSessionFactory is used by default.

## Development

### Requirements

All library dependencies can be resolved from Maven.

* [maven](http://maven.apache.org/) (2.2.x or later)
* [jetty](http://eclipse.org/jetty/) (8.0.0 or later)
* [spymemcached](http://code.google.com/p/spymemcached/)
* [xmemcached](http://code.google.com/p/xmemcached/)
* [kryo](http://code.google.com/p/kryo/)
* [xstream](http://xstream.codehaus.org/)
* [couchbase-client](http://www.couchbase.com/develop/java/current)

### Build

You can build project tree from top of the repository.

    $ git clone git://github.com/yyuu/jetty-nosql-memcached.git
    $ cd jetty-nosql-memcached
    $ mvn clean package

### Release

Use maven-release-plguin.

    $ mvn release:clean
    $ mvn -Darguments="-Dgpg.passphrase=${GPG_PASSPHRASE}" release:prepare release:perform


## License

* Copyright (c) 2011-2012 Geisha Tokyo Entertainment, Inc.
* Copyright (c) 2011-2012 Yamashita, Yuu

All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
and Apache License v2.0 which accompanies this distribution.

The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html

The Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php

You may elect to redistribute this code under either of these licenses.
