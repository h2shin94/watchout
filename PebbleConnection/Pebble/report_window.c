#include <pebble.h>
#include "report_window.h"
#include "report_sent_window.h"
#include "idle_window.h"

// BEGIN AUTO-GENERATED UI CODE; DO NOT MODIFY
static Window *s_window;
static MenuLayer *report_menu;

static void initialise_ui(void) {
  s_window = window_create();
  #ifndef PBL_SDK_3
    window_set_fullscreen(s_window, true);
  #endif
  
  // report_menu
  report_menu = menu_layer_create(GRect(0, 0, 144, 168));
  menu_layer_set_click_config_onto_window(report_menu, s_window);
  layer_add_child(window_get_root_layer(s_window), (Layer *)report_menu);
}

static void destroy_ui(void) {
  window_destroy(s_window);
  menu_layer_destroy(report_menu);
}
// END AUTO-GENERATED UI CODE

/**
* Called when report_window is closed to remove ui elements from memory
*/
static void handle_window_unload(Window* window) {
  destroy_ui();
}

/**
* Called when up button is clicked
* Scrolls up on the menu (if possible)
*/
static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  menu_layer_set_selected_next(report_menu, true, MenuRowAlignCenter, true);
}

/**
* Called when down button is clicked
* Scrolls down on the menu (if possible)
*/
static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  menu_layer_set_selected_next(report_menu, false, MenuRowAlignCenter, true);
}

/**
* Called when select button is clicked
* Sends the new hazard details to the phone
*/
static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  // Get the selected item in the menu
  MenuIndex selected = menu_layer_get_selected_index(report_menu);
  
  // Build a NEW message
  DictionaryIterator *iter;
  app_message_outbox_begin(&iter);
  int type = DICT_TYPE_NEW;
  dict_write_int(iter, DICT_TYPE_KEY, &type, sizeof(int), true);
  
  // Determine which hazard type the user is reporting
  switch (selected.section) {
    case 0:
      switch (selected.row) {
        case 0:
          dict_write_cstring(iter, DICT_HAZARD_TYPE_KEY, "Other");
          break;
        case 1:
          dict_write_cstring(iter, DICT_HAZARD_TYPE_KEY, "Pothole");
          break;
        case 2: 
          dict_write_cstring(iter, DICT_HAZARD_TYPE_KEY, "Road Closure");
          break;
        case 3: 
          dict_write_cstring(iter, DICT_HAZARD_TYPE_KEY, "Flooding");
          break;
        case 4: 
          dict_write_cstring(iter, DICT_HAZARD_TYPE_KEY, "Traffic Accident");
          break;
        case 5: 
          dict_write_cstring(iter, DICT_HAZARD_TYPE_KEY, "Broken Glass");
          break;
        case 6: 
          dict_write_cstring(iter, DICT_HAZARD_TYPE_KEY, "Road Works");
          break;
      }
      break;
  }
  
  // Send the message
  app_message_outbox_send();
  APP_LOG(APP_LOG_LEVEL_INFO, "New hazard sent");
  
  // Show notification
  show_report_sent_window();
}

/**
* Required to draw menu
*/
static uint16_t menu_get_num_sections_callback(MenuLayer *menu_layer, void *data) {
  return 1;
}

/**
* Required to draw menu
*/
static uint16_t menu_get_num_rows_callback(MenuLayer *menu_layer, uint16_t section_index, void *data) {
  if (section_index == 0) return 7;
  return 0;
}

/**
* Required to draw menu
*/
static int16_t menu_get_header_height_callback(MenuLayer *menu_layer, uint16_t section_index, void *data) {
  return MENU_CELL_BASIC_HEADER_HEIGHT;
}

/**
* Required to draw menu
* Defines section headers
*/
static void menu_draw_header_callback(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
  // Determine which section we're working with
  switch (section_index) {
    case 0:
      // Draw title text in the section header
      menu_cell_basic_header_draw(ctx, cell_layer, "What is the hazard?");
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
          menu_cell_basic_draw(ctx, cell_layer, "Other", "Or enter later", NULL);
          break;
        case 1:
          menu_cell_basic_draw(ctx, cell_layer, "Pothole", "", NULL);
          break;
        case 2: 
          menu_cell_basic_draw(ctx, cell_layer, "Road Closure", "", NULL);
          break;
        case 3: 
          menu_cell_basic_draw(ctx, cell_layer, "Flooding", "", NULL);
          break;
        case 4: 
          menu_cell_basic_draw(ctx, cell_layer, "Traffic Accident", "", NULL);
          break;
        case 5: 
          menu_cell_basic_draw(ctx, cell_layer, "Broken Glass", "", NULL);
          break;
        case 6: 
          menu_cell_basic_draw(ctx, cell_layer, "Road Works", "", NULL);
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
* Called to create and display report_window
* NOTE: Building in SDK 3.0 causes the app to crash on reaching this method
* Adding all of the APP_LOG calls in here stops this
*/
void show_report_window(void) {
  //APP_LOG(APP_LOG_LEVEL_INFO, "show_report_window called");
  
  // Build UI
  initialise_ui();
  //APP_LOG(APP_LOG_LEVEL_INFO, "ui initialised");
  window_set_window_handlers(s_window, (WindowHandlers) {
    .unload = handle_window_unload,
  });
  
  //APP_LOG(APP_LOG_LEVEL_INFO, "Window handlers set");
  
  // Connect button clicks to handlers
  window_set_click_config_provider(s_window, click_config_provider);
  
  //APP_LOG(APP_LOG_LEVEL_INFO, "Click provider set");
  
  // Setup menu
  menu_layer_set_callbacks(report_menu, NULL, (MenuLayerCallbacks){
    .get_num_sections = menu_get_num_sections_callback,
    .get_num_rows = menu_get_num_rows_callback,
    .get_header_height = menu_get_header_height_callback,
    .draw_header = menu_draw_header_callback,
    .draw_row = menu_draw_row_callback,
  });
  
  //APP_LOG(APP_LOG_LEVEL_INFO, "Menu stuff set");
  
  // Push the window to the screen
  window_stack_push(s_window, true);
  
  //APP_LOG(APP_LOG_LEVEL_INFO, "Window pushed");
}

/**
* Called to close and destroy report_window
*/
void hide_report_window(void) {
  window_stack_remove(s_window, true);
}
