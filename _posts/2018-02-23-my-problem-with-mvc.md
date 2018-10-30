---
layout: post
title: My Problem With MVC
---

# My problem with MVC

Imagine rearranging the furniture in a typical living room. The MVC approach to
rearranging would go something like this — slide the couch over, move the lamp
and tv, move the piano, slide the couch around, move the armchair out of the
way, place the couch where it needs to go, move the lamp and tv to their new
locations, slide the piano into position, and put the armchair back.

Rearranging in this fashion is certainly possible, and people do it all the
time. However, shuffling furniture this way introduces a lot of complexity. It
requires remembering past, present, and future locations (statefulness), the
moving of pieces into temporary positions (imperativeness), and the lifting of
items that didn’t actually need to move at all (unpredictability). What’s more,
the problem space grows in a nonlinear fashion. If you are tasked to arrange a
living room with twice the previous room’s area and twice the pieces of
furniture, the problem proves more than twice as hard. With more pieces to move,
there are more shuffles to make and more locations to remember- all leading to
more frustration.

We also have to consider the finicky issue of time. In the non-blocking
networking and blocking rendering environment of a browser, It’s unacceptable to
spend too much time shuffling at any given point. It’s like you live in a house
with temperamental and destructive roommates. If any roommate is kept waiting
around for more than a moment, they start to yell and break things. If anyone
wants to use the living room but it is not in a useable layout, they become
enraged and set fire to the house. Therefore, you cannot keep people out of the
living room for very long, and the layout cannot diverge too far from a usable
state. Additionally, at any point throughout the shuffle, a delivery man might
drop off a new sofa to replace the old one. Also, the post office needs to
deliver a critically important letter that cannot be missed, so you have to
constantly pause to look out the window at the mailbox.

Suddenly, the MVC strategy for rearrangement seems less and less favorable. At a
certain point, the imperative, stateful, and unpredictable nature of MVC will
make rearranging a living room virtually impossible. Can this mess be avoided?
Certainly, and we avoid it by using a reducer architecture.

Imagine there exists a magic wand that can instantly transpose the position of
two items. With the wand, rearranging furniture requires little more than
pointing at pieces, and there is no shuffling required. Now imagine a magic
door. This door prevents people from entering without your explicit permission,
but also entertains those in queue at the door well enough to make them forget
they are waiting at all. Lastly, imagine a magic mailbox. This mailbox signs for
packages and letters, holds onto deliveries, and notifies you of any activity.
With these three magic items, rearranging a living room of any size becomes a
methodical, manageable exercise.

In the reducer architecture, we have the magic wand, door, and mailbox at our
disposal. The wand is a composable hierarchy of finite state machines
commensurate with all sufficiently complex user interfaces. The door is a
yielding generator message bus inspired by domain driven design sagas. The
mailbox is a queued event-driven architecture derived from CQRS/event-sourcing.
Together, these pieces wrangle state, abstract away imperativeness, and
eliminate unpredictability, alleviating many of the pains that surface from MVC.
With this paradigm shift, rearranging the living room becomes a pleasant and
pragmatic task, no matter how dangerous your roommates or untimely your
deliveries.

To provide some more concrete context, the evolution of reducer architectures
started in the mainstream with Facebook’s
[Flux](https://github.com/facebook/flux). It has since evolved over a series of
slightly different *ux architectures. In the past year or so the dust has
settled and it seems that [Redux](https://redux.js.org/) is the reigning
champion, for now.
