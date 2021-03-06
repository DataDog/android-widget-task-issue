# android-widget-task-issue

## The problem

When launching a `startActivityForResult` or `ActivityCallback.StartActivityForResult` from an activity that was
launched by a widget configuration activity, the task management is not the same as when launched from a regular
activity.

## Forewords

I discovered this issue while working with widgets.

Users of these widgets must authenticate through an OAuth flow as part of the configuration process.

Everything was fine while I was opening the widget's configuration activity from a call
to `PendingIntent.getActivity(...)`.

Then I've been trying to implement
the [widget configuration mechanism](https://developer.android.com/guide/topics/appwidgets/configuration), where the
configuration activity is launched by the system, and from what I understand, a different task / process.

(This is done by the framework
method `com.android.server.appwidget.AppWidgetServiceImpl.createAppWidgetConfigIntentSender`)

That's where the trouble began: If launched from the system, the activities task management does not work the same as
from my own task / app process.

I've reproduced schematically the OAuth / Activity flow in a sandbox that you can check out in
this [github repository](https://github.com/Datadog/android-widget-task-issue).

And here is some ASCII art to illustrate this flow:

```
                          Back Stack Towards Top
                +------------------------------------------>

 +------------+            +---------------+            +----------------+     
 |            |     (1)    |               |     (2)    |                | 
 | Widget     +----------->|    OAuth      +----------->| OAuthCallback  +
 | Config     |            |    Activity   |            |   Activity     |
 | Activity   |<-----------+               |<-----------+                |
 |  (SI)      |     (4)    |      (ST)     |     (3)    |     (SI)       | 
 +------------+            +-+---+---------+            +----------------+

Legend:
(1): startActivityForResult to `OAuthActivity`
(2): startActivity to `OAuthCallBackActivity`
(3): startActivity with flags: CLEAR_TOP | SINGLE_TOP to `OAuthActivity`
(4): finish() (with setResult(OK/CANCEL)
(SI): launchMode="singleInstance"
(ST): launchMode="singleTask"
```

## Steps to reproduce:

#### Step 1

Clone this sandbox: https://github.com/Datadog/android-widget-task-issue

#### Step 2

Build & Install the application `WidgetTaskIssue`

### Android 12

#### Step 3

Open your widget picker for the `WidgetTaskIssue` application, and set up the widget named `Widget` on your home screen.

#### Step 4

- Tap `Open configuration` button on the widget.
- Tap `Start OAuth` button
- Tap `Get callback` button

In your logcat, filter by `System.out`, you should see the [proper output](#proper-logcat-output)

#### Step 5

Long press your widget and tap the configuration icon (The little pen) or use drag & drop to configure.

- Tap `Start OAuth` button
- Tap `Get callback` button

In your logcat, filter by `System.out`, you should see the [faulty output](#faulty-logcat-output)

### <= Android 11

#### Step 3

Open your widget picker for the `WidgetTaskIssue` application, and set up the widget named `Widget` on your home screen.

#### Step 4

The configuration activity should open automatically.

- Tap `Start OAuth` button
- Tap `Get callback` button
- Tap the close button to close the activity

In your logcat, filter by `System.out`, you should see the [faulty output](#faulty-logcat-output)

#### Step 5

Now that you have a widget installed and configured.

- Tap `Open configuration` button on the widget.
- Tap `Start OAuth` button
- Tap `Get callback` button

In your logcat, filter by `System.out`, you should see the [proper output](#proper-logcat-output)

## Tested on

- Pixel 4 / Android 12
- Pixel 3a / Android 12
- Xiaomi 10 / Android 11

## Why it is problematic

Whether my activity is launched from my app process vs a background service process, the task management should remain
identical, otherwise I can't trust
the [Android documentation](https://developer.android.com/guide/components/activities/tasks-and-back-stack) about
activities and launch flags.

It also breaks any OAuth implementing the aforementioned activity flow, which relies on previously created activity
resuming through `onNewIntent` / `onResume`, instead of a new activity being recreated in a different task.

## What the correct behavior should be

When I use `startActivityForResult` from a background service such as
the [widget configuration mechanism](https://developer.android.com/guide/topics/appwidgets/configuration), or from any
other method, I should always get the same task management than if I launch from an app process, which is the one
described in the [proper output](#proper-logcat-output).

## Additional question

- `startActivity` produces the expected task management, while `startActivityForResult` does not. Why is that ?
- Is there any workaround in the meantime ? Ideally one that would allow to keep the `startActivityForResult` call.

## Appendix

### Faulty logcat output

```
com.qlitzler.sandbox I/System.out: [Configuration]: A
com.qlitzler.sandbox I/System.out: [Oauth] Create: A
com.qlitzler.sandbox I/System.out: [Oauth] Resume: A
com.qlitzler.sandbox I/System.out: [Callback]: B
com.qlitzler.sandbox I/System.out: [Oauth] Create: C
com.qlitzler.sandbox I/System.out: [Oauth] Resume: C
com.qlitzler.sandbox I/System.out: [Oauth] Resume: A
```

`A`, `B`, `C` and `D` are integers representing an `activity.taskId`. In this case, several `OAuth` activity exists, and
it runs in two different taskIds: `A` and `C`.

### Proper logcat output

```
com.qlitzler.sandbox I/System.out: [Configuration]: X
com.qlitzler.sandbox I/System.out: [Oauth] Create: X
com.qlitzler.sandbox I/System.out: [Oauth] Resume: X
com.qlitzler.sandbox I/System.out: [Callback]: Y
com.qlitzler.sandbox I/System.out: [Oauth] Resume: X
```

`X` and `Y` are integers representing an `activity.taskId`. In this case, a single `OAuth` activity exists, and it runs
in the same `taskId`.