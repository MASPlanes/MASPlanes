=========== 
Development 
===========

MASPlanes is developed in java, so you will need a Java Development Kit (JDK)
installed on your system. We recomend the usage of some Integrated Development
Environment (IDE) to aid in exploring, refactoring and debuging the software. 
The main developers use `IntelliJ IDEA <http://www.jetbrains.com/idea/>`_ and
`Netbeans <http://www.netbeans.org/>`_, but any other Java IDE should work.

Most java IDEs have excellent support for maven-defined projects, including
auto-detection and configuration if you just clone the project and open it
directly from the IDE.


Implementing coordination algorithms
------------------------------------

If you got here, this probably means that you want to develop your own
coordination algorithm. We think that it is better if you first get an idea of
how the *MASPlanes* simulator works. Hence,  jump to the tutorial on `how to
implement the parallel single-item auctions`_. After going through that tutorial
you should be well equiped to start implementing your own algorithm!

.. _how to implement the parallel single-item auctions: Tutorial.rst

In the near future we expect to expand this document with a few guidelines on
how to debug the project, generate execution traces to analyze the algorithms,
how the simulator is organized, etc.. Unfortunately, we haven't had the time to
do that yet.

If you get stuck at some point, remember that the source code is fully
documented. Don't forget to check the javadocs, they may contain that bit of
information that you were missing to move forward!

If the javadocs do not help, please open an issue on the github page and we will
be glad to help you if we can.


.. Otherwise, keep reading this document, where we briefly review the major parts
.. of the platform and how they fit together.


.. Running a simulation
.. --------------------

.. At its essence, *MASPlanes* is a very simple step-based simulator. That is,
.. the platform represents a ``World`` and its step by step evolution. The world
.. contains two types of entities:

.. 1. ``Element`` entities do not perform any actions by themselves, but can be
..    manipulated and/or interacted with.

.. 2. ``Agent`` entities have some autonomy (they can perform actions).

.. The core of the simulation is implemented in the ``AbstractWorld#run()``
.. method. 
