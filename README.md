# Chill PL

The Chill Platform is a Java web and cloud application development platform.  It takes a different approach than most
other java web frameworks in that it:

* Provides complete out-of-the-box infrastructure for building cloud-based web applications, including authentication 
  and background job support
* Minimizes the amount of architecture that application builders need to deal with
* Favors code over configuration files or conventions, surfacing configuration hooks rather than relying on files or 
  implicit mappings

Chill takes advantage of many of the great, light-weight java projects available today including:

* The Javalin Web Framework
* The JobRunr Background Job Processor
* Picocli for command line processing
* HikariCP for connection pooling
* Jedis for working with Redis

## High-Level Architecture

Chill follows the web/worker model popularized by Heroku:

* Web nodes handle web requests
* Worker nodes handle background jobs

This allows you to scale the web infrastructure and the background job infrastructure independently.  At dev time,
a node will act as both a web node and a worker node, making it easy to develop.

Chill integrates standard cloud technologies to make developing applications easy:

* Redis is used heavily for job scheduling, session management, RPC implementation, etc.
* A simple wrapper around the S3 API is used for cloud file management
* A simple layer on top of JDBC for RDBMS access

For all of these resources, Chill PL provides a default implementation if none is specified, which allows you to
spin up a development server or run tests without any additional software installation.

Chill PL aims to produce a *single "fat" jar* as the final project for Chill-based applications, making deployment and
application launching very easy.

Chill PL uses Maven as a build tool, but tries to minimize the use of it, relying on it mainly for dependency resolution.
Maven is "the standard" and works well with many IDEs and tools, but Chill PL tries to provide command
line tools for application management tasks, rather than relying on Maven infrastructure.

## Chill PL Specific Technologies

Chill PL leverages existing java libraries where it makes sense, but it also includes a significant number of modules
that provide functionality to complete the platform:

* ChillRecord - A micro-ORM for working with databases, inspired by ActiveRecord, Mongoid and other ORMs
* ChillMigrations - A simple system for managing migrations (database mutations) in your system
* ChillUtils - A collection of quality of life improvements for java development, including `TheMissingUtil` and `NiceList`
* ChillLog - A pluggable logging adapter that can be wired into whatever your preferred logging infrastructure is
* ChillScript - A hypertalk-inspired scripting language for scripting in your Chill applications
* ChillTemplates - ChillScript-based templates for building first-class hypermedia applications
* ChillRoutes - An extension to the standard Javalin routing infrastructure
* ChillHelper - A mechanism for adding "helper" functions for use in your web templates, inspired by Rails
* ChillEnv - A mechanism for flexibly resolving environment variables in a clear and obvious manner
* ChillRPC - A mechanism calling and servicing Remote Procedure Calls (RPC) via Redis

### ChillRecord

ChillRecord is a micro-ORM that lives on top of JDBC.  It is insipred by ActiveRecord, Mongoid and many other simple
ORMs.  It tries to stay "close to the metal" with respect to JDBC, and not get in the way too much, with a focus on
things like validation life cycle events, rather than an elaborate OR mapping layer.

Also somewhat uniquely, ChillRecord relies on "inline code generation": ChillRecord domain objects include a main
method that will generate the boilerplate getters and setters for the class, based on the fields declared on it.
This is a novel way of dealing with the difficulty of meta-programming infrastructure on the JVM.  It takes a bit
to get used to, but once you do you will find it very easy to work with.

Here is an example ChillRecord:

