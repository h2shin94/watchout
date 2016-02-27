#include <pebble.h>
#include "idle_window.h"
#include "report_window.h"
#include "alert_window.h"

// BEGIN AUTO-GENERATED UI CODE; DO NOT MODIFY
static Window *s_window;
static GFont s_res_gothic_28_bold;
static GBitmap *s_res_haz_bitmap;
static TextLayer *time_label;
static ActionBarLayer *action_bar;
static TextLayer *steps_label;
static TextLayer *distance_label;

static void initialise_ui(void) {
  s_window = window_create();
  #ifndef PBL_SDK_3
    window_set_fullscreen(s_window, true);
  #endif
  
  s_res_gothic_28_bold = fonts_get_system_font(FONT_KEY_GOTHIC_28_BOLD);
  s_res_haz_bitmap = gbitmap_create_with_resource(RESOURCE_ID_haz_bitmap);
  // time_label
  time_label = text_layer_create(GRect(22, 40, 100, 60));
  text_layer_set_background_color(time_label, GColorClear);
  text_layer_set_text(time_label, "Hi");
  text_layer_set_text_alignment(time_label, GTextAlignmentCenter);
  text_layer_set_font(time_label, s_res_gothic_28_bold);
  layer_add_child(window_get_root_layer(s_window), (Layer *)time_label);
  
  // action_bar
  action_bar = action_bar_layer_create();
  action_bar_layer_add_to_window(action_bar, s_window);
  action_bar_layer_set_background_color(action_bar, GColorWhite);
  action_bar_layer_set_icon(action_bar, BUTTON_ID_DOWN, s_res_haz_bitmap);
  layer_add_child(window_get_root_layer(s_window), (Layer *)action_bar);
  
  // steps_label
  steps_label = text_layer_create(GRect(22, 100, 100, 20));
  text_layer_set_background_color(steps_label, GColorClear);
  text_layer_set_text(steps_label, " ");
  text_layer_set_text_alignment(steps_label, GTextAlignmentCenter);
  layer_add_child(window_get_root_layer(s_window), (Layer *)steps_label);
  
  // distance_label
  distance_label = text_layer_create(GRect(22, 120, 100, 20));
  text_layer_set_background_color(distance_label, GColorClear);
  text_layer_set_text(distance_label, " ");
  text_layer_set_text_alignment(distance_label, GTextAlignmentCenter);
  layer_add_child(window_get_root_layer(s_window), (Layer *)distance_label);
}

static void destroy_ui(void) {
  window_destroy(s_window);
  text_layer_destroy(time_label);
  action_bar_layer_destroy(action_bar);
  text_layer_destroy(steps_label);
  text_layer_destroy(distance_label);
  gbitmap_destroy(s_res_haz_bitmap);
}
// END AUTO-GENERATED UI CODE

// Time upon starting up the app - used for Health API calls to get data for current session
static time_t load_time;

/**
* Called when idle_window is closed to remove ui elements from memory
*/
static void handle_window_unload(Window* window) {
  destroy_ui();
}

/**
* Called when down button is clicked (and held for at least 0.5s)
* Opens report_window to allow the user to report a hazard
*/
static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  show_report_window();
}

/**
* Configures system to link button presses to handler functions
*/
static void click_config_provider(void *context) {
  // This calls down_click_handler once the down button has been held for 500ms
  window_long_click_subscribe(BUTTON_ID_DOWN, 500, down_click_handler, NULL);
}

/**
* Called once every minute when subscribed
* Updates clock display to current clock time
*/
void tick_handler(struct tm *tick_time, TimeUnits units_changed) {
  // Get the current time
  static char s_time_buffer[16];
  clock_copy_time_string(s_time_buffer, sizeof(s_time_buffer));
  
  // Display the current time
  if (time_label) text_layer_set_text(time_label, s_time_buffer);
}

/**
* Get step and distance data from Pebble to display
* Only if Health API available (on SDK 3.0 builds for Pebble Time and Time Round models)
*/
#if defined(PBL_HEALTH)
static void health_handler(HealthEventType event, void *context) {
  if (event == HealthEventSignificantUpdate || event == HealthEventMovementUpdate) {
    time_t now = time(NULL);
    HealthServiceAccessibilityMask stepMask = health_service_metric_accessible(HealthMetricStepCount, load_time, now);
    HealthServiceAccessibilityMask distanceMask = health_service_metric_accessible(HealthMetricWalkedDistanceMeters, load_time, now);
    if (stepMask & HealthServiceAccessibilityMaskAvailable) {
      HealthValue steps = health_service_sum(HealthMetricStepCount, load_time, now);
      static char steps_text[32];
      snprintf(steps_text, sizeof(steps_text), "Steps: %d", (int)steps);
      text_layer_set_text(steps_label, steps_text);
    }
    if (distanceMask & HealthServiceAccessibilityMaskAvailable) {
      HealthValue distance = health_service_sum(HealthMetricWalkedDistanceMeters, load_time, now);
      static char distance_text[32];
      snprintf(distance_text, sizeof(distance_text), "Distance: %dm", (int)distance);
      text_layer_set_text(distance_label, distance_text);
    }
  }
}
#endif

