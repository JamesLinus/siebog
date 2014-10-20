Siebog: An Enterprise-Scale Multiagent Middleware
======

Siebog is an enterprise-scale multiagent framework consisting of the following main modules:

 1. **Extensible Java EE-based Agent Framework (XJAF)**: Built using Java EE technologies, XJAF operates on top of computer clusters, offering automatic agent load-balancing, state-replication, and fault-tolerance.
 2. **Radigost**: Purely web-based multiagent platform, built using JavaScript and HTML5-related standards. Radigost agents are executed inside web browsers and can be used in a wide variety of hardware and software platforms, including personal computers, smartphones and tablets, Smart TVs, etc.
 3. **Jason interpreter, Enterprise Edition (Jason EE)**: A port of the popular [Jason interpreter](http://jason.sourceforge.net) to Java EE (i.e. using XJAF as the underlying infrastructure). 
 4. **Distributed Non-Axiomatic Reasoning System (DNARS)**: An advanced reasoning system based on the *Non-Axiomatic Logic* (NAL) formalism.
 
Selected Publications
-------

 * D. Mitrovic, M. Ivanovic, M. Vidakovic, Z. Budimac. [Extensible Java EE-based Agent Framework in Clustered Environements](http://perun.pmf.uns.ac.rs/xjaf/papers/xjaf2x.pdf). In J. Mueller, M. Weyrich, and A. L. C. Bazzan, eds., *12th German Conference on Multiagent System Technologies*, vol. 8732 of *Lecture Notes in Computer Science*, pp. 202-215. Springer International Publishing, 2014. The final publication is available at [SpringerLink](http://link.springer.com/chapter/10.1007/978-3-319-11584-9_14).
 * D. Mitrovic, M. Ivanovic, Z. Budimac, M. Vidakovic. [Radigost: Interoperable web-based multi-agent platform](http://www.sciencedirect.com/science/article/pii/S0164121214000028). *Journal of Systems and Software*, 90:167-178, 2014.
 * M. Vidakovic, M. Ivanovic, D. Mitrovic, Z. Budimac. [Extensible Java EE-based agent framework - past, present, future](http://link.springer.com/chapter/10.1007%2F978-3-642-33323-1_3). In M. Ganzha and L. C. Jain, eds., *Multiagent Systems and Applications*, vol. 45 of *Intelligent Systems Reference Library*, pp. 55-88. Springer Berlin Heidelberg, 2013.
 * D. Mitrovic, M. Ivanovic, Z. Budimac, M. Vidakovic. [Supporting heterogeneous agent mobility with ALAS](http://www.comsis.org/pdf.php?id=020-1201). Computer Science and Information Systems, 9(3):1203-1229, 2012.

Details about non-axiomatic reasoning and NAL can be found [here](http://www.cis.temple.edu/~pwang/papers.html).

Download
------

Binary distribution of Siebog is [available for download (ZIP, ~140MB)](http://perun.pmf.uns.ac.rs/xjaf/dist/siebog.zip).

The archive includes the [WildFly](http://wildfly.org) enterprise application server (formerly: JBoss AS), pre-configured to work with Siebog. After unpacking the archive, please [set an environment variable](https://www.google.com/search?q=how+to+set+environment+variables) named JBOSS\_HOME to point to the *wildfly-9.x* sub-folder (e.g. *C:\siebog\wildfly-9.x* on Windows, or */home/myself/siebog/wildfly-9.x* on Linux).

Please note that Siebog requires Java SE Development Kit (JDK) version 8 to run. The Java Runtime Environment (JRE) will not suffice. You may [download the latest JDK here](http://www.oracle.com/technetwork/java/javase/downloads/index.html). To make sure the correct Java version is used, you should set an environment variable named JAVA\_HOME to point to the JDK installation folder
(e.g. *C:\Program Files\Java\jdk1.8.0_20* on Windows, or */usr/lib/jvm/java-8-oracle* on Linux).

Finally, to start Siebog, go to the unpacked folder, and execute `java -jar siebog-start.jar`. Once you see the message *INFO: Siebog node ready.* visit the client application at [http://localhost:8080/siebog](http://localhost:8080/siebog). From there, you may explore the framework's functionalities and run some of the bundled example agents. For more information about writing your own agents, please consult the source code.

Running Siebog in a cluster
-------

Siebog operates in a symmetric cluster: each node is connected to every other node. A single node is recognized as the *master* and can be used to remotely control the cluster. Other nodes are called *slaves*.

By default, the `start-siebog` utility runs a single master node on localhost. This behavior can be changed using the program argument "--node":

 * If the node is master, use *--node=address*, where *address* is the node's network address.
 * Otherwise, use *--node=name@address,master@master\_address*, where *name* is the cluster-wide unique name of the slave node, *address* is its network address, and *master\_address* is the network address of a running master node.

Contact
-------

If you have any questions or need additional information about Siebog, please [send us an e-mail](mailto:mitrovic.dejan@gmail.com).
