#include <pebble.h>
#include <stdlib.h>
#include <string.h>
#include "alert_window.h"
#include "alert_action_window.h"
#include "idle_window.h"

// BEGIN AUTO-GENERATED UI CODE; DO NOT MODIFY
static Window *s_window;
static GFont s_res_gothic_28_bold;
static GFont s_res_gothic_18_bold;
static GFont s_res_gothic_14;
static GBitmap *s_res_triup_bitmap;
static GBitmap *s_res_circle_bitmap;
static GBitmap *s_res_tridown_bitmap;
static TextLayer *watch_out;
static TextLayer *hazard_title;
static TextLayer *hazard_description;
static ActionBarLayer *alert_action_bar;

static void initialise_ui(void) {
  s_window = window_create();
  #ifndef PBL_SDK_3
    window_set_fullscreen(s_window, true);
  #endif
  
  s_res_gothic_28_bold = fonts_get_system_font(FONT_KEY_GOTHIC_28_BOLD);
  s_res_gothic_18_bold = fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD);
  s_res_gothic_14 = fonts_get_system_font(FONT_KEY_GOTHIC_14);
  s_res_triup_bitmap = gbitmap_create_with_resource(RESOURCE_ID_triup_bitmap);
  s_res_circle_bitmap = gbitmap_create_with_resource(RESOURCE_ID_circle_bitmap);
  s_res_tridown_bitmap = gbitmap_create_with_resource(RESOURCE_ID_tridown_bitmap);
  // watch_out
  watch_out = text_layer_create(GRect(17, 15, 110, 30));
  text_layer_set_background_color(watch_out, GColorClear);
  text_layer_set_text(watch_out, "Watch Out!");
  text_layer_set_text_alignment(watch_out, GTextAlignmentCenter);
  text_layer_set_font(watch_out, s_res_gothic_28_bold);
  layer_add_child(window_get_root_layer(s_window), (Layer *)watch_out);
  
  // hazard_title
  hazard_title = text_layer_create(GRect(10, 50, 115, 20));
  text_layer_set_background_color(hazard_title, GColorClear);
  text_layer_set_text(hazard_title, "Hazard Title");
  text_layer_set_font(hazard_title, s_res_gothic_18_bold);
  layer_add_child(window_get_root_layer(s_window), (Layer *)hazard_title);
  
  // hazard_description
  hazard_description = text_layer_create(GRect(10, 80, 115, 60));
  text_layer_set_background_color(hazard_description, GColorClear);
  text_layer_set_text(hazard_description, "Hazard Description");
  text_layer_set_font(hazard_description, s_res_gothic_14);
  layer_add_child(window_get_root_layer(s_window), (Layer *)hazard_description);
  
  // alert_action_bar
  alert_action_bar = action_bar_layer_create();
  action_bar_layer_add_to_window(alert_action_bar, s_window);
  action_bar_layer_set_background_color(alert_action_bar, GColorWhite);
  action_bar_layer_set_icon(alert_action_bar, BUTTON_ID_UP, s_res_triup_bitmap);
  action_bar_layer_set_icon(alert_action_bar, BUTTON_ID_SELECT, s_res_circle_bitmap);
  action_bar_layer_set_icon(alert_action_bar, BUTTON_ID_DOWN, s_res_tridown_bitmap);
  layer_add_child(window_get_root_layer(s_window), (Layer *)alert_action_bar);
}

static void destroy_ui(void) {
  window_destroy(s_window);
  text_layer_destroy(watch_out);
  text_layer_destroy(hazard_title);
  text_layer_destroy(hazard_description);
  action_bar_layer_destroy(alert_action_bar);
  gbitmap_destroy(s_res_triup_bitmap);
  gbitmap_destroy(s_res_circle_bitmap);
  gbitmap_destroy(s_res_tridown_bitmap);
}
// END AUTO-GENERATED UI CODE

/**
* Called when action_sent_window is closed to remove ui elements from memory
*/
static void handle_window_unload(Window* window) {
  destroy_ui();
}

/**
* Called when up button is clicked
* Scrolls to previous alert (if possible)
*/
static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  // Move current_alert pointer through doubly linked list
  if (current_alert->prev) {
    current_alert = current_alert->prev;
  }
  
  // Redraw window to view the new hazard
  refresh_alert_window();
}

