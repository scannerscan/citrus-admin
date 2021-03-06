## Preconditions

The administration UI is in early beta status and still under construction. Following from that we have to deal with some limitations and
trade offs until the final version is released (hopefully end 2016, keep your fingers crossed!). However the administration UI is usable and
some features are stable.

Please consider following limitations:

### Java 8

The administration UI is implemented in Java 8. Following from that you need Java 8 to run it as web application on your machine.

### Build tools

At the moment we are limited to supporting standard Citrus projects using Maven. Gradle or ANT projects are currently not supported. The build tool used is important because the administration UI is reading
build information and uses Maven to execute test cases.
 
### Citrus version
 
Your project should use Citrus version 2.0 or higher in order to be able to work with the administration UI. Earlier Citrus versions might work too but are
not tested and will not get support with bugfixes.

### Browsers

At this early state we do not support browsers other than Chrome. This does not mean that other browsers are not working with the administration UI but the features are not tested yet with other browsers. The
Citrus development team is using Chrome so you can be sure that errors related to browser incompatibility will be fixed very soon for Chrome.

### Citrus annotations
 
The administration UI is looking fo all tests in your project. It is required that you use *@CitrusTest* and *@CitrusXmlTest* annotations on your test methods. Otherwise the test cases will not be found and displayed.

### Citrus Java DSL

Citrus provides both XML and Java DSL for writing test cases. The administration UI is definitely able to read your XML test cases. The UI should also be able to read and manage your Java DSL test cases but
this feature is not completely stable yet. There might be still some bugs and trade offs when reading Java DSL code. Please be curious and find out if the current working state is working with your project.