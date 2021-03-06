#include <pebble.h>
#include "report_sent_window.h"
#include "report_window.h"
#include "idle_window.h"
#include "alert_window.h"

// BEGIN AUTO-GENERATED UI CODE; DO NOT MODIFY
static Window *s_window;
static GFont s_res_gothic_18_bold;
static GBitmap *s_res_tick_bitmap;
static TextLayer *notification_text;
static BitmapLayer *tick_layer;

static void initialise_ui(void) {
  s_window = window_create();
  #ifndef PBL_SDK_3
    window_set_fullscreen(s_window, true);
  #endif
  
  s_res_gothic_18_bold = fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD);
  s_res_tick_bitmap = gbitmap_create_with_resource(RESOURCE_ID_tick_bitmap);
  // notification_text
  notification_text = text_layer_create(GRect(22, 110, 100, 28));
  text_layer_set_background_color(notification_text, GColorClear);
  text_layer_set_text(notification_text, "Hazard Saved");
  text_layer_set_text_alignment(notification_text, GTextAlignmentCenter);
  text_layer_set_font(notification_text, s_res_gothic_18_bold);
  layer_add_child(window_get_root_layer(s_window), (Layer *)notification_text);
  
  // tick_layer
  tick_layer = bitmap_layer_create(GRect(47, 35, 50, 50));
  bitmap_layer_set_bitmap(tick_layer, s_res_tick_bitmap);
  layer_add_child(window_get_root_layer(s_window), (Layer *)tick_layer);
}

static void destroy_ui(void) {
  window_destroy(s_window);
  text_layer_destroy(notification_text);
  bitmap_layer_destroy(tick_layer);
  gbitmap_destroy(s_res_tick_bitmap);
}
// END AUTO-GENERATED UI CODE

// Timer value - decremented each second in notif_tick_handler
static int countdown = 2;

/**
* Called to close and destroy report_sent_window
*/
static void handle_window_unload(Window* window) {
  destroy_ui();
}

/**
* Called when back button is clicked
* Does timed event immediately
*/
static void back_click_handler(ClickRecognizerRef recognizer, void *context) {
  hide_report_sent_window();
  hide_report_window();
  
  // Reconnect clock on idle_window to timer
  tick_timer_service_subscribe(MINUTE_UNIT, tick_handler);
    if (current_alert) show_alert_window();
  
  // Reset countdown
  countdown = 2;
  return;
}

/**
* Configures system to link button presses to handler functions
*/
static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_BACK, back_click_handler);
}

/**
* Called once every second when subscribed
* After ~2 seconds, will close down report_sent_window and report_window
* If there are any alerts received then will oped alert_window, else it will return to idle_window
*/
static void notif_tick_handler(struct tm *tick_time, TimeUnits units_changed) {
  // Check to see if two seconds have passed
  if (countdown == 0) {
    // Close the reporting windows
    hide_report_sent_window();
    hide_report_window();
    
    // Reconnect clock on idle_window to timer
    tick_timer_service_subscribe(MINUTE_UNIT, tick_handler);
    
    // If any alerts hav been received whilst reporting, display them
    if (current_alert) show_alert_window();
    
    // Reset countdown
    countdown = 2;
    return;
  }
  
  // Two seconds have not passed yet
  countdown--;
}

/**
* Called to create and display report_sent_window
*/
void show_report_sent_window(void) {
  // Build UI
  initialise_ui();
  window_set_window_handlers(s_window, (WindowHandlers) {
    .unload = handle_window_unload,
  });
  
  // Connect button clicks to handlers
  window_set_click_config_provider(s_window, click_config_provider);
  
  // Connect countdown sequence to timer
  tick_timer_service_subscribe(SECOND_UNIT, notif_tick_handler);
  
  // Push the window to the screen
  window_stack_push(s_window, true);
}

/**
* Called to close and destroy report_sent_window
*/
void hide_report_sent_window(void) {
  window_stack_remove(s_window, true);
}
