# android-widget-task-issue

## The problem

When launching a `startActivityForResult` or `ActivityCallback.StartActivityForResult` from an activity that was launched by a widget configuration activity, the task management is not the same as when launched from a regular activity.

## Forewords

I discovered this issue while working on the Datadog app.
The Datadog app has widgets for which users must first authenticate through an oAuth flow before being able to configure it.

Historically, to reach the widget configuration activity, from which our oAuth flow is launched, users simply clicked somewhere on our widget: This used
a `PendingIntent.getActivity(...)` mechanism.

We've been trying to implement the [new widget configuration mechanism](https://developer.android.com/guide/topics/appwidgets/configuration), where the configuration activity is launched by the system.

(From my understanding, this is done by the framework method `com.android.server.appwidget.AppWidgetServiceImpl.createAppWidgetConfigIntentSender`)
However, our oAuth flow is broken by this new feature.

I've reproduced schematically our flow in a sandbox that you can check out below.

Some ASCII art to illustrate this flow:

```
                          Back Stack Towards Top
                +------------------------------------------>

 +------------+            +---------------+      +----------------+     
 |            |     (1)    |               | (2)  |                | 
 | Widget     +----------->|    OAuth      +----->| OAuthCallback  +
 | Config     |            |    Activity   |      |   Activity     |
 | Activity   |<-----------+               |<-----+                |
 |  (SI)      |     (4)    |      (ST)     | (3)  |     (SI)       | 
 +------------+            +-+---+---------+      +----------------+

Legend:
(1): startActivityForResult
(2): startActivity
(3): startActivity with flags: CLEAR_TOP | SINGLE_TOP
(4): finish() (with setResult(OK/CANCEL)
(SI): launchMode="singleInstance"
(ST): launchMode="singleTask"
```
NOTE: In our actual app, the oAuth flow has an extra step which is the custom chrome tab to intercept the oauth redirect.
I could not add this step in the sandbox linked below, so I simplified.


## Steps to reproduce:

- Step 1: Clone this sandbox: https://github.com/DataDog/android-widget-task-issue
- Step 2: Build & Install the application `WidgetTaskIssue`
- Step 3: Open your widget picker for the `WidgetTaskIssue` application, and set up the widget named `Widget` your home.
- Step 4: Tap anywhere on the widget. Activities will open. In your logcat, filter by `System.out`, you should see:

```
com.datadog.sandbox I/System.out: [Configuration]: X
com.datadog.sandbox I/System.out: [Oauth] Create: X
com.datadog.sandbox I/System.out: [Oauth] Resume: X
com.datadog.sandbox I/System.out: [Callback]: Y
com.datadog.sandbox I/System.out: [Oauth] Resume: X
```

`X` and `Y` are integers representing an `activity.taskId`. In this case, a single `OAuth` activity exists, and it runs in the same `taskId`.


- Step 5: Long press your widget and tap the configuration icon (The little pen). Activities will open. In your logcat, filter by `System.out`, you should see:

```
com.datadog.sandbox I/System.out: [Configuration]: W
com.datadog.sandbox I/System.out: [Oauth] Create: W
com.datadog.sandbox I/System.out: [Oauth] Resume: W
com.datadog.sandbox I/System.out: [Callback]: X
com.datadog.sandbox I/System.out: [Oauth] Create: Y
com.datadog.sandbox I/System.out: [Oauth] Resume: Y
com.datadog.sandbox I/System.out: [Callback]: Z
com.datadog.sandbox I/System.out: [Oauth] Resume: Y
```

`W`, `X`, `Y` and `Z` are integers representing an `activity.taskId`. In this case, several `OAuth` activity exists, and it runs in two different taskIds: `W` and `Y`.

- Step 6: (Bonus). In the code, replace `startActivityForResult` by `startActivity`, and pick up at **Step 5**. You'll get the output of **Step 4** !

### What the correct behavior should be

When I use `startActivityForResult from the [new widget configuration mechanism](https://developer.android.com/guide/topics/appwidgets/configuration),
or from any other method, I should always get the same task management, which is the one described in the output of **Step 4**.

### Additionnal question

Why is `startActivityForResult` different from `startActivity` in this use case ? (see **Step 6**)
