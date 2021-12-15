# android-widget-task-issue

## The problem

When launching a `startActivityForResult` or `ActivityCallback.StartActivityForResult` from an activity that was launched by a widget configuration activity, the task management is not the same as when launched from a regular activity.

## Forewords

I discovered this issue while working with widgets.

Users of these widgets must authenticate through an OAuth flow as part of the configuration process.

Everything was fine while I was opening the widget's configuration activity from the `PendingIntent.getActivity(...)` mechanism.

Then I've been trying to implement the [new widget configuration mechanism](https://developer.android.com/guide/topics/appwidgets/configuration), where the configuration activity is launched by the system.

(From my understanding, this is done by the framework method `com.android.server.appwidget.AppWidgetServiceImpl.createAppWidgetConfigIntentSender`)
However, the oAuth flow is broken by this new feature.

I've reproduced schematically the OAuth flow in a sandbox that you can check out below.

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

## Steps to reproduce:

- **Step 1**: Clone this sandbox: https://github.com/Datadog/android-widget-task-issue
- **Step 2**: Build & Install the application `WidgetTaskIssue`
- **Step 3**: Open your widget picker for the `WidgetTaskIssue` application, and set up the widget named `Widget` your home.
- **Step 4**: Tap anywhere on the widget. Activities will open. In your logcat, filter by `System.out`, you should see:

```
com.qlitzler.sandbox I/System.out: [Configuration]: X
com.qlitzler.sandbox I/System.out: [Oauth] Create: X
com.qlitzler.sandbox I/System.out: [Oauth] Resume: X
com.qlitzler.sandbox I/System.out: [Callback]: Y
com.qlitzler.sandbox I/System.out: [Oauth] Resume: X
```

`X` and `Y` are integers representing an `activity.taskId`. In this case, a single `OAuth` activity exists, and it runs in the same `taskId`.

**Clean all opened activities**

- **Step 5**: Long press your widget and tap the configuration icon (The little pen). Activities will open. In your logcat, filter by `System.out`, you should see:

```
com.qlitzler.sandbox I/System.out: [Configuration]: A
com.qlitzler.sandbox I/System.out: [Oauth] Create: A
com.qlitzler.sandbox I/System.out: [Oauth] Resume: A
com.qlitzler.sandbox I/System.out: [Callback]: B
com.qlitzler.sandbox I/System.out: [Oauth] Create: C
com.qlitzler.sandbox I/System.out: [Oauth] Resume: C
com.qlitzler.sandbox I/System.out: [Callback]: D
com.qlitzler.sandbox I/System.out: [Oauth] Resume: C
```

`A`, `B`, `C` and `D` are integers representing an `activity.taskId`. In this case, several `OAuth` activity exists, and it runs in two different taskIds: `A` and `C`.

- **Step 6**: (Bonus). In the code, replace `startActivityForResult` by `startActivity`, and pick up at **Step 5**. You'll get the output of **Step 4** !

### Tested on

- Pixel 4 / Android 12
- Pixel 3a / Android 12

### Why it is problematic

Whether my activity is launched from my app process vs a background service process, the task management should remain identical, otherwise I can't trust the [Android documentation](https://developer.android.com/guide/components/activities/tasks-and-back-stack) about activities and launch flags.
It also breaks any OAuth implementing the aforementioned activity flow, which relies on previously created activity resuming through `onNewIntent` / `onResume`, instead of a new instead being recreated in a different task.

### What the correct behavior should be

When I use `startActivityForResult` from a background service such as the [new widget configuration mechanism](https://developer.android.com/guide/topics/appwidgets/configuration),
or from any other method, I should always get the same task management than if I launch from an app process, which is the one described in the output of **Step 4**.

### Additional question

- `startActivity` produces the expected task management, while `startActivityForResult` does not. Why is that ? (see **Step 6**)
- Is there any workaround in the meantime ? Ideally one that would allow to keep the `startActivityForResult` call.