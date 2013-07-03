=========== 
Development 
===========

MASPlanes is developed in java, so you will need a Java Development Kit (JDK)
installed on your system. We recomend the usageof some Integrated Development
Environment (IDE) to more easily explore, refactor and debug the software. The
main developers use `Netbeans <http://www.netbeans.org/>`_, so this will be
the easiest option unless you have a strong preference for another Java IDE.


IDE Setup
---------

If you choose *Netbeans* as your platform, getting setup should be as easy as
downloading the project to some location in your computer and opening it as a
Netbeans project.

In case you use another IDE, you can either import the Netbeans project (which
uses ant as the build tools) or create a new project and setup everything from
scratch, importing only the sources. If you choose to go this route, keep in
mind that the ``src`` folder contains regular source code, ``test`` contains unit
tests, and ``lib`` contains *jar* files of all the dependencies required by
*MASPlanes*.


How it works
------------

If you got here, this probably means that you want to develop your own
coordination algorithm. We think that it is better if you first get an idea of
how the *MASPlanes* simulator works. However, you can directly jump to the
tutorial on `how to implement the parallel single-item auctions`_ if you 
are thrilling to start implementing your own algorithm as soon as possible.

.. _how to implement the parallel single-item auctions: Tutorial.rst

Otherwise, keep reading this document, where we briefly review the major parts
of the platform and how they fit together.


Running a simulation
--------------------

At its essence, *MASPlanes* is a very simple step-based simulator. That is,
the platform represents a ``World`` and its step by step evolution. The world
contains two types of entities:

1. ``Element`` entities do not perform any actions by themselves, but can be
   manipulated and/or interacted with.

2. ``Agent`` entities have some autonomy (they can perform actions).

The core of the simulation is implemented in the ``AbstractWorld#run()``
method. 
