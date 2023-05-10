---
layout: post
---

# Clojurescript interop with javascript

Clojurescript is great, but sometimes it's necessary to use a javascript library
on `npm`, or to drop down to the native browser api.

Below is a comprehensive interoperability ("interop") guide that covers most
uses with concise examples. Make sure, at the very least, to read the section
about [advanced compilation](#advanced). Where it makes sense, clojurescript
examples are followed immediately by the corresponding javascript equivalent.

Before working via interop, check if the enormous
[goog-closure](https://google.github.io/closure-library/api/) library already
has an implementation (hint: it probably does).

- [js/](#js)
- [Property access](#property-access)
- [Function invocation](#function-invocation)
- [Setters](#setters)
- [Pitfalls](#pitfalls)
- [Syntax sugar](#syntax-sugar)
- [Instantiation](#instantiation)
- [Translation](#translation)
- [Advanced](#advanced)

## js/
Any top-level namespace is accessible through the `js/{namespace}` interface.
This includes things like `js/Math`, `js/Array`, or (in a browser) namespaces
such as `js/window` and `js/document`.

## Property access
The interface from clojurescript to javascript is called the "dot special form".
It works as both a property accessor and a means to invoke functions.

Get the `title` property on the `document`. Note the `-{property}` syntax, where
a property must be preceded by a hyphen.
```clojure
(. js/document -title)
```
```javascript
document.title
```

Get the nested `location.href` property on the `document`.
```clojure
;; can’t do this
(. js/document -location -href) ;; => error: “dot prop access with args”

;; have to do this instead
(. (. js/document -location) -href)
```
```javascript
document.location.href
```

Nested dots quickly become difficult to read and write.
```clojure
(. (. (. js/document -location) -href) -length)
```
```javascript
document.location.href.length
```

To help alleviate nesting there’s another interface called the "double-dot
special form". It is merely syntactic sugar over the dot special form, but it
can improve readability.
```clojure
(.. js/document -location -href -length)
(macroexpand ‘(.. js/document -location -href -length)) ;; => (. (. (. js/document -location) -href) -length)
```

## Function invocation
The dot form also enables function invocation.
```clojure
(. js/document hasFocus)
```
```javascript
document.hasFocus()
```

The `-` makes all the difference.
```clojure
(. js/document -hasFocus) ;; => ƒ hasFocus() { [native code] }
```
```javascript
document.hasFocus // => ƒ hasFocus() { [native code] }
```

Dont try to invoke something that’s not a function.
```clojure
(. js/document title) ;; => document.title is not a function
```
```javascript
document.title() // document.title is not a function
```

Interestingly, these forms are equivalent.
```clojure
(. js/document hasFocus)
(. js/document (hasFocus))
```

Why is there no distinction between these forms? Turns out, this is merely a
[specification of the
language](https://www.clojure.org/reference/java_interop#dot). The special forms
`(->) (->>) (..)`, etc. all behave in the same way.

> "Note that placing the method name in a list with any args is optional in the
> canonic form, but can be useful to gather args in macros built upon the form."

```clojure
(. js/document getElementsByTagName "html")
(. js/document (getElementsByTagName "html"))
```
```javascript
document.getElementsByTagName(“html”)
```

What if we want to get fancy with nested functions?

```javascript
document.foo = (x, y) => { return { bar: function(a) { return [a, x, y] }}}
document.foo(1, 2).bar(3) // => [3 1 2]
```
```clojure
(. (. js/document foo 1 2) bar 3) ;; => [3 1 2]
```
Again, parens are exactly equivalent to no parens.
```clojure
(= (. (. js/document (foo 1 2)) (bar 3))
   (. (. js/document foo 1 2) bar 3)) ;; => true
```

## Setters
The `set!` function provides a means to set native javascript object properties.

```clojure
(set! (.. js/window -location -search) "foo=bar")
```
```javascript
window.location.search = "foo=bar"
```

> A few places around the internet recommend the use of `aset` and the
> corresponding `aget`. These are not intended for property access or property
> assignment. The functions are explicitly for use with native arrays. The fact
> that they work for object properties is an implementation consequence, and is
> not supported behavior. Don't use them.

## Pitfalls
We can mix and match property access with native language function invocation
```javascript
document.foo = function(a) { return a;}
```
```clojure
((. js/document -foo) 1) ;; => 1
```

Be careful when mixing native invocation with interop, particularly in the
browser. The DOM api uses javascript’s invocation context (bind, apply)
everywhere.
```clojure
((. js/document -getElementsByTagName) “html”) ;; => Illegal invocation
```

This happens because we’re accessing a property on the `document` and _then_
invoking it instead of using direct invocation. The javascript equivalent looks
something like this:

```javascript
let f = document.getElementsByTagName
f(“html”) // => Illegal invocation
```

Instead we have to capture the context in one of a few ways.

```javascript
// call
f.call(document, “html”) // => HTMLCollection [...]

// bind
let f = document.getElementsByTagName.bind(document)
f(“html”) // => HTMLCollection [...]
```

This pattern looks very odd in clojurescript and should probably be avoided.
```clojure
;; call
(. (. js/document -getElementsByTagName) call js/document "html")

;; bind
((. (. js/document -getElementsByTagName) bind js/document) "html")
```

## Syntax sugar
There’s syntactic sugar for both property access and function invocation.
```clojure
(.-title js/document)
(macroexpand '(.-title js/document)) ;; => (. js/document -title)

(.hasFocus js/document)
(macroexpand '(.hasFocus js/document)) ;; => (. js/document hasFocus)
```

It's possible to mix and match the 5 interop syntaxes (dot access, shorthand
access, dot invocation, shorthand invocation, double dot access), but it leads
to extremely poor readability.

```clojure
(.-length (.. (. (.call (. js/document -getElementsByTagName) js/document "html") item 0) -children) )
```
{:.pattern-match}

I would recommend sticking to either only sugar-free or only-sugared access
patterns, and mixing them as little as possible.

```clojure
(.. (.item (.call (.-getElementsByTagName js/document) js/document “html”) 0) -children -length)
```

It's not always more readable, but maintaining a consistent pattern will prove
helpful over time.

## Instantiation
We can create native javascript structures in a few different ways.

The compiler `#js` literal is particularly helpful when the native structure is
small.

```clojure
(def my-obj #js {"a" 1 "b" 2}) ;; => #js {a: 1, b: 2}
(def my-arr #js ["a" "b" 2]) ;; => #js ["a", "b", 2]
```
```javascript
let my_obj = {"a": 1, "b": 2}
let my_arr = ["a", "b", 2]
```

Note the compiler literal doesn't handle nesting. A `#js` tag is required at
each "depth" of the data structure.

```clojure
(def my-obj #js {"a" 1 "b" {"c" 2 "d" 3}}) ;; => #js {a: 1, b: cljs.core/PersistentArrayMap} (dangerous)
(def my-obj #js {"a" 1 "b" #js {"c" 2 "d" 3}}) ;; => #js {a: 1, b: #js {c: 2, d: 3}} (safe)
```

It's important to remember the differences in language primitives, notably that

- javascript doesn't understand clojurescript keywords or symbols
- javascript object keys can only be strings

In some cases the translation happens seamlessly. In other cases mixing
primitives leads down a dangerous path.

```clojure
#js {:a 1 :b 2} ;; => #js {a: 1, b: 2} (safe)
#js [:a 'b "c" 3] ;; => #js [cljs.core.Keyword, cljs.core.Symbol, "c", 3] (dangerous)
```

The functions `js-obj` and `array` offer dynamic instantiation of javascript
structures. They are quite literal about translating keys, so be careful with
strings vs keywords vs symbols. They also do not handle nesting.

```clojure
(js-obj "foo" 1 "bar" 2) ;; => #js {foo: 1, bar: 2}
(js-boj :a 1 :b 2) ;; => #js {":a": 1, ":b": 2} (fairly dangerous)

(array 1 2 3) ;; => #js [1, 2, 3]
(array "a" :b 'c') ;; => #js ["a", cljs.core.Keyword, cljs.core.Symbol] (dangerous)
```

## Translation
Moving from cljs data structure to javascript data structures is easier with the
`(clj->js)` and `(js->clj)`. The functions consistently handle primitive
encoding and nesting. There are a few special rules to remember:

> "clj->js recursively transforms ClojureScript values to JavaScript.
> sets/vectors/lists become Arrays, keywords and symbols become strings, maps
> become Objects."

```clojure
(clj->js {:a 1 'b 2 "c" {:d 3}}) ;; => #js {a: 1, b: 2, c: #js {d: 3}}
(js->clj #js {a: 1, b: 2, c: #js {d: 3}}) ;; => {"a" 1 "b" 2 "c" {"d" 3}}
```

By default `name` is the function used to transform a cljs keyword to a js
keyword. For keys whose type is not a keyword, remember the rules listed above
(which are formally encoded in the
[`cljs.core/key->js`](https://github.com/clojure/clojurescript/blob/master/src/main/cljs/cljs/core.cljs#L10545-L10554)
function). Overriding the cljs-keyword-to-js-keyword function is as simple as
passing a `:keyword-fn`.  Again, this function will only be used for cljs
keywords and not other primitives.

```clojure
(clj->js {:a 1 'b 2 "c" {:d 3}} :keyword-fn (fn [x] (str "+" (name x)))) ;; => #js {"+a": 1, b: 2, c: #js {"+d": 3}}
```

In the inverse direction, `str` is the function used to transform a js keyword
to a cljs keyword. Since js keywords can only be keys, there's an option to
`:keywordize-keys` during encoding.

```clojure
(js->clj #js {a: 1, b: 2, c: #js {d: 3}} :keywordize-keys true) ;; => {:a 1 :b 2 :c {:d 3}}
```

## Advanced
Of course the story wouldn't be complete without explaining [advanced
compilation](https://clojurescript.org/reference/compiler-options#optimizations)
and how it affects everything mentioned so far.

Advanced compilation munges variable, function, and property names in order to
optimize the final artifact size. The compiler, in an attempt to optimized the
output javascript, effectively breaks the working contract between cljs and js
environments.

```javascript
window.my_js_fn = function() { return true; }
```
```clojure
(defn -main []
  (. js/window my-js-fn)))

(-main) ;; => Uncaught TypeError: window.ac is not a function
```

The compiler changed the call to `my-js-fn` into `ac()` to save bytes, but
`window.ac` is not a function. The same problem occurs with property
accessors.

```clojure
(. some-js-object -aproperty)
```
```javascript
// the output will not look like this:
some_js_object.aproperty
// but will instead look something like this:
some_js_object.fw
```

There are two solutions to this problem, either provide [externs
files](https://developers.google.com/closure/compiler/docs/api-tutorial3), or
use a library. Externs files require a bit of explanation, and become a
cumbersome part of the build process, so I recommend ignoring that option.

This means that *property interop is only safe when using the goog-closure
library's `goog.object` or a comparable library* such as
[cljs-oops](https://github.com/binaryage/cljs-oops).

Invoking a js function becomes a call to `get`.

```clojure
(ns main
  (:require [goog.object :as g]))

(defn -main []
  ((g/get js/window "my-js-fn"))))

(-main) ;; => true
```

Setting a js property becomes a call to `set`.

```clojure
(g/set js/window "my-js-property" false)
(g/get js/window "my-js-property") ;; => false
```

> It's important to know that the clojurescript compiler is aware of native
> language (and browser) apis, which means _most_ calls to `js/{ANativeApi}`
> will work properly without any externs files or library usage. The
> goog-closure library is also safe to use without externs files or libs.
> Again, this means the property names will not be shortened because the
> compiler internally knows not to do so.

I personally use `cljs.oops` because the api is friendlier than `goog.object`,
and offers some advanced features such as [soft and
hard](https://github.com/binaryage/cljs-oops#access-modifiers) property access.

Under the hood these libraries operate by working with strings instead of
symbols. The cljs compiler will never re-write strings, it only operates on
symbols which are "safe" (usually) to munge. Due to javascript's dot-property or
bracket-string notation, the interop works consistently in development and
advanced builds.

```javascript
// instead of emitting symbols (which will be rewritten)
window.myProperty = true // => window.ab = true

// cljs-oops and goog.object use strings (which are not rewritten)
window["myProperty"] = true // => window["myProperty"] = true
```
