---
layout: post
title: React, Please Let Me Drive
---

# React, please let me drive

I’ve been working with React for well over a year now and have used it to build
two large, non-trivial web applications. The framework brings some powerful
programming concepts to mainstream web development, and I foresee myself using
React [(or something like it)](http://riotjs.com/) for many future projects.

That being said, I don’t want to talk about where React is great. I want to talk
about where React is weak, then present some ideas for making it stronger.

> Much of this discussion is “heavily inspired by” (stolen from) work done under
> tutelage of [Mike Nichols](https://github.com/mnichols)

## The problem with components
React claims to be “only the V in MVC”, which is really only a half-truth. In a
normal React application, our components (`React.createClass()`) end up serving
as both the V and an implicit View Model (VM). I say implicit because for any
non-trivial component there is a lot of VM logic spread across the lifecycle
hooks…

```javascript
shouldComponentUpdate: function () {
  if (this.state.someState) {
    // do stuff
  } else {
    // do different stuff
  }
}
componentDidMount: function() {
  if (this.props.someProp && this.state.someOtherState) {
    if (!this.props.anotherProp) {
      // more stuff
    } else {
      // different stuff
    }
  }
}
render: function() {
  if (this.state.someState) {
    // do some stuff
  }

  if (this.props.someProperty) {
    if (this.someOtherState && this.someState) {
      // do more stuff
    } else {
      // do different stuff
    }
  } else if (this.props.someSecondProperty) {
    if (!this.someOtherState) {
      // do other different stuff
    }
  }

  // ... etc.
}
```

Look at all that messy state management handled inside our view… This isn’t
exclusively a React problem, but more of a UI problem in general. Managing view
states and all permutations/transitions across those states is difficult. A lot
of frameworks have cropped up to address this problem, such as
[Flux](http://facebook.github.io/flux/), [Fluxxor](http://fluxxor.com/),
[Reflux](https://github.com/reflux/refluxjs) … [insert other *ux library here].
These libraries all share the concept of one-way data flow that pairs so well
with React. Unfortunately, most of them fall short where the rubber meets the
road — actually managing the state.

```javascript
var action = payload.action;
var text;

switch(action.actionType) {
  case TodoConstants.TODO_CREATE:
    text = action.text.trim();
    if (text !== '') {
      create(text);
      TodoStore.emitChange();
    }
    break;

  case TodoConstants.TODO_DESTROY:
    destroy(action.id);
    TodoStore.emitChange();
    break;

  // ... and so on
```

This isn’t exactly efficient state management.

A giant switch statement isn’t much improvement over the manual state management
we had back in the view’s lifecycle hooks. There is still no concrete concept of
state. Instead, our state is embedded in a combination of switch statements,
action types, javascript objects, raw strings, and some comparison operators.

Wouldn’t it be nice if we had an explicit declaration of a component’s states
and could manage the transitions between those states?

As it turns out, this problem was solved a long time ago and the solution is a
founding pillar of computer science: [Finite State
Machines](https://en.wikipedia.org/wiki/Finite-state_machine). I’m not going to
provide a formal proof (reader submissions welcome), but UI components are just
finite state machines (FSM). Rather than reinvent the wheel for each component,
we might as well use a dedicated FSM library. My current go-to’s are
[possum](https://github.com/mnichols/possum) and
[machina](https://github.com/ifandelse/machina.js).

We can construct a dedicated VM from a FSM and greatly simplify the view logic
processing while simultaneously removing the cruft from inside our V components.
For example, compare the previous component to the following possum VM.

```javascript
possum()
  .config({ initialState: 'uninit' })
  .methods({
    validateAsync: function() {
      return somePromisifiedService()
        .then(this.handle.bind(this, 'valid')
        .catch(this.handle.bind(this, 'invalid')
    }
  })
  .states({
    'uninit': {
      init: function() {
        this.transition('waiting')
        return this.validateAsync()
      }
    }
    , 'waiting': {
      valid: function(isValid) {
        this.transition('valid')
      }
      , invalid: function() {
        this.transition('invalid')
      }
    }
    , 'invalid': {
      validate: function() {
        this.transition('waiting')
        return this.validateAsync()
      }
    }
    , 'valid': {}
  })
```

I didn’t write this and I can figure out how it functions with hardly any
effort.  Notice how simple it is to follow the logic? A FSM formalizes and
centralizes state management, so we no longer have to chase around `if, else, if
else, switch, ===, !=, action.type` to decipher what is going to happen next in
our component.

> As an added benefit, we no longer really have to test our views, but only our
> view models. Gone are dependencies on tools like Jest that help us run tests
> We no longer have to test that React rendered correctly, but that we handed
> React the correct properties to render.

How can we apply this to React?

To use a dedicated view model, we have to let it be in control- it manages
state, it manages properties, and it dictates what/when to render. If we don’t
give up 100% of control to the view model implementation, we end up with React
trying to render at inappropriate times, as well as multiple sources of truth
spread across the VM and the component.

## Driving the application from a view model

The first challenge is figuring out how to drive the application from a view
model. In a normal React application we have a tree of components. The initial
render starts with the root of the application mounting to some DOM node
(`React.render(<App someProps={ … } />, targetNode)`).

All future renders are a consequence of someone in the component hierarchy
calling `setState()` in response to an event (user input, an external http
request completes, etc.), which triggers an update cycle.

Rather than render the entire application hierarchy, we can use an orthogonal
rendering service to perform a more targeted render of a particular component
onto a particular node. We can then dictate from within a view model when a view
should render:

```javascript
function staticRender(componentFactory, model, node) {
  var reactElement = componentFactory(model)
  React.render(reactElement, node)
}

viewComponent = React.createClass({
  render: function() {
   return (
      <div className={this.props.state}>
       {this.props.someProp}
      </div>
    )
  }
})

...
.methods({
  render: function() {
    staticRender(viewComponent, {
      state: this.currentState
      , someProp: this.someProp
    }, someNode)
  }
})
.states({
  renderable: {
    someEventHandler: function(args) {
      this.someProp = args.newProp
      this.render()
    }
  }
})
```

Give up rendering control to some orthogonal rendering service.
Congratulations, `setState()` is no longer the only way to trigger a render!

## Caveats
There are some problems with this implementation. How do we update anything when
the user provides input? Well, avoiding the complexities of some top-down
architecture like Flux (for now), we can simply pass down callbacks from the
view model to the component — exactly like the examples in the React docs.

```javascript
...
  render: function() {
    staticRender(viewComponent, {
      state: this.currentState
      , someCallback: this.handle.bind(this, 'someInputHandler')
    }, someNode)
  }

viewComponent = React.createClass({
  render: function() {
   return (
     <div className={this.props.state}>
       <button onClick={this.props.someCallback}></button>
     </div>
    )
  }
})
...
```

This works for everything except `<input type=”text” />`, which raises an
interesting problem when stepping outside of React’s view-driven world. Each
call to `staticRender()` blows away the existing component completely, leading
to some really awful behavior for input fields.

Every keystroke invokes an update call to the view model, where the view model
updates its internal state with the new value, then re-renders the component.
During this destroy/create/render, React can’t (and shouldn’t) preserve DOM
state, meaning we lose the input field’s focus and caret position. It’s
hilariously frustrating to use such an input field.

```javascript
// view model
...
.methods({
    render: function () {
        staticRender(viewComponent, {
          state: this.currentState
          , value: this.value
          , setValue: this.handle.bind(this, 'setValue')
        }, someNode)
    }
})
.states({
  'inputting': {
    setValue: function(val) {
      this.value = val
      this.render()
    }
  }
})
...

// component
render: function() {
  return <input val={this.props.value} onChange={this.props.setValue} />
}
```

We don’t have a text input problem when using a controlled component, so what’s going on here? In a standard component, a controlled input triggers a callback `onChange` that invokes `setState()`, triggering a render with the new input value in a fashion that preserves field focus and caret position.
I haven not dug into the internals of React to figure out why, but setState follows a different update path than React.render. When rendering, the existing node is destroyed and recreated, but `setState` does its best to preserve the existing node, which consequently preserves properties like caret position and focus.

## Mounting vs Rendering
We can use React’s distinction between `Render` and `setState` to grant us even
finer grained control over a view from within our view model.

Let’s formalize these two distinct operations: mounting is when we mount a
component onto a node, and rendering is when we update a mounted component. In
practice, mounting will delegate to `React.render()` and rendering will delegate
to a mounted instance’s `setState()`.

Assume we have an orthogonal `staticMount` service that eventually returns the
mounted component. When a triggered event requires the view model to update the
view, we can update the mounted component in a manner that will preserve node
state (focus, caret position) by using the component’s `setState()`.

```javascript
.methods({
  mount: function() {
    staticMount(viewComponent, {
      state: this.currentState
      , someProp: this.someProp
    }
    , someNode
    , doneMounting)
  }
  , doneMounting: function(mountedDomponent) {
    this.component = mountedComponent
  }
  , render: function() {
    this.component.setState({
      state: this.currentState
      , someProp: this.someProp
    })
  }
})
.states({
  uninit: {
    init: function() {
      this.mount()
    }
  }
  renderable: {
    someEventHandler: function(args) {
      this.someProp = args.newProp
      this.render()
    }
  }
})
```

### Props vs State
Astute readers will notice a problem introduced by distinguishing between
mounting and rendering. We have sneakily introduced an implementation burden on
components relating to props and state.

A normal React component manages the differences between props and state via
`this.setState()`. In a VM-driven component that can be mounted and rendered, it
must remember that a mount provides all necessary properties via props (a
consequence of `React.render()`), and renders provide all necessary properties
via state (a consequence of `this.setState()` ). Gross. This is a terrible
burden to place on each implementation.

Instead, why don’t we just ignore props entirely and focus exclusively on state inside each component? After all, props is just a construct introduced by React to satisfy the demands of its implementation. Let’s offer a simpler construct: a view only has access to an immutable this.state, and that state is managed by a view model. We no longer have to worry about props vs state because our view models contain application state, and they push need-to-know state into a view.

> While this is considered an
> [anti-pattern](https://facebook.github.io/react/tips/props-in-getInitialState-as-anti-pattern.html)
> in React, I hope you can see why it doesn’t really apply in this case.

```javascript
// mixin to remove the notion of props
function replacePropsWithState(reactClass) {
  reactClass.getInitialState = function () {
    return { ... this.props }
  }
}

var component = React.createClass({
  render: function() {
    // props? who needs 'em
    return <div>{this.state.propA}, {this.state.propB}</div>
  }
})
replacePropsWithState(component)
```

## Nondeterminism
We are close to an application controlled by FSMs that uses React as a fast rendering implementation with helpful lifecycle hooks and a convenient DOM construction syntax. One problem remains, and I’m not sure what I can do, if anything, to fix it.

From the React docs:

> "There is no guarantee of synchronous operation of calls to setState and calls
> may be batched for performance gains."

No big deal, right? We wanted a promise API anyways, so we can just use the “I’m
done setting state” callback provided by React.

```javascript
render: function (args) {
  var component = args.component
  return new Promise(function (resolve, reject) {
      component.setState(args, function () {
        resolve(this)
      })
  })
}
```

Not so fast. Remember the earlier problem with caret position? Well, here, [have
another](http://stackoverflow.com/questions/28922275/in-reactjs-why-does-setstate-behave-differently-when-called-synchronously).
Typing into an input field anywhere that is not the end of the field will input
your keystroke, then jump the caret to the end of the input line. It is, yet
again, hilariously frustrating.

## Inconclusion?
We have jumped through some hoops to:

1. wrestle away rendering control from the React root hierarchy construct
2. hand the reigns over to proper FSM view models that simplify state management
3. differentiate between a `mount` action and an `render` action
4. create an even simpler component api that doesn’t differentiate `props` vs
   `state` expose a promisified component api

…but I just don’t know where to go from here.

In my current implementations I have actually reverted back to using
`this.setState()` from directly within a component via `onChange` handlers, and
when I’m “done editing” (usually via `onBlur()`) an input, I will notify the
view model. This is also gross. I’m splitting state management and introducing
another source of truth, and running counter to the whole purpose of this
implementation.

For the time being this seems to work, though I’m not really sure why. I suspect
it has something to do with a promisified render always triggering an
asynchronous `setState()` update, whereas a component invoking `this.setState()`
directly always guarantees synchronous updates.

