---
layout: post
title: The Best Talks of Strangeloop 2019
description: My favorite strangeloop talks.
---

# The Best Talks of Strangeloop 2019

Almost every talk at Strangeloop sounds intriguing, so how do you pick where to
spend your time? The cliché from Strangeloop veterans is something along the
lines of: pick the abstracts that you don't understand, order them based on your
personal interest, then go to those talks which interest you the least. This was
my first year attending in person, but I have watched about half all the
material available in the online archive (that dates back to 2014), which is
about 200 presentations. From this experience I can confirm the sage advice of
the veterans.

In case you don't have the time or inclination to get through this year's
collection, what follows is a curated list of my favorites that I hope you might
enjoy. In case you couldn't tell from the following, my biases are towards
Clojure, functional programming, and language design.

## Imogen Heap

[video (not yet available)]

Imogen Heap gave a resounding keynote. Her team spent the last 10 years working
on interactive "music gloves" that enable a performer to engage a synthesizer by
waving hands and fingers in the air. The glove's sensors provide an incredibly
rich interface for looping and sampling, and the three-dimensional space
provides the canvas on which to perform. It reminds me loosely of a
[theremin](https://en.wikipedia.org/wiki/Theremin), but with infinitely more
possibilities. The performance is mesmerizing. This wizard on stage is grabbing
sound waves out of the air– like they're tangible strings of yarn, then
contorting them into new shapes to produce a beautiful song. The blend of
entertainment, activism, and education was beautifully done, and I saw quite a
few wet eyes in the audience during the ovation.

## Alda's Dynamic Relationship with Clojure

[video](https://www.youtube.com/watch?v=6hUihVWdgW0)

Dave Yarwood takes us through his journey developing
[alda](https://github.com/alda-lang), a music programming language and tool set.
The market leading GUI-based musical composition software such as Sibelius and
Pro Tools are pretty limiting and cumbersome. alda was introduced as a means to
compose music within a workflow that doesn't feel cumbersome or creatively
limiting. He (bravely) admits his mistakes over the years, such as his ignorance
about inter-process communication that led to an unnecessary
client/server-over-HTTP architecture. It's great to see the phased evolution of
a product as it moves from simple, to "involved", to complex, and back to
simple. Interestingly, he eventually migrates from a clojure to golang
implementation in order to satisfy some non-negotiable usability constraints.
What were those constraints? Startup time, process coordination, and "it's very
important to me to that I'd be able to continue to write algorithmic music in
clojure, and I had to find a way to do that". I'm a little disappointed that he
essentially had to write in a language he didn't want in order to write in the
language he did want.

## On the Expressive Power of Programming Languages

[video](https://www.youtube.com/watch?v=43XaZEn2aLc)

This is technically part of a Strangeloop pre-conference event, Papers We Love,
but easily makes the favorites list. Shriram Krishnamurthi is impressively
engaging, and manages to boil down some rather difficult mathematics and formal
proofs into comprehensible, bite-sized chunks. It reminds me a lot of [Beating
the Averages](http://www.paulgraham.com/avg.html), but if you made the arguments
from formal mathematical proof instead of opinion. Some languages are provably
more expressive than others, even if they are all touring complete.

## Meander: Declarative Explorations at the Limits of FP

[video](https://www.youtube.com/watch?v=9fhnJpCgtUw)

Jimmy Miller doesn't think we (functional programmers) have yet arrived at the
promised land. The base abstractions of map, filter, and reduce are undeniable
improvements over iterative loops, but they don't significantly simplify our
thinking for any sufficiently complex data transformations. We still end up
"playing computer" by tracing values and structures through the code. The inputs
and outputs are opaque. Importantly, when debugging we quickly learn that our
transformations are opaque in a way that types don’t really address. He
introduces a clever library [meander](https://github.com/noprompt/meander) which
provides pattern-matching style symbolic descriptions of transformations. There
are no pipelines of composed functions, only data literals and symbols that
document inputs and outputs. The library figures out how to get from `Input
Shape -> Output Shape` in the background, and you only have to worry about
describing the data structures. It's "data is code is data" taken to another
extreme. I can't wait to try it out, as this is a problem I encounter regularly.

## Performance Matters

[video](https://www.youtube.com/watch?v=r-TLSBdHe1A)

Emery Berger from the CS department at UMass Amherst shared some fascinating
work on program optimization at the system level. He points out how it is
essentially impossible to profile code in a straightforward way given today's
processors and compilers. Everything from measuring performance to tracing the
true impact of a code change is ripe with pitfalls and complexities. The
presentation is example driven, and introduces his research teams' tools
[stabilizer](https://dl.acm.org/citation.cfm?id=2451116.2451141) and
[coz-profiler](https://github.com/plasma-umass/coz). I'm effectively convinced
that without tools like these you are never going to squeeze more performance
out of a system.

## Uptime 15,364 Days

[video](https://www.youtube.com/watch?v=H62hZJVqs2o)

Aaron Cummings covers in extreme detail the missions of Voyager 1 and 2 (and 3,
which I had no idea existed). The unparalleled demonstration of skill and effort
of the mission teams really shines through his talk. Imagine designing a system
with dozens of precise, mobile instruments that operates in the vacuum of space,
sitting next to kilograms of plutonium-238, on hardware of the 60s/70s, with
only 470 watts of power, that has to maintain 100% uptime for at least 5 years.
The entire system was also configurable and extensible enough to be completely
reconfigured on the fly (float?) should the need arise due to failing hardware.
The amount of redundancy is mind-boggling, and I wish we had the time (and
funding) to build every critical system in this manner.