```java
public class Vehicle extends ChillRecord {

    ChillField<Long> id = pk("id");
    ChillField<Timestamp> createdAt = createdAt("created_at");
    ChillField<Timestamp> updatedAt = updatedAt("updated_at").optimisticLock();

    ChillField<String> make = field("make", String.class);
    ChillField<String> model = field("model", String.class);
    ChillField<String> uuid = uuid("uuid");

    ChillField<Integer> year = field("year", Integer.class);

    FK<Vehicle, User> user = fk("user_id", User.class);

    //region chill.Record GENERATED CODE

    public Vehicle createOrThrow(){
        if(!create()){
            throw new chill.db.ChillValidation.ValidationException(getErrors());
        }
        return this;
    }

    public Vehicle saveOrThrow(){
        if(!save()){
            throw new chill.db.ChillValidation.ValidationException(getErrors());
        }
        return this;
    }

    public Vehicle firstOrCreateOrThrow(){
        return (Vehicle) firstOrCreateImpl();
    }

    @chill.db.ChillRecord.Generated public java.lang.Long getId() {
        return id.get();
    }

    @chill.db.ChillRecord.Generated public java.sql.Timestamp getCreatedAt() {
        return createdAt.get();
    }

    // ... getters/setter omitted
 
    public static final chill.db.ChillRecord.Finder<Vehicle> find = finder(Vehicle.class);

    //endregion

    public static void main(String[] args) {
        codeGen();
    }

}
```

This class consists of three regions:

* In the first region, `ChillField`s are declared on the class.  This tells Chill how to map this class to an underlying
  database table.  This is the source of truth for the mapping between the class and the DB: there is no other config file!
* The second region begins with ``//region chill.Record GENERATED CODE``.  This is the generated code for the class and
  includes getters/setters for all the fields, a static `find` field for finding things in the database, etc.  Note that
  most editors will allow you to collapse this region, which is nice because it's all boilerplate anyway.
* The final section is the code generator, which is a `main()` method on the record itself.  This method simply calls the
  `codeGen()` method, which generates the code for the current model class, and is found on all model classes.  When you
  add or remove fields, you simply run this main method and copy and paste the generated code into the second region.
  note that if you decide to explicitly code a getter or setter, the code generator will detect that and will not generate
  a getter or setter.  This allows you to cleanly develop your domain logic in the "normal" java manner without confusing
  collisions or a complicated inheritance hierarchy.

### Chill Migrations

Chill Migrations are database migrations, specified in the `model.Migrations` file.  Here is an example migrations file:

```java
package model;

import chill.db.ChillMigrations;

public class Migrations extends ChillMigrations {

    public final ChillMigration migration_2022_03_08_22_26_44 = new ChillMigration("create user"){
        protected void up() {
            exec("""
                    CREATE TABLE user (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  created_at TIMESTAMP,
                                  updated_at TIMESTAMP,
                                  email VARCHAR(250) DEFAULT NULL,
                                  password VARCHAR(250) DEFAULT NULL,
                                  uuid VARCHAR(250) DEFAULT NULL
                                );
                    """);
        }
        protected void down() {
            exec("DROP TABLE user");
        }
    };

    public static void main(String[] args) {
        generateNewMigration();
    }
}
```

A `model.Migrations` file consists of a series of migrations specified as fields of type `ChillMigration`.  Each `ChillMigration`
has an `up()` and `down()` method in which is applies and rolls back changes to the underlying database.  SQL is the 
primary mechanism for mutating the database, but you can also run domain logic as well: it's just java code!

Migrations can be applied to a database in two ways:

* via a command line: `$ java jar MyApp.jar --migrations up:*`
* via a migrations console: `$ java jar MyApp.jar --migrations`

### ChillUtils

Chill provides many "quality of life" improvements for java developers through the `chill.util` package.  In particular,
`TheMissingUtils` provides a number of useful methods for day-to-day java coding:

* `forceThrow()` and `safely()` allows you to force checked exceptions to be thrown as unchecked exceptions to avoid
  checked exception hell.
* `time()` can time an operation for you
* `join()` can join a string on given a delimiter

Many of these features are small and exist scattered around the various Java libraries, but `TheMissingUtils` centralizes
them into one place and makes them easy to find.

Another quality of life improvement is `chill.util.NiceList` which extends `java.util.List` and provides many functions found on 
the java Streams objects, without necessitating going through that library.  `NiceList` is used extensively in other
chill packages to make life easier for you.

### Chill Logging

Chill logging is Yet Another Logging Facade with a simple and easy plugin point, `ChillLogs.setAdapter()` that allows
you to route logs to whatever backend logging framework you prefer.

By default, Chill Apps will set things up so the default logging is done to standard out at level `INFO` but toggleable
on a per-user basis to `DEBUG` when you need to troubleshoot a particular account.

This behavior is easy to override in your own application.

