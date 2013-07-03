--------------------------------------------------------
Implementing parallel single-item auctions coordination
--------------------------------------------------------

This tutorial is a walk-through on how to implement a coordination mechanism
in *MASPlanes*, based on parallel single-item auctions. Therefore, we start by
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

1. Each plane opens an auction for each of its current tasks. 

2. Upon receiving an auction anouncement, a plane replies with a *bid*,
   specifying the cost to fulfill that task (its current distance from the task).

3. The auctioneer collects all the bids for each of its tasks, and reallocates
   each task to the best bid (the one with lowest cost).

4. Finally, planes receive whatever tasks have been newly allocated to them,
   and the task allocation procedure is complete.

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

Now that we have a message to tell other planes about the auctions we are
opening, it is time to actually send those out. Because auction opening
messages are not sent in response to other messages, we must use one of the
aforementioned action methods of our behavior. Notice that, being a step-based
simulator, messages sent by a plane in the current step will not be received
by other planes until the next one. Therefore, it does not really matter
wether we send these auction opening messages during the ``preStep``,
``beforeMessages``, or ``afterMessages`` phases of a step. In this tutorial,
we arbitrarily chose to do in the ``afterMessages`` phase. 

However, there's still a minor issue to sort out. If we simply open an auction
at every step, we would be starting new auctions for tasks that are already
being auctioned. This is not what we want, so we have to somehow control that
a new action is only started after the older ones have finished. Fortunately,
this is fairly easy to do in our step-based simulator. From the explanation of
parallel single-item auctions above, we know that the whole process takes
exactly four steps. As a consequence, we can simply start a new auction every
four steps, and rest assured that there will never be two simultaneous auctions
for the same task.

All this can be easily implemented by modifying our ``PSIAuctionsBehavior``
class, where we add the following:

.. sourcecode:: java

    @Override
    public void afterMessages() {
        // Open new auctions only once every four steps
        if (getAgent().getWorld().getTime() % 4 == 0) {
            openAuctions();
        }
    }

    private void openAuctions() {
        TutorialPlane plane = getAgent();
        for (Task t : plane.getTasks()) {
            OpenAuctionMessage msg = new OpenAuctionMessage(t);
            plane.send(msg);
        }
    }


Bidding for tasks
^^^^^^^^^^^^^^^^^

Now that the planes already start auctions for their tasks, it's time to make
them bid on the auctions they receive. These bids will be messages sent to the
tasks' auctioneers, so we have to start by defining the ``BidMessage`` class.
In this case, the message must identify for which task the bid is, as well as
the cost for the sending plane to perform the bid's task:

.. sourcecode:: java

    public class BidMessage extends AbstractMessage {
        
        private double cost;
        private Task task;
        
        public BidMessage(Task t, double cost) {
            this.task = t;
            this.cost = cost;
        }
        
        public double getCost() {
            return cost;
        }
        
        public Task getTask() {
            return this.task;
        }
        
    }

Next, we need to actually send these bid messages out in response to the
incoming ``OpenAuctionMessage`` messages. Therefore, these (re)action can be
implmented by introducing a new ``on(OpenAuctionMessage)`` method to our
``PSIAuctionBehavior``:

.. sourcecode:: java

    public void on(OpenAuctionMessage auction) {
        TutorialPlane plane = getAgent();
        Task t = auction.getTask();
        
        double cost = plane.getLocation().distance(t.getLocation());
        BidMessage bid = new BidMessage(t, cost);
        bid.setRecipient(auction.getSender());
        plane.send(bid);
    }

There is nothing fancy going on here. Upon a receiving an
``OpenAuctionMessage``, the plane simply (i) computes the cost to perform the
auction's task (defined as the current distance from the plane to the task);
and (ii) sends a bid to the auctioneer (the sender of the auction message)
specifying that cost.

Since this method will get call once for each incoming ``OpenAuctionMessage``,
this is all we need to implement for the planes to perform the second step of
the coordination algorithm, and we are ready to move on.


Choosing the winners
^^^^^^^^^^^^^^^^^^^^

At first, the winner selection action may seem to be a (re)action to the
received bids, just like bidding was a reaction to the received
``OpenAuctionMessage`` messages. Nonetheless, we must collect all the incoming
bids for a task before choosing the winner. As a consequence, the winner
determination process must be decomposed in two parts.

