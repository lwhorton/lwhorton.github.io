---
layout: post
description: The advantages of clojurescript.
---

# Why Clojurescript?

I would like to share with you an abbreviated version of my journey that led to
Clojure and Clojurescript. Hopefully, by the end, I will have convinced you to
fire up a REPL and at least play around for a few hours.

## Preface
I didn't get it.

Around 2012 this thing called Clojurescript popped onto my radar via js weekly.
What? Someone is trying to make a lisp-into-javascript compiler? That's almost
laughable; what a convoluted way to handle client-side code. Functional
programming is wonderful in a lot of contexts and domains, but a client's
browser is not one of them.

## It won't work
The whole idea of lisp-in-a-browser reminded me of a conference talk I once
attended. During the presentation some really bright folks gave a compelling
demonstration of the power and wonders of [functional reactive
programming](https://wiki.haskell.org/Functional_Reactive_Programming). They
established a series of pipes, filters, nodes, trees, sinks, and drains that
worked wonders on input streams and processed flows beautifully. In the final
minutes of the presentation, they switched over to demo mode and tried to use
this method of programming to handle events in a browser. It did not go well.

Between the three presenters, each a FRP expert, it took them over 20 minutes to
get a `<button>` and `<input>` on the page that reliably accepted user input.
Ouch.  The layers of wrappers and adapters and signal converters were too deep.
The presenters stumbled their way through exceptions and undefined or null
errors.  There were too many abstractions that had to be shoe-horned onto the
existing environment for anything to work in the slightest, and that cognitive
overhead proved too much even for the experts.

No thanks. [I'll](https://www.polymer-project.org/)
[stick](https://www.sencha.com/products/extjs/) [to](http://sproutcore.com/)
[my](http://knockoutjs.com/) [flavor](https://www.emberjs.com/)
[of](https://angularjs.org/) [the](http://backbonejs.org/)
[month](https://www.meteor.com/) [javascript](https://vuejs.org/)
[framework](https://aurelia.io/).  They are much "simpler", have a ton of useful
features, and are familiar to my way of thinking. Heck, if I wanted a transpiled
language, why not pick up something like
[Coffeescript](http://coffeescript.org/)? It saves a few keystrokes, smoothes
over some rough javascript edges, and introduces familiar concepts like classes
and inheritance.  Importantly, it interfaces very easily with [flavor of the
month framework], since it's ultimately just javascript in ruby's clothing.

Coincidentally, I actually did end up working with Coffeescript for a while at
my regular day job.  It performed exactly as expected and, looking back now,
produced code that was more or less the same quality as if I wasn't using a
transpiler at all.

## Time is a funny thing
Fast forward a few years in my life as a developer. I met an invaluable mentor
who showed me true craftsmanship, and what it meant to be a professional in this
domain. I started thinking about process and not just product. The ratios of
input to output, effort to quality, and bugs to features weighed more heavily on
my decisions. Architectural concerns, business value propositions, and reliable,
consistent growth of systems (as well as businesses) moved into the forefront of
my mind. I became engrossed in learning as much as possible about software
engineering in the abstract: what worked historically, what works now, and
what's going to work in the future.

Among these experiments, blogs, tutorials, lessons, conferences, videos, and
discussions, a few gems shined very brightly from beneath the piles of dirt.
Perhaps the most personally influential gems were the now-famous [Simple Made
Easy](https://www.infoq.com/presentations/Simple-Made-Easy), as well as [The
Value of Values](https://www.youtube.com/watch?v=-6BsiVyC1kM). While they were
presented in the abstract, applicable to any language or system, it was a
logical next step for me to check out what this Rich Hickey guy was working
with. Surely these fantastical ideas are purely academic and cannot be applied
reliably in "the real world" of computer systems, right? I couldn't have been
more wrong.

What follows is a summary of _some_ of the super powers afforded by
Clojurescript that I have come to appreciate over the last three years of
regular use.

## Player 2 has entered the game
Clojurescript is truly functional programming. Nestled in with all the other
benefits of the functional paradigm is **context-free programming**. This makes it
easy for engineers to work on small parts of the system in complete isolation.

What does this mean in practice? You don't have to fit the whole system into
your head before making changes. What you see is what you get– a collection of
functions that act only on inputs and return only outputs. There is no global
mutable state, no side effecting, no "I hope this doesn't break anything else"
mentality. Ramp-up time is negligible for unfamiliar engineers because they can
digest bite-sized pieces, and the boundaries between pieces are always well
defined.

Clojurescript is **expressive** by design. It's been said that, in a lisp, you don't
write software; you write the language in which you are going to write your
software. The same holds true for Clojurescript. 

Consider a pretty trivial example meant to demonstrate the power of
expressivity: a user fills out a form. That user may or may not have unsaved
changes on the form. We'd like to write a function that asks a user to confirm
before exiting, but only if they have unsaved changes on the form.

Compare these two examples in clojurescript and javascript. How long it take you
to understand the intent of the functions and the implementer's approach and
design? How much syntax did you have to parse in your working memory to figure
everything out? How much base level of knowledge of the language did you need to
grok before things made sense? How many different implementations did you work
through before landing on something that worked well?

```clojure
;; an example of the form
{:first-name "..."
 :last-name "..."
 :address {:street "..."}
 :languages #{"en" "de" "fr"}}

(defn confirm-leave? [original-form changed-form]
    (= original-form changed-form))
```

```javascript
function confirmLeave(originalForm, changedForm) {
    // remember that conventionally is* variables are boolean
    // remember that let allows you to mutate this assignment
    let isChanged = false

    // use the language's forlet construct to iterate an Object. the forlet
    // iterates on keys, though, so dont forget to use key-value lookups.
    for (let key in changedForm) {
        // remember to ignore inherited/prototyped props 
        // remember to use the Object's base hasOwnProperty function
        // ... you could maybe also use originalForm.hasOwnProperty, but only if
        // it hasn't been mutated 
        if (Object.hasOwnProperty(originalForm, key)) {
            // TODO: for now we're assuming all values are scalar so we can check equality 
            // ... but equality checking non-scalars is a trickier problem
            // dont forget that strict equality requires the triple form ===,
            // otherwise you end up with auto-casting
            if (changedForm[key] !== originalForm[key]) {
                isChanged = true

                // we could technically exit the loop here because we found at
                // least 1 non-equal value, but we don't have a way to break the
                // forlet loop
            }
        }
    }

    // dont forget to return the computed value
    return isChanged
}
```

There are about 7 "things" to hold in your head when writing the javascript. 

- how does variable assignment work, and what are the differences between `var`,
`let`, and `const`?
- how do i iterate the form?
```javascript
for...in 
for...of  
Object.keys(o).forEach(key...) 
Object.values(o).forEach(val ...) 
Object.entries(o).forEach([key, val] ...) 
Object.getOwnPropertyNames(o).forEach(key ...) 
Reflect.ownKeys(o).forEach(key ...)
```
- am i equality checking a scalar, or something else, and how do i do that?
- remember to reassign a variable which exists in a hoisted scope 
- how can i exit the loop when i found a matching condition?
- remember to return the value or we return `undefined`

If the author isn't intimately familiar with the peculiarities of the language,
a simple equality check between two forms becomes an error-prone mess of
complexity.

## Simplicity
**Simplicity** is at the heart of everything, and the community upholds this
tenant. The fundamental unit of information in Clojurescript is data. There's native
support for the most common data types: maps `{:house "stark"}` , sets
`#{:arya :ned :sansa}`, lists `'("winter" "is" "coming")`, vectors `["winter"
"finally" "arrived"]` , and keywords `:didnt-see-that-coming`.

Libraries, systems, and APIs are structured around Data > Functions > Macros,
which means everything composes by simply using data to communicate. Generally,
third-party libraries "do one thing" well and expose a simple, data-based API.
You can build a dependency tree that matches your domain properly, instead of
stuffing libs together that only play nice with particular partners or "sort of"
do what you need. Bonus– you also have available the entire existing java
ecosystem, or javascript ecosystem, depending on the host platform.

Clojurescript is **extremely well designed**. It is a hosted, dynamic,
general-purpose, functional language that features immutable, persistent data
structures and a robust infrastructure for multithreading. The language is small
(clojure.core is about 5k loc, or just a weekend of reading), offers consistent
abstractions, and always maintains backwards compatibility.

Let me elaborate on a particularly important part of the previous paragraph:
**immutable, persistent data structures**. What does this mean? You cannot
mutate data, only copy it into a new structure with some new changes. All
references to the previous structure remain in tact, un-mutated, forever. Don't
worry, though– this doesn't result in an excessive use of memory or CPU.
Persistent data structures are [well
studied](https://github.com/matthiasn/talk-transcripts/blob/master/Hickey_Rich/PersistentDataStructure.md),
and have become extremely efficient over the years. Native javascript
operations, conversely, don't use persistent structures and [they pay for
it](https://medium.com/@dtinth/immutable-js-persistent-data-structures-and-structural-sharing-6d163fbd73d2)
in performance.

```clojure
;; refs are never mutated
(let [a [1 2 3]
      b (conj a 4)]
  (println a) ;; [1 2 3]
  (println b) ;; [1 2 3 4]
  ;; equality is value-based, not identity based
  (= a b) ;; false
  (= a (drop-last b)) ;; true
  ;; but you can still compare identity
  (identical? a (drop-last b))) ;; false
```
```javascript
// vars are mutable
var a = [1, 2, 3]
var b = a.push(4)
console.log(a) // [1, 2, 3, 4]
console.log(b) // 4, huh?

// equality has to be checked on a per-field basis
b = [1, 2, 3, 4]
console.log(a == b) // false
console.log(a === b) // false
;[1,2,3] == [1,2,3] // false
```

A recent trend in javascript is to move towards persistent structures through
tools like [immutablejs](immutablejs). Immutable javascript is a great step in
the right direction, except it introduces a couple really big problems: the
libraries expose a pervasive API that requires holistic buy-in for an
application, and immutable structures don't interop with anything that's not
also an immutable structure (goodbye [lodash](https://lodash.com/),
[jquery](https://jquery.com/), or even [alternative immutable
implementations](https://github.com/swannodette/mori).).

But wait, you might say, we can use ES6/ES2017 syntax to achieve immutability!
Yes, you can make immutable objects via the new ... spread operators, or the
likes of `Object.assign({}, {...old, new: true})` and proper use of
`map/filter/reduce`. Unfortunately, it has introduced another prickly edge to
javascript; remember to use immutable operators everywhere. This is not a
trivial burden, particularly when you're talking about updating indexed arrays
or objects inside arrays. Also, the benefits of structural sharing are lost, so
expect heavy performance implications.

```clojure
(let [old {:a 1 :b 2}
      new-o (assoc old :c 3)]
  (println old)) ;; {:a 1 :b 2}

(let [arr [1 2 3]
      new-a (conj arr 4)]
  (println arr)) ;; [1 2 3]

(let [obj-arr [{:a 1} {:b 2} {:c 3}]
      new-o-a (assoc obj-arr 1 {:b -2})]
  (println obj-arr) ;; {:a 1} {:b 2} {:c 3}]
  (println new-o-a)) ;; {:a 1} {:b -2} {:c 3}]
```
```javascript
var old = {a: 1, b: 2}
var newO = Object.assign({}, {...old, c: 3})
console.log(old) // { a: 1, b: 2 }

var arr = [1, 2, 3]
var newA = [...arr, 4]
console.log(arr) // [1, 2, 3]

// what about updates/insertions? careful not to miss any ellipses or slices or off-by-ones
var objArr = [{a: 1}, {b: 2}, {c: 3}]
var newOA = [...objArr.slice(0, idx), {...objArr[idx], b: -2}, ...objArr.slice(idx+1)]
console.log(objArr) // [{a: 1}, {b: 2}, {c: 3}]
console.log(newOA) // [{a: 1}, {b: -2}, {c: 3}]
```

**Clojurescript erases almost all the woes and pitfalls of javascript**:
inconsistent type comparisons, truthy/falsy inconsistency, null/undefined/NaN
problems, unintended type conversions, shallow-vs-deep copying, passing values
versus passing refs, inconsistent equality checking, and huge operator
precedence tables, to name a few. But just in cast you're a glutton for
punishment, host [interop](/2018/10/20/clojurescript-interop-with-javascript.html) is seamless via `(js/myFunction args)` or
`(.myFunction js/myModule args)`. Calling Clojurescript from javascript is a
matter of `^:export my-function`, which exposes to javascript
`my_namespace.my_function()`.

```clojure
;; types are what they say they are, instances don't lie
(let [arr []]
  (type arr) ;; clojure.lang.PersistentVector
  (instance? clojure.lang.PersistentVector arr)) ;; true

;; look at that beautiful consistency
(= 1 "1") ;; false
(= 1 [1]) ;; false
(= 0 false) ;; false
(= "" false) ;; false
(= nil false) ;; false
(= "0" true) ;; false
(= "false" true) ;; false
(= [] true) ;; false
(= {} true) ;; false
(= (fn [] nil) true) ;; false
(= [] ",,") ;; false

;; you don't have to remember tables
(+ (- (/ (* 1 1) 2) 4) 2)

;; or more commonly
(-> (* 1 1)
    (/ 2)
    (- 4)
    (+ 2)) ;; -1.5

```
```javascript
// types and instances lie all the time
var arr = new Array
arr.constructor === Array // true, but constructor can be modified
arr instanceof Array // oops, barfs
Object.prototype.toString.call(arr) === '[object Array]' // true because, I mean, why not?
Array.isArray(arr) // true, finally!
Array.isArray(Array.prototype) // true, huh?
typeof arr == 'object' // true, but it's a string?
arr = undefined
typeof arr // 'undefined', another string?

// look at how inconsistent it is
1 == '1' // true
1 == [1] // true
1 === '1' // false
1 === [1] // false
0 == false // true
'' == "" == false // true
null == false // true
undefined == false // true
NaN == false // true
'0' == true // true
'false' == true // true
[] == true  // true
{} == true // true
function() {} == true // true
Array(3) == ",," // true

// do you remember your order precedence table?
1 * 1 / 2 - 4 + 2 // ?
```

Clojurescript **tooling is amazing**. [REPL driven
development](https://vimeo.com/223309989) deserves an entirely separate post.
Just know that it affords immediate code execution under the cursor (no test
harness required), immediate source and documentation lookup, runtime-remote
injection and debugging, and an [immediate feedback
loop](https://dzone.com/articles/the-developer-feedback-loop) at all stages of
development. [Leinengen](https://leiningen.org/) offers scaffolding, building,
packaging, and deploying under one roof, which means consistency and
little-to-no tooling churn.
[Figwheel](http://rigsomelight.com/2014/05/01/interactive-programming-flappy-bird-clojurescript.html)
has paved the path to the future of javascript development.
[Mount](https://github.com/tolitius/mount) or
[component](https://github.com/stuartsierra/component) or
[integrant](https://github.com/weavejester/integrant) offer runtime system
setup/teardown/halt/resume capabilities. nREPL allows direct-injection into the
browser's runtime.  Debugging tools like
[re-frame-10x](https://github.com/Day8/re-frame-10x) make you feel like a
super-hero with x-ray vision, perfect knowledge, and time-manipulation. Also,
when necessary, you can reach into any profiling, debugging, or monitoring tools
in the host ecosystem– some of which have been battle-hardened for decades.

Clojurescript has an **opt-in, expressive, powerful, and integrated system for
specification, instrumentation, and testing** via
[clojure.spec](https://clojure.org/about/spec). Docs alone are not enough for a
whole variety of reasons that we won't get into here.  Instead of trying to
solve the human-related documentation problem, Clojurescript offers a simple and
extensible way to describe your application's data. With spec you gain data
parsing and conformance, function instrumentation, and [generative
testing](https://github.com/clojure/test.check), all with as-needed flexibility.

Clojurescript utilizes the [Google Closure
Compiler](https://developers.google.com/closure/compiler/). The poorly-named GCC
performs extremely aggressive javascript optimizations via renaming, munging,
dead code elimination, and inlining. It's a full-blown optimizing compiler that
understands the javascript runtime, whereas other optimizing tools only parse
and shuffle the AST. At the risk of throwing out extremely general and debatable
numbers, a good explanation of the power of GCC follows: historically, if
delivering javascript and jquery to a client was 1x, Clojurescript floated at
around 2x. But for the last few years rich web applications have required much
more beyond javascript and jquery. Thanks to the power of Clojurescript, which
has not required a proliferation of libraries, and the GCC, it has remained at
2x. Meanwhile, a comparable javascript solution consisting of [flavor of the
month framework] + react rendering engine + [extra libs] has reached 4x.

```javascript
// given this code to optimize
function unusedFunction(note) {
    alert(note['text']);
}
function displayNoteTitle(note) {
    alert(note['title']);
}
var flowerNote = {};
flowerNote['title'] = "Flowers";
displayNoteTitle(flowerNote);

// rollup turns it into:
function displayNoteTitle(note) {
    alert(note['title']);
}
var flowerNote = {};
flowerNote['title'] = "Flowers";
displayNoteTitle(flowerNote);

// and Google Closure Compiler turns it into:
alert("Flowers");
```

Clojurescript also integrates seamlessly with the [Google Closure
Library](https://developers.google.com/closure/library/). The GCL is not very
popular for some reason, but it's been around for years and has been used
internally at Google in most of their product lines going back a decade: search,
gmail, maps, docs, calendar, plus, photos. In short, **GCL is the missing std-lib
for javascript**; it fills the void by providing standard implementations for
dates, i18n, math, styles, strings, dom manipulation, and much more. Portions of
Clojurescript are built on top of the GCL, and any functionality not provided by
the language directly is only one `(:import goog.SomeClosureLibrary)` away.

Lastly, the Clojurescript developer experience in terms of ergonomics, speed to
delivery, extensibility, and bug-to-feature ratio is phenomenal. This is largely
in part due to **powerful libraries** that a functional, immutable language has
enabled. [Reagent](https://github.com/reagent-project/reagent/) is what React
hopes to be one day. [Re-frame](https://github.com/Day8/re-frame) is a simple,
powerful, extensible, uni-directional, single-source-of-truth, event-based
system that both predates and outshines the capabilities of Flux/Redux-style
architectures. Libraries in general tend to be single-purpose, dependency-free
(or dependency-light), and composable. For those rare instances when the
Clojurescript ecosystem does not already have a solution, it's trivial to reach
into the node ecosystem.

## Learn from my mistakes
I started my journey as a developer by severely overlooking the edges.
Non-standard, under-adopted, or even just strange-looking approaches went
ignored. I was content staying in the lines and following the trends– however
wacky and fickle they might have been. I balked at some "crazy" concepts that,
in reality, were diamonds masked by a learning curve and some rough edges. I
made [Alan Perlis](https://twitter.com/codewisdom/status/708670665059147777)
sad.

While I have since transformed into a furiously curious engineer that welcomes
the unconventional, it bothers me to know that I probably missed out on many
opportunities to learn great ideas. I know that, usually, greatness hides on the
edges somewhere.

Clojure certainly started on the edges, and while it has gained [an enormous
amount of traction](https://insights.stackoverflow.com/survey/2017), it is also
more than happy to [persist along the
edges](http://blog.cognitect.com/blog/2016/4/22/the-new-normal-everything-relies-on-sharp-tools)
if necessary. Hopefully I have convinced you to at least take a peek behind the
unfamiliar, because maybe there's some good ideas in hiding.

## Where to go from here

Here is a list of material I wish someone handed me when I first started
to learn Clojurescript. I would say good luck, but you won't need it.

- Why does the language exist?
[http://www.infoq.com/presentations/Simple-Made-Easy](http://www.infoq.com/presentations/Simple-Made-Easy)

- The best preview/tour of the language in video form
[https://www.youtube.com/watch?v=wASCH_gPnDw](https://www.youtube.com/watch?v=wASCH_gPnDw)

- Basic syntax, plus the benefits offered by a REPL
[https://clojure.org/guides/learn/syntax](https://clojure.org/guides/learn/syntax)

- Get your feet wet with data structures and built-in functions
[http://clojurescriptkoans.com/](http://clojurescriptkoans.com/)

- The nitty gritty with respect to namespaces, packages, structuring
an application, actually loading cljs into the browser, then dealing with the
DOM and js interoperability, rendering html, ajax, testing, and was probably the
most helpful resource
[https://github.com/magomimmo/modern-cljs](https://github.com/magomimmo/modern-cljs)

- Language reference that is *very* high level, but helps with looking
up things you dont understand from the above
[http://clojure.org/reference/reader](http://clojure.org/reference/reader)

- Explanation of the 4 different state mechanisms. Clojurescript runs
on JS, which doesn’t have threading, so only the last construct applies (atoms).
[http://clojure.org/reference/vars](http://clojure.org/reference/vars)

- The “->” thread macro is used almost everywhere, and this is a great explanation
[https://www.youtube.com/watch?v=qxE5wDbt964&feature=youtu.be](https://www.youtube.com/watch?v=qxE5wDbt964&feature=youtu.be)

- What the heck is this `“^”` thing people use occasionally?
[http://clojure.org/reference/metadata](http://clojure.org/reference/metadata)

- Now that you understand the basic syntax and constructs, take a look at a really
lightweight framework: [http://reagent-project.github.io/](http://reagent-project.github.io/)

- Re-Frame is a convention around reagent with a default implementation
[https://github.com/Day8/re-frame](https://github.com/Day8/re-frame)

- Certain syntax tokens are really hard to google, so someone compiled a list to
help with that
[https://clojure.org/guides/weird_characters](https://clojure.org/guides/weird_characters)