### ChillScript

ChillScript is a hypertalk-inspired dynamically typed scripting language for Chill applications:

```applescript
  set lst to [1, 2, 3]
  for num in lst
    System.out.println(num)
  end
```

The main focus of Chill PL is on *java* development, but a dynamic scripting language comes in useful in a few places:

* You can start a console repl (inspired by the Rails console) that allows you to work with your application dynamically
* You can create maintenance scripts, etc. in the language
* It is used in the templating system, `ChillTemplates`, discussed below

### ChillTemplates

ChillTemplates are the Server-Side Rendering (SSR) templating library for Chill Applications.  They rely on ChillScript
and should look familiar:

```html
#layout /layout/layout.html

<section>
    <h3>Contacts</h3>
    #for contact in current_user.contacts
      <div><a href="/contacts/${contact.uuid}">${contact.name}</a></div>
    #end
    <div>
        <a href="/contacts/new">New Contact</a>
    </div>
</section>
```

You can see that ChillTemplates use `#` to start directives and `${}` for embedding expressions.

Here we see a template that specifies layout for the current template, using the `#layout` directive and then
iterates over a list of contacts using the `#for` directive.

The name of the contact is embedded in a link via a familiar `${contact.name}` syntax.  Expressions are HTML escaped by
default.

### ChillRoutes

Chill routes is a wrapper around the Javalin web framework.  Like Javalin, it allows you to specify routes in a 
semi-declarative manner in a `web.web.Routes` file:

```java
package web;

public class web.Routes extends ChillRoutes {

  public void init() {

    //====================================================
    //  CORE ROUTES
    //====================================================

    get("/", () -> render("index.html"));

    // include another routes file
    include(OtherRoutes.class);
    
  }
  
}
```

You can see here we include another routes file, allowing us to group our HTTP routes together in an organized manner.

### ChillHelper

ChillHelper allows you to surface helper methods in your views, via the `web.Helper` class:

```java
package web;

import chill.web.ChillHelper;
import chill.web.macros.FormMacros;

public class Helper extends ChillHelper {
    {
        include(FormHelper.class);
    }

    public static String exampleHelperMethod() {
       return "Here is an example helper!";
    }
}
```

Static methods found in the helper class will be automatically available within templates.  You can include other helper
classes via the `include()` method.

### ChillEnv

The `ChillEnv` class resolves environment variables against config files (`config/chill.toml`), environment variables,
or manual overrides.  It will print out the state of environment variables on system startup:

```
INFO  [2022-04-14T13:32:37.696369017] - Chill Environment: 
INFO  [2022-04-14T13:32:37.706742681] -     MODE=DEV (source:Manually Set)
INFO  [2022-04-14T13:32:37.706985949] -     PORT=8800 (source:Default Value)
INFO  [2022-04-14T13:32:37.707148326] -     DB_URL=j0dbc:h2:file:./db/demo;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=true (source:TOML file)
INFO  [2022-04-14T13:32:37.707297615] -     DB_DRIVER=org.h2.Driver (source:TOML file)
INFO  [2022-04-14T13:32:37.707440319] -     DB_USERNAME=null <unset>
INFO  [2022-04-14T13:32:37.707576879] -     DB_PASSWORD=null <unset>
INFO  [2022-04-14T13:32:37.707724069] -     DB_CONNECTION_POOL_CONFIG=null <unset>
INFO  [2022-04-14T13:32:37.707866529] -     REDIS_URL=localhost (source:Default Value)
INFO  [2022-04-14T13:32:37.707995548] -     S3.REGION=null <unset>
INFO  [2022-04-14T13:32:37.708096770] -     S3.ACCESS_KEY=null <unset>
INFO  [2022-04-14T13:32:37.708238307] -     S3.SECRET_KEY=null <unset>
```

### ChillRPC

ChillRPC allows you to register and invoke RPC handlers:

```java
    RPC.implement(SampleInterface.class).with(new SampleImpl())
    var rpcInterface = RPC.make(SampleInterface.class);
    int oneAdded = rpcInterface.addOne(41);
```

RPC functions will be invoked through Redis, so no URL end point configuration is required.
