---
layout: post
title: "Is Sentry slow in Node?"
description: Take caution when using Sentry's SDK in Node.
---

# Is Sentry slow in Node?

After deploying [Sentry's Node SDK](https://sentry.io) I observed a range of
5-16% increase in CPU usage across the service. This is an insanely high tax for
an observability tool. While I think Sentry's error capture and performance
profiling are excellent additions to any observability stack, it is only useable
if the additional load can remain in the low single digits. Let's see if we can
decipher what is happening to the poor CPU, and if the situation can be amended.

## Introducing observability

To start, here's a little background on the relevant parts of the system: We
deploy many services in GCP using a mix of Google App Engine and Cloud Tasks.
Demand for the system arrives at fairly consistent time windows– we expect three
peaks per day in the morning, early afternoon, and late evening. The volume of
load during the peaks is fairly unpredictable, however. We can see anywhere from
a 2x to a 20x increase in (hand-wavey) "load" for a period of two to three
hours. 

The observability of our system is decent, but it could be much better. There is
a a hodgepodge of GCP dashboards to track metrics such as database read/write
throughput and memory/CPU usage. We also have email alerts for critical code
errors and metric thresholds. 

Overall, I think the alerts coming out of GCP are severely lacking in
debugability- they often contain only a few lines of an unhelpful stack trace
with no broader context capture. Here's an example of a regular alert:

```
} FetchError: request to https://storage.googleapis.com/upload/storage/v1/b/simbe-jobs/o?name=e7383d6e-49c2-45ae-9584-67f07fac04ae&uploadType=resumable failed, reason: connect ETIMEDOUT 142.250.128.207:443
    at ClientRequest.<anonymous> (/app/node_modules/node-fetch/lib/index.js:1505:11)
    at /app/node_modules/@opentelemetry/context-async-hooks/build/src/AbstractAsyncHooksContextManager.js:50:55
    at AsyncLocalStorage.run (node:async_hooks:330:14)
    at AsyncLocalStorageContextManager.with (/app/node_modules/@opentelemetry/context-async-hooks/build/src/AsyncLocalStorageContextManager.js:33:40)
    at ClientRequest.contextWrapper (/app/node_modules/@opentelemetry/context-async-hooks/build/src/AbstractAsyncHooksContextManager.js:50:32)
    at ClientRequest.emit (node:events:513:28)
    at TLSSocket.socketErrorListener (node:_http_client:494:9)
    at TLSSocket.emit (node:events:525:35)
    at emitErrorNT (node:internal/streams/destroy:157:8)
    at emitErrorCloseNT (node:internal/streams/destroy:122:3)
    at processTicksAndRejections (node:internal/process/task_queues:83:21) { 
```

We are missing an enormous amount of useful context on this error:
- was this error handled, or not?
- what code even making a connection that required resetting? 
- what and where is the up/down-stack calling code for this trace?
- was there a user context available?
- if this is an HTTP request error, where is the http stack: request/response
headers, status code, API keys, payload, and so on?
- what environment are we even in? staging, demo, or prod?
- what is the impact/scope of this issue? 
- how often have we seen this exact issue before?

GCP observability, by itself, leaves much to be desired. I'm not sure it's even
possible to debug the issue from this alert alone. I would spend a large amount
of time simply rebuilding context around the issue. This is a job for a tool,
not a developer.

## Sentry to the rescue?

[Sentry](sentry.io) is a tried and true observability tool that I've leveraged
for nearly 10 years. Out of the box it provides excellent error capture metrics,
wrapped neatly in a helpful UI. In recent years the sentry team has expanded
their offering to include tracing, profiling/performance, session capture, and
[RED](https://grafana.com/blog/2018/08/02/the-red-method-how-to-instrument-your-services/)/[Golden
metrics](https://sre.google/sre-book/monitoring-distributed-systems/) tracking.

A typical Node sdk configuration is straightforward, or so I thought.

1. install the sdk dependency with npm
1. invoke the initialization of the SDK early in the bootstrapping of the
   service
1. provide sane sdk defaults, along with some per-project DSN configurations
1. configure release/deployment tracking for the project via API or cli during
   the build process of the service

> As with all third party dependencies, I recommend wrapping sentry's SDK inside your own
application-specific module in order to gain control over the interface. The
Node ecosystem is particularly unapologetic when it comes to introducing
breaking changes.

## Beating the CPU to death

A few hours after deploying a "default" sentry configuration I noticed a
dramatic increase in CPU usage across all instances of our service. Consider the
pink line in this chart compared to the green line, which is an overlapping time
window from the previous day:

![cpu-usage-1-day-back](/assets/cpu-1-day-back.png)

The CPU climbs to >95% a few times. Oops. Let's see if we can rescue this poor
CPU from my poor assumptions. The first knob to turn with sentry is often
`tracesSampleRate`. My hypothesis is that we're sampling traces and profiles
with too much frequency, and the CPU simply cannot keep up with all that extra
work. I will happily trade volume of traces/profiles for a reduced load.
Debugging often only requires a very small handful of traces anyway.

I'll deploy a much lower sample rate, and monitor the CPU for a few days. The
thinking is that (hopefully) the CPU's behavior will "level out" over a bit of
time. 

This was wishful thinking, of course, and after a few days I became increasingly
nervous that usage was still too high at ~90% during peak hours. Here is the
chart for CPU usage over a ~4d period. That blue line is a really big problem. 

![cpu-usage-1-week-back](/assets/cpu-1-week-back.png)

At this point we're in a bit of a pickle. The `tracesSampleRate` has already
been turned down very, very low. During non-peak hours we receive barely a
trickle of telemetry from sentry. During peak hours, however, we're still
utilizing far too much CPU to complete the tracing/profiling work. 

The sentry team provides an excellent escape hatch for these types of
problems: a `tracesSampler` function. We can leverage the function to sample
at-time CPU usage and react dynamically with a trace/don't-trace decision. This
allows us to use a slightly higher sample rate, and to turn that rate down
during times of high load.

```javascript
function tracesSampler({ tracesSampleRate }) {

  // https://docs.sentry.io/platforms/javascript/configuration/sampling/
  return function(context) {
    // returning true is equivalent to 1, false is equivalent to 0, and
    // everything between is a percentage-based decision.
    let load = getCPULoad()

    if (load > 1) {
      // turn off sampling if we're thrashing CPU
      return 0
    }

    if (load > 0.9) {
      // this is critical usage cpu, nearly turn off our sampling
      return tracesSampleRate * 0.1
    }

    if (load > 0.75) {
      // this is too much cpu, cut our sample rate
      return tracesSampleRate * 0.5
    }

    if (load > 0.5) {
      // this is approaching too much cpu, slow our sample rate
      return tracesSampleRate * 0.75
    }

    if (context.parentSampled !== undefined) {
      return context.parentSampled
    }

    // use the default sample rate for all other cases
    return tracesSampleRate
  }
}
```

I hesitate to share the `getCPULoad` implementation. Node doesn't put this kind
of programming close at hand, and it isn't very well documented. What I've
managed to cobble together "works", but it doesn't feel like an officially
supported implementation. In certain environments (like Windows), this won't
work at all. Additionally, it's important to note that I am not an expert in
Node's process model, nor its clustering implementation. There might be
multithreading consequences of the following which I do not fully understand.
For these reasons I have cordoned off this particularly piece of code into a
"safe" place where we _knowingly_ cannot rely upon its correctness or
reliability.

```javascript
function getCPULoad() {
  let current = performance.now()

  // cache the CPU load response for a bit
  if ((current - lastCheckTime) < 5000) {
    return cachedCPULoad
  }

  let currentUsage = process.cpuUsage()
  let userDiff = currentUsage.user - lastCPUUsage.user
  let systemDiff = currentUsage.system - lastCPUUsage.system
  let timeDiff = current - lastCheckTime

  // process times are in microseconds, convert our timestamp to microseconds
  // to get process load as a number 0-1.0.
  let CPULoad = (userDiff + systemDiff) / (timeDiff * 1000)

  // update the cache
  cachedCPULoad = CPULoad
  lastCPUUsage = currentUsage
  lastCheckTime = current
    ;[loadAvg1, loadAvg5, loadAvg15] = os.loadavg()

  logger.info(`[${process.pid}] tracesSampler CPULoad: ${CPULoad}, loadAvg1: ${loadAvg1 / 100}, loadAvg5: ${loadAvg5 / 100}}`)
  return CPULoad
}
```

Let's take a look at the CPU for the dynamic sampling configuration: 

![cpu-fix-1-day-back](/assets/cpu-fix-1-day-back.png)

That orange line is looking quite a bit healthier. Importantly, the graph covers
a full peak-hours period. The CPU topped out at <80%, which is a much better
scenario than the previous configurations of ~95%.

## Where were we?

To close, I want to return to the original problem at hand: error capture. Let's
take a look at all the extra context provided by sentry for the exact same
`FetchError` issue mentioned earlier.

#### The what, when, where

![handled](/assets/sentry-handled.png)

#### The stack trace
![stack-trace](/assets/sentry-stack-trace.png)

#### The http context
![http](/assets/sentry-http.png)

#### What was happening just prior to the error (breadcrumbs)
![breadcrumbs](/assets/sentry-breadcrumbs.png)

#### All the other relevant system information
![context](/assets/sentry-context.png)

The rich context provided by sentry makes GCP's error alerts look anemic by
comparison. This is a sharp tool, however, and sharp tools require extra-careful
handling and operation. With the proper configuration, and a little patience, I
believe sentry can be a highly effective addition to any Node observability
stack.
