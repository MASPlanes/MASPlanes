--------------------------------------------------------
Implementing parallel single-item auctions coordination
--------------------------------------------------------

This tutorial is a walk-through on how to implement a coordination mechanism
for the planes, based on parallel single-item auctions. Therefore, we start by
quickly explaining what parallel single-item auctions are, and how they can
help in solving the coordination problem represented by *MASPlanes*.
Thereafter, we will jump into the implementation details, until we end up with
a functioning implementation of this coordination mechanism.

.. contents:: Contents of this tutorial:

Parallel single-item auctions
-----------------------------

At a particular point in time, the *MASPlanes* coordination problem can be
represented as a task allocation problem. For instance, consider that the
world is in the state represented by the following picture:

..  image:: file://localhost/Users/marc/Documents/Projects/Netbeans/planes/img/tutorial1.png
    :align: center
    :width: 500px
    :alt: Example problem at a specific point in time.

As you can see, the situation is the following:

- Tasks ``T1`` and ``T2`` are allocated to Plane ``P1``.
- Task ``T4`` is allocated to Plane ``P2``.
- Tasks ``T3`` and ``T5`` are allocated to Plane ``P3``.

From the picture, it is pretty easy to tell that this is not an optimal
allocation by any means. Therefore, our objective is to employ parallel
single-item auctions to obtain a better allocation. The parallel single-item
auctions mechanism is pretty simple, and can be explained in three simple
steps:

1. Each plane opens an auction for each of its current tasks. 2. Upon
receiving an auction anouncement, a plane replies with a *bid*, specifying the
cost to fulfill that task (its current distance from the task). 3. Finally,
the auctioneer collects all the bids for each of its tasks, and reallocates
each task to the best bid (the one with lowest cost)

At the end of this simple procedure, each task will be assigned to the plane
that is currently nearest. In this particular case, this means that on the new
allocation ``P1`` will get ``T1``, ``P2`` will get ``T2``, ``T3``, and ``T4``;
and ``P3`` will get ``T5``. Although the end result may not be optimal, this
allocation looks much better than the previous one.


Implementation
--------------

All the code implemented by this tutorial is available in a branch named
``tutorial`` within the *MASPlanes* git repository. You will have to download
that branch if you want to follow the already-implemented code. However, we
recommend you to follow through the implementation as described here, so you
get more acquainted with the platform.

In this tutorial, we will put all new classes into a specific package
(``es.csic.iiia.planes.tutorial`` in the already-implemented version).
Unfortunately there are some classes of the platform that we have to edit, so
your code must be implemented within the same *MASPlanes* project and can not
be isolated in a project of its own.


Custom planes type ``TutorialPlane``
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

We want to implement the above procedure within the simulator. In essence,
this means that we want planes that behave in a custom manner. Therefore, the
first thing we must do is create our own type of planes. Therefore, we will
create a  ``TutorialPlane`` class, implementing the ``Plane`` interface to
allow it to be treated as an actual plane by the platform.

Although we could implement the ``Plane`` plane interface by ourselves, the
platform includes an ``AbstractPlane`` class that has most of the common
functionally already implemented. This includes flying to the nearest task
assigned to this plane and deciding what to do whenever it has no assigned
tasks, among many others. Therefore, we create our class extending from this
one:

.. sourcecode :: java

    public class TutorialPlane extends AbstractPlane {

        public TutorialPlane(Location location) {
            super(location);
        }
     
        @Override
        protected void taskCompleted(Task t) {
            System.out.println(this + " completed " + t);
        }

        @Override
        protected void taskAdded(Task t) {
            System.out.println(t + " is now allocated to " + this);
        }

        @Override
        protected void taskRemoved(Task t) {
            System.out.println(t + " is no longer allocated to " + this);
        }
        
    }

As you can see, extending ``AbstractPlane`` forces us to implement three
methods. These methods are called by the base class whenever a task is
completed by this plane, added (allocated) to this plane, or removed
(deallocated) from this plane respectively. For now, we will just print out
what happened to the standard error.


Launch a simulation with our custom planes
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

At this point we should already have functional (albeit very silly) planes.
However, we need to let the simulator know that it can use those planes by
modyfing the available configuration options. This can be easily done by
modifying the (private) method
``es.csic.iiia.planes.cli.Configuration#getPlaneClasses()`` method, adding a
new entry for our custom plane type:

.. sourcecode :: java

    private Map<String, Class<? extends Plane>> getPlaneClasses() {
        return new HashMap<String, Class<? extends Plane>>() {{
           put("auction", AuctionPlane.class);
           put("none", DefaultPlane.class);
           put("maxsum", MSPlane.class);
           put("omniscient", OmniscientPlane.class);
           put("tutorial"), TutorialPlane.class);
        }};
    }

This part is optional, but it is also nice to document that this new type of
planes is available in the default configuration file. Therefore, we can edit
the ``es.csic.iiia.planes.cli.settings.properties`` file:

.. sourcecode :: diff

    @@ -22,6 +22,7 @@ operator-strategy=nearest-inrange
     #   auction     Planes coordinate with each other using auctions.
     #   maxsum      Planes coordinate using max-sum.
     #   omniscient  Planes that coordinate through an omniscient entity.
    +#   tutorial    Use the planes implemented in the MASPlanes tutorial.
     planes=none
     
     # Type of the battery used by the planes.