/**
* Called when down button is clicked
* Scrolls to next alert (if possible)
*/
static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  // Move current_alert pointer through doubly linked list
  if (current_alert->next) {
    current_alert = current_alert->next;
  }
  
  // Redraw window to view the new hazard
  refresh_alert_window();
}

/**
* Called when select button is clicked
* Opens alert_action_window to allow the user to act on current alert
*/
static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  show_alert_action_window();
}

/**
* Called when back button is clicked
* Deletes all remaining alerts and sends off the NACK responses to phone
* Closes alert_window to return to idle_window
*/
static void back_click_handler(ClickRecognizerRef recognizer, void *context) {
  // Start at the front of the alert list and remove each on in turn until the list is empty
  while (first_alert) {
    // Build a NACK message
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
    
    // Fill in message fields
    int type = DICT_TYPE_ACTION;
    dict_write_int(iter, DICT_TYPE_KEY, &type, sizeof(int), true);
    dict_write_int(iter, DICT_HAZARD_ID_KEY, &(first_alert->alert_id), sizeof(int), true);
    int action = DICT_ACTION_NACK;
    dict_write_int(iter, DICT_ACTION_KEY, &action, sizeof(int), true);
    
    // Send the NACK message
    app_message_outbox_send();
    
    // Remove the alert
    remove_alert_node(first_alert);
  }
  
  // Reset the window so it never attempts to read from the locations of the removed strings
  refresh_alert_window();
  
  // Return to idle_window
  hide_alert_window();
}

/**
* Configures system to link button presses to handler functions
*/
static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_BACK, back_click_handler);
}

/**
* Called when a new alert is received
* Add some new alert to the list
*/
void add_alert(int id, char *t, char *d, int di) {
  // Create space for new alert
  Alert_List_Node *new_node = malloc(sizeof(Alert_List_Node));
  if (!new_node) {
    APP_LOG(APP_LOG_LEVEL_ERROR, "Ran out of memory!");
    return;
  }
  
  // Fill in hazard details
  // Note the tentative character limit on the amount displayed
  new_node->alert_id = id;
  new_node->title = malloc(15 * sizeof(char));
  if (!(new_node->title)) {
    APP_LOG(APP_LOG_LEVEL_ERROR, "Ran out of memory!");
    return;
  }
  strncpy(new_node->title, t, 15 * sizeof(char));
  new_node->desc = malloc(80 * sizeof(char));
  if (!(new_node->desc)) {
    APP_LOG(APP_LOG_LEVEL_ERROR, "Ran out of memory!");
    return;
  }
  strncpy(new_node->desc, d, 80 * sizeof(char));
  
  // If we include distance updates, we will need to maintain this field
  new_node->distance = di;
  
  if (!last_alert) {
    // If the alert list is empty, this becomes the only node
    first_alert = new_node;
    last_alert = new_node;
    current_alert = new_node;
  } else {
    // Otherwise, this node is added at the end
    last_alert->next = new_node;
    new_node->prev = last_alert;
    last_alert = new_node;
  }
}

/**
* Lookup an alert using its id number
* If no such alert exists, returns null pointer
*/
Alert_List_Node *get_alert_with_id(int id) {
  // Advance a pointer through the list
  Alert_List_Node *node = first_alert;
  while (node) {
    // If we find the match, return it
    if (node->alert_id == id) return node;
    
    // Otherwise we continue looking
    node = node->next;
  }
  
  // When the end of the list is reached, we know no such alert exists
  return NULL;
}

