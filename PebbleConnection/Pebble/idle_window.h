/**
* Constants used for messages as agreed with PebbleMessage class on phone
*/
#define DICT_TYPE_KEY 0
#define DICT_TYPE_ALERT 0
#define DICT_TYPE_NEW 1
#define DICT_TYPE_ACTION 2
#define DICT_TYPE_IGNORE 3
#define DICT_TYPE_UPDATE 4
#define DICT_HAZARD_ID_KEY 1
#define DICT_HAZARD_TYPE_KEY 2
#define DICT_HAZARD_DESC_KEY 3
#define DICT_HAZARD_DIST_KEY 4
#define DICT_ACTION_KEY 5
#define DICT_ACTION_ACK 0
#define DICT_ACTION_DIS 1
#define DICT_ACTION_NACK 2

void show_idle_window(void);
void hide_idle_window(void);
void tick_handler(struct tm *, TimeUnits);