Recompile the project, and check that your changes are actually effective:

1. If you updated the default settings file, check that the changes are shown
when you dump the default settings file:

   .. code:: bash
    
    java -jar dist/MASPlanes.jar -d

2. Then, run the simulator with your shiny new planes instead of the default ones:
   
   .. code:: bash

    java -jar dist/MASPlanes.jar -o planes=tutorial problem.json -g

If everything went well, you should see the messages being printed by the
planes whenever they get and complete tasks. For now, the planes are not
coordinating at all. Thus, the operator allocates tasks to whatever plane it
can, and then this plane is going to complete this tasks one after the other
(by always going to the nearest allocated task).


Improving the plane's behavior
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Now that we have working planes, it is time to add some interesting behaviors
to them. In *MASPlanes*, this is achieved by adding ``Behavior`` classes to
the planes. A behavior is a class that bundles together some actions and
reactions, possibly involving communicating with other planes.

To better understand the capabilities of these behaviors, take a look at the
javadoc of the ``Behavior`` interface. Basically, the interface defines the
following action methods, where a plane can initiate some actions (such as
sending messages):

``preStep()``     
    This method is invoked at the beggining of each step. The
    platform guarantees that this method will be called on **all** behaviors of
    **all** agents before any other action methods are called. That is, the
    plaform will never call the ``beforeMessages()`` method of an agent's behavior
    unless all other agents have already executed their ``preStep()`` operations.

``beforeMessages()``
    This method is invoked right before processing any messages received in this 
    step.

``on(MessageType)``
    You can have as many of these methods as you wish. These methods are invoked 
    once for each message of type ``MessageType`` received in this step.

``afterMessage()``
    Invoked immediately after the plane has processed all the received messages.

``postStep()``
    Called after **all** behaviors of **all** agents have processed their messages.

Knowing this, we can now try to implement the parallel single-item auctions
mechanism using a behavior. Instead of implementing all of the ``Behavior``
methods, we will simply extend the ``AbstractBehavior`` class, which gives us
a default (no action) implementation for all the above methods:

.. sourcecode :: java

    public class PSIAuctionsBehavior extends AbstractBehavior<TutorialPlane> {

        public PSIAuctionsBehavior(TutorialPlane agent) {
            super(agent);
        }
        
        @Override
        public Class[] getDependencies() {
            return null;
        }
        
    }

For now you can ignore the ``getDependencies()`` method, whose function we
will explain later on. Before expanding this behavior, let's actually make our
planes use it. Since we used the ``AbstractPlane`` as a base class for our
``TutorialPlane``, it is now very easy to incorporate a behavior to our
planes. In fact, we only have to call the ``addBehavior(Behavior)`` method at
some point, and the plane will start using it. Typically, the best place where
to add behaviors is during the plane's initialization function. Therefore, we
can open our ``TutorialPlane`` class and override its initialization method,
adding our new behavior

.. sourcecode :: java

    @Override
    public void initialize() {
        super.initialize();
        addBehavior(new PSIAuctionsBehavior(this));
    }

Our planes will now execute the ``PSIAuctionBehavior``, performing any actions
defined in their action methods and reacting to messages appropiately.


Opening auctions
^^^^^^^^^^^^^^^^

The next step is to make the planes open an auction for each of their
currently allocated tasks. Actually, this amounts to sending a (broadcast)
message to announce the auction. Therefore, we should first define this
message.

Unsurprisingly, all classes defining a message type must implement the
``Message`` interface. From that interface's javadoc, it is clear that
messages must specify a sender and a recipient. However, the recipient of a
message can be set to ``null``, in which case it will be considered as a
broadcast message.

Back to our auction opening, we will create an ``OpenAuctionMessage`` class
defining our messages to open auctions. Instead of directly implementing the
``Message`` interface, we can extend from the ``AbstractMessage`` class, which
already implements the facilities to get and set the sender/recipient.
Messages opening auctions must specify who the auctioneer is, and which Task
is being auctioned. The auctioneer is always the sender of the message, so
there's no need to add a specific field for that. However, we do have to add a
field to specify which Task is being auctioned:

.. sourcecode:: java

    public class OpenAuctionMessage extends AbstractMessage {
        
        private Task task;
        
        public OpenAuctionMessage(Task t) {
            this.task = t;
        }
        
        public Task getTask() {
            return task;
        }
        
    }

Now that we have a message to tell other planes about the auctions we are opening, it is time to actually send those out. Because auction opening messages are not sent in response to other messages, we must use one of the aforementioned action methods of our behavior. Notice that, being a step-based simulator, messages sent by a plane in the current step will not be received by other planes until the next one. Therefore, it does not really matter wether we send these auction opening messages during the ``preStep``, ``beforeMessages``, or ``afterMessages`` phases of a step. In this tutorial, we arbitrarily chose to do in the ``afterMessages`` phase. Thus, we modify our ``PSIAuctionsBehavior`` class, adding the following:

.. sourcecode:: java