Collect bids for each task
..........................

First, we must collect all the incoming bids, preferably separated by the task
they are for. Thus, we need a ``Map`` from ``Task`` to a set of received bids.
This map must be cleared at each simulation step, before actually processing
the messages. Thus, the map clearing will be implemented within the
``beforeMessages()`` actions. Thereafter, we can actually collect the
``BidMessages`` using an ``on(BidMessage)`` (re)action. With this aim, we add
the following code to our ``PSIAuctionBehavior`` class:

.. sourcecode:: java

    private Map<Task, List<BidMessage>> collectedBids =
            new HashMap<Task, List<BidMessage>>();

    @Override
    public void beforeMessages() {
        collectedBids.clear();
    }

    public void on(BidMessage bid) {
        Task t = bid.getTask();

        // Get the list of bids for this task, or create a new list if
        // this is the first bid for this task.
        List<BidMessage> taskBids = collectedBids.get(t);
        if (taskBids == null) {
            taskBids = new ArrayList<BidMessage>();
            collectedBids.put(t, taskBids);
        }

        taskBids.add(bid);
    }

Choose winners and reallocate tasks
....................................

Second, we must determine the winner and reallocate the tasks if the winner of
a task is not the plane where the task is currently allocated. As should be
familiar by now, this reallocation should be notified with a message sent from
the auctioneer to whatever plane the task must be reallocated to. Therefore,
we will first create a ``ReallocateMessage`` message class to perform such
notifications:

.. sourcecode:: java

    public class ReallocateMessage extends AbstractMessage {

        private Task task;

        public ReallocateMessage(Task t) {
            this.task = t;
        }

        public Task getTask() {
            return task;
        }

    }

Now we can proceed to compute the auction winners, but only after having
processed all the incoming messages. Hence, the winner determination procedure
must be performed int the ``afterMessages()`` actions. Since this method is
aleady implemented in our behavior, we have to add the code along with the
existing one:

.. sourcecode:: java

    @Override
    public void afterMessages() {
        // Open new auctions only once every four steps
        if (getAgent().getWorld().getTime() % 4 == 0) {
            openAuctions();
        }

        // Compute auction winners only if we have received bids in this step
        if (!collectedBids.isEmpty()) {
            computeAuctionWinners();
        }
    }

The new code calls a function that we have yet to implement. Hence, we also
need to add the following methods to our ``PSIAuctionBehavior`` class:

.. sourcecode:: java

    private void computeAuctionWinners() {
        for (Task t : collectedBids.keySet()) {
            BidMessage winner = computeAuctionWinner(collectedBids.get(t));
            reallocateTask(winner);
        }
    }

    private BidMessage computeAuctionWinner(List<BidMessage> bids) {
        BidMessage winner = null;
        double minCost = Double.MAX_VALUE;

        for (BidMessage bid : bids) {
            if (bid.getCost() < minCost) {
                winner = bid;
                minCost = bid.getCost();
            }
        }

        return winner;
    }

    private void reallocateTask(BidMessage winner) {
        TutorialPlane plane = getAgent();
        
        // No need to reallocate when the task is already ours
        if (winner.getSender() == plane) {
            return;
        }

        // Remove the task from our list of pending tasks
        plane.removeTask(winner.getTask());
        
        // Send it to the auction's winner
        ReallocateMessage msg = new ReallocateMessage(winner.getTask());
        msg.setRecipient(winner.getSender());
        plane.send(msg);
    }

Although this is a big chunk of code, it should be pretty self-explanatory.
Basically, we compute the winner for each task we are auctioning. Notice that
the winner of an auction is usually whoever makes the highest bid. However, in
this particular case we are bidding costs, so the winner will be whoever has
the lowest valued bid. Finally, we reallocate those tasks for which we did not
win the auction.


Accepting reallocated tasks
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Apparently, the only thing left to do is to make planes accept those tasks
that have been reallocated to them. This is clearly a pure reaction to the
received ``ReallocateMessage`` messages, so we just have to add a simple
method to our behavior:

.. sourcecode:: java

    public void on(ReallocateMessage msg) {
        getAgent().addTask(msg.getTask());
    }

And that's it. At this point we should have planes that coordinate using the
parallel single-item auctions. However, we must still test that everything
works correctly before finishing!


Testing
-------

