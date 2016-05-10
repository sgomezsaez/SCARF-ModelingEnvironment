jQuery-typing with events and data-api support
=============

Assign callbacks for started/stopped typing events. This version supports events and data-api and is backwards compatible with original version by narf.


Usage (events automatically attached just-in-time based on the data API - bootstrap style)
-----

  <input data-provide="typing" data-typing-delay="400" />
  $(body).bind('typing:start', ':input', function(event) { $(this).css('background', '#fa0'); });
  $(body).bind('typing:start', ':input', function(event) { $(this).css('background', '#f00'); });

Alternative Usage 
-----

    $(':text').typing({
        start: function (event, $elem) {
            $elem.css('background', '#fa0');
        },
        stop: function (event, $elem) {
            $elem.css('background', '#f00');
        },
        delay: 400
    });

Add `data-provide="typing"` attribute to your input and it will auto-magically start triggering `typing:start` and `typing:stop` events - you can treat these as ony other DOM events. This comes handy when working with frameworks like [Backbone JS][].

`typing` command takes key-value object with `start`, `stop` and
`delay` keys. They are all optional, so you can either pass only
`start` callback, `stop` callback, `stop` callback and `delay` time,
or everything.

`delay` is amount of time the plugin waits for another keypress before
judging that typing has stopped; it is expressed in milliseconds and
defaults to 400. Regardless of `delay`'s value, the `stop` callback is
called immediately when blur event occurs.

Callbacks are passed two arguments: event that caused callback execution
and jQuery object for matched element. Possible events are `keypress`
or `keydown` for `start` callbacks and `keyup` or `blur` for `stop`
callbacks.


Demo
----

Visit <http://tnajdek.github.io/jquery-typing/>


Download
--------

Get production version from & development version visit [GitHub][].

  [GitHub]: http://github.com/tnajdek/jquery-typing


Meta
----

jQuery-typing is written by [Maciej Konieczny][] and uses
[semantic versioning][] for release numbering.  Everything in `plugin/`
directory is released into the [public domain][].

jQuery-typing has been tweaked by [Tom Najdek][] to support data-api and trigger events.

  [Maciej Konieczny]: http://narf.pl/
  [Tom Najdek]: http://doppnet.com/
  [semantic versioning]: http://semver.org/
  [public domain]: http://unlicense.org/
  [Backbone JS]: http://backbonejs.org/
