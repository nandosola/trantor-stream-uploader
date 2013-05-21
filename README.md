Stream Uploader Proxy Servlet
-----------------------------
Stream-uploading big files with Ruby Rack 1.x [is impossible](https://groups.google.com/forum/?fromgroups=#!topic/rack-devel/T5YE-aFzSIQ).
The Problem is that [Rack would read the entire request body into memory](http://stackoverflow.com/questions/3027564), and there
are no obvious ways to interact with raw requests and responses in this framework. AFAIK, to accomplish this task, people
just use non-Rack stuff, such as [Goliath](https://github.com/postrank-labs/goliath), Node.js, JavaEE or even the venerable FastCGI.

As a JRuby-friendly way, this project consists of simple Servlet, packaged in a WAR file ready to be deployed in
[Torquebox](http://torquebox.org/documentation/). The stream is guaranteed to be 1KB wide, so its memory footprint is
quite ridiculous. And it's fast. Moreover, no external Java libraries are used.

### Motivation
In my case, the motivation came from Trantor, an internal doc archiving system we are developing. Trantor is distributed,
so the web front-end and API (Sinatra/Rack app) lives in a different server than the file server back-end (another Sinatra/Rack app).
The front-end app also hosts a `FileServiceProxy < Sinatra::Base` proxy for the GET, DELETE, OPTIONS and HEAD XMLHttpRequest:s
coming from the HTML5 client, which uses [jQuery-File-Upload](https://github.com/blueimp/jQuery-File-Upload). Because of
the limitations of Rack mentioned above, the POST requests (file creation) must be routed through the Servlet.

Last but not least, I wanted to learn how to use Mockito to test Servlets. I ended up using PowerMockito (PowerMock + Mockito)
because Mockito by itself won't allow mocking final classes, such as Java's `URL`.

### Caveats
Please be aware, that there is Trantor-specific code living under `cc.abstra.trantor.wcamp` that takes care of:
* Document metadata archiving after a successful POST from the API
* Forwarding the request's OAuth 2.0 credentials to the OmniAuth middleware living in the front-end and processing authorization responses.

Delete this package and the references to its classes from your clone.

### Configure it
Its configuration is done via `src/main/webapp/web.xml` is simple:

```xml
…
  <servlet>
    …
    <init-param>
      <param-name>targetUri</param-name>
      <param-value>http://remotehost:9090/files</param-value>
    </init-param>
  </servlet>
…
```

The resulting WAR will be deployed at the `/uploader` context. If you wish to change it, then edit `src/main/webapp/jboss-web.xml`.

### Build it
This is a Maven 3 project. Mosey along: `mvn clean package`

### Deploy it (JBoss 7 AS)
After the build process is complete, drop `target/stream-uploader.war` into `$JBOSS_HOME/standalone/deployments` or execute
`mvn -Plocal-deploy clean package` or `mvn -Premote-deploy -DremoteIp=a.b.c.d clean package`. See `pom.xml` for more details.

### TO-DO
* More tests


### License
Licensed under the Apache License, Version 2.0. See LICENSE file for more details.

### Credits
The inspiration came from [this StackOverflow post](http://stackoverflow.com/questions/2471799) and I stole good implementation
 ideas from the [HTTP-Proxy-Servlet](https://github.com/dsmiley/HTTP-Proxy-Servlet) GitHub project by dsmiley. The `HttpHeaders`
 class comes from the Guava project and `MockServletInputStream` comes from [two-tiers-utils](https://github.com/DomDerrien/two-tiers-utils)