/**
* Removes a given alert from the current list
* (Assumes there are no duplicates of that node)
* (Assumes that the doubly linked list structure is preserved so there will be no dangling pointers)
*/
void remove_alert_node(Alert_List_Node *to_remove) {
  // If the alert does not exist, we do not need to remove it
  if (!to_remove) return;
  
  if (to_remove == first_alert && to_remove == last_alert) {
    // If this was the only alert, the list becomes empty
    first_alert = NULL;
    last_alert = NULL;
    current_alert = NULL;
  } else if (to_remove == first_alert) {
    // If this was the first alert, the first_alert pointer must be updated
    first_alert = to_remove->next;
    if (to_remove == current_alert) {
      current_alert = first_alert;
    }
    first_alert->prev = NULL;
  } else if (to_remove == last_alert) {
    // If this was the last alert, the last_alert pointer must be updated
    last_alert = to_remove->prev;
    if (to_remove == current_alert) {
      current_alert = last_alert;
    }
    last_alert->next = NULL;
  } else {
    // Otherwise it is in the middle of the list so must update the alerts either side
    to_remove->next->prev = to_remove->prev;
    to_remove->prev->next = to_remove->next;
    if (to_remove == current_alert) {
      current_alert = to_remove->next;
    }
  }
  
  // Free the memory used
  free(to_remove->title);
  free(to_remove->desc);
  free(to_remove);
}

/**
* DEBUG FUNCTION
* Prints contents of an alert list node to app log
* Time-expensive so only call when debugging
*/
static void print_alert_node(Alert_List_Node *node) {
  if (!node) {
    APP_LOG(APP_LOG_LEVEL_INFO, "node null");
    return;
  }
  APP_LOG(APP_LOG_LEVEL_INFO, "Alert ID: %d", node->alert_id);
  if (!(node->prev)) APP_LOG(APP_LOG_LEVEL_INFO, "prev null");
  else APP_LOG(APP_LOG_LEVEL_INFO, "prev: %d", node->prev->alert_id);
  if (!(node->next)) APP_LOG(APP_LOG_LEVEL_INFO, "next null");
  else APP_LOG(APP_LOG_LEVEL_INFO, "next: %d", node->next->alert_id);
  APP_LOG(APP_LOG_LEVEL_INFO, " ");
}

/**
* DEBUG FUNCTION
* Prints contents of all nodes in alert list to app log
* Time-expensive so only call when debugging
*/
void print_alert_list(void) {
  Alert_List_Node *node = first_alert;
  APP_LOG(APP_LOG_LEVEL_INFO, "First Alert");
  while (node) {
    if (node == current_alert) APP_LOG(APP_LOG_LEVEL_INFO, "Current Alert");
    if (node == last_alert) APP_LOG(APP_LOG_LEVEL_INFO, "Last Alert");
    print_alert_node(node);
    node = node->next;
  }
  APP_LOG(APP_LOG_LEVEL_INFO, " ");
}

/**
* Updates text fields on alert_window to ensure they are relevant to the current alert
*/
void refresh_alert_window() {
  if (current_alert) {
    // If the distance notifications are added, attach these to the title
    /*static char title_text[20];
    snprintf(title_text, 20, "%s in %dm", current_alert->title, current_alert->distance);
    text_layer_set_text(hazard_title, title_text);*/
    
    // Otherwise, just display the title
    text_layer_set_text(hazard_title, current_alert->title);
    
    // Display the description
    text_layer_set_text(hazard_description, current_alert->desc);
    
    // Update the scroll indicators as appropriate
    if (current_alert->next) action_bar_layer_set_icon(alert_action_bar, BUTTON_ID_DOWN, s_res_tridown_bitmap);
    else action_bar_layer_clear_icon(alert_action_bar, BUTTON_ID_DOWN);
    if (current_alert->prev) action_bar_layer_set_icon(alert_action_bar, BUTTON_ID_UP, s_res_triup_bitmap);
    else action_bar_layer_clear_icon(alert_action_bar, BUTTON_ID_UP);
  } else {
    // If there are no alerts, reset them to default strings so there are no dangling pointers
    text_layer_set_text(hazard_title, "Hazard Title");
    text_layer_set_text(hazard_description, "Hazard Description");
  }
}

/**
* Called to create and display alert_window
*/
void show_alert_window(void) {
  // Build UI
  initialise_ui();
  window_set_window_handlers(s_window, (WindowHandlers) {
    .unload = handle_window_unload,
  });
  
  // Connect button clicks to handlers
  window_set_click_config_provider(s_window, click_config_provider);
  
  // Update title and description as appropriate for the current alert
  refresh_alert_window();
  
  // Push the window to the screen
  window_stack_push(s_window, true);
}

/**
* Called to close and destroy alert_action_window
*/
void hide_alert_window(void) {
  window_stack_remove(s_window, true);
}
