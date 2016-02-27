/*
  To Do List:
  -/- Add image resources (hazard symbol, ticks)
  -/- Add sending of hazard to phone (finalise provided list of hazards)
  -/- Add decoding of data from phone (and remove click handler to alert screen)
  -/- Add sending of response to phone
  -/- Test doubly linked list of alerts and paginating
  - Add health metrics stuff
  -/- Reactivate vibes
  -/- Add ignoring of alerts
  -/- Add distance approximations and updating
  -/- Make brief notes on progress for Emma
  -/- Prevent duplicate alerts
  -/- Improve quick cancel of Sent windows
  
*/

#include <pebble.h>
#include "idle_window.h"

/**
* Called when app opened
* Starts app by opening idle_window
*/
void handle_init(void) {
  show_idle_window();
}

/**
* Called when app closed
*/
void handle_deinit(void) {
}

/**
* Main procedure for app
*/
int main(void) {
  handle_init();
  app_event_loop();
  handle_deinit();
}
