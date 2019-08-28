---
layout: post
title: GraphQL Tradeoffs
---

# GraphQL Tradeoffs

Last year I had the opportunity to work with GraphQL for about 5 months on a
client project. There has finally been enough distance and hammock time to put
together some thoughts on the matter. Plenty of resources exist to explain the
specification, the tooling, and the proposed benefits of a GraphQL API. There
are a lot fewer resources which talk about tradeoffs and pain points, so I'm
going to provide some more pointed critiques.

## What you get

### Schemas
The spec requires everything to have a schema. This is great. It enables
powerful tooling such as
[playground](https://github.com/prismagraphql/graphql-playground) and
[graphiql](https://github.com/graphql/graphiql). If you're appropriately
[evolving](http://blog.datomic.com/2017/01/the-ten-rules-of-schema-growth.html)
your schema (and not breaking things), you're really going to appreciate the
wins of GraphQL schemas.

### Bytes over the wire
It's very easy to optimize payload size. This is one of the major selling points
of GraphQL. A client requests only what it needs, and no excess data is
transferred over the wire.

### Number of requests
The number of HTTP requests _can_ be dramatically reduced. Retrieving resources
and their relationships (graphs!) is easy, and enables clients to (in general)
make far fewer round trips to the server (than traditional HTTP APIs).

## What you give up

### Specification
Fundamental concepts to any web-based application such as safe/unsafe and
idempotent methods, authentication schemes, response codes, header controls, and
error semantics are well-defined and have been carefully considered for over 20
years. While there are definitely warts in HTTP, there is a mountain of value in
a tightly defined specification that _others_ have hardened. Currently the spec
for GraphQL doesn't cover any of these topics, which places the burden on each
application. It takes an enormous amount of time and effort to develop protocols
or semantics, and that time is probably better spent solving the actual business
problem at hand.

### Caching
Caching is a fundamental pillar of the web and deserves a separate mention.
GraphQL pushes caching out of the network layer (HTTP) and into the application
layer. We lose the long lever of intermediaries (CDNs, reverse proxies,
gateways, etc.) and features like Etag, Is-Modified-Since, Last-Modified, or
Cache-Control. Each application has to develop a custom caching strategy, which
is no small feat. You then have to find intermediaries that speak this new
caching language, or write (and maintain, and deploy) some yourself.

### Interfaces
A query language over a datastore is not an interface. The whole point of an API
is to empower clients to achieve outcomes by having a sensible conversation
about a particular domain. GraphQL is akin to RPC-over-HTTP, with an unbounded
set of ad-hoc operations acting on domain entities. Simply allowing clients to
modify payloads without making changes to the server is not a meaningful
decoupling mechanism. When entities evolve, or business processes change, how
does the coupling of N operations to Y entities hold up over time? Are those
conversations still sensible?

### Optimizations
By exposing any node or edge to a client, you have unleashed
unlimited-complexity queries. There are some boxing strategies for limiting
depth and width, but this is another _thing_ which must be maintained and
communicated to clients. Optimizing hot-path queries just became a lot more
daunting. It's no longer a known trade-off of one set of queries _instead_ of
another set because you have an unbounded set of unknown queries.

## Another approach

If you are considering making the above tradeoffs and adopting GraphQL, I would
first urge you to take a closer look at some hypermedia implementations.
Hypermedia in general provides excellent specification, grants meaningful
client-server decoupling (a closed set of operations on an open set of
resources), offers optimization levers (a known set of resources with known
query semantics), and utilizes fundamental HTTP semantics (including
network-level caching).

In particular, [json:api](https://jsonapi.org/) provides a powerful
specification that covers both serialization formats _and_ expected HTTP
behaviors. [Compound
documents](https://jsonapi.org/format/#document-compound-documents) are a
mechanism for reducing the number of HTTP requests. Likewise, [sparse
fieldsets](https://jsonapi.org/format/#fetching-sparse-fieldsets) provide
comparable flexibility to GraphQL's payload optimizations.