/**
* Called when a message received from phone
* Handles ALERT, UPDATE and IGNORE messages separately
*/
static void app_message_received_handler(DictionaryIterator *iter, void *context) {
  APP_LOG(APP_LOG_LEVEL_INFO, "Received message");
  
  // If the KEY field is not filled, the message received is not appropriately formatted
  Tuple *message_tuple = dict_find(iter, DICT_TYPE_KEY);
  if (message_tuple) {
    // Identify the type of the message
    switch ((int) (message_tuple->value->int32)) {
      Tuple *type_tuple;
      Tuple *desc_tuple;
      Tuple *id_tuple;
      Tuple *dist_tuple;
      case DICT_TYPE_ALERT:
        // Retrieve the required fields
        type_tuple = dict_find(iter, DICT_HAZARD_TYPE_KEY);
        desc_tuple = dict_find(iter, DICT_HAZARD_DESC_KEY);
        id_tuple = dict_find(iter, DICT_HAZARD_ID_KEY);
        dist_tuple = dict_find(iter, DICT_HAZARD_DIST_KEY);
        if (!type_tuple || !desc_tuple || !id_tuple || !dist_tuple) break;
      
        // Ignore if an alert for that hazard is already up on the Pebble
        if (get_alert_with_id((int)(id_tuple->value->int32))) break;
      
        // Vibrate to notify the user
        vibes_short_pulse();
      
        // Add the alert
        add_alert((int)(id_tuple->value->int32), (char *)(type_tuple->value->cstring), (char *)(desc_tuple->value->cstring), (int)(dist_tuple->value->int32));
      
        // Display the alert if there are no other activities
        if (window_stack_get_top_window() == s_window) show_alert_window();
        refresh_alert_window();
        break;
      case DICT_TYPE_IGNORE:
        // Retrieve the id of the alert being ignored
        id_tuple = dict_find(iter, DICT_HAZARD_ID_KEY);
      
        // Remove the alert
        if (id_tuple) remove_alert_node(get_alert_with_id((int)(id_tuple->value->int32)));
        break;
      case DICT_TYPE_UPDATE:
        // Retrieve the required fields
        id_tuple = dict_find(iter, DICT_HAZARD_ID_KEY);
        dist_tuple = dict_find(iter, DICT_HAZARD_DIST_KEY);
        if (!id_tuple || !dist_tuple) break;
      
        // Get the alert to be updates
        Alert_List_Node *node = get_alert_with_id((int)(id_tuple->value->int32));
      
        // Update the alert
        if (node) node->distance = (int)(dist_tuple->value->int32);
        if (node == current_alert) refresh_alert_window();
        break;
      default:
        
        break;
    }
  }
}

/**
* Called to create and display idle_window
*/
void show_idle_window(void) {
  // Build UI
  initialise_ui();
  window_set_window_handlers(s_window, (WindowHandlers) {
    .unload = handle_window_unload,
  });
  
  // Connect button clicks to handlers
  window_set_click_config_provider(s_window, click_config_provider);
  
  // Connect clock to timer
  tick_timer_service_subscribe(MINUTE_UNIT, tick_handler);
  
  // Get the current time as the start time of the app
  load_time = time(NULL);
  
  #ifdef PBL_HEALTH
  if(!health_service_events_subscribe(health_handler, NULL)) {
    APP_LOG(APP_LOG_LEVEL_ERROR, "Health not subscribed!");
  }
  #else
  APP_LOG(APP_LOG_LEVEL_ERROR, "Health not available!");
  #endif /* PBL_HEALTH */

  // Allow messages to be received
  app_message_register_inbox_received(app_message_received_handler);
  app_message_open(app_message_inbox_size_maximum(), app_message_outbox_size_maximum());
  
  // Push window to the screen
  window_stack_push(s_window, true);
}

/**
* Called to close and destroy idle_window
*/
void hide_idle_window(void) {
  window_stack_remove(s_window, true);
}
