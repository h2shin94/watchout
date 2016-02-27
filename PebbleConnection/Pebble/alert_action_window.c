#include <pebble.h>
#include "alert_action_window.h"
#include "action_sent_window.h"
#include "alert_window.h"
#include "idle_window.h"

// BEGIN AUTO-GENERATED UI CODE; DO NOT MODIFY
static Window *s_window;
static MenuLayer *action_menu;

static void initialise_ui(void) {
  s_window = window_create();
  #ifndef PBL_SDK_3
    window_set_fullscreen(s_window, true);
  #endif
  
  // action_menu
  action_menu = menu_layer_create(GRect(0, 0, 144, 96));
  menu_layer_set_click_config_onto_window(action_menu, s_window);
  layer_add_child(window_get_root_layer(s_window), (Layer *)action_menu);
}

static void destroy_ui(void) {
  window_destroy(s_window);
  menu_layer_destroy(action_menu);
}
// END AUTO-GENERATED UI CODE

/**
* Called when alert_action_window is closed to remove ui elements from memory
*/
static void handle_window_unload(Window* window) {
  destroy_ui();
}

/**
* Called when up button is clicked
* Scrolls up on the menu (if possible)
*/
static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  menu_layer_set_selected_next(action_menu, true, MenuRowAlignNone, true);
}

/**
* Called when down button is clicked
* Scrolls down on the menu (if possible)
*/
static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  menu_layer_set_selected_next(action_menu, false, MenuRowAlignNone, true);
}

/**
* Called when select button is clicked
* Sends the action to the phone and removes the alert
*/
static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  // Get the selected item in the menu
  MenuIndex selected = menu_layer_get_selected_index(action_menu);
  
  // The menu only has section 0 at the moment, if it is not this then we have an erroneous state
  if (selected.section == 0) {
    // Build the message
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
    
    // Write the message type and hazard ID fields in
    int type = DICT_TYPE_ACTION;
    dict_write_int(iter, DICT_TYPE_KEY, &type, sizeof(int), true);
    dict_write_int(iter, DICT_HAZARD_ID_KEY, &(current_alert->alert_id), sizeof(int), true);
    
    // Determine which action the user has selected
    int action;
    switch (selected.row) {
      case 0:
        action = DICT_ACTION_ACK;
        break;
      case 1:
        action = DICT_ACTION_DIS;
        break;
    }
    dict_write_int(iter, DICT_ACTION_KEY, &action, sizeof(int), true);
    
    // Send the message
    app_message_outbox_send();
    
    // Remove alert from the Pebble and show the notification
    remove_alert_node(current_alert);
    refresh_alert_window();
    show_action_sent_window();
  }
}

/**
* Required to draw menu
*/
static uint16_t menu_get_num_sections_callback(MenuLayer *menu_layer, void *data) {
  return 0;
}

/**
* Required to draw menu
*/
static uint16_t menu_get_num_rows_callback(MenuLayer *menu_layer, uint16_t section_index, void *data) {
  // Only a single section to consider
  return 2;
}

/**
* Required to draw menu
*/
static int16_t menu_get_header_height_callback(MenuLayer *menu_layer, uint16_t section_index, void *data) {
  // No header is required
  return 0;
}

/**
* Required to draw menu
* Defines section headers
*/
static void menu_draw_header_callback(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
  // Determine which section we're working with
  switch (section_index) {
    case 0:
      // Draw title text in the section header e.g.
      //menu_cell_basic_header_draw(ctx, cell_layer, "Was the hazard there?");
      break;
  }
}

/**
* Required to draw menu
* Defines item text
*/
static void menu_draw_row_callback(GContext* ctx, const Layer *cell_layer, MenuIndex *cell_index, void *data) {
  // Determine which section we're going to draw in
  switch (cell_index->section) {
    case 0:
      // Use the row to specify which item we'll draw
      switch (cell_index->row) {
        case 0:
          menu_cell_basic_draw(ctx, cell_layer, "I can see it", "", NULL);
          break;
        case 1:
          menu_cell_basic_draw(ctx, cell_layer, "It's gone", "", NULL);
          break;
      }
      break;
    // default:
  }
}

/**
* Configures system to link button presses to handler functions
*/
static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
}

/**
* Called to create and display alert_action_window
*/
void show_alert_action_window(void) {
  // Build UI
  initialise_ui();
  window_set_window_handlers(s_window, (WindowHandlers) {
    .unload = handle_window_unload,
  });
  
  // Connect button clicks to handlers
  window_set_click_config_provider(s_window, click_config_provider);
  
  // Setup menu
  menu_layer_set_callbacks(action_menu, NULL, (MenuLayerCallbacks){
    .get_num_sections = menu_get_num_sections_callback,
    .get_num_rows = menu_get_num_rows_callback,
    .get_header_height = menu_get_header_height_callback,
    .draw_header = menu_draw_header_callback,
    .draw_row = menu_draw_row_callback,
  });
  
  // Push the window to the screen
  window_stack_push(s_window, true);
}

/**
* Called to close and destroy alert_action_window
*/
void hide_alert_action_window(void) {
  window_stack_remove(s_window, true);
}
